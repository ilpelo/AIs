package org.pelizzari.ship;

import java.util.Iterator;

import org.pelizzari.gis.Displacement;
import org.pelizzari.gis.Point;

public class TrackLocationError {
	
	final static float PARAM_MAX_DISTANCE_ERROR_THRESHOLD = 0.01f;
	
	float[] errorVector;
	ShipTrack baseTrack;
			
	public TrackLocationError(ShipTrack track) {
		baseTrack = track;
		int size = baseTrack.getPosList().size();
		errorVector = new float[size];
	}
	
	public void computeError(ShipTrack track) {
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
	
	public float meanError() {
		float meanError = 0;
		float sumSquareErrors = 0;
		for (int i = 0; i < errorVector.length; i++) {
			sumSquareErrors += errorVector[i] * errorVector[i];
		}
		meanError = (float) Math.sqrt(sumSquareErrors);
		return meanError;
	}
	
	public float meanErrorWithThreshold() {
		float meanError = 0;
		float sumSquareErrors = 0;
		for (int i = 0; i < errorVector.length; i++) {
			float distanceToTarget = errorVector[i];
			if (distanceToTarget > PARAM_MAX_DISTANCE_ERROR_THRESHOLD) {
				distanceToTarget = distanceToTarget*100;
			}
			sumSquareErrors += distanceToTarget * distanceToTarget;
		}
		meanError = (float) Math.sqrt(sumSquareErrors);
		return meanError;
	}
		
	public String toString() {
		String s = "Track error: ";
		for (int i = 0; i < errorVector.length; i++) {
			s = s + errorVector[i] + " ";
		}
		return s;
	}
}
