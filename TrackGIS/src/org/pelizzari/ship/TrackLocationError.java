package org.pelizzari.ship;

import org.pelizzari.gis.Point;

public class TrackLocationError {

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
				errorVector[i] = p1.distanceInMiles(p2);
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

}
