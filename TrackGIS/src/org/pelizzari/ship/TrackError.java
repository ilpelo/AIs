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
	
	//final static float PARAM_MAX_DISTANCE_ERROR_THRESHOLD = 10E-2f; // position is not too far if < threshold
	final static float NEIGHBORHOOD_SEGMENT_END = 1; // number of positions to be checked to control segment length	
	final static float NEIGHBORHOOD_SEGMENT_FRAME = 0.1f; // frame of the box around the segment in percentage of the segment length
														  	
	final static float DISTANCE_ERROR_AMPLIFIER = 100f; // multiply distance of positions that are too far 
	final static float MAX_CHANGE_OF_HEADING_ANGLE = 40f; // max angle for a change of heading not to be over the limit
	final static float HEADING_ERROR_AMPLIFIER = 10E4f; // multiply by the number of  changes of heading over the limit 
	final static float BAD_TRACK_SEGMENT_FITNESS = 10E3f; // artificially high distance for segment that does not follow the target path
	final static float BAD_TRACK_FITNESS = 10E4f; // artificially high distance for segment that does not follow the target path
	final static float MIN_POSITION_COVERAGE_THRESHOLD = 0.99f; // percentage of target positions that are covered by the track
	
	// the track to which the error refers to 
	ShipTrack baseTrack;
	// the destination of our journey (!) 
	ShipPosition destinationPos;
	// a measure of the error of each position of the baseTrack
	float[] segmentErrorVector;
	// target positions coverage (%)
	float targetPositionCoverage = 0;
	// number of segments with no coverage of target positions
	float noCoverageSegmentCounter = 0;
	// average squared distance of the target positions to the segments for the whole baseTrack
	float avgSquaredDistanceAllSegments = 0;
	// extra info for debug purposes (1 string for each position)
	String[] extraInfo;
	boolean debug = false;
			
	public TrackError(ShipTrack track, boolean debug) {
		this.debug = debug;
		baseTrack = track;
		int size = baseTrack.getPosList().size();
		segmentErrorVector = new float[size];
		if(debug) extraInfo = new String[size];
	}
	

	// basic point to point distance error
//	public void computePointToPointErrorVector(ShipTrack targetTrack) {
//			if(errorVector.length != targetTrack.getPosList().size()) {
//				System.err.println("computeError: track size does not match: "+
//						errorVector.length + "<>" + targetTrack.getPosList().size());
//			}
//			for (int i = 0; i < errorVector.length; i++) {
//				Point p1 = baseTrack.getPosList().get(i).point;
//				Point p2 = targetTrack.getPosList().get(i).point;
//				float distance = p1.distanceInMiles(p2);
//				errorVector[i] = distance;
//			}
//	}

	public static Box makeSegmentBox(Point p1, Point p2) {
		Point segmentCenterPoint = new Point((p1.lat + p2.lat)/2, (p1.lon + p2.lon)/2);
		float segmentLength = p1.distance(p2);
		Box box = new Box(segmentCenterPoint, segmentLength*(1+NEIGHBORHOOD_SEGMENT_FRAME)/2);
		return box;
	}
	
	public void computeSegmentErrorVector(ShipTrack targetTrack) throws Exception {
		// store destination
		destinationPos = targetTrack.getLastPosition();
		//
		ShipPosition p1 = null;
		ShipPosition p2 = null;
		int i = 0;
		int totCoveredPositions = 0;
		float totSquaredDistance = 0;
		for (ShipPosition pos : baseTrack.posList) {
			if (p1 == null) {
				p1 = pos;
				segmentErrorVector[0] = 0; // first position is fixed and always ok
				if(debug) extraInfo[0] = "";
				i = 1;
				continue;
			}
			p2 = pos;
			//
			if(debug) extraInfo[i] = "###### Segment "+i+": "+p1+"-"+p2+"\n";
			// ts of the 2nd position of the GA track is based on the avg. speed of the target track
			float targetAvgSpeed = targetTrack.computeAverageSpeed();
			if(targetAvgSpeed == 0) {
				System.err.println("computeErrorVector: average speed is zero");
				throw new Exception("Zero Average Speed");
			}			
			float segmentLengthInMiles = p1.point.distanceInMiles(p2.point);
			int duration = (int) (segmentLengthInMiles / targetAvgSpeed * 3600); // in seconds
			p2.setTs(p1.ts.getTs()+duration);
			// bounding box of the 2 positions of the GA track (including a frame)
			Box box = makeSegmentBox(p1.point, p2.point);
			// time difference of the 2 positions of the GA track
			TimeInterval interval = new TimeInterval(p1.ts, p2.ts);
			if(debug) extraInfo[i] = extraInfo[i] + 
					//"Center = "+segmentCenterPoint+"; length = "+segmentLength+"\n"+
					box+"\n";
			// filter position of target track and compute total squared distance to segment
			List<ShipPosition> targetPosList = targetTrack.getPosListInBoxAndInterval(box, interval);
			//List<ShipPosition> targetPosList = targetTrack.getPosListInInterval(interval);
			int nPos = targetPosList.size();
			// increase total covered positions
			totCoveredPositions += nPos;
			float totSquaredDistanceBySegment = 0;
			float totSquaredDistanceToSegmentEnd = 0;
			if(nPos == 0) { // segment does not cover any position: set bad fitness
			   noCoverageSegmentCounter++;
			} else {
				int j = 0;
				for (ShipPosition targetPos : targetPosList) {
					// distance of target positions from the segment
					float squaredPointDistance = targetPos.point.approxSquaredDistanceToSegment(p1.point, p2.point);
					if(debug) extraInfo[i] = extraInfo[i] + "Covered Pos "+targetPos + ": "+ squaredPointDistance + "\n";
//					if(squaredPointDistance > PARAM_MAX_DISTANCE_ERROR_THRESHOLD) {
//						totClosePositions++;
//					}
					// sum up distances to segment
					totSquaredDistanceBySegment += squaredPointDistance;
					// sum up distances for the whole track
					totSquaredDistance += squaredPointDistance;
					// add squared distance of last target positions (neighborhood) from p2
					// to avoid the end segment being too far from the target track
					if(j >= nPos - NEIGHBORHOOD_SEGMENT_END) {
						float squaredDistanceToSegmentEnd = targetPos.point.squaredDistance(p2.point);
						totSquaredDistanceToSegmentEnd += squaredDistanceToSegmentEnd;
					}
					j++;
				}
				totSquaredDistanceBySegment = totSquaredDistanceBySegment/nPos; // average over all positions covered by the segment
			}
			// sum up the average distance to the segment and the distance to the segment end
			segmentErrorVector[i] = totSquaredDistanceBySegment + totSquaredDistanceToSegmentEnd;
			if(debug) {
				extraInfo[i] = extraInfo[i] + "Avg squared distance: "+ totSquaredDistanceBySegment + 
						"; to segment end: "+ totSquaredDistanceToSegmentEnd + "\n";
			}
			// update index
			i++;
			// start of next segment is the end of this one
			p1 = p2;
		}
		// set track fitness based on average distance from target points
		avgSquaredDistanceAllSegments = totSquaredDistance/targetTrack.posList.size();
		// bad track fitness if the target positions covered by the track are less than threshold (e.g. 90%)
		targetPositionCoverage = (float) totCoveredPositions/targetTrack.posList.size();
	}

	public float totalSegmentError() {
		float sumErrors = 0;
		for (int i = 0; i < segmentErrorVector.length; i++) {
			sumErrors += segmentErrorVector[i];
		}
		return sumErrors;
	}
	
	
