package uk.ac.nott.cs.g53dia.multiagent;
import uk.ac.nott.cs.g53dia.multilibrary.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TaskManager {
    public ArrayList<AreaScan> regions = new ArrayList<>();
    public ArrayList<Task> wasteBlackList = new ArrayList<>();
    public ArrayList<Task> recyclingBlacklist = new ArrayList<>();
    public ArrayList<ArrayList<Task>> taskQueue = new ArrayList<>();
    private final String WASTETASK = "class uk.ac.nott.cs.g53dia.multilibrary.WasteTask";
    private final String RECYCLINGTASK = "class uk.ac.nott.cs.g53dia.multilibrary.RecyclingTask";
    public static ArrayList<GarryTheAgent> agents = new ArrayList<>();
    public Helper helper = new Helper();

    public TaskManager(GarryTheAgent agent){
        agents.add(agent);
    }

    public static void clear(){
        agents.clear();
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
                int distance = taskList.get(0).getPosition().distanceTo(agent.getPosition());
                if (distance == 0) {
                    distance = 1;
                }
                currentScore = currentScore / distance;
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

    public float taskScore(ArrayList<Task> list, GarryTheAgent agent){
        float score = 0;
        int distance = 0;
        for (Task t : list) {
            score += t.getRemaining();
        }
        distance = list.get(0).getPosition().distanceTo(agent.getPosition());
        if (distance == 0) {
            distance = 1;
        }
        return score / distance;
    }

    public ArrayList<Task> upgradeTask(GarryTheAgent agent) {
        if (!taskQueue.isEmpty() && !agent.forageList.isEmpty()) {
                float currentTaskScore = taskScore(agent.forageList, agent);
                ArrayList<Task> bestList = new ArrayList<>();
                float bestScore = 0;
                float currentScore;
                for (ArrayList<Task> taskList : taskQueue) {
                    currentScore = taskScore(taskList, agent);
                    if (currentScore > bestScore) {
                        bestScore = currentScore;
                        bestList = taskList;
                    }
                }

                if (currentTaskScore < bestScore) {
                    taskQueue.add(agent.forageList);
                    removeTaskFromPool(bestList);
                    return bestList;
                }
        }
            return agent.forageList;
    }

    public ArrayList<Task> upgradeRecyclingTask(GarryTheAgent agent){
        return getSpecificTasks(agent, RECYCLINGTASK);
    }

    public ArrayList<Task> upgradeWasteTask(GarryTheAgent agent){
        return getSpecificTasks(agent, WASTETASK);
    }

    private ArrayList<Task> getSpecificTasks(GarryTheAgent agent, String task) {
        if(!taskQueue.isEmpty() && !agent.forageList.isEmpty()){
            float currentTaskScore = taskScore(agent.forageList, agent);
            ArrayList<Task> bestList = new ArrayList<>();
            float bestScore = 0;
            float currentScore;
            for (ArrayList<Task> taskList : taskQueue) {
                int remaining = 0;
                for(Task t : taskList){
                    remaining += t.getRemaining();
                }
                currentScore = taskScore(taskList, agent);
                if (currentScore > bestScore && taskList.get(0).getClass().toString().equals(task)) {
                    if(task.equals(WASTETASK)){
                        if(agent.getWasteLevel() + remaining < GarryTheAgent.MAX_LITTER){
                            bestScore = currentScore;
                            bestList = taskList;
                        }
                    } else if(task.equals(RECYCLINGTASK)){
                        if(agent.getRecyclingLevel() + remaining < GarryTheAgent.MAX_LITTER){
                            bestScore = currentScore;
                            bestList = taskList;
                        }
                    }
                }
            }

            if (currentTaskScore < bestScore) {
                taskQueue.add(agent.forageList);
                removeTaskFromPool(bestList);
                return bestList;
            }
        }
        return agent.forageList;
    }


    /**
     * buildForageList determines all viable tasks for the agent to complete en route to the relevant station
     * @param list list of potential tasks to iterate over
     */
    public void buildForageList(Task initialTask, ArrayList<Task> list) {
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
                // If the distance from the current task to the station is similar to the new task
                if (!isTaskOnBlackList(newTask)) {
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
            Task evaluation = (Task) helper.closestTaskFromPoint(unConstructedPath, initialTask.getPosition());
            unConstructedPath.remove(evaluation);
            original.remove(evaluation);
            constructedPath.add(evaluation);
            helper.closestTaskFromTask(original, evaluation, unConstructedPath);
        }
        return constructedPath;
    }

    public void constructTaskList() {
        for (GarryTheAgent agent : agents) {
            if (agent.currentRegion != null && !agent.currentRegion.wasteTasks.isEmpty()) {
                Task regionsBestWaste = superiorTask(agent, agent.currentRegion.wasteTasks);
                if (!isTaskOnBlackList(regionsBestWaste)) {
                    Collections.sort(agent.currentRegion.wasteTasks, Comparator.comparingInt(Task::getRemaining).reversed());
                    buildForageList(regionsBestWaste, agent.currentRegion.wasteTasks);
                }
            }
            if (agent.currentRegion != null && !agent.currentRegion.recyclingTasks.isEmpty()) {
                Task regionsBestRecycling = superiorTask(agent, agent.currentRegion.recyclingTasks);
                if (!isTaskOnBlackList(regionsBestRecycling)) {
                    Collections.sort(agent.currentRegion.recyclingTasks, Comparator.comparingInt(Task::getRemaining).reversed());
                    buildForageList(regionsBestRecycling, agent.currentRegion.recyclingTasks);
                }
            }
        }
    }

    /*
            for (GarryTheAgent agent : agents) {
            if (agent.currentRegion != null && !agent.currentRegion.wasteTasks.isEmpty()) {
                Task regionsBestWaste = superiorTask(agent, agent.currentRegion.wasteTasks);
                if (!isTaskOnBlackList(regionsBestWaste)) {
                    Collections.sort(agent.currentRegion.wasteTasks, Comparator.comparingInt(Task::getRemaining).reversed());
                    buildForageList(regionsBestWaste, agent.currentRegion.wasteTasks);
                }
            }
            if (agent.currentRegion != null && !agent.currentRegion.recyclingTasks.isEmpty()) {
                Task regionsBestRecycling = superiorTask(agent, agent.currentRegion.recyclingTasks);
                if (!isTaskOnBlackList(regionsBestRecycling)) {
                    Collections.sort(agent.currentRegion.recyclingTasks, Comparator.comparingInt(Task::getRemaining).reversed());
                    buildForageList(regionsBestRecycling, agent.currentRegion.recyclingTasks);
                }
            }
        }
     */

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
}
