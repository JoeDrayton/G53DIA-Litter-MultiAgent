package uk.ac.nott.cs.g53dia.multiagent;

import javafx.util.Pair;
import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.*;

public class TaskManager {
    public ArrayList<Task> wasteWorkingList = new ArrayList<>();
    public ArrayList<Task> recyclingWorkingList = new ArrayList<>();
    public ArrayList<TaskList> wasteQueue = new ArrayList<>();
    public ArrayList<TaskList> recyclingQueue = new ArrayList<>();
    public HashMap<Integer, TaskList> activeList = new HashMap<>();
    private final String WASTETASK = "class uk.ac.nott.cs.g53dia.multilibrary.WasteTask";
    private final String RECYCLINGTASK = "class uk.ac.nott.cs.g53dia.multilibrary.RecyclingTask";
    public static ArrayList<GarryTheAgent> agents = new ArrayList<>();
    public int activeListIndex = -1;

    public TaskManager(GarryTheAgent agent) {
        agents.add(agent);
    }

    public static void clear() {
        agents.clear();
    }

    public void addTaskToWorkingList(Task task) {
        if (task.getClass().toString().equals(WASTETASK)) {
            wasteWorkingList.add(task);
        } else if (task.getClass().toString().equals(RECYCLINGTASK)) {
            recyclingWorkingList.add(task);
        }
    }

    public boolean isTaskOnWorkingList(Task task) {
        if (task.getClass().toString().equals(WASTETASK)) {
            if (wasteWorkingList.contains(task)) {
                return true;
            }
        } else if (task.getClass().toString().equals(RECYCLINGTASK)) {
            if (recyclingWorkingList.contains(task)) {
                return true;
            }
        }
        return false;
    }

