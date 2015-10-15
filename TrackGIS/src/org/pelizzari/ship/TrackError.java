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
														  	
	//final static float DISTANCE_ERROR_AMPLIFIER = 100f; // multiply distance of positions that are too far 
	//final static float MAX_CHANGE_OF_HEADING_ANGLE = 40f; // max angle for a change of heading not to be over the limit
//	final static float BAD_TRACK_SEGMENT_FITNESS = 10E3f; // artificially high distance for segment that does not follow the target path
//	final static float BAD_TRACK_FITNESS = 10E4f; // artificially high distance for segment that does not follow the target path
//	final static float MIN_POSITION_COVERAGE_THRESHOLD = 0.99f; // percentage of target positions that are covered by the track
	
	final static float HEADING_ERROR_FACTOR = 1f; // multiply by the number of  changes of heading over the limit 
	final static float TOTAL_COVERAGE_ERROR_FACTOR = 10f; // multiply by the number of  changes of heading over the limit 
	
	// the track to which the error refers to 
	ShipTrack baseTrack;
	// the target positions used to compute the fitness 
	ShipPositionList trainingPosList;
	// the destination of our journey (!) 
	ShipPosition destinationPos;
	// a measure of the error of each position of the baseTrack
	//float[] segmentErrorVector;
	// target positions coverage (0.0-1.0)
	float targetPositionCoverage = 0;
	// target positions coverage (0.0-1.0) averaged over number of segments
	float avgTargetPositionCoverageBySegment = 0;
	// average squared distance to the target points, averaged over number of segments
	float avgAvgSquaredDistanceToTargetPositionsBySegment = 0;
	// average distances of the segment ends to the target points, averaged over number of segments
	float avgAvgSegmentEndsDistanceToTargetPositionsBySegment = 0;
	// variance of the squared distance to the target points, averaged over number of segments
	float avgVarianceOfDistanceToTargetPositionsBySegment = 0;
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
		//int size = baseTrack.getPosList().size();
		//segmentErrorVector = new float[size];
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
		// keep the timestamps set during reconstruction and based on the duration of the real voyage
		baseTrack.computeTrackSegments();
		
		for (ShipTrackSegment seg : baseTrack.getSegList()) {			
			// bounding box of the 2 positions of the GA track (including a frame)
			//Box box = makeSegmentBox(seg);
			
			// filter position of target track and compute total squared distance to segment
			//List<ShipPosition> targetPosList = targetTrack.getPosListInIntervalAndBox(interval, box);
			
			// use this if you want to get only position that correspond to the temporal interval of the segment
			List<ShipPosition> targetPosList = trainingPosList.getPosListInInterval(seg.getTimeInterval());

			// use this if you want to get only position that correspond to the temporal interval of the segment
			// and are located on the perpendicular stripe
			//List<ShipPosition> targetPosList = trainingPosList.getPosListInIntervalAndOnStripe(seg);
	
			// use this if you want to get only positions within a "corridor" of width NEIGHBORHOOD_SEGMENT_SQUARED_DISTANCE  
//			List<ShipPosition> targetPosList = 
//					trainingPosList.getPosListInIntervalAndBoxAndCloseToSegment(
//							seg.getTimeInterval(),
//							box,
//							seg.p1.point, seg.p2.point, 
//							NEIGHBORHOOD_SEGMENT_SQUARED_DISTANCE);
			
			seg.setTargetPosList(targetPosList);
			seg.computeDistancesToTargetPositions();
			seg.computeStatsForFitness();
		}
	}
	
	public void computeStatsForFitness() {
		noCoverageSegmentCounter = 0;
		int coveredTargetPositionCount = 0;
		float sumSegVariance = 0, 
			  sumSegAvgSquaredPerpendicularDistance = 0,
			  sumSegAvgSegmentEndsDistance = 0;
		for (ShipTrackSegment seg : baseTrack.getSegList()) {
			int segTargetPosCounter = seg.getNumberOfCoveredTargetPositions();
			coveredTargetPositionCount += segTargetPosCounter;
			if(segTargetPosCounter <= 1) { // segment should cover at least 2 positions
				noCoverageSegmentCounter++;
			}
			sumSegAvgSquaredPerpendicularDistance += seg.getAvgSquaredPerpendicularDistanceToTargetPositions();
			sumSegAvgSegmentEndsDistance += seg.getAvgSegmentEndsDistanceToTargetPositions();
			sumSegVariance += seg.getVarSquaredDistanceToTargetPositions();			
		}
		targetPositionCoverage = 
				(float) coveredTargetPositionCount / trainingPosList.getPosList().size();
		avgTargetPositionCoverageBySegment = 
				targetPositionCoverage / baseTrack.getSegList().size();
		avgAvgSquaredDistanceToTargetPositionsBySegment = 
				sumSegAvgSquaredPerpendicularDistance / baseTrack.getSegList().size();
		avgAvgSegmentEndsDistanceToTargetPositionsBySegment =
				sumSegAvgSegmentEndsDistance / baseTrack.getSegList().size();
		avgVarianceOfDistanceToTargetPositionsBySegment = 
				sumSegVariance / baseTrack.getSegList().size();
	}
	
