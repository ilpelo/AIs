package org.pelizzari.ai;

import ec.*;
import ec.simple.SimpleProblemForm;
import ec.util.*;

import java.awt.Color;
import java.io.*;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.pelizzari.gis.Box;
import org.pelizzari.gis.DisplacementSequence;
import org.pelizzari.gis.Map;
import org.pelizzari.kml.KMLGenerator;
import org.pelizzari.mine.MineVoyages;
import org.pelizzari.ship.ChangeOfHeadingSequence;
import org.pelizzari.ship.HeadingSequence;
import org.pelizzari.ship.ShipPosition;
import org.pelizzari.ship.ShipPositionList;
import org.pelizzari.ship.ShipTrack;
import org.pelizzari.ship.ShipTrackSegment;
import org.pelizzari.ship.TrackError;
import org.pelizzari.time.Timestamp;
import org.w3c.dom.Element;

import ec.vector.*;

public class BestStatistics extends Statistics {
	
	//public static final boolean DEBUG = true;
	
	// The parameter string and log number of the file for our readable
	// population
	public static final String P_POPFILE = "pop-file";
	public int popLog;

	// The parameter string and log number of the file for our best-genome-#3
	// individual
	public static final String P_INFOFILE = "info-file";
	public int infoLog;

	public int genCount = 0;
	public int genMax = 0; // max number of generation (from params file)
	public static final int GEN_OUTPUT_RATE = 10; // print log every Nth
													// generations
	public static Map map, map1;
	//public KMLGenerator kmlGenerator;
	// the filename where the final map is saved
	public static final String P_IMGFILE = "image-file";
	File imageFile;
	// KML file
	static final String FILE_DIR = "c:/master_data/";
	public static final String KML_OUTFILE = "AIs-result";	

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
		// init Map
		map = new Map();
		map1 = new Map();
		// init KML
		KMLGenerator kmlGenerator = null;
		try {
			kmlGenerator = new KMLGenerator();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		kmlGenerator.addIconStyle("trainingPositionStyle",
				//"http://maps.google.com/mapfiles/kml/shapes/target.png");
				"http://maps.google.com/mapfiles/kml/shapes/placemark_circle.png");
		
		// show training positions
		DisplacementSequenceProblem prob = (DisplacementSequenceProblem)state.evaluator.p_problem;
		map.plotShipPositions(prob.getTrainingShipPositionList(), Color.GREEN);
		map1.plotShipPositions(prob.getTrainingShipPositionList(), Color.GREEN);
		
		//map.setVisible(true);
		
		// on KML too
		for(ShipPosition pos: prob.getTrainingShipPositionList().getPosList()) {
			kmlGenerator.addPoint("trainingPositionStyle", "", pos.getPoint().lat, pos.getPoint().lon);
		}
		String kmlFile = FILE_DIR+KML_OUTFILE+"_trainingset.kml";
		System.out.println("Saving KML: "+kmlFile);
		kmlGenerator.saveKMLFile(kmlFile);
		
		// target displacements
//		state.output.println("Target track: \n" + prob.getTargetTrack(), popLog);
//		DisplacementSequence displSeq = prob.getTargetTrack().computeDisplacements();
//		state.output.println("Target displacements: \n" + displSeq, popLog);
//		HeadingSequence headSeq = prob.getTargetTrack().computeHeadingSequence();
//		state.output.println("Target heading sequence: \n" + headSeq, popLog);		
//		ChangeOfHeadingSequence cohSeq = prob.getTargetTrack().computeChangeOfHeadingSequence();
//		state.output.println("Target change of heading sequence: \n" + cohSeq, popLog);		
	}

	public void postEvaluationStatistics(final EvolutionState state) {
		// be certain to call the hook on super!
		super.postEvaluationStatistics(state);

		showBestIndividual(state); //, genCount, genMax, popLog);
	}	
		
	public Individual getBestIndividual(EvolutionState state) {
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
		return simplyTheBest;
	}
	
	public void drawSegmentBoxes(ShipTrack track, Map map) {
		List<ShipTrackSegment> segments = track.getSegList();
		for (ShipTrackSegment segment : segments) {
			Box box = TrackError.makeSegmentBox(segment);
			map.plotBox(box, Color.GRAY);
		}
	}
	
