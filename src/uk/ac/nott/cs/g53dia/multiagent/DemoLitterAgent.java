package uk.ac.nott.cs.g53dia.multiagent;

import java.util.Random;

import uk.ac.nott.cs.g53dia.multilibrary.*;

/**
 * A simple example LitterAgent
 * 
 * @author Julian Zappala
 */
/*
 * Copyright (c) 2011 Julian Zappala
 * 
 * See the file "license.terms" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
public class DemoLitterAgent extends LitterAgent {

	public DemoLitterAgent() {
		this(new Random());
	}

	/**
	 * The tanker implementation makes random moves. For reproducibility, it
	 * can share the same random number generator as the environment.
	 * 
	 * @param r
	 *            The random number generator.
	 */
	public DemoLitterAgent(Random r) {
		this.r = r;
	}

	/*
	 * The following is a simple demonstration of how to write a tanker. The
	 * code below is very stupid and simply moves the tanker randomly until the
	 * charge agt is half full, at which point it returns to a charge pump.
	 */
	public Action senseAndAct(Cell[][] view, long timestep) {

		// If charge is low and not at the charge pump then move towards the charge
		// pump.
		if ((getChargeLevel() <= MAX_CHARGE / 2) && !(getCurrentCell(view) instanceof RechargePoint)) {
			return new MoveTowardsAction(RECHARGE_POINT_LOCATION);
		} else {
			// Otherwise, move randomly.
			return new MoveAction(r.nextInt(8));
		}
	}

}
