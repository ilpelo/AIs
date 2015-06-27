package org.pelizzari.ship;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pelizzari.gis.Box;
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
	
	// the track to which the error refers to 
	ShipTrack baseTrack;
	// a measure of the error of each position of the baseTrack
	float[] errorVector;
			
	public TrackError(ShipTrack track) {
		baseTrack = track;
		int size = baseTrack.getPosList().size();
		errorVector = new float[size];
	}
	
	// basic point to point distance error
	public void computePointToPointErrorVector(ShipTrack targetTrack) {
			if(errorVector.length != targetTrack.getPosList().size()) {
				System.err.println("computeError: track size does not match: "+
						errorVector.length + "<>" + targetTrack.getPosList().size());
			}
			for (int i = 0; i < errorVector.length; i++) {
				Point p1 = baseTrack.getPosList().get(i).point;
				Point p2 = targetTrack.getPosList().get(i).point;
				float distance = p1.distanceInMiles(p2);
				errorVector[i] = distance;
			}
	}

	public void computeErrorVector(ShipTrack targetTrack) {
//		Heading head1 = null;
//		Heading head2 = null;
//		for (Heading heading : headingSeq) {
//			if (head1 == null) {
//				head1 = heading;
//				continue;
//			}
//			head2 = heading;
//			ChangeOfHeading coh = new ChangeOfHeading(head1, head2);
//			changeOfHeadingSeq.add(coh);
//			head1 = head2;
//		}
		ShipPosition p1 = null;
		ShipPosition p2 = null;
		int i = 0;
		for (ShipPosition pos : baseTrack.posList) {
			if (p1 == null) {
				p1 = pos;
				continue;
			}
			p2 = pos;
			Box box = new Box(p1.point, p2.point);
			List<ShipPosition> targetPosList = targetTrack.getPosListInBoxAndInterval(box, interval)

			errorVector[i] = distance;
			i++;
			p1 = p2;
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
