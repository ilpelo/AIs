package org.pelizzari.ai;

import ec.*;
import ec.simple.SimpleProblemForm;
import ec.util.*;

import java.awt.Color;
import java.io.*;

import org.pelizzari.gis.DisplacementSequence;
import org.pelizzari.gis.Map;
import org.pelizzari.ship.ShipTrack;
import org.pelizzari.ship.TrackLocationError;

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
	public static final int GEN_OUTPUT_RATE = 3000; // print log every Nth
													// generations
	public static Map map, map1;
	// the filename where the final map is saved
	public static final String P_IMGFILE = "image-file";
	File imageFile;

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
		
		
		// build filename to print the map, use crossover/mutation prob and selection
		// and tournament size
		int tournSize = state.parameters.getInt(new Parameter("select.tournament.size"), null);
		Double crossoverLikelihood = state.parameters.getDouble(new Parameter("pop.subpop.0.species.pipe.source.0.likelihood"), null);
		Double mutationProb = state.parameters.getDouble(new Parameter("pop.subpop.0.species.mutation-prob"), null);
		String fileName = state.parameters.getString(base.push(P_IMGFILE), null)+
						"_gen_"+genMax+
						"_cross_"+String.format("%2.2f", crossoverLikelihood)+
						"_mut_"+String.format("%2.2f", mutationProb)+
						"_tourn_"+tournSize+
						".png";
		imageFile = new File(fileName);
		state.output.println("Map image: " + imageFile.getAbsolutePath(), popLog);	
		// init Map and show target track
		map = new Map();
		map1 = new Map();
		DisplacementSequenceProblem prob = (DisplacementSequenceProblem)state.evaluator.p_problem;
		map.plotTrack(prob.getTargetTrack(), Color.GREEN);
		map1.plotTrack(prob.getTargetTrack(), Color.GREEN);
		map.setVisible(true);
		// target displacements
		state.output.println("Target track: \n" + prob.getTargetTrack(), popLog);
		state.output.println("Target displacements: \n" + prob.getTargetTrack().computeDisplacements(), popLog);
		
	}

	public void postEvaluationStatistics(final EvolutionState state) {
		// be certain to call the hook on super!
		super.postEvaluationStatistics(state);
		// show best individual
		boolean lastGen = genCount == genMax - 1;
		if (genCount % GEN_OUTPUT_RATE == 0 || lastGen) {
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
			
			// print individual to pop log file
			state.output.println("BEST", popLog);
			simplyTheBest.printIndividualForHumans(state, popLog);
						
			// build ship track using the winner's displacements
			// and starting from the first position if the Target track
			if (state.evaluator.p_problem instanceof DisplacementSequenceProblem) {
				DisplacementSequenceProblem prob = (DisplacementSequenceProblem)state.evaluator.p_problem;
				ShipTrack bestTrack = prob.makeTrack(state, (GeneVectorIndividual)simplyTheBest);
				state.output.println(bestTrack.toString(), popLog);
				
				TrackLocationError trackError = bestTrack.computeTrackLocationError(prob.getTargetTrack());
				state.output.println(""+trackError, popLog);
				
				Color trackColor = lastGen?Color.PINK:Color.GRAY;
				map.plotTrack(bestTrack, trackColor, ""+state.generation);
				if(lastGen) {
					map1.plotTrack(bestTrack, Color.PINK, ""+state.generation);
					map1.setVisible(true);
					// set up imageFile					
					map1.saveAsImage(imageFile);
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
