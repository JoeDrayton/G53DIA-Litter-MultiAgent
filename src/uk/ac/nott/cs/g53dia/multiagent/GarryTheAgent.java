package uk.ac.nott.cs.g53dia.multiagent;

import uk.ac.nott.cs.g53dia.multilibrary.*;
import uk.ac.nott.cs.g53dia.multisimulator.MultiEvaluator;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.abs;


/**
 * A simple example LitterAgent
 *
 * @author Julian Zappala
 */
/*
 * Copyright (c) 2011 Julian Zappala
 *
 * See the file "license.terms" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
// Enum for agent state, controls the SeekAndAct method
enum AgentState {
    INIT, EXPLORING, MOVETOCHARGER, FORAGING, MOVETOLITTERBIN, LITTERCOLLECTION, LITTERDISPOSAL, FORAGELITTERBINS;
}

// Enum for the
enum Direction {
    NORTH, EAST, SOUTH, WEST;

    private static Direction[] vals = values();

    public Direction next() {
        return vals[(this.ordinal() + 1) % vals.length];
    }
}

public class GarryTheAgent extends LitterAgent {
    public static int agentID;
    // String constants for full class names of relevant classes
    private static final String WASTEBIN = "class uk.ac.nott.cs.g53dia.multilibrary.WasteBin";
    private static final String WASTESTATION = "class uk.ac.nott.cs.g53dia.multilibrary.WasteStation";
    private static final String RECYCLINGBIN = "class uk.ac.nott.cs.g53dia.multilibrary.RecyclingBin";
    private static final String RECYCLINGSTATION = "class uk.ac.nott.cs.g53dia.multilibrary.RecyclingStation";
    private static final String RECHARGEPOINT = "class uk.ac.nott.cs.g53dia.multilibrary.RechargePoint";
    private static final String WASTETASK = "class uk.ac.nott.cs.g53dia.multilibrary.WasteTask";
    private static final String RECYCLINGTASK = "class uk.ac.nott.cs.g53dia.multilibrary.RecyclingTask";
    public static TaskManager taskManager;
    public Helper helper = new Helper();

    public ArrayList<Task> forageList = new ArrayList<>();
    // AgentState variables for control of state in SeekAndAct
    protected AgentState agentState;
    private AgentState previousState;
    protected Cell[][] view;
    // Point variables for agent's exploring state
    public Point explorationLocation, originalPoint;

    // ArrayList and AreaScan object for tracking and control of the various recorded regions
    //public ArrayList<AreaScan> regions = new ArrayList<>();
    AreaScan currentRegion;


    public GarryTheAgent() {
        this(new Random(), 0);
    }

    /**
     * The tanker implementation makes random moves. For reproducibility, it
     * can share the same random number generator as the environment.
     *
     * @param r The random number generator.
     */
    public GarryTheAgent(Random r, int id) {
        this.r = r;
        //taskManager = new TaskManager(this);
        this.agentID = id;
        // Initialise agent state to INIT
        agentState = agentState.INIT;
        switch(id % 4){
            case 0:
                direction = Direction.NORTH;
                break;
            case 1:
                direction = Direction.EAST;
                break;
            case 2:
                direction = Direction.SOUTH;
                break;
            case 3:
                direction = Direction.WEST;
                break;
        }
    }

    /**
     * Controls when the agent records a new region and when to just rescan the current region
     * @param view
     */
    public void evaluateRegion(Cell[][] view) {
        boolean tooClose = false;
        for(AreaScan r : taskManager.regions){
            if(getPosition().distanceTo(r.location) < 15){
                tooClose = true;
            }
        }
        if (!tooClose) {
            // Record a new region and scan it
            currentRegion = new AreaScan(getPosition());
            currentRegion.scanCells(view);
            taskManager.regions.add(currentRegion);
            // Call regionSelect to determine which region the agent should select
            regionSelect();
        } else {
            // Just scan
            currentRegion.scanCells(view);
        }
    }

    /**
     * regionSelect iterates over the region list and determines a score for each
     * region based on 'potential' in the region
     */
    public void regionSelect() {
        // Initialises variables
        AreaScan bestRegion = currentRegion;
        int combinedPotential, wastePotential, recyclingPotential, distanceToRegion;
        double regionScore = 0;
        // Iterate over variables list
        for (AreaScan region : taskManager.regions) {
            wastePotential = 0;
            // Get potential score for waste tasks in selected region
            for (Task waste : region.wasteTasks) {
                wastePotential += waste.getRemaining();
            }
            recyclingPotential = 0;
            // Get potential score for recycling tasks in selected region
            for (Task recycling : region.recyclingTasks) {
                recyclingPotential += recycling.getRemaining();
            }
            // Sums potential score
            combinedPotential = wastePotential + recyclingPotential;

            // Works out distance for the agent to get to the selected region
            distanceToRegion = getPosition().distanceTo(region.location);
            if (distanceToRegion == 0) {
                distanceToRegion = 1;
            }
            // If the region score is higher then assign bestRegion to the higher scored region
            double newRegionScore = combinedPotential / distanceToRegion;
            if (newRegionScore > regionScore) {
                regionScore = newRegionScore;
                bestRegion = region;
            }
        }
        this.currentRegion = bestRegion;
    }




    protected Task currentTask;
    private Direction direction;

    /**
     * The major method in GarryTheAgent, controls the litter collection process with each timestep
     * @param view The cells the agent can currently see.
     * @param timestep The current timestep.
     * @return returns an action for the simulator/evaluator
     */
    public Action senseAndAct(Cell[][] view, long timestep) {
        this.view = view;
        //System.out.println(timestep);
        if(timestep == 800) {
            //System.out.println(timestep);
        }
        // If statements for charge control, always goes to the closest charger and aims to minimise distance travelled
        if (getChargeLevel() != MAX_CHARGE && abs(getPosition().distanceTo(helper.closestRecharge(this)) - getChargeLevel()) < 8) {
            if (agentState != AgentState.MOVETOCHARGER) {
                previousState = agentState;
            }
            agentState = AgentState.MOVETOCHARGER;
        }
        if (getChargeLevel() < MAX_CHARGE / 2 && getPosition().distanceTo(helper.closestRecharge(this)) < 5) {
            if (agentState != AgentState.MOVETOCHARGER) {
                previousState = agentState;
            }
            agentState = AgentState.MOVETOCHARGER;
        }

        // Switch statement
        switch (agentState) {
            case INIT:
                // Variable intialisation
                currentRegion = new AreaScan(getPosition());
                currentRegion.scanCells(view);
                //direction = Direction.NORTH;
                taskManager.regions.add(currentRegion);
                this.originalPoint = getPosition();
                this.explorationLocation = this.originalPoint;
                // Attempt to find task
                if (currentRegion.wasteTasks.isEmpty()) {
                    if (currentRegion.recyclingTasks.isEmpty()) {
                        agentState = AgentState.EXPLORING;
                    }
                }

            case EXPLORING:
                taskManager.constructTaskList();
                forageList = taskManager.requestTask(this);
                evaluateRegion(view);
                if (forageList.isEmpty()) {
                    Point temp;
                    // Move between North, South, East and West
                    if (this.explorationLocation.equals(getPosition())) {
                        switch (direction) {
                            case NORTH:
                                temp = this.explorationLocation;
                                this.explorationLocation = new Point(this.originalPoint.getX(), this.originalPoint.getY() + r.nextInt(100));
                                this.originalPoint = temp;
                                break;
                            case EAST:
                                temp = this.explorationLocation;
                                this.explorationLocation = new Point(this.originalPoint.getX() + r.nextInt(100), this.originalPoint.getY());
                                this.originalPoint = temp;
                                break;
                            case SOUTH:
                                temp = this.explorationLocation;
                                this.explorationLocation = new Point(this.originalPoint.getX(), this.originalPoint.getY() - r.nextInt(100));
                                this.originalPoint = temp;
                                break;
                            case WEST:
                                temp = this.explorationLocation;
                                this.explorationLocation = new Point(this.originalPoint.getX() - r.nextInt(100), this.originalPoint.getY());
                                this.originalPoint = temp;
                                break;
                        }
                        direction = direction.next();
                    }
                    return new MoveTowardsAction(this.explorationLocation);
                } else {
                    this.currentTask = forageList.get(0);
                    agentState = AgentState.FORAGELITTERBINS;
                    return new MoveTowardsAction(this.currentTask.getPosition());
                }

            case MOVETOCHARGER:
                if (getCurrentCell(view) instanceof RechargePoint) {
                    // Reset agent state
                    agentState = previousState;
                    if (getChargeLevel() == MAX_CHARGE) {
                        agentState = previousState;
                    } else {
                        // Charge agent
                        return new RechargeAction();
                    }
                } else {
                    if (!getPosition().equals(helper.closestRecharge(this))) {
                        // Or move to closest charger
                        return new MoveTowardsAction(helper.closestRecharge(this));
                    }
                }

            case FORAGELITTERBINS:
                evaluateRegion(view);
                taskManager.constructTaskList();
                if(getRecyclingLevel()!=0){
                    forageList = taskManager.upgradeRecyclingTask(this);
                } else if(getWasteLevel()!=0){
                    forageList = taskManager.upgradeWasteTask(this);
                } else {
                    forageList = taskManager.upgradeTask(this);
                }
                if(!forageList.isEmpty()) {
                    this.currentTask = forageList.get(0);
                }
                if (this.currentTask.getClass().toString().equals(WASTETASK)) {
                    // If it is a waste task and forage list is not empty
                    if (!forageList.isEmpty()) {
                        // Pop off a task from the list and execute it
                        Task forageTask = forageList.get(0);
                        if (getCurrentCell(view) instanceof WasteBin && getPosition().equals(forageTask.getPosition())) {
                            // Deal with forage task also
                            forageList.remove(0);
                            currentRegion.wasteTasks.remove(forageTask);
                            agentState = AgentState.FORAGELITTERBINS;
                            return new LoadAction(forageTask);
                        } else {
                            agentState = AgentState.FORAGELITTERBINS;
                            if (!getPosition().equals(forageTask.getPosition())) {
                                if (!getPosition().equals(forageTask.getPosition())) {
                                    return new MoveTowardsAction(forageTask.getPosition());
                                }
                            }
                        }
                /*    } else if(getWasteLevel() > MAX_LITTER/2) {
                        forageList = taskManager.upgradeWasteTask(this);
                        if(!forageList.isEmpty()){
                            this.currentTask = forageList.get(0);
                            return new MoveTowardsAction(this.currentTask.getPosition());
                        }

                 */
                    } else {
                        // When done dispose litter
                        agentState = AgentState.LITTERDISPOSAL;
                        return new MoveTowardsAction(helper.closestPointOfAll(currentRegion.wasteStations, taskManager.regions, getPosition()));
                    }
                } else if (this.currentTask.getClass().toString().equals(RECYCLINGTASK)) {
                    // If it is a recycling task and forage list is not empty
                    if (!forageList.isEmpty()) {
                        // Pop off a task from the list and execute it
                        Task forageTask = forageList.get(0);
                        if (getCurrentCell(view) instanceof RecyclingBin && getPosition().equals(forageTask.getPosition())) {
                            // Deal with forage task also
                            forageList.remove(0);
                            currentRegion.recyclingTasks.remove(forageTask);
                            agentState = AgentState.FORAGELITTERBINS;
                            return new LoadAction(forageTask);
                        } else {
                            agentState = AgentState.FORAGELITTERBINS;
                            if (!getPosition().equals(forageTask.getPosition())) {
                                return new MoveTowardsAction(forageTask.getPosition());
                            }
                        }
                /*    } else if(getRecyclingLevel() > MAX_LITTER/2) {
                        forageList = taskManager.upgradeRecyclingTask(this);
                        if(!forageList.isEmpty()){
                            this.currentTask = forageList.get(0);
                            return new MoveTowardsAction(this.currentTask.getPosition());
                        }

                 */
                    } else {
                        // When done dispose litter
                        agentState = AgentState.LITTERDISPOSAL;
                        return new MoveTowardsAction(helper.closestPointOfAll(currentRegion.recyclingStations, taskManager.regions, getPosition()));
                    }
                }

            case LITTERDISPOSAL:
                evaluateRegion(view);
                if (this.currentTask.getClass().toString().equals(WASTETASK)) {
                        if (getCurrentCell(view) instanceof WasteStation && getWasteLevel() != 0) {
                            agentState = AgentState.EXPLORING;
                            return new DisposeAction();
                    } else if (getWasteLevel() != 0) {
                        Point closestLitterDisposal;
                        try {
                            closestLitterDisposal = helper.closestPoint(currentRegion.wasteStations, this).getPoint();
                        } catch (NullPointerException e) {
                            closestLitterDisposal = helper.closestPointOfAll(currentRegion.wasteStations, taskManager.regions, getPosition());
                        }
                        if (!getPosition().equals(closestLitterDisposal)) {
                            // Move to closest disposal
                            return new MoveTowardsAction(closestLitterDisposal);
                        }
                    } else {
                        // If not move to explore
                        agentState = AgentState.EXPLORING;
                        return new MoveTowardsAction(explorationLocation);
                    }
                } else if (this.currentTask.getClass().toString().equals(RECYCLINGTASK)) {
                    if (getCurrentCell(view) instanceof RecyclingStation && getRecyclingLevel() != 0) {
                        agentState = AgentState.EXPLORING;
                        return new DisposeAction();
                    } else if (getRecyclingLevel() != 0) {
                        Point closestRecyclingDisposal;
                        try {
                            closestRecyclingDisposal = helper.closestPoint(currentRegion.recyclingStations, this).getPoint();
                        } catch (NullPointerException e) {
                            closestRecyclingDisposal = helper.closestPointOfAll(currentRegion.recyclingStations, taskManager.regions, getPosition());
                        }
                        if (!getPosition().equals(closestRecyclingDisposal)) {
                            return new MoveTowardsAction(closestRecyclingDisposal);
                        }
                    } else {
                        // Move to closest disposal
                        agentState = AgentState.EXPLORING;
                        return new MoveTowardsAction(explorationLocation);
                    }
                }
            default:
                return null;
        }
    }
}