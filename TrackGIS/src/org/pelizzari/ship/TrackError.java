package org.pelizzari.ship;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pelizzari.gis.Box;
import org.pelizzari.gis.Displacement;
import org.pelizzari.gis.Point;
import org.pelizzari.time.TimeInterval;
import org.pelizzari.time.Timestamp;


/**
 * Error is computed based on the point-to-point distance to the target track 
 * @author andrea@pelizzari.org
 */
public class TrackError {
	
	final static float PARAM_MAX_DISTANCE_ERROR_THRESHOLD = 0.01f; // position is not too far if < threshold
	final static float DISTANCE_ERROR_AMPLIFIER = 100f; // multiply distance of positions that are too far 
	final static float MAX_CHANGE_OF_HEADING_ANGLE = 40f; // max angle for a change of heading not to be over the limit
	final static float HEADING_ERROR_AMPLIFIER = 1000f; // multiply by the number of  changes of heading over the limit 
	final static float BAD_TRACK_SEGMENT_FITNESS = 1000000f; // artificially high distance for segment that does not follow the target path
	
	// the track to which the error refers to 
	ShipTrack baseTrack;
	// a measure of the error of each position of the baseTrack
	float[] errorVector;
	// extra info for debug purposes (1 string for each position)
	String[] extraInfo;
	boolean debug = false;
			
	public TrackError(ShipTrack track, boolean debug) {
		this.debug = debug;
		baseTrack = track;
		int size = baseTrack.getPosList().size();
		errorVector = new float[size];
		if(debug) extraInfo = new String[size];
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

	public void computeErrorVector(ShipTrack targetTrack) throws Exception {
		ShipPosition p1 = null;
		ShipPosition p2 = null;
//		Timestamp ts1 = null;
//		Timestamp ts2 = null;
		int i = 0;
		for (ShipPosition pos : baseTrack.posList) {
			if (p1 == null) {
				p1 = pos;
				//ts1 = targetTrack.getFirstPosition().ts;
				errorVector[0] = 0; // first position is fixed and always ok
				if(debug) extraInfo[0] = "";
				i = 1;
				continue;
			}
			p2 = pos;
			// ts of the 2nd position of the GA track is based on the avg. speed of the target track
			float targetAvgSpeed = targetTrack.computeAverageSpeed();
			if(targetAvgSpeed == 0) {
				System.err.println("computeErrorVector: average speed is zero");
				throw new Exception("Zero Average Speed");
			}			
			float distance = p1.point.distanceInMiles(p2.point);
			int duration = (int) (distance / targetAvgSpeed * 3600); // in seconds
			p2.setTs(p1.ts.getTs()+duration);
			// bounding box of the 2 positions of the GA track
			Box box = new Box(p1.point, p2.point);
			// time difference of the 2 positions of the GA track
			TimeInterval interval = new TimeInterval(p1.ts, p2.ts);
			// filter position of target track and compute total squared distance to segment
			List<ShipPosition> targetPosList = targetTrack.getPosListInBoxAndInterval(box, interval);
			int nPos = targetPosList.size();
			float totSquaredDistance = 0;
			if(nPos == 0 || 
			   (nPos == 1 && i == 1)) { // first segment always include starting point of target track
				totSquaredDistance = BAD_TRACK_SEGMENT_FITNESS;
				if(debug) extraInfo[i] = "Segment "+i+": no points within segment: "+p1+"-"+p2+"\n";
			} else {
				if(debug) extraInfo[i] = "Segment "+i+": "+p1+"-"+p2+"\n";
				for (ShipPosition targetPos : targetPosList) {
					float squaredPointDistance = targetPos.point.approxSquaredDistanceToSegment(p1.point, p2.point);
					if(debug) extraInfo[i] = extraInfo[i] + "Point "+targetPos + ": "+ squaredPointDistance + "\n";					
					totSquaredDistance += squaredPointDistance;
				}
				totSquaredDistance = totSquaredDistance/nPos; // average over all filtered positions 
			}
			errorVector[i] = totSquaredDistance;
			if(debug) extraInfo[i] = extraInfo[i] + "Average squared distance: "+ totSquaredDistance + "\n";				
			i++;
			p1 = p2;
		}
	}

	public float meanDistanceToSegmentError() {
		float sumErrors = 0;
		for (int i = 0; i < errorVector.length; i++) {
			sumErrors += errorVector[i];
		}
		return sumErrors;
	}
	
	
	public float meanSquaredLocError() {
		float meanError = 0;
		float sumSquareErrors = 0;
		for (int i = 0; i < errorVector.length; i++) {
			sumSquareErrors += errorVector[i] * errorVector[i];
		}
		meanError = (float) Math.sqrt(sumSquareErrors);
		return meanError;
	}
	
	public float meanSquaredLocErrorWithThreshold() {
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
		if(extraInfo != null) {
			String extra = "Extra info: ";
			for (int i = 0; i < extraInfo.length; i++) {
				extra = extra + extraInfo[i] + "\n";
			}
			s = s + extra + "\n";						
		}
		//s = s + "meanSquaredLocError = " + meanSquaredLocError() + "\n";		
		//s = s + "meanSquaredLocErrorWithThreshold = " + meanSquaredLocErrorWithThreshold() + "\n";		
		s = s + "meanDistanceToSegmentError = " + meanDistanceToSegmentError() + "\n";		
		s = s + "headingError = " + headingError() + "\n";		
		return s;
	}
}
