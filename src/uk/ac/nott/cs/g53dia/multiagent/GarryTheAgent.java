package uk.ac.nott.cs.g53dia.multiagent;

import uk.ac.nott.cs.g53dia.multilibrary.*;
import uk.ac.nott.cs.g53dia.multisimulator.MultiEvaluator;

import java.util.ArrayList;
import java.util.HashMap;
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

// Enum for the direction
enum Direction {
    NORTH, EAST, SOUTH, WEST;

    private static Direction[] vals = values();

    public Direction next() {
        return vals[(this.ordinal() + 1) % vals.length];
    }
}

enum AgentSpecialisation {
    HYBRID, WASTE, RECYCLING;
}
public class GarryTheAgent extends LitterAgent {
    public int agentID;
    // String constants for full class names of relevant classes
    private static final String WASTETASK = "class uk.ac.nott.cs.g53dia.multilibrary.WasteTask";
    private static final String RECYCLINGTASK = "class uk.ac.nott.cs.g53dia.multilibrary.RecyclingTask";
    public static TaskManager taskManager;
    public Helper helper = new Helper();

    public TaskList forageList = new TaskList();
    public int forageReference;
    // AgentState variables for control of state in SeekAndAct
    protected AgentState agentState;
    private AgentState previousState;
    protected AgentSpecialisation agentSpecialisation;
    protected Cell[][] view;
    // Point variables for agent's exploring state
    public Point explorationLocation, originalPoint;
    protected Task currentTask;
    private Direction direction;

