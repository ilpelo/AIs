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
import org.pelizzari.ship.TrackError;
import org.pelizzari.time.Timestamp;

import ec.*;
import ec.simple.*;
import ec.util.*;
import ec.vector.*;

public class DisplacementSequenceProblem extends Problem implements
		SimpleProblemForm {

	static ShipTrack targetTrack;
	static ShipPosition startPosition;
	static final boolean FROM_FILE = true;
	static final float SPEED = 10; // knots
	// static final float[] TRACK_LAT = {31f, 33f};
	// static final float[] TRACK_LON = {-12f, -12.1f};
	static final float[] TRACK_LAT = { 31f, 32f, 31f, 30f, 31f };
	static final float[] TRACK_LON = { -12f, -11f, -10f, -11f, -12f };

	// init target track, map, etc.
	static {
		ShipTrack track = new ShipTrack();
		if (!FROM_FILE) {
			Point p = null;
			Point prevP = null;
			for (int i = 0; i < TRACK_LON.length; i++) {
				p = new Point(TRACK_LAT[i], TRACK_LON[i]);
				int duration = 0;
				if (i > 0) {
					duration = (int) (prevP.distanceInMiles(p) / SPEED * 3600);
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
		// WARNING: overwrite timestamps!!! 10 knots.
		DisplacementSequence displSeq = track.computeDisplacements();
		displSeq = displSeq.increaseDisplacements(2);
		targetTrack = ShipTrack.reconstructShipTrack(track.getFirstPosition(),
				displSeq, SPEED);
		// set start position close to the first position of the track (0.1 deg North)
		Point startPoint = new Point(track.getFirstPosition().getPoint().lat+0.1f,
									 track.getFirstPosition().getPoint().lon);
		startPosition = new ShipPosition(startPoint, track.getFirstPosition().getTs());
		System.out.println("Problem initialized; Target " + track);
	}

	// ind is the individual to be evaluated.
	// We're given state and threadnum primarily so we
	// have access to a random number generator
	// (in the form: state.random[threadnum] )
	// and to the output facility
	public void evaluate(final EvolutionState state, final Individual ind,
			final int subpopulation, final int threadnum) {
		if (ind.evaluated)
			return; // don't evaluate the individual if it's already evaluated
		if (!(ind instanceof GeneVectorIndividual))
			state.output.fatal("evaluate: not a GeneVectorIndividual", null);
		GeneVectorIndividual displSeqInd = (GeneVectorIndividual) ind;
		ShipTrack trackInd = makeTrack(state, displSeqInd);

		// compute fitness
		TrackError trackError = null;
		try {
			trackError = trackInd.computeTrackError(targetTrack);
		} catch (Exception e) {
			state.output.fatal("computeTrackError: "+e, null);
			e.printStackTrace();
		}

		//float totalSegmentError = trackError.totalSegmentError();
		// float meanLocErrorWithThreshold = trackError.meanLocErrorWithThreshold();
//		float headingError = trackError.headingError();
//		float destinationError = trackError.destinationError();
//		float distanceError = trackError.getAvgSquaredDistanceAllSegments();
//		float noCoverageError = trackError.getNoCoverageError();
		// int numberOfSegments = trackError.getTrackSize();

		float error =
		// trackError.headingError() +
		//trackError.destinationError() +
		//trackError.getAvgSquaredDistanceAllSegments() +
		trackError.getNoCoverageError() +
		//trackError.avgTotalSegmentError() +
		0f;

		if (!(displSeqInd.fitness instanceof SimpleFitness))
			state.output.fatal("evaluate: not a SimpleFitness", null);

		((SimpleFitness) displSeqInd.fitness).setFitness(state,
		// ...the fitness... (negative!)
				-error,
				// /... is the individual ideal? Indicate here...
				// /error < 1);
				// WARNING: overwrite. Never find ideal.
				false);
		displSeqInd.evaluated = true;
	}

	@Override
	public void closeContacts(EvolutionState state, int result) {
		// TODO Auto-generated method stub
		super.closeContacts(state, result);
		System.out.println("============= Found in generation: "
				+ state.generation + "\n");
		BestStatistics bestStats = (BestStatistics) state.statistics.children[0];
		// bestStats.showBestIndividual(state);
		Individual idealInd = bestStats.getBestIndividual(state);
		ShipTrack idealTrack = makeTrack(state, (GeneVectorIndividual) idealInd);
		System.out.println("Ideal Track: " + idealTrack);
		bestStats.drawOnMap(idealTrack, state, true);
	}

	public ShipTrack makeTrack(EvolutionState state,
			GeneVectorIndividual displSeqInd) {
		// build track corresponding to the individual (sequence of
		// displacements)
		ShipTrack track = new ShipTrack();
		DisplacementSequence displSeq = new DisplacementSequence();
		for (int i = 0; i < displSeqInd.genome.length; i++) {
			if (!(displSeqInd.genome[i] instanceof DisplacementGene))
				state.output.fatal("evaluate: not a DisplacementGene", null);
			Displacement displ = ((DisplacementGene) displSeqInd.genome[i])
					.getAllele();
			displSeq.add(displ);
		}
		track = ShipTrack.reconstructShipTrack(startPosition, displSeq, SPEED);
		return track;
	}

	public ShipTrack getTargetTrack() {
		return targetTrack;
	}

}