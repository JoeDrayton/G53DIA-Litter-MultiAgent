package uk.ac.nott.cs.g53dia.multiagent;

import uk.ac.nott.cs.g53dia.multilibrary.Point;
import uk.ac.nott.cs.g53dia.multilibrary.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TaskList extends ArrayList<Task> {
    public Point listStation;
    public TaskList(){
    }

    public TaskList(TaskList copy){
        this.addAll(copy);
    }

    public int getTotalRemaining(){
        int amount = 0;
        for(Task t : this){
            amount += t.getRemaining();
        }
        return amount;
    }

    public int getTotalDistance(){
        int distance = 0;
        Task previous = this.get(0);
        for(Task t : this){
            distance += previous.getPosition().distanceTo(t.getPosition());
            previous = t;
        }
        distance += previous.getPosition().distanceTo(this.listStation);
        return distance;
    }

    public int compareTaskList(HashMap<Integer, TaskList> activeList){
        int foundIndex = -1;
        for(Map.Entry entry : activeList.entrySet()){
            TaskList activeTask = (TaskList) entry.getValue();
            for(Task t : this) {
                if (activeTask.contains(t)) {
                    foundIndex = (int) entry.getKey();
                    return foundIndex;
                }
            }
        }
        return -1;
    }

    public TaskList removeClashes(HashMap<Integer, TaskList> activeList){
        TaskList newList = new TaskList(this);
        for(Map.Entry entry : activeList.entrySet()){
            TaskList activeTask = (TaskList) entry.getValue();
            for(Task t : this) {
                if (activeTask.contains(t)) {
                    newList.remove(t);
                }
            }
        }
        return newList;
    }

    /**
     * findPath quite simply finds a low cost path through the nodes in the TaskList
     * finishing at the litter station
     * @return returns a sorted list
     */
    public TaskList findPath(GarryTheAgent agent, Helper helper) {
        TaskList original = new TaskList(this);
        // Initialisations
        TaskList constructedPath = new TaskList();
        TaskList unConstructedPath = new TaskList();
        Task initialTask = helper.closestTask(this, agent);
        original.remove(initialTask);
        unConstructedPath.add(initialTask);
        // While nodes are left to sort
        while (!unConstructedPath.isEmpty()) {
            // Take the closest node from the list
            Task currentNode = closestTaskFromPoint(unConstructedPath, initialTask.getPosition());
            unConstructedPath.remove(currentNode);

            original.remove(currentNode);
            constructedPath.add(currentNode);
            closestTaskFromTask(original, unConstructedPath);
        }
        return constructedPath;
    }

    /**
     * Finds the closest task from a single task
     * @param original
     * @param unConstructedPath
     */
    public void closestTaskFromTask(TaskList original, TaskList unConstructedPath) {
        int distance, closestDistance;
        // Init closestDistance to 1000 so it will always set the first element
        closestDistance = 1000;
        Task closestTask = null;

        // Iterating over the task list
        for (Task t : original) {
            distance = t.getPosition().distanceTo(listStation);
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

    /**
     * Finds the closest task from a single point
     * @param list
     * @param startPosition
     */
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
     * counts which station has the highest count of being closest to the tasks
     * @param stations
     * @return
     */
    public Point majorityStation(HashMap<Point, Integer> stations){
        Point majorityStation = null;
        int maxStationCount = 0;
        for(Point station : stations.keySet()){
            int currentStationCount = stations.get(station);
            if(maxStationCount < currentStationCount){
                majorityStation = station;
                maxStationCount = currentStationCount;
            }
        }
        return majorityStation;
    }

    /**
     * Iterates over the TaskList to find which station is closest to most of the tasks
     * @param helper
     * @param stationList
     */
    public void selectStation(Helper helper, ArrayList<?> stationList) {
        HashMap<Point, Integer> stations = new HashMap<>();
        for (Task t : this) {
            Point wasteStation = helper.closestPointFromPoint(stationList, t.getPosition()).getPoint();
            if (stations.containsKey(wasteStation)) {
                int current = stations.get(wasteStation) + 1;
                stations.replace(wasteStation, current);
            } else {
                stations.put(wasteStation, 1);
            }
        }
        this.listStation = majorityStation(stations);
    }
}
