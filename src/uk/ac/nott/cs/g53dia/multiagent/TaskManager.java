package uk.ac.nott.cs.g53dia.multiagent;

import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.Point;
import uk.ac.nott.cs.g53dia.multilibrary.Task;

import java.util.ArrayList;

import static java.lang.Math.abs;

public class TaskManager {
    public ArrayList<AreaScan> regions = new ArrayList<>();
    public ArrayList<Task> wasteBlackList = new ArrayList<>();
    public ArrayList<Task> recyclingBlacklist = new ArrayList<>();
    public ArrayList<ArrayList<Task>> taskQueue = new ArrayList<>();
    private final String WASTETASK = "class uk.ac.nott.cs.g53dia.multilibrary.WasteTask";
    private final String RECYCLINGTASK = "class uk.ac.nott.cs.g53dia.multilibrary.RecyclingTask";

    public static ArrayList<GarryTheAgent> agents = new ArrayList<>();
    public TaskManager(){

    }
    public TaskManager(GarryTheAgent agent){
        agents.add(agent);
    }

    public static void clear(){
        agents.clear();
    }
    public void activateRegion(AreaScan region){
        //regions.remove(region);
    }
    public void deactivateRegion(AreaScan region){
        //regions.add(region);
    }

    public void blacklistTask(Task task){
        if (task.getClass().toString().equals(WASTETASK)) {
            wasteBlackList.add(task);
        } else if (task.getClass().toString().equals(RECYCLINGTASK)) {
            recyclingBlacklist.add(task);
        }
    }

    public boolean isTaskOnBlackList(Task task){
        if (task.getClass().toString().equals(WASTETASK)) {
            if(wasteBlackList.contains(task)){
                return true;
            }
        } else if (task.getClass().toString().equals(RECYCLINGTASK)) {
            if(recyclingBlacklist.contains(task)){
                return true;
            }
        }
        return false;
    }

    public void removeTaskFromPool(ArrayList<Task> list){
        taskQueue.remove(list);
    }

    public ArrayList<Task> requestTask(GarryTheAgent agent){
        if(!taskQueue.isEmpty()){
            ArrayList<Task> bestList = new ArrayList<>();
            int bestScore = 0;
            int currentScore = 0;
            for(ArrayList<Task> taskList : taskQueue){
                for(Task t : taskList){
                    currentScore += t.getRemaining();
                }
                currentScore = currentScore / taskList.get(0).getPosition().distanceTo(agent.getPosition());
                if(currentScore > bestScore){
                    bestScore = currentScore;
                    bestList = taskList;
                }
            }
            removeTaskFromPool(bestList);
            return bestList;
        }
        return new ArrayList<>();
    }

    /**
     * buildForageList determines all viable tasks for the agent to complete en route to the relevant station
     * @param closestStation location of the closest relevant station
     * @param list list of potential tasks to iterate over
     */
    public void buildForageList(Task initialTask, Point closestStation, ArrayList<Task> list) {
        for(GarryTheAgent a : agents){
            a.evaluateRegion(a.view);
        }
        int potentialCapacity = 0;

        ArrayList<Task> forageList = new ArrayList<>();
        // Iterates over the task list
        for (Task newTask : list) {
            // If the new task is close enough to the current task and won't overfill the capacity
            int distance = initialTask.getPosition().distanceTo(newTask.getPosition());
            if (distance < 10 && potentialCapacity + newTask.getRemaining() < GarryTheAgent.MAX_LITTER) {
                int oldDist = initialTask.getPosition().distanceTo(closestStation);
                int newDist = newTask.getPosition().distanceTo(closestStation);
                // If the distance from the current task to the station is similar to the new task
                if (abs(oldDist - newDist) < 5 && !isTaskOnBlackList(newTask)) {
                    // Add it to the list and set current task to the new task
                    potentialCapacity += initialTask.getRemaining();
                    forageList.add(newTask);
                    blacklistTask(newTask);
                    initialTask = newTask;
                }
            }
        }
        // If the list is not empty then use findPath
        if (!forageList.isEmpty()) {
            forageList = findPath(forageList, initialTask);
            taskQueue.add(forageList);
        }
    }

    /**
     * findPath quite simply finds a low cost path between the tasks supplies
     * @param original original list of tasks passed by buildForageList
     * @return returns a sorted list
     */

