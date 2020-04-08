package uk.ac.nott.cs.g53dia.multiagent;
import uk.ac.nott.cs.g53dia.multilibrary.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class TaskManager {
    public ArrayList<AreaScan> regions = new ArrayList<>();
    public ArrayList<Task> wasteWorkingList = new ArrayList<>();
    public ArrayList<Task> recyclingWorkingList = new ArrayList<>();
    public ArrayList<ArrayList<Task>> taskQueue = new ArrayList<>();
    public HashMap<Integer, ArrayList<Task>> activeList = new HashMap<>();
    private final String WASTETASK = "class uk.ac.nott.cs.g53dia.multilibrary.WasteTask";
    private final String RECYCLINGTASK = "class uk.ac.nott.cs.g53dia.multilibrary.RecyclingTask";
    public static ArrayList<GarryTheAgent> agents = new ArrayList<>();
    public Helper helper = new Helper();
    public int activeListIndex = -1;

    public TaskManager(GarryTheAgent agent){
        agents.add(agent);
    }

    public static void clear(){
        agents.clear();
    }


    public void addTaskToWorkingList(Task task){
        if (task.getClass().toString().equals(WASTETASK)) {
            wasteWorkingList.add(task);
        } else if (task.getClass().toString().equals(RECYCLINGTASK)) {
            recyclingWorkingList.add(task);
        }
    }

    public boolean isTaskOnWorkingList(Task task){
        if (task.getClass().toString().equals(WASTETASK)) {
            if(wasteWorkingList.contains(task)){
                return true;
            }
        } else if (task.getClass().toString().equals(RECYCLINGTASK)) {
            if(recyclingWorkingList.contains(task)){
                return true;
            }
        }
        return false;
    }

    public int activateList(ArrayList<Task> list){
        if(!activeList.containsValue(list)) {
            taskQueue.remove(list);
            activeListIndex++;
            activeList.put(activeListIndex, list);
            return activeListIndex;
        } else {
            return -1;
        }
    }

    public void deactivateList(int index) {
        activeList.remove(index);
    }

    public ArrayList<Task> requestTask(GarryTheAgent agent){
        if(!taskQueue.isEmpty()){
            ArrayList<Task> bestList = new ArrayList<>();
            float bestScore = 0;
            float currentScore = 0;
            for(ArrayList<Task> taskList : taskQueue){
                    for (Task t : taskList) {
                        currentScore += t.getRemaining();
                    }
                    int distance = taskList.get(0).getPosition().distanceTo(agent.getPosition());
                    if (distance == 0) {
                        distance = 1;
                    }
                    currentScore = currentScore / distance;
                    if (currentScore > bestScore) {
                        bestScore = currentScore;
                        bestList = taskList;
                    }
            }
            agent.forageReference = activateList(bestList);
            return bestList;
        }
        return new ArrayList<>();
    }

    public float taskScore(ArrayList<Task> list, GarryTheAgent agent){
        float score = 0;
        int distance;
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
                    deactivateList(agent.forageReference);
                    agent.forageReference = activateList(bestList);
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
                if(!activeList.containsValue(taskList)) {
                    int remaining = 0;
                    for (Task t : taskList) {
                        remaining += t.getRemaining();
                    }
                    currentScore = taskScore(taskList, agent);
                    if (currentScore > bestScore && taskList.get(0).getClass().toString().equals(task)) {
                        if (task.equals(WASTETASK)) {
                            if (agent.getWasteLevel() + remaining < GarryTheAgent.MAX_LITTER) {
                                bestScore = currentScore;
                                bestList = taskList;
                            }
                        } else if (task.equals(RECYCLINGTASK)) {
                            if (agent.getRecyclingLevel() + remaining < GarryTheAgent.MAX_LITTER) {
                                bestScore = currentScore;
                                bestList = taskList;
                            }
                        }
                    }
                }
            }

            if (currentTaskScore < bestScore) {
                taskQueue.add(agent.forageList);
                deactivateList(agent.forageReference);
                agent.forageReference = activateList(bestList);
                return bestList;
            }
        }
        return agent.forageList;
    }

    /**
     * buildForageList determines all viable tasks for the agent to complete en route to the relevant station
     * @param list list of potential tasks to iterate over
     */
    public void buildForageList(ArrayList<Task> list) {
        while(!list.isEmpty()) {
            int potentialCapacity = 0;
            ArrayList<Task> forageList = new ArrayList<>();
            Task initialTask = list.get(0);
            list.remove(initialTask);
            if(!initialTask.isComplete()) {
                forageList.add(initialTask);
                potentialCapacity = initialTask.getRemaining();
            }
            // Iterates over the task list
            for (Task newTask : list) {
                // If the new task is close enough to the current task and won't overfill the capacity
                int distance = initialTask.getPosition().distanceTo(newTask.getPosition());
                if (distance < 10 && (potentialCapacity + newTask.getRemaining() < GarryTheAgent.MAX_LITTER)) {
                    // If the distance from the current task to the station is similar to the new task
                    if (!isTaskOnWorkingList(newTask) && !newTask.isComplete()) {
                        // Add it to the list and set current task to the new task
                        potentialCapacity += initialTask.getRemaining();
                        forageList.add(newTask);
                        addTaskToWorkingList(newTask);
                        initialTask = newTask;
                    }
                }
            }
            // If the list is not empty then use findPath
            if (!forageList.isEmpty()) {
                forageList = findPath(forageList, initialTask);
                if(!activeList.containsValue(forageList)) {
                    taskQueue.add(forageList);
                }
            }
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
        ArrayList<Task> wasteTasks = new ArrayList<>();
        ArrayList<Task> recyclingTasks = new ArrayList<>();
        taskQueue.clear();
        wasteWorkingList.clear();
        recyclingWorkingList.clear();
        for (GarryTheAgent agent : agents) {
            if (agent.currentRegion != null && !agent.currentRegion.wasteTasks.isEmpty()) {
                for (Task waste : agent.currentRegion.wasteTasks) {
                    if (!wasteTasks.contains(waste)) {
                        wasteTasks.add(waste);
                    }
                }
            }
            if (agent.currentRegion != null && !agent.currentRegion.recyclingTasks.isEmpty()) {
                for (Task recycling : agent.currentRegion.recyclingTasks) {
                    if (!recyclingTasks.contains(recycling)) {
                        recyclingTasks.add(recycling);
                    }
                }
            }
        }
        Collections.sort(wasteTasks, Comparator.comparingInt(Task::getRemaining).reversed());
        Collections.sort(recyclingTasks, Comparator.comparingInt(Task::getRemaining).reversed());
        if (!wasteTasks.isEmpty()) {
            buildForageList(wasteTasks);
        }
        if (!recyclingTasks.isEmpty()) {
            buildForageList(recyclingTasks);
        }
    }
}