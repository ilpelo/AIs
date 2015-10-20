package org.pelizzari.ship;

import java.util.ArrayList;
import java.util.List;

import org.pelizzari.gis.Point;
import org.pelizzari.time.TimeInterval;

public class ShipTrackSegment {
	ShipPosition p1;
	ShipPosition p2;

	Point center;
	float length = 0; // length of the segment (in degrees)
	float lengthInMiles = 0; // length of the segment (in miles)
	int durationInSeconds = 0; // difference of the timestamp of the start and end positions
	int expectedCoveredPositions = 0; // the expected number of positions this segment should cover
	
	List<ShipPosition> spaceTargetPosList = new ArrayList<ShipPosition>();
	List<ShipPosition> targetPosList = new ArrayList<ShipPosition>();
	
	float[] squaredDistanceOfTargetPositionArray;
	float[] segmentEndsDistanceToTargetPositionArray;
	int numberOfCoveredTargetPositions = 0; // number of target positions covered to the segment 
	float avgSquaredDistanceToTargetPositions = 0f;
	float minSquaredDistanceToTargetPositions = 0f;
	float maxSquaredDistanceToTargetPositions = 0f;
	float varSquaredDistanceToTargetPositions = 0f;
	float squaredDistanceOfSegmentEndToLastTargetPosition = 0f;
	float avgSegmentEndsDistanceToTargetPositions = 0f;
	float coverageOfExpectedPositions = 0f; // % of expected positions covered by this segment
	int differenceFromExpectedPositions = 0; // (abs) difference of covered and expected positions 

	
	public ShipTrackSegment(ShipPosition p1, ShipPosition p2) {
		this.p1 = p1;
		this.p2 = p2;
		center = new Point((p1.point.lat + p2.point.lat)/2, (p1.point.lon + p2.point.lon)/2);
		length = p1.point.distance(p2.point);
		lengthInMiles = p1.point.distanceInMiles(p2.point);
		durationInSeconds = (int) ((p2.getTs().getTsMillisec() - p1.getTs().getTsMillisec()) / 1000); // in seconds
	}
	
	
	/**
	 * Create a segment with the given position.
	 * WARNING: overwrite timestamp of p2 based on the given speed.
	 * @param p1
	 * @param p2
	 * @param speedInKnots
	 */
	public ShipTrackSegment(ShipPosition p1, ShipPosition p2, float speedInKnots) {
//		this.p1 = p1;
//		this.p2 = p2;
//		center = new Point((p1.point.lat + p2.point.lat)/2, (p1.point.lon + p2.point.lon)/2);
//		length = p1.point.distance(p2.point);
//		lengthInMiles = p1.point.distanceInMiles(p2.point);
		this(p1, p2);
		durationInSeconds = (int) (lengthInMiles / speedInKnots * 3600f); // in seconds
		p2.setTs(p1.ts.getTsMillisec()+durationInSeconds*1000);
	}
	
	/*
	 * Compute the distances to the target points (perpendicular and to segment ends) 
	 * and store the values in arrays.
	 */
	public void computeDistancesToTargetPositions() {
		if(numberOfCoveredTargetPositions == 0) {
			return;
		}
		squaredDistanceOfTargetPositionArray = new float[numberOfCoveredTargetPositions];
		segmentEndsDistanceToTargetPositionArray = new float[numberOfCoveredTargetPositions];
		int i = 0;
		ShipPosition lastTargetPos = null;
		for (ShipPosition targetPos : targetPosList) {
			// distance of target positions from the segment
			squaredDistanceOfTargetPositionArray[i] = 
					targetPos.point.approxSquaredDistanceToSegment(p1.point, p2.point);
			// sum of the distances of target positions to the segment ends
			segmentEndsDistanceToTargetPositionArray[i] =					
					targetPos.point.distance(p1.point) + targetPos.point.distance(p2.point);
			i++;
		}
		lastTargetPos = targetPosList.get(numberOfCoveredTargetPositions-1);
		squaredDistanceOfSegmentEndToLastTargetPosition = p2.point.distance(lastTargetPos.point);
	}
	
