package uk.ac.nott.cs.g53dia.multiagent;

import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.ArrayList;

public class AreaScan {
    private static final String WASTEBIN = "class uk.ac.nott.cs.g53dia.multilibrary.WasteBin";
    private static final String WASTESTATION = "class uk.ac.nott.cs.g53dia.multilibrary.WasteStation";
    private static final String RECYCLINGBIN = "class uk.ac.nott.cs.g53dia.multilibrary.RecyclingBin";
    private static final String RECYCLINGSTATION = "class uk.ac.nott.cs.g53dia.multilibrary.RecyclingStation";
    private static final String RECHARGEPOINT = "class uk.ac.nott.cs.g53dia.multilibrary.RechargePoint";
    private static final String EMPTYCELL = "class uk.ac.nott.cs.g53dia.multilibrary.EmptyCell";
    public ArrayList<WasteBin> wasteBins = new ArrayList<>();
    public ArrayList<WasteStation> wasteStations = new ArrayList<>();
    public ArrayList<RecyclingBin> recyclingBins = new ArrayList<>();
    public ArrayList<RecyclingStation> recyclingStations = new ArrayList<>();
    public ArrayList<RechargePoint> rechargingStations = new ArrayList<>();
    public ArrayList<Task> wasteTasks = new ArrayList<>();
    public ArrayList<Task> recyclingTasks = new ArrayList<>();
    public Point location;

    public AreaScan(Point position){
        location = position;
    }
    public void scanCells(Cell[][] view) {
        for (int i = 0; i < view.length; i++) {
            for (int j = 0; j < view.length; j++) {
                Cell currentCell = view[i][j];
                switch (view[i][j].getClass().toString()) {
                    case WASTEBIN:
                        if (!wasteBins.contains(currentCell)) {
                            WasteBin wasteBin = (WasteBin) currentCell;
                            if (wasteBin.getTask() != null) {
                                Task newTask = wasteBin.getTask();
                                wasteTasks.add(newTask);
                            }
                            wasteBins.add((WasteBin) view[i][j]);
                        }
                        break;
                    case WASTESTATION:
                        if (!wasteStations.contains(currentCell)) {
                            wasteStations.add((WasteStation) view[i][j]);
                        }
                        break;
                    case RECYCLINGBIN:
                        if (!recyclingBins.contains(currentCell)) {
                            RecyclingBin recycleBin = (RecyclingBin) currentCell;
                            if (recycleBin.getTask() != null) {
                                Task newTask = recycleBin.getTask();
                                recyclingTasks.add(newTask);
                            }
                            recyclingBins.add((RecyclingBin) view[i][j]);
                        }
                        break;
                    case RECYCLINGSTATION:
                        if (!rechargingStations.contains(currentCell)) {
                            recyclingStations.add((RecyclingStation) view[i][j]);
                        }
                        break;
                    case RECHARGEPOINT:
                        if (!rechargingStations.contains(currentCell)) {
                            rechargingStations.add((RechargePoint) view[i][j]);
                        }
                        break;
                    case EMPTYCELL:
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
