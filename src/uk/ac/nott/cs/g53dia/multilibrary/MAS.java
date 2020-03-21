package uk.ac.nott.cs.g53dia.multilibrary;

import java.util.*;

public class MAS extends ArrayList<LitterAgent> {

    private static final long serialVersionUID = 8031611383212571139L;

    /**
     * The average score achieved by agents in the MAS
     */
    public long getScore() {
	int disposed = 0;
		
	for (LitterAgent a:this) {
	    disposed += a.recyclingDisposed + a.wasteDisposed;    
	}
		
	// Return the average score for each agent 
	return disposed / this.size();
    }
}