	public void drawOnMap(
			ShipTrack track, 
			EvolutionState state, 
			boolean lastGen,
			String[] wayPointLabels) {
		Color trackColor = lastGen?Color.PINK:Color.GRAY;
		map.plotTrack(track, trackColor, ""+state.generation);
		//drawSegmentBoxes(track, map);
		// make KML
		KMLGenerator kmlGenerator1 = null;
		try {
			kmlGenerator1 = new KMLGenerator();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		final int HUE_LEVELS = track.getSegList().size();
		kmlGenerator1.addColoredStyles("pointStyle", 
				"http://maps.google.com/mapfiles/kml/shapes/placemark_circle.png",
				HUE_LEVELS, false);
		kmlGenerator1.addWaypointStyle("waypointStyle");
		
		Element trainingShipPositionsFolder = kmlGenerator1.addFolder("Training Set"); 
				
		// draw training points
		int i = 0;
		for(ShipTrackSegment seg: track.getSegList()) {
			for(ShipPosition pos: seg.getTargetPosList()) {
				kmlGenerator1.addPoint(
						trainingShipPositionsFolder,
						"pointStyle"+i, 
						"", 
						pos.getPoint().lat, 
						pos.getPoint().lon);
			}
			i++;
		}
		// draw best track
		kmlGenerator1.addTrack(track, wayPointLabels);
		
		String kmlFile1 = FILE_DIR+KML_OUTFILE+"_"+state.generation+".kml";
		System.out.println("Saving KML: "+kmlFile1);
		kmlGenerator1.saveKMLFile(kmlFile1);
		// save map image
		if(lastGen) {
			map1.plotTrack(track, Color.PINK, ""+state.generation);
			//drawSegmentBoxes(track, map1);
			
			//map1.setVisible(true);
			
			// set up imageFile		
			map1.saveAsImage(imageFile);
			// flush
			try {
				System.in.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}			
	}
	
//	public void makeKML(ShipPositionList trainingPositionList, ShipTrack bestTrack) {
//		for(ShipPosition pos: trainingPositionList.getPosList()) {
//			kmlGenerator.addPoint("trainingPositionStyle", "", pos.getPoint().lat, pos.getPoint().lon);
//		}
//		
//		kmlGenerator.addTrack(bestTrack, "");
//		String kmlFile = FILE_DIR+"/"+KML_OUTFILE+".kml";
//		System.out.println("Saving KML: "+kmlFile);
//		kmlGenerator.saveKMLFile(kmlFile);
//		try {
//			System.in.read();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	public void showBestIndividual(EvolutionState state 
//									      int genCount,
//									      int genMax,
//									      int popLog,
									      ) {			
		// show best individual
		boolean lastGen = genCount == genMax - 1;
		if (genCount % GEN_OUTPUT_RATE == 0 || lastGen) {
			state.output.println("============= GENERATION " + state.generation, popLog);
			// print out the population
			// state.population.printPopulation(state, popLog);
			// print out best genome individual in subpop 0
			
			Individual simplyTheBest = getBestIndividual(state);
			
			// print individual to pop log file
			state.output.println("BEST Track", popLog);
			simplyTheBest.printIndividualForHumans(state, popLog);
						
			// build ship track using the winner's displacements
			// and starting from the first position of the Target track
			if (state.evaluator.p_problem instanceof DisplacementSequenceProblem) {
				DisplacementSequenceProblem prob = (DisplacementSequenceProblem)state.evaluator.p_problem;
				ShipTrack bestTrack = prob.makeTrack(state, (GeneVectorIndividual)simplyTheBest);
				state.output.println(bestTrack.toString(), popLog);
				
				TrackError trackError = null;
				try {
					// make the corresponding segments and normalize time
					bestTrack.normalizeTimestampsAndComputeTrackSegments(
							new Timestamp(MineVoyages.REFERENCE_START_DT), 
							MineVoyages.REFERENCE_VOYAGE_DURATION_IN_SEC);
					trackError = bestTrack.computeTrackError(
							prob.getTrainingShipPositionList(),
							prob.getDestinationPoint(),
							prob.DISTANCE_TO_DESTINATION_ERROR_FACTOR,
							prob.DISTANCE_ERROR_FACTOR,
							prob.HEADING_ERROR_FACTOR);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				state.output.println(""+trackError, popLog);
				
				String[] wayPointLabels = new String[bestTrack.getSegList().size()];
				int i=0;
				for (ShipTrackSegment seg : bestTrack.getSegList()) {
					wayPointLabels[i] = "D="+seg.getAvgSquaredPerpendicularDistanceToTargetPositions();
					i++;
				}
				
				drawOnMap(bestTrack, state, lastGen, wayPointLabels);
			}
		}
		genCount++;
	}
}
