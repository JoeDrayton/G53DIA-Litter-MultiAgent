package uk.ac.nott.cs.g53dia.multiagent;

import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.ArrayList;

public class Helper {
    public ArrayList<WasteStation> wasteStations = new ArrayList<>();
    public ArrayList<RecyclingStation> recyclingStations = new ArrayList<>();
    public ArrayList<RechargePoint> rechargePoints = new ArrayList<>();

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

    /**
     * Takes an array list of tasks, works out the closest task to the agent
     *
     * @param seenTasks ArrayList of tasks passed in
     * @return returns the closest Task
     */
    public Task closestTask(ArrayList<Task> seenTasks, GarryTheAgent agent) {
        int distance, closestDistance;
        // Init closestDistance to 1000 so it will always set the first element
        closestDistance = 1000;
        Task closestTask = null;
        for (Task t : seenTasks) {
            distance = t.getPosition().distanceTo(agent.getPosition());
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
    public Cell closestPoint(ArrayList<?> list, GarryTheAgent agent) {
        int distance, closestDistance;
        // Same as closestTask, just initialisation
        closestDistance = 1000;
        Cell closestPoint = null;
        for (Object element : list) {
            // cast element to Cell variable
            Cell point = (Cell) element;
            distance = point.getPoint().distanceTo(agent.getPosition());
            // if new element is closer, set it to closestPoint
            if (distance < closestDistance) {
                closestDistance = distance;
                closestPoint = point;
            }
        }
        return closestPoint;
    }

    /**
     * Takes the agent's view and adds the various stations three lists
     * @param agentView
     */
    public void addToUniversalLists(AreaScan agentView){
        for(WasteStation wasteStation : agentView.wasteStations){
            if(!wasteStations.contains(wasteStation)){
                wasteStations.add(wasteStation);
            }
        }
        for(RecyclingStation recyclingStation : agentView.recyclingStations){
            if(!recyclingStations.contains(recyclingStation)){
                recyclingStations.add(recyclingStation);
            }
        }
        for(RechargePoint rechargePoint : agentView.rechargingStations){
            if(!rechargePoints.contains(rechargePoint)){
                rechargePoints.add(rechargePoint);
            }
        }
    }
}
