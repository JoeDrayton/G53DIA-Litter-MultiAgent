package uk.ac.nott.cs.g53dia.multiagent;
import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.*;

public class DemoMAS extends MAS {

    /** 
     * Number of agents in the MAS (this is just an **example**, not a requirement).
     */
    private static int MAS_SIZE = 1;
	public static TaskManager taskManager;
    public DemoMAS() {
    	this(new Random());
    }

	/**
	 * The DemoLitterAgent implementation makes random moves. For reproducibility, it
	 * can share the same random number generator as the environment.
	 * 
	 * @param r
	 *            The random number generator.
	 */
    public DemoMAS(Random r) {
    	taskManager.clear();
	// Create the agents
	for (int i=0; i<MAS_SIZE; i++) {
		GarryTheAgent agent; // = new GarryTheAgent(r, i, AgentSpecialisation.HYBRID);
		if(MAS_SIZE == 1) {
			 agent = new GarryTheAgent(r, i, AgentSpecialisation.HYBRID);
		} else if(MAS_SIZE %2 == 0){
			if(i % 2 == 0){
				agent = new GarryTheAgent(r, i, AgentSpecialisation.RECYCLING);
			} else {
				agent = new GarryTheAgent(r, i, AgentSpecialisation.WASTE);
			}
		} else {
			if(i % 2 == 0 && i != MAS_SIZE - 1){
				agent = new GarryTheAgent(r, i, AgentSpecialisation.RECYCLING);
			} else if(i % 2 == 1){
				agent = new GarryTheAgent(r, i, AgentSpecialisation.WASTE);
			} else {
				agent = new GarryTheAgent(r, i, AgentSpecialisation.HYBRID);
			}
		}

	    this.add(agent);
		taskManager = new TaskManager(agent);
	    agent.taskManager =  taskManager;
	}
    }


}