	/*
	 * Compute some stats that can be used for fitness evaluation.
	 * Beware: distance to target positions MUST be called before
	 */
	public void computeStatsForFitness() {
		// coverage of positions compared to the expected number
		if(expectedCoveredPositions != 0) {
			coverageOfExpectedPositions = (float) numberOfCoveredTargetPositions / expectedCoveredPositions;
		} else {
			coverageOfExpectedPositions = 0;
		}		
		differenceFromExpectedPositions = Math.abs(numberOfCoveredTargetPositions - expectedCoveredPositions);
		if(numberOfCoveredTargetPositions == 0) {
			return;
		}
		// mean, min, and max distance
		minSquaredDistanceToTargetPositions = Float.MAX_VALUE;
		maxSquaredDistanceToTargetPositions = 0;
		float sumSquaredDistance = 0, sumSegmentEndsDistance = 0;
		for (int i = 0; i < squaredDistanceOfTargetPositionArray.length; i++) {
			float squaredDistance = squaredDistanceOfTargetPositionArray[i];
			if(squaredDistance < minSquaredDistanceToTargetPositions) {
				minSquaredDistanceToTargetPositions = squaredDistance;
			}
			if(squaredDistance > maxSquaredDistanceToTargetPositions) {
				maxSquaredDistanceToTargetPositions = squaredDistance;
			}
			// sum up perpendicular distances
			sumSquaredDistance += squaredDistance;
			// sum up distances to segment ends
			sumSegmentEndsDistance += segmentEndsDistanceToTargetPositionArray[i];
		}
		// average of the perpendicular distance
		avgSquaredDistanceToTargetPositions = sumSquaredDistance/numberOfCoveredTargetPositions;
		// average of the distances to the segment ends
		avgSegmentEndsDistanceToTargetPositions = sumSegmentEndsDistance/numberOfCoveredTargetPositions;
		// variance
		float sumVariance = 0;
		for (float sqrDist : squaredDistanceOfTargetPositionArray) {
			sumVariance += (avgSquaredDistanceToTargetPositions - sqrDist) * 
					(avgSquaredDistanceToTargetPositions - sqrDist); 
		}
		varSquaredDistanceToTargetPositions = sumVariance/numberOfCoveredTargetPositions;
	}

	/**
	 * Return if a point is located within the circle 
	 * with center p1 and radius equals to the segment.
	 * @param p
	 * @return
	 */
	public boolean isWithinSegmentCircle(Point p) {
		boolean isWithin = false;
		float distanceToCenter = p1.point.distance(p);
		isWithin = distanceToCenter <= length;
		return isWithin;
	}	
	
	/**
	 * Return if a point is located within the stripe perpendicular to this segment or not.
	 * @param p
	 * @return
	 */
	public boolean isWithinPerpendicularStripe(Point p) {
		boolean isWithin = false;
		Point intersectionPoint = p.computeIntersectionOfPerpendicular(p1.point, p2.point);
		isWithin = intersectionPoint.isOnSegment(p1.point, p2.point);
		return isWithin;
	}
	
	/**
	 * Return if a point is located within the corridor of given width next to this segment or not.
	 */
	public boolean isWithinCorridor(Point p, float width) {
		boolean isWithin = false;
		float P2_P1_P_angle = angle(p1.point, p2.point, p);
		float P1_P_distance = p1.point.distance(p);
		isWithin = (P1_P_distance*Math.cos(P2_P1_P_angle) < length) ||
				   (P1_P_distance*Math.sin(P2_P1_P_angle) < width/2);		
		return isWithin;
	}
	
	/**
	 * Return if a point is located within the ellipse having the segment ends as the two foci
	 * and major axis equals to the segment length multiplied by the given factor (> 1)
	 */
	public boolean isWithinEllipse(Point p, float majorAxisFactor) {
		boolean isWithin = false;
		float majorAxis = length*majorAxisFactor;
		float sumOfDistancesToSegmentEnds = p.distance(p1.point) + p.distance(p2.point);
		isWithin = sumOfDistancesToSegmentEnds < majorAxis;		
		return isWithin;
	}
		
	
	public List<ShipPosition> getTargetPosList() {
		return targetPosList;
	}

	/**
	 * Add all target positions covered by this segment at once
	 * @param targetPosList
	 */
	public void setTargetPosList(List<ShipPosition> targetPosList) {
		this.targetPosList = targetPosList;
		numberOfCoveredTargetPositions = targetPosList.size();
	}

	/**
	 * Add one target position covered by this segment
	 * @param targetPosList
	 */
	public void addTargetPos(ShipPosition targetPos) {
		targetPosList.add(targetPos);
		numberOfCoveredTargetPositions = targetPosList.size();
	}
	
	public ShipPosition getP1() {
		return p1;
	}

	public ShipPosition getP2() {
		return p2;
	}
	
