package org.pelizzari.ai;

import ec.*;
import ec.simple.SimpleProblemForm;
import ec.util.*;

import java.awt.Color;
import java.io.*;

import org.pelizzari.gis.DisplacementSequence;
import org.pelizzari.gis.Map;
import org.pelizzari.ship.ShipTrack;

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

	// Output params
	public int genCount = 0;
	public int genMax = 0; // max number of generation (from params file)
	public static final int GEN_OUTPUT_RATE = 100; // print log every Nth
													// generations
	public static Map map;

	public void setup(final EvolutionState state, final Parameter base) {
		// DO NOT FORGET to call super.setup(...) !!
		super.setup(state, base);
		// check if the problem is the right one
		if (!(state.evaluator.p_problem instanceof DisplacementSequenceProblem)) {
			state.output.fatal("Wrong problem, expecting DisplacementSequenceProblem, found: "
					+ state.evaluator.p_problem);
		}		
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
		// read max generation number
		genMax = state.parameters.getInt(new Parameter("generations"), null);
		state.output.println("generations: " + genMax, popLog);
		// init Map and show target track
		map = new Map();
		DisplacementSequenceProblem prob = (DisplacementSequenceProblem)state.evaluator.p_problem;
		map.plotTrack(prob.getTargetTrack(), Color.BLACK);
		map.setVisible(true);
	}

	public void postEvaluationStatistics(final EvolutionState state) {
		// be certain to call the hook on super!
		super.postEvaluationStatistics(state);
		// write out a warning that the next generation is coming
		if (genCount % GEN_OUTPUT_RATE == 0 || genCount == genMax - 1) {
			state.output.println("GENERATION " + state.generation, popLog);
			// print out the population
			// state.population.printPopulation(state, popLog);
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
			Individual simplyTheBest = state.population.subpops[0].individuals[best];
			state.output.println("BEST", popLog);
			simplyTheBest.printIndividualForHumans(state, popLog);
			// built ship track using the winner's displacements
			// and starting from the first position if the Target track
			if (state.evaluator.p_problem instanceof DisplacementSequenceProblem) {
				DisplacementSequenceProblem prob = (DisplacementSequenceProblem)state.evaluator.p_problem;
				ShipTrack bestTrack = prob.makeTrack(state, (GeneVectorIndividual)simplyTheBest);
				state.output.println(bestTrack.toString(), popLog);
				map.plotTrack(bestTrack, Color.GRAY, ""+state.generation);
				if(genCount == genMax - 1) {
					try {
						System.in.read();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		genCount++;
	}
}
