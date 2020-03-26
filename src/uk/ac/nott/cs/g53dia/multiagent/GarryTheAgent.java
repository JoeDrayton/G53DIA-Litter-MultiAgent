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

    private ArrayList<Task> forageList = new ArrayList<>();
    // AgentState variables for control of state in SeekAndAct
    protected AgentState agentState;
    private AgentState previousState;
    protected Cell[][] view;
    // Point variables for agent's exploring state
    public Point explorationLocation, originalPoint;

    // Boolean variables for foraging state to determine what is being foraged
    public boolean forageForRecycling, forageForWaste, dumpContent;

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
        switch(id){
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
     * Takes an array list of tasks, works out the closest task to the agent
     *
     * @param seenTasks ArrayList of tasks passed in
     * @return returns the closest Task
     */
    public Task closestTask(ArrayList<Task> seenTasks) {
        int distance, closestDistance;
        // Init closestDistance to 1000 so it will always set the first element
        closestDistance = 1000;
        Task closestTask = null;
        for (Task t : seenTasks) {
            distance = t.getPosition().distanceTo(getPosition());
            // If new task is closer, set it to closest task
            if (distance < closestDistance) {
                closestDistance = distance;
                closestTask = t;
            }
        }
        return closestTask;
    }

    /**
     * Very similar to closestTask, closestPoint takes an abstract ArrayList, converts it to
     * a Cell and works out which in the list is closest to the agent
     *
     * @param list generalised ArrayList
     * @return returns the closest element in the list
     */
    public Cell closestPoint(ArrayList<?> list) {
        int distance, closestDistance;
        // Same as closestTask, just initialisation
        closestDistance = 1000;
        Cell closestPoint = null;
        for (Object element : list) {
            // cast element to Cell variable
            Cell point = (Cell) element;
            distance = point.getPoint().distanceTo(getPosition());
            // if new element is closer, set it to closestPoint
            if (distance < closestDistance) {
                closestDistance = distance;
                closestPoint = point;
            }
        }
        return closestPoint;
    }

    /**
     * A more severe implementation for only the most dire circumstances...
     * Searches every reigon to find the closest instance of whatever list is passed in
     *
     * @param list
     * @return returns the closest instance of the object ? to the agent
     */
    public Point closestPointOfAll(ArrayList<?> list) {
        // Switch statement for the class of list
        switch (list.getClass().toString()) {
            case RECHARGEPOINT:
                // If the class is a RechargePoint, search all region recharging stations
                ArrayList<Point> rechargingStations = new ArrayList<>();
                for (AreaScan region : taskManager.regions) {
                    rechargingStations.add(closestPoint(region.rechargingStations).getPoint());
                }
                return closestPoint(rechargingStations).getPoint();
            case RECYCLINGBIN:
                // If the class is a RecyclingBin, search all region recharging stations
                ArrayList<Point> recyclingBins = new ArrayList<>();
                for (AreaScan region : taskManager.regions) {
                    recyclingBins.add(closestPoint(region.recyclingBins).getPoint());
                }
                return closestPoint(recyclingBins).getPoint();
            case RECYCLINGSTATION:
                // If the class is a RecyclingStation, search all region recharging stations
                ArrayList<Point> recyclingStations = new ArrayList<>();
                for (AreaScan region : taskManager.regions) {
                    recyclingStations.add(closestPoint(region.recyclingStations).getPoint());
                }
                return closestPoint(recyclingStations).getPoint();
            case WASTEBIN:
                // If the class is a WasteBin, search all region recharging stations
                ArrayList<Point> wasteBins = new ArrayList<>();
                for (AreaScan region : taskManager.regions) {
                    wasteBins.add(closestPoint(region.wasteBins).getPoint());
                }
                return closestPoint(wasteBins).getPoint();
            case WASTESTATION:
                // If the class is a WasteStation, search all region recharging stations
                ArrayList<Point> wasteStations = new ArrayList<>();
                for (AreaScan region : taskManager.regions) {
                    wasteStations.add(closestPoint(region.wasteStations).getPoint());
                }
                return closestPoint(wasteStations).getPoint();
            default:
                // By default return 0,0 something went wrong clearly
                return RECHARGE_POINT_LOCATION;
        }
    }

    /**
     * Important function to return the closest recharging point, separate due to the severity of the
     * maintenance goal
     *
     * @return returns the closest RechargePoint
     */
    public Point closestRecharge() {
        Point closestPoint, newPoint;
        // If there is a rechargingStation in the current region
        if (!currentRegion.rechargingStations.isEmpty()) {
            // use closestPoint to get the coordinates and return it
            closestPoint = closestPoint(currentRegion.rechargingStations).getPoint();
            return closestPoint;
        } else {
            // Init closestPoint to RECHARGE_POINT_LOCATION
            closestPoint = RECHARGE_POINT_LOCATION;
            for (AreaScan region : taskManager.regions) {
                // If region has a rechargingStation
                if (!region.rechargingStations.isEmpty()) {
                    newPoint = closestPoint(region.rechargingStations).getPoint();
                    // Compare newPoint and closestPoint to find the closest station
                    if (newPoint.distanceTo(getPosition()) < closestPoint.distanceTo(getPosition())) {
                        closestPoint = newPoint;
                    }
                }
            }
        }
        return closestPoint;
    }

    /**
     * Initialises this.currentTask variable, used at the start of the agent's life as well as
     * cases where this.currentTask is nullified.
     */
    public void initCurrentTask() {
        // If wasteTasks has elements
        if (!currentRegion.wasteTasks.isEmpty()) {
            // Set to closest, task optimisation later
            this.currentTask = closestTask(currentRegion.wasteTasks);
        }
        // If recyclingTasks has elements
        if (!currentRegion.recyclingTasks.isEmpty()) {
            // Set to closest task, optimisation later
            this.currentTask = closestTask(currentRegion.recyclingTasks);
        }
    }

    /**
     * Crucial function for the agent to perform optimally. Finds the best task of a specific type.
     *
     * @param list        ArrayList of type task passed in for the function to iterate over and compare to currentTask
     * @param currentTask The current task the agent is working towards
     * @return returns the best task out of the list for the agent to persure
     */
    /*
    public Task evaluateTask(ArrayList<Task> list, Task currentTask) {
        // Variable initialisation
        int newScore, currentScore, distanceToTask;
        int distanceToNewTask;
        // If somehow current task is null set state to exploring
        if (currentTask == null) {
            agentState = AgentState.EXPLORING;
        }
        // initialise 'best' task to currentTask
        Task bestTask = currentTask;
        // Iterate over the task list and calculate the score to cost ratio for current task and new task
        for (Object task : list) {
            Task potentialTask = (Task) task;
            newScore = potentialTask.getRemaining();
            distanceToNewTask = potentialTask.getPosition().distanceTo(getPosition());
            currentScore = bestTask.getRemaining();
            distanceToTask = bestTask.getPosition().distanceTo(getPosition());

            if (distanceToNewTask == 0) {
                distanceToNewTask = 1;
            }
            if (distanceToTask == 0) {
                distanceToTask = 1;
            }
            // If the new task is better then replace the best task with the new potential task
            if ((newScore / distanceToNewTask) > currentScore / distanceToTask) {
                bestTask = potentialTask;
            } else {
                if (distanceToTask == -1) {
                    bestTask = potentialTask;
                }
            }
        }
        return bestTask;
    }
    */

    /**
     * Superior task uses evaluateTask to determine the best recycling task and waste task, then it compares the two
     * returning the best task of all for the agent to persue
     * @param currentTask the current task selected by the agent
     * @param view the agent's view
     * @return returns the best task for the agent to persue
     */
    /*
    public Task superiorTask(Task currentTask, Cell[][] view) {
        // Refresh the agent's view for most updated task lists
        evaluateRegion(view);
        // Block of if statements to ensure foraging is not negatively effected by superiorTask
        if (forageForWaste) {
            currentTask = closestTask(currentRegion.wasteTasks);
            currentTask = evaluateTask(currentRegion.wasteTasks, currentTask);
            return currentTask;
        } else if (forageForRecycling) {
            currentTask = closestTask(currentRegion.recyclingTasks);
            currentTask = evaluateTask(currentRegion.recyclingTasks, currentTask);
            return currentTask;
        } else if (currentTask == null) {
            agentState = AgentState.EXPLORING;
        }
        // Initialise currentBest much like in evaluateTask
        Task currentBest = currentTask;
        if (!currentRegion.recyclingTasks.isEmpty()) {
            // Finds the best recycling task
            currentBest = evaluateTask(currentRegion.recyclingTasks, currentBest);
        }
        // Finds the best task of all using currentBest as the best recycling task
        currentBest = evaluateTask(currentRegion.wasteTasks, currentBest);
        return currentBest;
    }
     */

    /**
     * Resets the booleans, sets the state and performs DisposeAction in litterdisposal
     * @return
     */
    private Action resetParams() {
        dealWithTask();
        forageForRecycling = false;
        forageForWaste = false;
        dumpContent = false;
        agentState = AgentState.EXPLORING;
        return new DisposeAction();
    }

    /**
     * As in its name this method deals with the just completed task.
     * It nullifies the current task, assigns a new task and removes the old task
     * from the task list.
     */
    private void dealWithTask() {
        if (this.currentTask.getClass().toString().equals(WASTETASK)) {
            currentRegion.wasteTasks.remove(this.currentTask);
            this.currentTask = null;
            if (forageForWaste) {
                this.currentTask = closestTask(currentRegion.wasteTasks);
            } else {
                initCurrentTask();
            }
            initCurrentTask();
        } else if (this.currentTask.getClass().toString().equals(RECYCLINGTASK)) {
            currentRegion.recyclingTasks.remove(this.currentTask);
            this.currentTask = null;
            if (forageForRecycling) {
                this.currentTask = closestTask(currentRegion.recyclingTasks);
            } else {
                initCurrentTask();
            }
        }
    }

    /**
     * Controls when the agent records a new region and when to just rescan the current region
     * @param view
     */
    public void evaluateRegion(Cell[][] view) {
        boolean tooClose = false;
        for(AreaScan r : taskManager.regions){
            if(getPosition().distanceTo(r.location) < 30){
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

    /**
     * buildForageList determines all viable tasks for the agent to complete en route to the relevant station
     * @param view view for evaluateRegion
     * @param currentTask currentTask for the initial starting point of the list
     * @param closestStation location of the closest relevant station
     * @param list list of potential tasks to iterate over
     */
    public void buildForageList(Cell[][] view, Task currentTask, Point closestStation, ArrayList<Task> list) {
        int potentialCapacity = 0;
        if (currentTask.getClass().toString().equals(WASTETASK)) {
            // If current task is a waste task, remove it from the wasteTasks list and add the capacity to potential capacity
            potentialCapacity = getWasteLevel();
            currentRegion.wasteTasks.remove(currentTask);
        } else if (currentTask.getClass().toString().equals(RECYCLINGTASK)) {
            // If current task is a recycling task, remove it from the recyclingTasks list and add the capacity to potential capacity
            potentialCapacity = getRecyclingLevel();
            currentRegion.recyclingTasks.remove(currentTask);
        }
        evaluateRegion(view);
        // Clear forageList
        forageList.clear();

        // Iterates over the task list
        for (Task newTask : list) {
            // If the new task is close enough to the current task and won't overfill the capacity
            if (currentTask.getPosition().distanceTo(newTask.getPosition()) < 5 && potentialCapacity + newTask.getRemaining() < MAX_LITTER) {
                int oldDist = currentTask.getPosition().distanceTo(closestStation);
                int newDist = newTask.getPosition().distanceTo(closestStation);
                // If the distance from the current task to the station is similar to the new task
                if (abs(oldDist - newDist) < 5) {
                    // Add it to the list and set current task to the new task
                    potentialCapacity += currentTask.getRemaining();
                    forageList.add(newTask);
                    currentTask = newTask;
                }
            }
        }
        // If the list is not empty then use findPath
        if (!forageList.isEmpty()) {
            forageList = findPath(forageList);
        }
    }

    /**
     * findPath quite simply finds a low cost path between the tasks supplies
     * @param original original list of tasks passed by buildForageList
     * @return returns a sorted list
     */
    public ArrayList<Task> findPath(ArrayList<Task> original) {
        // Initialisations
        ArrayList<Task> constructedPath = new ArrayList<>();
        ArrayList<Task> unConstructedPath = new ArrayList<>();
        unConstructedPath.add(original.get(0));
        // While nodes are left to sort
        while (!unConstructedPath.isEmpty()) {
            // Take the closest node from the list
            Task evaluation = closestTask(unConstructedPath);
            unConstructedPath.remove(evaluation);
            constructedPath.add(evaluation);
            closestTaskFromTask(original, evaluation, unConstructedPath);
        }
        return constructedPath;
    }

    public void closestTaskFromTask(ArrayList<Task> taskList, Task evalationTask, ArrayList<Task> unConstructedPath) {
        int distance, closestDistance;
        // Init closestDistance to 1000 so it will always set the first element
        closestDistance = 1000;
        Task closestTask = null;

        // Iterating over the task list
        for (Task t : taskList) {
            distance = t.getPosition().distanceTo(evalationTask.getPosition());
            // If new task is closer, set it to closest task
            if (distance < closestDistance) {
                closestDistance = distance;
                closestTask = t;
            }
        }

        if (!closestTask.equals(evalationTask)) {
            unConstructedPath.add(closestTask);
        }
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
        if(timestep == 3953) {
            //System.out.println(timestep);
        }
        // If statements for charge control, always goes to the closest charger and aims to minimise distance travelled
        if (getChargeLevel() != MAX_CHARGE && abs(getPosition().distanceTo(closestRecharge()) - getChargeLevel()) < 8) {
            if (agentState != AgentState.MOVETOCHARGER) {
                previousState = agentState;
            }
            agentState = AgentState.MOVETOCHARGER;
        }
        if (getChargeLevel() < MAX_CHARGE / 2 && getPosition().distanceTo(closestRecharge()) < 5) {
            if (agentState != AgentState.MOVETOCHARGER) {
                previousState = agentState;
            }
            agentState = AgentState.MOVETOCHARGER;
        }

        // Switch statement
        switch (agentState) {
            case INIT:
                // Variable intialisation
                dumpContent = false;
                forageForRecycling = false;
                forageForWaste = false;
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


   /*         case MOVETOLITTERBIN:
                // Distinguish between waste and litter
                if (this.currentTask.getClass().toString().equals(WASTETASK)) {
                    if (getCurrentCell(view) instanceof WasteBin && getPosition().equals(this.currentTask.getPosition())) {
                        // If we have reached our target load from the bin
                        agentState = AgentState.LITTERDISPOSAL;
                        return new LoadAction(this.currentTask);
                    } else {
                        // Move to target
                        agentState = AgentState.MOVETOLITTERBIN;
                        if (currentRegion.wasteTasks.isEmpty() && currentRegion.recyclingTasks.isEmpty()) {
                            agentState = AgentState.EXPLORING;
                        } else {
                            // If not find the best task in view
                            this.currentTask = taskManager.superiorTask(this);
                            taskManager.verifyTask(this);
                            return new MoveTowardsAction(this.currentTask.getPosition());
                        }
                        if (!getPosition().equals(this.currentTask.getPosition())) {
                            // Move to current task
                            return new MoveTowardsAction(this.currentTask.getPosition());
                        }
                    }
                } else if (this.currentTask.getClass().toString().equals(RECYCLINGTASK)) {
                    if (getCurrentCell(view) instanceof RecyclingBin && getPosition().equals(this.currentTask.getPosition())) {
                        // If we have reached our target load from the bin
                        agentState = AgentState.LITTERDISPOSAL;
                        return new LoadAction(this.currentTask);
                    } else {
                        // Move to target
                        agentState = AgentState.MOVETOLITTERBIN;
                        if (currentRegion.wasteTasks.isEmpty() && currentRegion.recyclingTasks.isEmpty()) {
                            agentState = AgentState.EXPLORING;
                        } else {
                            // If not find the best task in view
                            this.currentTask = taskManager.superiorTask(this);
                            taskManager.verifyTask(this);
                        }
                        if (!getPosition().equals(this.currentTask.getPosition())) {
                            // Move to current task
                            return new MoveTowardsAction(this.currentTask.getPosition());
                        }
                    }
                }

    */


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
                    if (!getPosition().equals(closestRecharge())) {
                        // Or move to closest charger
                        return new MoveTowardsAction(closestRecharge());
                    }
                }

            case FORAGING:
                evaluateRegion(view);
                if (this.currentTask.getClass().toString().equals(WASTETASK)) {
                    // Find the closest waste station
                    Point closestStation;
                    try {
                        closestStation = closestPoint(currentRegion.wasteStations).getPoint();
                    } catch (NullPointerException e) {
                        closestStation = closestPointOfAll(currentRegion.wasteStations);
                    }
                    currentRegion.wasteTasks.remove(this.currentTask);
                    if (currentRegion.wasteTasks.isEmpty()) {
                        // If there is no waste task go to the nearest station to unload
                        if (getWasteLevel() != 0) {
                            forageForWaste = false;
                            dumpContent = true;
                            agentState = AgentState.LITTERDISPOSAL;
                            if (!getPosition().equals(closestStation)) {

                                return new MoveTowardsAction(closestStation);
                            }
                        } else {
                            // If not go explore
                            agentState = AgentState.EXPLORING;
                            return new MoveTowardsAction(explorationLocation);
                        }
                    } else {
                        // There is at least one waste task, set to current task
                        Task newTask= taskManager.superiorTask(this, currentRegion.wasteTasks);
                        if (newTask == null) {
                            dumpContent = true;
                            agentState = AgentState.LITTERDISPOSAL;
                            if (!getPosition().equals(closestStation)) {
                                // Move toward station
                                return new MoveTowardsAction(closestStation);
                            }
                        }
                        // If the closest station is closer than the current task go dump it
                        if (getPosition().distanceTo(closestStation) < getPosition().distanceTo(newTask.getPosition())) {
                            forageForWaste = false;
                            dumpContent = true;
                            agentState = AgentState.LITTERDISPOSAL;
                            if (!getPosition().equals(closestStation)) {
                                return new MoveTowardsAction(closestStation);
                            }
                        } else {
                            // Forage litter bins on the way to the station
                            agentState = AgentState.FORAGELITTERBINS;
                            // Find a low cost path for the bins worth going to on the way
                            buildForageList(view, this.currentTask, closestStation, currentRegion.wasteTasks);
                            if (!forageList.isEmpty()) {
                                if (!getPosition().equals(forageList.get(0).getPosition())) {
                                    // If list is populated go deal with it
                                    return new MoveTowardsAction(forageList.get(0).getPosition());
                                }
                            } else {
                                // If not go to the original new task
                                this.currentTask = newTask;
                                agentState = AgentState.FORAGELITTERBINS;
                                forageList.add(newTask);
                                if (!getPosition().equals(newTask.getPosition())) {
                                    return new MoveTowardsAction(newTask.getPosition());
                                }
                            }
                        }
                    }
                } else if (this.currentTask.getClass().toString().equals(RECYCLINGTASK)) {
                    // Find the closest recycling station
                    Point closestStation;
                    try {
                        closestStation = closestPoint(currentRegion.recyclingStations).getPoint();
                    } catch (NullPointerException e) {
                        closestStation = closestPointOfAll(currentRegion.recyclingStations);
                    }
                    currentRegion.recyclingTasks.remove(this.currentTask);
                    if (currentRegion.recyclingTasks.isEmpty()) {
                        // If there is no waste task go to the nearest station to unload
                        if (getRecyclingLevel() != 0) {

                            forageForRecycling = false;
                            dumpContent = true;
                            agentState = AgentState.LITTERDISPOSAL;
                            if (!getPosition().equals(closestStation)) {
                                return new MoveTowardsAction(closestStation);
                            }
                        } else {
                            // If not go explore
                            agentState = AgentState.EXPLORING;
                            return new MoveTowardsAction(explorationLocation);
                        }
                    } else {
                        // There is at least one waste task, set to current task
                        Task newTask = taskManager.superiorTask(this, currentRegion.recyclingTasks);
                        if (newTask == null) {
                            dumpContent = true;
                            agentState = AgentState.LITTERDISPOSAL;
                            if (!getPosition().equals(closestStation)) {
                                return new MoveTowardsAction(closestStation);
                            }
                        }
                        // If the closest station is closer than the current task go dump it
                        if (getPosition().distanceTo(closestStation) < getPosition().distanceTo(newTask.getPosition())) {
                            forageForWaste = false;
                            dumpContent = true;
                            agentState = AgentState.LITTERDISPOSAL;
                            if (!getPosition().equals(closestStation)) {
                                return new MoveTowardsAction(closestStation);
                            }
                        } else {
                            agentState = AgentState.FORAGELITTERBINS;
                            buildForageList(view, this.currentTask, closestStation, currentRegion.recyclingTasks);
                            if (!forageList.isEmpty()) {
                                if (!getPosition().equals(forageList.get(0).getPosition())) {
                                    return new MoveTowardsAction(forageList.get(0).getPosition());
                                }
                            } else {
                                // If not go to the original new task
                                this.currentTask = newTask;
                                agentState = AgentState.FORAGELITTERBINS;
                                forageList.add(newTask);
                                if (!getPosition().equals(newTask.getPosition())) {
                                    return new MoveTowardsAction(newTask.getPosition());
                                }
                            }
                        }
                    }
                }

            case FORAGELITTERBINS:
                if (this.currentTask.getClass().toString().equals(WASTETASK)) {
                    // If it is a waste task and forage list is not empty
                    if (!forageList.isEmpty()) {
                        // Pop off a task from the list and execute it
                        Task forageTask = forageList.get(0);
                        if (getCurrentCell(view) instanceof WasteBin && getPosition().equals(forageTask.getPosition())) {
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
                    } else {
                        // When done dispose litter
                        agentState = AgentState.LITTERDISPOSAL;
                        return new MoveTowardsAction(closestPointOfAll(currentRegion.wasteStations));
                    }
                } else if (this.currentTask.getClass().toString().equals(RECYCLINGTASK)) {
                    // If it is a recycling task and forage list is not empty
                    if (!forageList.isEmpty()) {
                        // Pop off a task from the list and execute it
                        Task forageTask = forageList.get(0);
                        if (getCurrentCell(view) instanceof RecyclingBin && getPosition().equals(forageTask.getPosition())) {
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
                    } else {
                        // When done dispose litter
                        agentState = AgentState.LITTERDISPOSAL;
                        return new MoveTowardsAction(closestPointOfAll(currentRegion.recyclingStations));
                    }
                }

            case LITTERDISPOSAL:
                evaluateRegion(view);
                if (this.currentTask.getClass().toString().equals(WASTETASK)) {
                    /*if (getWasteLevel() < MAX_LITTER * 0.8 && !currentRegion.wasteTasks.isEmpty() && !dumpContent) {
                        // If the quota has not been filled for foraging, go forage
                        agentState = AgentState.FORAGING;
                        forageForRecycling = false;
                        forageForWaste = true;
                        return new MoveTowardsAction(closestPointOfAll(currentRegion.wasteTasks));
                    } else*/
                        if (getCurrentCell(view) instanceof WasteStation && getWasteLevel() != 0) {
                        // Reset parameters and dispose of waste
                            return resetParams();
                    } else if (getWasteLevel() != 0) {
                        Point closestLitterDisposal;
                        try {
                            closestLitterDisposal = closestPoint(currentRegion.wasteStations).getPoint();
                        } catch (NullPointerException e) {
                            closestLitterDisposal = closestPointOfAll(currentRegion.wasteStations);
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
                    /*if (getRecyclingLevel() < MAX_LITTER * 0.8 && !currentRegion.recyclingTasks.isEmpty() && !dumpContent) {
                        // If the quota has not been filled for foraging, go forage
                        agentState = AgentState.FORAGING;
                        forageForRecycling = true;
                        forageForWaste = false;
                        return new MoveTowardsAction(closestPointOfAll(currentRegion.recyclingTasks));
                    } else */
                    if (getCurrentCell(view) instanceof RecyclingStation && getRecyclingLevel() != 0) {
                        // Reset parameters and dispose of waste
                        return resetParams();
                    } else if (getRecyclingLevel() != 0) {
                        Point closestRecyclingDisposal;
                        try {
                            closestRecyclingDisposal = closestPoint(currentRegion.recyclingStations).getPoint();
                        } catch (NullPointerException e) {
                            closestRecyclingDisposal = closestPointOfAll(currentRegion.recyclingStations);
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