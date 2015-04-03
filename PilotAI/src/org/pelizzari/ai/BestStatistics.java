package org.pelizzari.ai;

import ec.*;
import ec.util.*;
import java.io.*;
import ec.vector.*;

public class BestStatistics extends Statistics {
	// The parameter string and log number of the file for our readable
	// population
	public static final String P_POPFILE = "pop-file";
	public int popLog;

	// The parameter string and log number of the file for our best-genome-#3
	// individual
	public static final String P_INFOFILE = "info-file";
	public int infoLog;

	public void setup(final EvolutionState state, final Parameter base) {
		// DO NOT FORGET to call super.setup(...) !!
		super.setup(state, base);
		// set up popFile
		File popFile = state.parameters.getFile(base.push(P_POPFILE), null);
		if (popFile != null)
			try {
				popLog = state.output.addLog(popFile, true);
			} catch (IOException i) {
				state.output
						.fatal("An IOException occurred while trying to create the log "
								+ popFile + ":\n" + i);
			}

		// similarly we set up infoFile
		File infoFile = state.parameters.getFile(base.push(P_INFOFILE), null);
		if (infoFile != null)
			try {
				infoLog = state.output.addLog(infoFile, true);
			} catch (IOException i) {
				state.output
						.fatal("An IOException occurred while trying to create the log "
								+ infoFile + ":\n" + i);
			}

	}

	public void postEvaluationStatistics(final EvolutionState state) {
		// be certain to call the hook on super!
		super.postEvaluationStatistics(state);
		// write out a warning that the next generation is coming
		state.output.println("-----------------------\nGENERATION "
				+ state.generation + "\n-----------------------", popLog);
		// print out the population
		//state.population.printPopulation(state, popLog);
		// print out best genome individual in subpop 0
		int best = 0;
		Fitness best_fit = state.population.subpops[0].individuals[0].fitness;
		for (int y = 1; y < state.population.subpops[0].individuals.length; y++) {
			Fitness val_fit = state.population.subpops[0].individuals[y].fitness;
			if (val_fit.betterThan(best_fit)) {
				best = y;
				best_fit = val_fit;
			}
		}
		state.output.println(
				"-----------------------\nTHE BEST\n-----------------------",
				popLog);
		state.population.subpops[0].individuals[best].printIndividualForHumans(
				state, popLog);
	}
}
