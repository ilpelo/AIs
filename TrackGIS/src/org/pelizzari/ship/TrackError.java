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
	
	final static float NEIGHBORHOOD_SEGMENT_SQUARED_DISTANCE = 0.001f; // position is not too far if < threshold
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
	// the target positions used to compute the fitness 
	ShipPositionList trainingPosList;
	// the destination of our journey (!) 
	ShipPosition destinationPos;
	// a measure of the error of each position of the baseTrack
	float[] segmentErrorVector;
	// target positions coverage (0.0-1.0)
	float targetPositionCoverage = 0;
	// target positions coverage (0.0-1.0) averaged over number of segments
	float avgTargetPositionCoverageBySegment = 0;
	// number of segments with no coverage of target positions
	float noCoverageSegmentCounter = 0;
	// average squared distance of the target positions to the segments for the whole baseTrack
	float avgSquaredDistanceAllSegments = 0;
	// extra info for debug purposes (1 string for each position)
	//String[] extraInfo;
	boolean debug = false;
			
	public TrackError(ShipTrack track, boolean debug) {
		this.debug = debug;
		baseTrack = track;
		int size = baseTrack.getPosList().size();
		segmentErrorVector = new float[size];
		//if(debug) extraInfo = new String[size];
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

	public static Box makeSegmentBox(ShipTrackSegment segment) {
		Box box = new Box(segment.center, segment.length*(1+NEIGHBORHOOD_SEGMENT_FRAME)/2);
		return box;
	}
	
	/**
	 * The main method to compute the error (and consequently fitness) of the track compared to the target track.
	 * @param targetTrack
	 * @throws Exception
	 */
	public void computeSegmentErrorVector(ShipPositionList trainingPosList) throws Exception {
		// store destination
		this.trainingPosList = trainingPosList;
//		float targetAvgSpeed = targetTrack.computeAverageSpeed();
//		if(targetAvgSpeed == 0) {
//			System.err.println("computeErrorVector: average speed is zero");
//			throw new Exception("Zero Average Speed");
//		}			
		// use the reference speed to set the timestamps of each position of the base track
		baseTrack.computeTrackSegmentsAndNormalizeTime(trainingPosList.getFirstPosition().getTs(),
													   ShipTrack.REFERENCE_SPEED_IN_KNOTS);
		//
		//int i = 0;
//		int totCoveredPositions = 0;
//		float totSquaredDistance = 0;
		for (ShipTrackSegment seg : baseTrack.getSegList()) {			
			// bounding box of the 2 positions of the GA track (including a frame)
			Box box = makeSegmentBox(seg);
			// filter position of target track and compute total squared distance to segment
			//List<ShipPosition> targetPosList = targetTrack.getPosListInIntervalAndBox(interval, box);
			//List<ShipPosition> targetPosList = targetTrack.getPosListInInterval(interval);
			List<ShipPosition> targetPosList = 
					trainingPosList.getPosListInIntervalAndBoxAndCloseToSegment(
							seg.getTimeInterval(),
							box,
							seg.p1.point, seg.p2.point, 
							NEIGHBORHOOD_SEGMENT_SQUARED_DISTANCE);
			seg.setTargetPosList(targetPosList);
			seg.computeDistanceToTargetPositions();
			seg.computeStatsForFitness();
			// increase total covered positions
			//totCoveredPositions += nCoveredPos;
//			float totSquaredDistanceBySegment = 0;
//			float totSquaredDistanceToSegmentEnd = 0;
//			if(nPos == 0) { // segment does not cover any position
//			   noCoverageSegmentCounter++;
//			} else {
//				int j = 0;
//				for (ShipPosition targetPos : targetPosList) {
//					// distance of target positions from the segment
//					float squaredPointDistance = targetPos.point.approxSquaredDistanceToSegment(p1.point, p2.point);
//					if(debug) extraInfo[i] = extraInfo[i] + "Covered Pos "+targetPos + ": "+ squaredPointDistance + "\n";
////					if(squaredPointDistance > PARAM_MAX_DISTANCE_ERROR_THRESHOLD) {
////						totClosePositions++;
////					}
//					// sum up distances to segment
//					totSquaredDistanceBySegment += squaredPointDistance;
//					// sum up distances for the whole track
//					totSquaredDistance += squaredPointDistance;
//					// add squared distance of last target positions (neighborhood) from p2
//					// to avoid the end segment being too far from the target track
//					if(j >= nPos - NEIGHBORHOOD_SEGMENT_END) {
//						float squaredDistanceToSegmentEnd = targetPos.point.squaredDistance(p2.point);
//						totSquaredDistanceToSegmentEnd += squaredDistanceToSegmentEnd;
//					}
//					j++;
//				}
//				totSquaredDistanceBySegment = totSquaredDistanceBySegment/nPos; // average over all positions covered by the segment
//			}
//			// sum up the average distance to the segment and the distance to the segment end
//			segmentErrorVector[i] = totSquaredDistanceBySegment;
//				//+ totSquaredDistanceToSegmentEnd;
//			if(debug) {
//				extraInfo[i] = "###### "+i+": "+seg;
//				extraInfo[i] = extraInfo[i] + 
//						//"Center = "+segmentCenterPoint+"; length = "+segmentLength+"\n"+
//						box+"\n";
//			}
			// update index
			//i++;
		}
		// set track fitness based on average distance from target points
		//avgSquaredDistanceAllSegments = totSquaredDistance/targetTrack.posList.size();
		// bad track fitness if the target positions covered by the track are less than threshold (e.g. 90%)
		//targetPositionCoverage = (float) totCoveredPositions/targetTrack.posList.size();
	}

	
	public void computeStatsForFitness() {
		noCoverageSegmentCounter = 0;
		int coveredTargetPositionCount = 0;
		for (ShipTrackSegment seg : baseTrack.getSegList()) {
			int segTargetPosCounter = seg.getNumberOfCoveredTargetPositions();
			coveredTargetPositionCount += segTargetPosCounter;
			if(segTargetPosCounter == 0) {
				noCoverageSegmentCounter++;
			}
		}
		targetPositionCoverage = (float) coveredTargetPositionCount / trainingPosList.getPosList().size();
		avgTargetPositionCoverageBySegment = targetPositionCoverage / baseTrack.getSegList().size();
	}
	
	/**
	 * Sum up all segment errors (no average).
	 * @return
	 */
	public float totalSegmentError() {
		float sumErrors = 0;
		for (int i = 0; i < segmentErrorVector.length; i++) {
			sumErrors += segmentErrorVector[i];
		}
		return sumErrors;
	}

	/**
	 * Average segment error (total seg. err. / n. of segments).
	 * @return
	 */
	public float avgTotalSegmentError() {
		return totalSegmentError() / segmentErrorVector.length;
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
	
//	public float destinationError() {
//		float distanceToDestination = baseTrack.getLastPosition().point.distanceInMiles(destinationPos.point);
//		return distanceToDestination;
//	}
	
	/**
	 * This should return a high value if the coverage of the target positions is bad
	 * @return
	 */
	public float getCoverageError() {
//		float noCoverageError = 0f;
//
////		if(targetPositionCoverage < MIN_POSITION_COVERAGE_THRESHOLD) {
////			noCoverageError = BAD_TRACK_FITNESS;
////		}
//		noCoverageError += noCoverageSegmentCounter * BAD_TRACK_SEGMENT_FITNESS;
		//return noCoverageError;
		return 1f-targetPositionCoverage; //+noCoverageError;
		//return 1f-avgTargetPositionCoverageBySegment;
	}

	public int getNumberOfTrackSegments() {
		return baseTrack.posList.size();
	}
	
	public float getAvgSquaredDistanceAllSegments() {
		return avgSquaredDistanceAllSegments;
	}


	public String toString() {
		String s = "Track error: \n";
		for (ShipTrackSegment segment : baseTrack.getSegList()) {
			s = s + segment + "\n";
		}
		s = s + "Target track positions coverage = " + targetPositionCoverage*100 + " %\n";		
		//s = s + "meanSquaredLocError = " + meanSquaredLocError() + "\n";		
		//s = s + "meanSquaredLocErrorWithThreshold = " + meanSquaredLocErrorWithThreshold() + "\n";		
		s = s + "totalSegmentError (sum of squared distances) = " + totalSegmentError() + "\n";		
		s = s + "avgSquaredDistanceAllSegments = " + getAvgSquaredDistanceAllSegments() + "\n";		
		s = s + "headingError = " + headingError() + "\n";		
		//s = s + "destinationError = " + destinationError() + "\n";		
		s = s + "noCoverageError = " + getCoverageError() + "\n";	
		s = s + "avgTotalSegmentError = " + avgTotalSegmentError() + "\n"; 
		return s;
	}
}