//	/**
//	 * Sum up all segment errors (no average).
//	 * @return
//	 */
//	public float totalSegmentError() {
//		float sumErrors = 0;
//		for (int i = 0; i < segmentErrorVector.length; i++) {
//			sumErrors += segmentErrorVector[i];
//		}
//		return sumErrors;
//	}
//
//	/**
//	 * Average segment error (total seg. err. / n. of segments).
//	 * @return
//	 */
//	public float avgTotalSegmentError() {
//		return totalSegmentError() / segmentErrorVector.length;
//	}
	
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
	
	/**
	 * This should return a high value if the average change of heading is big
	 * @return
	 */
	public float getAvgChangeOfHeading() {
		return baseTrack.sumChangeOfHeading() / (getNumberOfTrackSegments()-1);
		
		//return HEADING_ERROR_FACTOR * baseTrack.sumChangeOfHeading() / (getNumberOfTrackSegments()-1);
		//		int cohOnceOverLimitCount = baseTrack.countChangeOfHeadingOverLimit(MAX_CHANGE_OF_HEADING_ANGLE);
//		int cohTwiceOverLimitCount = baseTrack.countChangeOfHeadingOverLimit(2*MAX_CHANGE_OF_HEADING_ANGLE);
//		return (cohOnceOverLimitCount * HEADING_ERROR_AMPLIFIER +
//			   cohTwiceOverLimitCount * HEADING_ERROR_AMPLIFIER * 100) / (float) getNumberOfTrackSegments();
	}
	
//	public float destinationError() {
//		float distanceToDestination = baseTrack.getLastPosition().point.distanceInMiles(destinationPos.point);
//		return distanceToDestination;
//	}
	
	/**
	 * This should return a high value if the coverage of the target positions is bad
	 * @return
	 */
	public float getSegmentCoverageError() {
		return noCoverageSegmentCounter*10; // artificially high
	}

	/**
	 * This should return a high value if the overall coverage of the target positions is bad
	 * @return
	 */
	public float getTotalCoverageError() {
//		float noCoverageError = 0f;
//
////		if(targetPositionCoverage < MIN_POSITION_COVERAGE_THRESHOLD) {
////			noCoverageError = BAD_TRACK_FITNESS;
////		}
//		noCoverageError += noCoverageSegmentCounter * BAD_TRACK_SEGMENT_FITNESS;
		
		// good = 0.0, bad = 1.0 * factor
		return (1f-targetPositionCoverage)*TOTAL_COVERAGE_ERROR_FACTOR; // artificially high
				
		//return 1f-avgTargetPositionCoverageBySegment;
	}
	
	
	public float getError() {
		float error =
			//trackError.destinationError() +
			//trackError.getAvgSquaredDistanceAllSegments() +
			//getSegmentCoverageError() +
			//getTotalCoverageError() +
			getVarianceError() +
			getAvgDistanceError() +
			getAvgChangeOfHeading()*HEADING_ERROR_FACTOR +
			//trackError.avgTotalSegmentError() +
			0f;
		return error;
	}

	
	
	/**
	 * This should return a high value if the average distance to the target positions for each segment is high
	 * @return
	 */
	public float getAvgDistanceError() {
		//return avgAvgSquaredDistanceToTargetPositionsBySegment;
		return avgAvgSegmentEndsDistanceToTargetPositionsBySegment;
	}	
		
	/**
	 * This should return a high value if the variance of the target position distance to each segment is high
	 * @return
	 */
	public float getVarianceError() {
		return avgVarianceOfDistanceToTargetPositionsBySegment;
	}	

	
	public int getNumberOfTrackSegments() {
		return baseTrack.posList.size();
	}
	
	public float getAvgSquaredDistanceAllSegments() {
		return avgSquaredDistanceAllSegments;
	}

	
	public float getAvgAvgSquaredDistanceToTargetPositionsBySegment() {
		return avgAvgSquaredDistanceToTargetPositionsBySegment;
	}


	public String toString() {
		String s = "Track error: \n";
		for (ShipTrackSegment segment : baseTrack.getSegList()) {
			s = s + segment + "\n";
		}
		s = s + "Target track positions coverage = " + targetPositionCoverage*100 + " %\n";		
		//s = s + "meanSquaredLocError = " + meanSquaredLocError() + "\n";		
		//s = s + "meanSquaredLocErrorWithThreshold = " + meanSquaredLocErrorWithThreshold() + "\n";		
		//s = s + "totalSegmentError (sum of squared distances) = " + totalSegmentError() + "\n";		
		s = s + "avgSquaredDistanceAllSegments = " + getAvgSquaredDistanceAllSegments() + "\n";		
		s = s + "avgChangeOfHeading = " + getAvgChangeOfHeading() + "\n";		
		//s = s + "destinationError = " + destinationError() + "\n";		
		s = s + "segmentCoverageError = " + getSegmentCoverageError() + "\n";	
		s = s + "totalCoverageError = " + getTotalCoverageError() + "\n";	
		//s = s + "avgTotalSegmentError = " + avgTotalSegmentError() + "\n"; 
		s = s + "avgSegmentSquaredDistanceError = " + getAvgDistanceError() + "\n"; 
		s = s + "varianceError = " + getVarianceError() + "\n"; 
		s = s + "error for fitness = " + getError() + "\n"; 
		return s;
	}
}
