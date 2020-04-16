// Written by Haoyang Wang
// Modified by nza, bsl, Simon Castle-Green

package uk.ac.nott.cs.g53dia.multisimulator;

import uk.ac.nott.cs.g53dia.multiagent.*;
import uk.ac.nott.cs.g53dia.multilibrary.*;

import java.text.*;
import java.util.Random;

public class MultiEvaluator {

	private static int DURATION = 10000;
	private static int NRUNS = 10;
	private static int SEED = 20;

	public static void main(String[] args) {

		long score = 0;
		DecimalFormat df = new DecimalFormat("0.000E00");

		// run the agent for NRUNS times and compute the average score
		for (int i = 0; i < NRUNS; i++) {
			String error = "";
			Random r = new Random((long) i + SEED);
			// Create an environment
			Environment env = new Environment(LitterAgent.MAX_CHARGE/2, r);
			// Create a MAS
			MAS mas = new GarryMAS(r);
			// Start executing the agents
			run:
			while (env.getTimestep() < DURATION) {
				// Advance the environment timestep
				env.tick();
				for (LitterAgent a:mas) {
					// Get the current view of the agent
					Cell[][] view = env.getView(a.getPosition(), LitterAgent.VIEW_RANGE);
					// Let the agent choose an action
					Action act = a.senseAndAct(view, env.getTimestep());
					// Try to execute the action
					try {
						act.execute(env, a);
					} catch (OutOfBatteryException obe) {
						error = " " + obe.getMessage() + " at timestep " + env.getTimestep();
						break run;
					} catch (IllegalActionException ile) {
						System.err.println(ile.getMessage());
					} catch (Exception e) {
						error = " " + e.getMessage() + " at timestep " + env.getTimestep();
						break run;
					}
				}
			}
			System.out.println("Run: " + i + "score: " + df.format(mas.getScore()) + error);
			score = score + mas.getScore();
		}
		System.out.println("\nTotal average score: " + df.format(score / NRUNS));
	}
}