    public ArrayList<Task> findPath(ArrayList<Task> original, Task initialTask) {
        // Initialisations
        ArrayList<Task> constructedPath = new ArrayList<>();
        ArrayList<Task> unConstructedPath = new ArrayList<>();
        unConstructedPath.add(original.get(0));
        // While nodes are left to sort
        while (!unConstructedPath.isEmpty()) {
            // Take the closest node from the list
            Task evaluation = (Task) closestTaskFromPoint(unConstructedPath, initialTask.getPosition());
            unConstructedPath.remove(evaluation);
            original.remove(evaluation);
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

        if (closestTask != null) {
            unConstructedPath.add(closestTask);
        }
    }

    public Task closestTaskFromPoint(ArrayList<?> list, Point startPosition) {
        int distance, closestDistance;
        // Same as closestTask, just initialisation
        closestDistance = 1000;
        Task closestPoint = null;
        for (Object element : list) {
            // cast element to Cell variable
            Task point = (Task) element;
            distance = point.getPosition().distanceTo(startPosition);
            // if new element is closer, set it to closestPoint
            if (distance < closestDistance) {
                closestDistance = distance;
                closestPoint = point;
            }
        }
        return closestPoint;
    }

    /**
     * Very similar to closestTask, closestPoint takes an abstract ArrayList, converts it to
     * a Cell and works out which in the list is closest to the agent
     *
     * @param list generalised ArrayList
     * @return returns the closest element in the list
     */
    public Cell closestPointFromPoint(ArrayList<?> list, Point startPosition) {
        int distance, closestDistance;
        // Same as closestTask, just initialisation
        closestDistance = 1000;
        Cell closestPoint = null;
        for (Object element : list) {
            // cast element to Cell variable
            Cell point = (Cell) element;
            distance = point.getPoint().distanceTo(startPosition);
            // if new element is closer, set it to closestPoint
            if (distance < closestDistance) {
                closestDistance = distance;
                closestPoint = point;
            }
        }
        return closestPoint;
    }

    public void constructTaskList() {
        for (GarryTheAgent agent : agents) {
            Point closestStation;
            if (agent.currentRegion != null && !agent.currentRegion.wasteTasks.isEmpty()) {
                Task regionsBestWaste = superiorTask(agent, agent.currentRegion.wasteTasks);
                if (!isTaskOnBlackList(regionsBestWaste)) {
                    closestStation = closestPointFromPoint(agent.currentRegion.wasteStations, regionsBestWaste.getPosition()).getPoint();
                    buildForageList(regionsBestWaste, closestStation, agent.currentRegion.wasteTasks);
                }
            }
            if (agent.currentRegion != null && !agent.currentRegion.recyclingTasks.isEmpty()) {
                Task regionsBestRecycling = superiorTask(agent, agent.currentRegion.recyclingTasks);
                if (!isTaskOnBlackList(regionsBestRecycling)) {
                    closestStation = closestPointFromPoint(agent.currentRegion.recyclingStations, regionsBestRecycling.getPosition()).getPoint();
                    buildForageList(regionsBestRecycling, closestStation, agent.currentRegion.recyclingTasks);
                }
            }
        }
    }

    /**
     * Crucial function for the agent to perform optimally. Finds the best task of a specific type.
     *
     * @param list        ArrayList of type task passed in for the function to iterate over and compare to currentTask
     * @return returns the best task out of the list for the agent to persure
     */
    public Task superiorTask(GarryTheAgent agent, ArrayList<Task> list) {
        agent.evaluateRegion(agent.view);
        // Variable initialisation
        int newScore, currentScore, distanceToTask;
        int distanceToNewTask;

        // initialise 'best' task to currentTask
        Task bestTask = list.get(0);
        // Iterate over the task list and calculate the score to cost ratio for current task and new task
        for (Object task : list) {
            Task potentialTask = (Task) task;
            newScore = potentialTask.getRemaining();
            distanceToNewTask = potentialTask.getPosition().distanceTo(agent.getPosition());
            currentScore = bestTask.getRemaining();
            distanceToTask = bestTask.getPosition().distanceTo(agent.getPosition());

            if (distanceToNewTask == 0) {
                distanceToNewTask = 1;
            }
            if (distanceToTask == 0) {
                distanceToTask = 1;
            }
            // If the new task is better then replace the best task with the new potential task
            if ((newScore / distanceToNewTask) > currentScore / distanceToTask) {
                bestTask = potentialTask;
            }
        }
        return bestTask;
    }

    /**
     * Superior task uses evaluateTask to determine the best recycling task and waste task, then it compares the two
     * returning the best task of all for the agent to persue
     * @return returns the best task for the agent to persue
     */
    /*
    public Task superiorTask(GarryTheAgent agent) {

        // Initialise currentBest much like in evaluateTask
        Task currentBest = agent.currentTask;
        if (!agent.currentRegion.recyclingTasks.isEmpty()) {
            // Finds the best recycling task
            currentBest = evaluateTask(agent, agent.currentRegion.recyclingTasks, currentBest);
        }
        // Finds the best task of all using currentBest as the best recycling task
        if (!agent.currentRegion.wasteTasks.isEmpty()) {
            currentBest = evaluateTask(agent, agent.currentRegion.wasteTasks, currentBest);
        }
        return currentBest;
    }


     */
    public void verifyTask(GarryTheAgent currentAgent){
        for(GarryTheAgent a : agents){
            if(!currentAgent.equals(a) && a.currentTask!= null){
                if(currentAgent.currentTask.equals(a.currentTask)){
                    resolveConflict(currentAgent, a);
                }
            }
        }
    }

    public void resolveConflict(GarryTheAgent agent1, GarryTheAgent agent2){
        int agent1Distance = agent1.getPosition().distanceTo(agent1.currentTask.getPosition());
        int agent2Distance = agent2.getPosition().distanceTo(agent2.currentTask.getPosition());
        if(agent1Distance < agent2Distance){
            // Code to fix task bullshit
            agent2.currentTask = findNewTask(agent2);
        } else {
            // Code to fix task bullshit
            agent1.currentTask = findNewTask(agent2);
        }
    }

    public Task findNewTask(GarryTheAgent agent){
        ArrayList<Task> taskList;
        Task newTask = null;
        if(agent.currentTask.getClass().toString().equals(WASTETASK)){
            taskList = agent.currentRegion.wasteTasks;
            if(taskList.size()!=0) {
                taskList.remove(agent.currentTask);
                newTask = superiorTask(agent, taskList);
            }
        } else if(agent.currentTask.getClass().toString().equals(RECYCLINGTASK)){
            taskList = agent.currentRegion.recyclingTasks;
            if(taskList.size()!=0) {
                taskList.remove(agent.currentTask);
                newTask = superiorTask(agent, taskList);
            }
        }
        return newTask;
    }
}
