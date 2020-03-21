package uk.ac.nott.cs.g53dia.multiagent;

import javafx.util.Pair;

import java.util.ArrayList;

public class TaskManager {
    public ArrayList<AreaScan> regions = new ArrayList<>();
    private static ArrayList<GarryTheAgent> agents = new ArrayList<>();
    public TaskManager(GarryTheAgent agent){
        agents.add(agent);
    }

    public void activateRegion(AreaScan region){
        regions.remove(region);
    }
    public void deactivateRegion(AreaScan region){
        regions.add(region);
    }

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
        } else {
            // COde to fix task bullshit
        }
    }
}
