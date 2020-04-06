package uk.ac.nott.cs.g53dia.multiagent;

import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.Point;
import uk.ac.nott.cs.g53dia.multilibrary.Task;

import java.util.ArrayList;

public class Helper {
    private static final String WASTEBIN = "class uk.ac.nott.cs.g53dia.multilibrary.WasteBin";
    private static final String WASTESTATION = "class uk.ac.nott.cs.g53dia.multilibrary.WasteStation";
    private static final String RECYCLINGBIN = "class uk.ac.nott.cs.g53dia.multilibrary.RecyclingBin";
    private static final String RECYCLINGSTATION = "class uk.ac.nott.cs.g53dia.multilibrary.RecyclingStation";
    private static final String RECHARGEPOINT = "class uk.ac.nott.cs.g53dia.multilibrary.RechargePoint";

    /**
     * A more severe implementation for only the most dire circumstances...
     * Searches every reigon to find the closest instance of whatever list is passed in
     *
     * @param list
     * @return returns the closest instance of the object ? to the agent
     */
    public Point closestPointOfAll(ArrayList<?> list, ArrayList<AreaScan> regions, Point startPosition) {
        // Switch statement for the class of list
        switch (list.getClass().toString()) {
            case RECHARGEPOINT:
                // If the class is a RechargePoint, search all region recharging stations
                ArrayList<Point> rechargingStations = new ArrayList<>();
                for (AreaScan region : regions) {
                    rechargingStations.add(closestPointFromPoint(region.rechargingStations, startPosition).getPoint());
                }
                return closestPointFromPoint(rechargingStations, startPosition).getPoint();
            case RECYCLINGBIN:
                // If the class is a RecyclingBin, search all region recharging stations
                ArrayList<Point> recyclingBins = new ArrayList<>();
                for (AreaScan region : regions) {
                    recyclingBins.add(closestPointFromPoint(region.recyclingBins, startPosition).getPoint());
                }
                return closestPointFromPoint(recyclingBins, startPosition).getPoint();
            case RECYCLINGSTATION:
                // If the class is a RecyclingStation, search all region recharging stations
                ArrayList<Point> recyclingStations = new ArrayList<>();
                for (AreaScan region : regions) {
                    recyclingStations.add(closestPointFromPoint(region.recyclingStations, startPosition).getPoint());
                }
                return closestPointFromPoint(recyclingStations, startPosition).getPoint();
            case WASTEBIN:
                // If the class is a WasteBin, search all region recharging stations
                ArrayList<Point> wasteBins = new ArrayList<>();
                for (AreaScan region : regions) {
                    wasteBins.add(closestPointFromPoint(region.wasteBins, startPosition).getPoint());
                }
                return closestPointFromPoint(wasteBins, startPosition).getPoint();
            case WASTESTATION:
                // If the class is a WasteStation, search all region recharging stations
                ArrayList<Point> wasteStations = new ArrayList<>();
                for (AreaScan region : regions) {
                    wasteStations.add(closestPointFromPoint(region.wasteStations, startPosition).getPoint());
                }
                return closestPointFromPoint(wasteStations, startPosition).getPoint();
            default:
                // By default return 0,0 something went wrong clearly
                return new Point(0,0);
        }
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

    /**
     * Important function to return the closest recharging point, separate due to the severity of the
     * maintenance goal
     *
     * @return returns the closest RechargePoint
     */
    public Point closestRecharge(GarryTheAgent agent) {
        Point closestPoint, newPoint;
        // If there is a rechargingStation in the current region
        if (!agent.currentRegion.rechargingStations.isEmpty()) {
            // use closestPoint to get the coordinates and return it
            closestPoint = closestPoint(agent.currentRegion.rechargingStations, agent).getPoint();
            return closestPoint;
        } else {
            // Init closestPoint to RECHARGE_POINT_LOCATION
            closestPoint = GarryTheAgent.RECHARGE_POINT_LOCATION;
            for (AreaScan region : agent.taskManager.regions) {
                // If region has a rechargingStation
                if (!region.rechargingStations.isEmpty()) {
                    newPoint = closestPoint(region.rechargingStations, agent).getPoint();
                    // Compare newPoint and closestPoint to find the closest station
                    if (newPoint.distanceTo(agent.getPosition()) < closestPoint.distanceTo(agent.getPosition())) {
                        closestPoint = newPoint;
                    }
                }
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
}