    /**
     * The tanker implementation makes random moves. For reproducibility, it
     * can share the same random number generator as the environment.
     *
     * @param r The random number generator.
     */
    public GarryTheAgent(Random r, int id, AgentSpecialisation agentSpecialisation) {
        this.r = r;
        this.agentSpecialisation = agentSpecialisation;
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

    private void setExplorationLocation(int exploreDistance) {
        switch (direction) {
            case NORTH:
                this.explorationLocation = new Point(getPosition().getX(), getPosition().getY() + exploreDistance);
                break;
            case EAST:
                this.explorationLocation = new Point(getPosition().getX() + exploreDistance, getPosition().getY());
                break;
            case SOUTH:
                this.explorationLocation = new Point(getPosition().getX(), getPosition().getY() - exploreDistance);
                break;
            case WEST:
                this.explorationLocation = new Point(getPosition().getX() - exploreDistance, getPosition().getY());
                break;
        }
    }

    public Boolean shouldAgentCharge(){
        if(this.currentTask.getClass().toString().equals(WASTETASK)){
            forageList.selectStation(helper, helper.wasteStations);
        } else if(this.currentTask.getClass().toString().equals(RECYCLINGTASK)){
            forageList.selectStation(helper, helper.recyclingStations);
        }
        if(getChargeLevel() - (getPosition().distanceTo(this.currentTask.getPosition()) + forageList.getTotalDistance() + forageList.listStation.distanceTo(helper.closestPointFromPoint(helper.rechargePoints, forageList.listStation).getPoint())) <= 10){
            if (agentState != AgentState.MOVETOCHARGER) {
                previousState = agentState;
            }
            agentState = AgentState.MOVETOCHARGER;
            return true;
        } else {
            agentState = AgentState.FORAGELITTERBINS;
            return false;
        }
    }

    /**
     * The major method in GarryTheAgent, controls the litter collection process with each timestep
     * @param view The cells the agent can currently see.
     * @param timestep The current timestep.
     * @return returns an action for the simulator/evaluator
     */
    public Action senseAndAct(Cell[][] view, long timestep) {
        this.view = view;
        AreaScan agentView = new AreaScan(getPosition());
        agentView.scanCells(view);
        helper.addToUniversalLists(agentView);
        //System.out.println(timestep);
        if(timestep >= 500) {
            //System.out.println(timestep);
        }
        // Switch statement
        switch (agentState) {
            case INIT:
                int initDistance = 2;
                setExplorationLocation(initDistance);
                // Attempt to find task
                agentState = AgentState.EXPLORING;

            case EXPLORING:
                taskManager.constructTaskList(view);
                switch(agentSpecialisation){
                    case HYBRID:
                        forageList = taskManager.requestTask(this);
                        break;
                    case WASTE:
                        forageList = taskManager.requestTaskFromQueue(taskManager.wasteQueue, this);
                        break;
                    case RECYCLING:
                        forageList = taskManager.requestTaskFromQueue(taskManager.recyclingQueue, this);
                        break;
                }
                if (forageList.isEmpty()) {
                    Point temp;
                    // Move between North, South, East and West
                    if (this.explorationLocation.equals(getPosition())) {
                        direction = direction.next();
                        int exploreDistance = 23;
                        setExplorationLocation(exploreDistance);
                    }
                    return new MoveTowardsAction(this.explorationLocation);
                } else {
                    this.currentTask = forageList.get(0);
                    if(shouldAgentCharge()){
                        return new MoveTowardsAction(helper.closestPoint(helper.rechargePoints, this).getPoint());
                    } else {
                        return new MoveTowardsAction(this.currentTask.getPosition());
                    }
                }

            case MOVETOCHARGER:
                if (getCurrentCell(view) instanceof RechargePoint) {
                    // Reset agent state
                    previousState = agentState;
                    if (getChargeLevel() == MAX_CHARGE) {
                        agentState = previousState;
                    } else {
                        // Charge agent
                        return new RechargeAction();
                    }
                } else {
                    if (!getPosition().equals(helper.closestPoint(helper.rechargePoints, this).getPoint())) {
                        // Or move to closest charger
                        return new MoveTowardsAction(helper.closestPoint(helper.rechargePoints, this).getPoint());
                    }
                }

            case FORAGELITTERBINS:
                taskManager.constructTaskList(view);
                switch(agentSpecialisation){
                    case HYBRID:
                        if(getRecyclingLevel()!=0){
                            forageList = taskManager.upgradeRecyclingTask(this);
                        } else if(getWasteLevel()!=0){
                            forageList = taskManager.upgradeWasteTask(this);
                        } else {
                            forageList = taskManager.upgradeTask(this);
                        }
                        break;
                    case WASTE:
                        forageList = taskManager.upgradeWasteTask(this);
                        break;
                    case RECYCLING:
                        forageList = taskManager.upgradeRecyclingTask(this);
                        break;
                }

                if(!forageList.isEmpty()) {
                    this.currentTask = forageList.get(0);
                    if(shouldAgentCharge()){
                        return new MoveTowardsAction(helper.closestPoint(helper.rechargePoints, this).getPoint());
                    }
                }
                if (this.currentTask.getClass().toString().equals(WASTETASK)) {
                    // If it is a waste task and forage list is not empty
                    if (!forageList.isEmpty()) {
                        forageList.selectStation(helper, helper.wasteStations);
                        forageList = forageList.findPath(this, helper);
                        // Pop off a task from the list and execute it
                        this.currentTask = forageList.get(0);
                        if (getCurrentCell(view) instanceof WasteBin && getPosition().equals(this.currentTask.getPosition())) {
                            // Deal with forage task also
                            forageList.remove(0);
                            agentState = AgentState.FORAGELITTERBINS;
                            return new LoadAction(this.currentTask);
                        } else {
                            agentState = AgentState.FORAGELITTERBINS;
                            if (!getPosition().equals(this.currentTask.getPosition())) {
                                if (!getPosition().equals(this.currentTask.getPosition())) {
                                    return new MoveTowardsAction(this.currentTask.getPosition());
                                }
                            }
                        }
                    } else {
                        // When done dispose litter
                        taskManager.deactivateList(forageReference);
                        agentState = AgentState.LITTERDISPOSAL;
                        taskManager.verifyProximity(this);
                        return new MoveTowardsAction(helper.closestPoint(helper.wasteStations, this).getPoint());
                    }
                } else if (this.currentTask.getClass().toString().equals(RECYCLINGTASK)) {
                    // If it is a recycling task and forage list is not empty
                    if (!forageList.isEmpty()) {
                        forageList.selectStation(helper, helper.recyclingStations);
                        forageList = forageList.findPath(this, helper);
                        // Pop off a task from the list and execute it
                        this.currentTask = forageList.get(0);
                        if (getCurrentCell(view) instanceof RecyclingBin && getPosition().equals(this.currentTask.getPosition())) {
                            // Deal with forage task also
                            forageList.remove(0);
                            agentState = AgentState.FORAGELITTERBINS;
                            return new LoadAction(this.currentTask);
                        } else {
                            agentState = AgentState.FORAGELITTERBINS;
                            if (!getPosition().equals(this.currentTask.getPosition())) {
                                return new MoveTowardsAction(this.currentTask.getPosition());
                            }
                        }
                    } else {
                        // When done dispose litter
                        taskManager.deactivateList(forageReference);
                        taskManager.verifyProximity(this);
                        agentState = AgentState.LITTERDISPOSAL;
                        return new MoveTowardsAction(helper.closestPoint(helper.recyclingStations, this).getPoint());
                    }
                }

            case LITTERDISPOSAL:
                if (this.currentTask.getClass().toString().equals(WASTETASK)) {
                        if (getCurrentCell(view) instanceof WasteStation && getWasteLevel() != 0) {
                            agentState = AgentState.EXPLORING;
                            return new DisposeAction();
                    } else if (getWasteLevel() != 0) {
                        Point closestLitterDisposal;
                        closestLitterDisposal = helper.closestPoint(helper.wasteStations, this).getPoint();
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
                        closestRecyclingDisposal = helper.closestPoint(helper.recyclingStations, this).getPoint();
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