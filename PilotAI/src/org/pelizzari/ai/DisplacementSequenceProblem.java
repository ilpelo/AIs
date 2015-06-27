package org.pelizzari.ai;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.pelizzari.gis.Displacement;
import org.pelizzari.gis.DisplacementSequence;
import org.pelizzari.gis.Map;
import org.pelizzari.gis.Point;
import org.pelizzari.ship.ShipPosition;
import org.pelizzari.ship.ShipTrack;
import org.pelizzari.ship.Timestamp;
import org.pelizzari.ship.TrackError;

import ec.*;
import ec.simple.*;
import ec.util.*;
import ec.vector.*;

public class DisplacementSequenceProblem extends Problem implements SimpleProblemForm {

	static ShipTrack targetTrack;
	static final boolean FROM_FILE = true;
	static final float SPEED = 10; // knots
//	static final float[] TRACK_LAT = {31f, 33f};
//	static final float[] TRACK_LON = {-12f, -12.1f};
	static final float[] TRACK_LAT = {31f, 32f, 31f, 30f, 31f};
	static final float[] TRACK_LON = {-12f, -11f, -10f, -11f, -12f};
	
	// init target track, map, etc.
	static {
		ShipTrack track = new ShipTrack();
		if (!FROM_FILE) {
			Point p = null;
			Point prevP = null;			
			for (int i = 0; i < TRACK_LON.length; i++) {
				p = new Point(TRACK_LAT[i], TRACK_LON[i]);
				int duration = 0;
				if(i>0) {
					duration = (int)(prevP.distanceInMiles(p)/SPEED*3600);
				}
				prevP = p;
				Timestamp ts = new Timestamp(100000 + i * duration);
				ShipPosition pos = new ShipPosition(p, ts);
				pos.setIndex(i);
				track.addPosition(pos);
			}
		} else {
			try {
				FileReader fr = new FileReader("C:\\master_data\\pos.csv");
				track.loadTrack(fr);
				fr.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		targetTrack = track;
		System.out.println("Problem initialized; Target " + track);
	}
	
	// ind is the individual to be evaluated.
	// We're given state and threadnum primarily so we
	// have access to a random number generator
	// (in the form:  state.random[threadnum] ) 
	// and to the output facility
	public void evaluate(final EvolutionState state,
			final Individual ind,
			final int subpopulation,
			final int threadnum)
	{
		if (ind.evaluated) return;   //don't evaluate the individual if it's already evaluated
		if (!(ind instanceof GeneVectorIndividual))
			state.output.fatal("evaluate: not a GeneVectorIndividual",null);
		GeneVectorIndividual displSeqInd = (GeneVectorIndividual) ind;
		// check if the length of target track corresponds to number of displacements 
		if(displSeqInd.genome.length != targetTrack.getPosList().size()-1) {
			state.output.fatal("evaluate: track length and displacement sequence do not match",null);
		}		
//		// build track corresponding to the individual (sequence of displacements)
//		ShipTrack trackInd = new ShipTrack();
//		DisplacementSequence displSeq = new DisplacementSequence();
//		for(int i=0; i < displSeqInd.genome.length; i++) {
//			if(!(displSeqInd.genome[i] instanceof DisplacementGene))
//				state.output.fatal("evaluate: not a DisplacementGene",null);
//			Displacement displ = ((DisplacementGene) displSeqInd.genome[i]).getAllele();
//			displSeq.add(displ);
//		}
		ShipTrack trackInd = makeTrack(state, displSeqInd);
		
		// compute fitness
		TrackError trackError = trackInd.computeTrackError(targetTrack);
		
		//float error = trackError.meanError();
		//float error = trackError.meanErrorWithThreshold();
		float error = trackError.headingAndLocationError();
		
		if (!(displSeqInd.fitness instanceof SimpleFitness))
			state.output.fatal("evaluate: not a SimpleFitness",null);

			((SimpleFitness)displSeqInd.fitness).setFitness(state,
				// ...the fitness... (negative!)
				-error,
				///... is the individual ideal?  Indicate here...
				error < 1);
		displSeqInd.evaluated = true;
	}
	
	public ShipTrack makeTrack(EvolutionState state, GeneVectorIndividual displSeqInd) {
		// build track corresponding to the individual (sequence of displacements)
		ShipTrack track = new ShipTrack();
		DisplacementSequence displSeq = new DisplacementSequence();
		for(int i=0; i < displSeqInd.genome.length; i++) {
			if(!(displSeqInd.genome[i] instanceof DisplacementGene))
				state.output.fatal("evaluate: not a DisplacementGene",null);
			Displacement displ = ((DisplacementGene) displSeqInd.genome[i]).getAllele();
			displSeq.add(displ);
		}
		track = ShipTrack.reconstructShipTrack(targetTrack.getFirstPosition(), displSeq, SPEED);
		return track;
	}
	
	public ShipTrack getTargetTrack() {
		return targetTrack;
	}
	
}