package uk.ac.nott.cs.g53dia.multiagent;
import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.util.*;

public class DemoMAS extends MAS {

    /** 
     * Number of agents in the MAS (this is just an **example**, not a requirement).
     */
    private static int MAS_SIZE = 3;
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
	// Create the agents

	for (int i=0; i<MAS_SIZE; i++) {
	    this.add(new GarryTheAgent(r, i));
	}
    }
}