	public Point getCenter() {
		return center;
	}

	public float getLength() {
		return length;
	}

	public TimeInterval getTimeInterval() throws Exception {
		return new TimeInterval(p1.ts, p2.ts);
	}

	public int getNumberOfCoveredTargetPositions() {
		return numberOfCoveredTargetPositions;
	}

	public float getAvgSquaredPerpendicularDistanceToTargetPositions() {
		return avgSquaredDistanceToTargetPositions;
	}

	public float getAvgSegmentEndsDistanceToTargetPositions() {
		return avgSegmentEndsDistanceToTargetPositions;
	}	

	public float getMinSquaredDistanceToTargetPositions() {
		return minSquaredDistanceToTargetPositions;
	}

//	public void setMinSquaredDistanceToTargetPositions(
//			float minSquaredDistanceToTargetPositions) {
//		this.minSquaredDistanceToTargetPositions = minSquaredDistanceToTargetPositions;
//	}

	public float getMaxSquaredDistanceToTargetPositions() {
		return maxSquaredDistanceToTargetPositions;
	}

//	public void setMaxSquaredDistanceToTargetPositions(
//			float maxSquaredDistanceToTargetPositions) {
//		this.maxSquaredDistanceToTargetPositions = maxSquaredDistanceToTargetPositions;
//	}
	
	public float getVarSquaredDistanceToTargetPositions() {
		return varSquaredDistanceToTargetPositions;
	}
		
	public float getCoverageOfExpectedPositions() {
		return coverageOfExpectedPositions;
	}

		
	public int getDifferenceFromExpectedPositions() {
		return differenceFromExpectedPositions;
	}


	public void setExpectedCoveredPositions(int expectedCoveredPositions) {
		this.expectedCoveredPositions = expectedCoveredPositions;
	}


	public String toString() {
		String s = "Segment: ";
		s = s + p1 + " --- " + p2 +", l = "+ length + " deg, d = " + durationInSeconds + " s" + "\n";
		if(targetPosList != null && targetPosList.size() > 0) {
//			int i = 0;
//			for (ShipPosition targetPos : targetPosList) {
//				s = s + "Covered Pos: " + targetPos + 
//						", d^2="+ squaredDistanceOfTargetPositionArray[i] + "\n";
//				i++;
//			}
			s = s + "Total covered positions: " + targetPosList.size() + "\n"; 
			
			ShipPosition firstCoveredPos = targetPosList.get(0);
			s = s + "First covered pos: " + firstCoveredPos + 
					", d^2="+ squaredDistanceOfTargetPositionArray[0] + "\n";
			
			ShipPosition lastCoveredPos = targetPosList.get(targetPosList.size()-1);
			s = s + "Last covered pos: " + lastCoveredPos + 
					", d^2="+ squaredDistanceOfTargetPositionArray[targetPosList.size()-1] + "\n";
			
			s = s + "Squared perpendicular distance: avg=" + avgSquaredDistanceToTargetPositions +
					", min=" + minSquaredDistanceToTargetPositions +
					", max=" + maxSquaredDistanceToTargetPositions +
					", to segment end=" + squaredDistanceOfSegmentEndToLastTargetPosition + "\n";
			s = s + "Distance to segment ends: avg=" + avgSegmentEndsDistanceToTargetPositions + "\n";
			s = s + "Coverage of expected positions=" + coverageOfExpectedPositions + 
					"(" + numberOfCoveredTargetPositions + "/" + expectedCoveredPositions + ")\n";
		} else {
			s = s + "No position covered!\n";
			s = s + "Number of expected positions=" + expectedCoveredPositions + "\n";
		}
		return s;
	}
	
	public static float dot(Point p1, Point p2) {
		return p1.lon * p2.lon + p1.lat * p2.lat;
	}
	
	public static float angle(Point origin, Point p1, Point p2) {
		// move the origin to (lat=0,lon=0)
		Point vp1 = new Point(p1.lat-origin.lat, p1.lon-origin.lon);
		Point vp2 = new Point(p2.lat-origin.lat, p2.lon-origin.lon);
		
		float dotProd = dot(vp1, vp2);
		float magVp1 = (float)Math.sqrt(dot(vp1, vp1));
		float magVp2 = (float)Math.sqrt(dot(vp2, vp2));
		float cosValue = dotProd/magVp1/magVp2;
		float angle = (float)Math.acos(cosValue);
		return angle;		
	}
	
}