    public int activateList(TaskList list) {
        if (!activeList.containsValue(list)) {
            wasteQueue.remove(list);
            recyclingQueue.remove(list);
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

    public TaskList requestTask(GarryTheAgent agent) {
        ArrayList<TaskList> combinedPool = new ArrayList<>();
        combinedPool.addAll(wasteQueue);
        combinedPool.addAll(recyclingQueue);
        return requestTaskFromQueue(combinedPool, agent);
    }

    public TaskList requestTaskFromQueue(ArrayList<TaskList> taskQueue, GarryTheAgent agent) {
        if (!taskQueue.isEmpty()) {
            TaskList bestList = new TaskList();
            float bestScore = 0;
            float currentScore = 0;
            for (TaskList taskList : taskQueue) {
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
        return new TaskList();
    }

    public float taskListScore(TaskList list, GarryTheAgent agent) {
        float score = list.getTotalRemaining();
        int distance;
        distance = list.get(0).getPosition().distanceTo(agent.getPosition());
        if (distance == 0) {
            distance = 1;
        }
        return score / (distance);
    }

    public void verifyProximity(GarryTheAgent currentAgent) {
        ArrayList<GarryTheAgent> closeAgents = new ArrayList<>();
        closeAgents.add(currentAgent);
        for (GarryTheAgent agent : agents) {
            if (currentAgent.getPosition().distanceTo(agent.getPosition()) < 25 && !currentAgent.equals(agent)) {
                closeAgents.add(agent);
            }
        }
        if (closeAgents.size() > 1) {
            int recycling = 0;
            int waste = 0;
            for (GarryTheAgent agent : closeAgents) {
                switch (agent.agentSpecialisation) {
                    case RECYCLING:
                        recycling++;
                        break;
                    case WASTE:
                        waste++;
                        break;
                }
            }
            if (waste < recycling) {
                currentAgent.agentSpecialisation = AgentSpecialisation.WASTE;
            } else if (recycling < waste) {
                currentAgent.agentSpecialisation = AgentSpecialisation.RECYCLING;
            } else {
                currentAgent.agentSpecialisation = AgentSpecialisation.HYBRID;
            }
        } else {
            currentAgent.agentSpecialisation = AgentSpecialisation.HYBRID;
        }
    }

    public TaskList upgradeTask(GarryTheAgent agent) {
        ArrayList<TaskList> combinedPool = new ArrayList<>();
        combinedPool.addAll(wasteQueue);
        combinedPool.addAll(recyclingQueue);
        return upgradeTaskFromQueue(combinedPool, agent);
    }

    public TaskList upgradeTaskFromQueue(ArrayList<TaskList> taskQueue, GarryTheAgent agent) {
        if (!taskQueue.isEmpty() && !agent.forageList.isEmpty()) {
            float currentTaskScore = taskListScore(agent.forageList, agent);
            TaskList bestList = new TaskList();
            float bestScore = 0;
            float currentScore;
            for (TaskList taskList : taskQueue) {
                currentScore = taskListScore(taskList, agent);
                if (bestScore < currentScore) {
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

    public TaskList upgradeRecyclingTask(GarryTheAgent agent) {
        return getSpecificTasks(recyclingQueue, agent, RECYCLINGTASK);
    }

    public TaskList upgradeWasteTask(GarryTheAgent agent) {
        return getSpecificTasks(wasteQueue, agent, WASTETASK);
    }

    private TaskList getSpecificTasks(ArrayList<TaskList> taskQueue, GarryTheAgent agent, String task) {
        if (!taskQueue.isEmpty() && !agent.forageList.isEmpty()) {
            float currentTaskScore = taskListScore(agent.forageList, agent);
            TaskList bestList = new TaskList();
            float bestScore = 0;
            float currentScore;
            for (TaskList taskList : taskQueue) {
                if (!activeList.containsValue(taskList)) {
                    int remaining = 0;
                    for (Task t : taskList) {
                        remaining += t.getRemaining();
                    }
                    currentScore = taskListScore(taskList, agent);
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
     *
     * @param list list of potential tasks to iterate over
     */
    public void buildForageList(ArrayList<TaskList> taskQueue, ArrayList<Task> list) {
        while (!list.isEmpty()) {
            int potentialCapacity = 0;
            TaskList forageList = new TaskList();
            Task initialTask = list.get(0);
            list.remove(initialTask);
            if (!initialTask.isComplete()) {
                forageList.add(initialTask);
                potentialCapacity = initialTask.getRemaining();
            }
            // Iterates over the task list
            for (Task newTask : list) {
                // If the new task is close enough to the current task and won't overfill the capacity
                int distance = initialTask.getPosition().distanceTo(newTask.getPosition());
                if (distance < 11 && (potentialCapacity + newTask.getRemaining() < GarryTheAgent.MAX_LITTER)) {
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
                if(forageList.compareTaskList(activeList) == -1){
                    taskQueue.add(forageList);
                } else {
                    forageList = forageList.removeClashes(activeList);
                    if(!forageList.isEmpty()){
                        taskQueue.add(forageList);
                    }
                }
            }
        }
    }

    public void constructTaskList(Cell[][] view) {
        TaskList wasteTasks = new TaskList();
        TaskList recyclingTasks = new TaskList();
        wasteQueue.clear();
        recyclingQueue.clear();
        wasteWorkingList.clear();
        recyclingWorkingList.clear();
        for (GarryTheAgent agent : agents) {
            if(agent.view!= null && agent.view.equals(view)) {
                AreaScan agentView = new AreaScan(agent.getPosition());
                agentView.scanCells(view);
                if (!agentView.wasteTasks.isEmpty()) {
                    for (Task waste : agentView.wasteTasks) {
                        if (!wasteTasks.contains(waste)) {
                            wasteTasks.add(waste);
                        }
                    }
                }
                if (!agentView.recyclingTasks.isEmpty()) {
                    for (Task recycling : agentView.recyclingTasks) {
                        if (!recyclingTasks.contains(recycling)) {
                            recyclingTasks.add(recycling);
                        }
                    }
                }
            }

        }
        Collections.sort(wasteTasks, Comparator.comparingInt(Task::getRemaining).reversed());
        Collections.sort(recyclingTasks, Comparator.comparingInt(Task::getRemaining).reversed());
        if (!wasteTasks.isEmpty()) {
            buildForageList(wasteQueue, wasteTasks);
        }
        if (!recyclingTasks.isEmpty()) {
            buildForageList(recyclingQueue, recyclingTasks);
        }
    }
}