package org.pelizzari.ship;

import java.util.Iterator;

import org.pelizzari.gis.Displacement;
import org.pelizzari.gis.Point;


/**
 * Error is computed based on the point-to-point distance to the target track 
 * @author andrea@pelizzari.org
 */
public class TrackError {
	
	final static float PARAM_MAX_DISTANCE_ERROR_THRESHOLD = 0.01f; // position is not too far if < threshold
	final static float DISTANCE_ERROR_AMPLIFIER = 100f; // multiply distance of positions that are too far 
	final static float MAX_CHANGE_OF_HEADING_ANGLE = 40f; // max angle for a change of heading not to be over the limit
	final static float HEADING_ERROR_AMPLIFIER = 1000f; // multiply by the number of  changes of heading over the limit 
	
	float[] errorVector;
	ShipTrack baseTrack;
			
	public TrackError(ShipTrack track) {
		baseTrack = track;
		int size = baseTrack.getPosList().size();
		errorVector = new float[size];
	}
	
	public void computeErrorVector(ShipTrack track) {
			if(errorVector.length != track.getPosList().size()) {
				System.err.println("computeError: track size does not match: "+
						errorVector.length + "<>" + track.getPosList().size());
			}
			for (int i = 0; i < errorVector.length; i++) {
				Point p1 = baseTrack.getPosList().get(i).point;
				Point p2 = track.getPosList().get(i).point;
				float distance = p1.distanceInMiles(p2);
				errorVector[i] = distance;
			}
	}
	
	public float meanLocError() {
		float meanError = 0;
		float sumSquareErrors = 0;
		for (int i = 0; i < errorVector.length; i++) {
			sumSquareErrors += errorVector[i] * errorVector[i];
		}
		meanError = (float) Math.sqrt(sumSquareErrors);
		return meanError;
	}
	
	public float meanLocErrorWithThreshold() {
		float meanError = 0;
		float sumSquareErrors = 0;
		for (int i = 0; i < errorVector.length; i++) {
			float distanceToTarget = errorVector[i];
			if (distanceToTarget > PARAM_MAX_DISTANCE_ERROR_THRESHOLD) {
				distanceToTarget = distanceToTarget*DISTANCE_ERROR_AMPLIFIER;
			}
			sumSquareErrors += distanceToTarget * distanceToTarget;
		}
		meanError = (float) Math.sqrt(sumSquareErrors);
		return meanError;
	}

	public float headingError() {
		int cohOnceOverLimitCount = baseTrack.countChangeOfHeadingOverLimit(MAX_CHANGE_OF_HEADING_ANGLE);
		int cohTwiceOverLimitCount = baseTrack.countChangeOfHeadingOverLimit(2*MAX_CHANGE_OF_HEADING_ANGLE);
		return cohOnceOverLimitCount * HEADING_ERROR_AMPLIFIER +
			   cohTwiceOverLimitCount * HEADING_ERROR_AMPLIFIER * 100;
	}	

	
	public String toString() {
		String s = "Track error: ";
		for (int i = 0; i < errorVector.length; i++) {
			s = s + errorVector[i] + " ";
		}
		s = s + "\n";
		s = s + "meanLocError = " + meanLocError() + "\n";		
		s = s + "meanLocErrorWithThreshold = " + meanLocErrorWithThreshold() + "\n";		
		s = s + "headingError = " + headingError() + "\n";		
		return s;
	}
}
