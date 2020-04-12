package uk.ac.nott.cs.g53dia.multiagent;

import uk.ac.nott.cs.g53dia.multilibrary.Point;
import uk.ac.nott.cs.g53dia.multilibrary.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TaskList extends ArrayList<Task> {
    public Point listStation;
    public TaskList(){
        //listStation = station;
    }

    public TaskList(TaskList copy){
        this.addAll(copy);
    }

    public void setListStation(Point station){
        int x = station.getX();
        int y = station.getY();
        listStation = new Point(x, y);
    }

    public Point getListStation(){
        return this.listStation;
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
        //distance += previous.getPosition().distanceTo(this.listStation);
        return distance;
    }


    public Task closestTaskToStation(){
        int maxDistance = -1;
        Task closestTask = this.get(0);
        for(Task t : this){
            int currentDistance = t.getPosition().distanceTo(this.listStation);
            if(maxDistance < currentDistance){
                maxDistance = currentDistance;
                closestTask = t;
            }
        }
        return closestTask;
    }

    /**
     * findPath quite simply finds a low cost path between the tasks supplies
     * @return returns a sorted list
     */
    public TaskList findPath() {
        TaskList original = new TaskList(this);
        // Initialisations
        TaskList constructedPath = new TaskList();
        TaskList unConstructedPath = new TaskList();
        Task initialTask = original.get(0);
        original.remove(initialTask);
        unConstructedPath.add(initialTask);
        // While nodes are left to sort
        while (!unConstructedPath.isEmpty()) {
            // Take the closest node from the list
            Task evaluation = closestTaskFromPoint(unConstructedPath, initialTask.getPosition());
            unConstructedPath.remove(evaluation);
            original.remove(evaluation);
            constructedPath.add(evaluation);
            closestTaskFromTask(original, unConstructedPath);
        }
        return constructedPath;
    }

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


}