//	public float meanSquaredLocError() {
//		float meanError = 0;
//		float sumSquareErrors = 0;
//		for (int i = 0; i < errorVector.length; i++) {
//			sumSquareErrors += errorVector[i] * errorVector[i];
//		}
//		meanError = (float) Math.sqrt(sumSquareErrors);
//		return meanError;
//	}
	
//	public float meanSquaredLocErrorWithThreshold() {
//		float meanError = 0;
//		float sumSquareErrors = 0;
//		for (int i = 0; i < errorVector.length; i++) {
//			float distanceToTarget = errorVector[i];
//			if (distanceToTarget > PARAM_MAX_DISTANCE_ERROR_THRESHOLD) {
//				distanceToTarget = distanceToTarget*DISTANCE_ERROR_AMPLIFIER;
//			}
//			sumSquareErrors += distanceToTarget * distanceToTarget;
//		}
//		meanError = (float) Math.sqrt(sumSquareErrors);
//		return meanError;
//	}
	

	public float headingError() {
		int cohOnceOverLimitCount = baseTrack.countChangeOfHeadingOverLimit(MAX_CHANGE_OF_HEADING_ANGLE);
		int cohTwiceOverLimitCount = baseTrack.countChangeOfHeadingOverLimit(2*MAX_CHANGE_OF_HEADING_ANGLE);
		return (cohOnceOverLimitCount * HEADING_ERROR_AMPLIFIER +
			   cohTwiceOverLimitCount * HEADING_ERROR_AMPLIFIER * 100) / (float) getNumberOfTrackSegments();
	}
	
	public float destinationError() {
		float distanceToDestination = baseTrack.getLastPosition().point.distanceInMiles(destinationPos.point);
		return distanceToDestination;
	}
	
	public float getNoCoverageError() {
		float noCoverageError = 0f;
		if(targetPositionCoverage < MIN_POSITION_COVERAGE_THRESHOLD) {
			noCoverageError = BAD_TRACK_FITNESS;
		}
		noCoverageError += noCoverageSegmentCounter * BAD_TRACK_SEGMENT_FITNESS;
		return noCoverageError;
	}

	public int getNumberOfTrackSegments() {
		return baseTrack.posList.size();
	}
	
	public float getAvgSquaredDistanceAllSegments() {
		return avgSquaredDistanceAllSegments;
	}


	public String toString() {
		String s = "Track error: ";
		for (int i = 0; i < segmentErrorVector.length; i++) {
			s = s + segmentErrorVector[i] + " ";
		}
		s = s + "\n";
		s = s + "Target track positions coverage = " + targetPositionCoverage*100 + " %\n";		
		if(extraInfo != null) {
			String extra = "Extra info: ";
			for (int i = 0; i < extraInfo.length; i++) {
				extra = extra + extraInfo[i] + "\n";
			}
			s = s + extra + "\n";						
		}
		//s = s + "meanSquaredLocError = " + meanSquaredLocError() + "\n";		
		//s = s + "meanSquaredLocErrorWithThreshold = " + meanSquaredLocErrorWithThreshold() + "\n";		
		s = s + "totalSegmentError (sum of squared distances) = " + totalSegmentError() + "\n";		
		s = s + "avgSquaredDistanceAllSegments = " + getAvgSquaredDistanceAllSegments() + "\n";		
		s = s + "headingError = " + headingError() + "\n";		
		s = s + "destinationError = " + destinationError() + "\n";		
		s = s + "noCoverageError = " + getNoCoverageError() + "\n";		
		return s;
	}
}
