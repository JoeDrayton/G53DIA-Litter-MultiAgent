package uk.ac.nott.cs.g53dia.multiagent;
import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.*;

public class GarryMAS extends MAS {

    /** 
     * Number of agents in the MAS (this is just an **example**, not a requirement).
     */
    private static int MAS_SIZE = 1;
	public static TaskManager taskManager;
    public GarryMAS() {
    	this(new Random());
    }
	/*
	1:
	Run: 0score: 3.660E04
	Run: 1score: 3.635E04
	Run: 2score: 3.628E04
	Run: 3score: 3.669E04
	Run: 4score: 3.850E04
	Run: 5score: 3.741E04
	Run: 6score: 3.800E04
	Run: 7score: 3.933E04
	Run: 8score: 3.740E04
	Run: 9score: 3.722E04

	Total average score: 3.738E04 - 14.952


	2:
	Run: 0score: 3.359E04
	Run: 1score: 3.463E04
	Run: 2score: 3.570E04
	Run: 3score: 3.631E04
	Run: 4score: 3.758E04
	Run: 5score: 3.439E04
	Run: 6score: 3.405E04
	Run: 7score: 3.651E04
	Run: 8score: 3.478E04
	Run: 9score: 3.531E04

	Total average score: 3.528E04 - 14.112 : 0.84 ^ 0.42


	3: Run: 0score: 3.365E04
	Run: 1score: 3.343E04
	Run: 2score: 3.415E04
	Run: 3score: 3.539E04
	Run: 4score: 3.570E04
	Run: 5score: 3.630E04
	Run: 6score: 3.484E04
	Run: 7score: 3.548E04
	Run: 8score: 3.456E04
	Run: 9score: 3.344E04

	Total average score: 3.470E04 - 13.88 : 1.072 ^ 0.357

	4: Run: 0score: 3.424E04
	Run: 1score: 3.293E04
	Run: 2score: 3.420E04
	Run: 3score: 3.494E04
	Run: 4score: 3.413E04
	Run: 5score: 3.454E04
	Run: 6score: 3.429E04
	Run: 7score: 3.491E04
	Run: 8score: 3.476E04
	Run: 9score: 3.412E04

	Total average score: 3.430E04 - 13.72 : 1.232 ^ 0.308
	 */
	/**
	 * The DemoLitterAgent implementation makes random moves. For reproducibility, it
	 * can share the same random number generator as the environment.
	 * 
	 * @param r
	 *            The random number generator.
	 */
    public GarryMAS(Random r) {
    	taskManager.clear();
	// Create the agents
	for (int i=0; i<MAS_SIZE; i++) {
		GarryTheAgent agent;
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
