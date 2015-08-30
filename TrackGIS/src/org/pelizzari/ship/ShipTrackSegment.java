package org.pelizzari.ship;

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
	
	List<ShipPosition> targetPosList;
	float[] squaredDistanceOfTargetPositionArray;
	int numberOfCoveredTargetPositions = 0; // number of target positions covered to the segment 
	float avgSquaredDistanceToTargetPositions = 0f;
	float minSquaredDistanceToTargetPositions = 0f;
	float maxSquaredDistanceToTargetPositions = 0f;
	float squaredDistanceOfSegmentEndToLastTargetPosition = 0f;

	/**
	 * Create a segment with the given position.
	 * WARNING: overwrite timestamp of p2 based on the given speed.
	 * @param p1
	 * @param p2
	 * @param speedInKnots
	 */
	public ShipTrackSegment(ShipPosition p1, ShipPosition p2, float speedInKnots) {
		this.p1 = p1;
		this.p2 = p2;
		center = new Point((p1.point.lat + p2.point.lat)/2, (p1.point.lon + p2.point.lon)/2);
		length = p1.point.distance(p2.point);
		lengthInMiles = p1.point.distanceInMiles(p2.point);
		durationInSeconds = (int) (lengthInMiles / speedInKnots * 3600f); // in seconds
		p2.setTs(p1.ts.getTsMillisec()+durationInSeconds*1000);
	}
	
	/*
	 * Compute the distance to the target points and store the values in an array
	 */
	public void computeDistanceToTargetPositions() {
		if(targetPosList == null || targetPosList.size() == 0) {
			numberOfCoveredTargetPositions = 0;
			return;
		}
		numberOfCoveredTargetPositions = targetPosList.size();
		squaredDistanceOfTargetPositionArray = new float[numberOfCoveredTargetPositions];
		int i = 0;
		ShipPosition lastTargetPos = null;
		for (ShipPosition targetPos : targetPosList) {
			// distance of target positions from the segment
			squaredDistanceOfTargetPositionArray[i] = 
					targetPos.point.approxSquaredDistanceToSegment(p1.point, p2.point);
			i++;
			lastTargetPos = targetPos;
		}
		squaredDistanceOfSegmentEndToLastTargetPosition = p2.point.distance(lastTargetPos.point);
	}
	
	/*
	 * Compute some stats that can be used for fitness evaluation.
	 */
	public void computeStatsForFitness() {
		if(numberOfCoveredTargetPositions == 0) {
			return;
		}
		minSquaredDistanceToTargetPositions = Float.MAX_VALUE;
		maxSquaredDistanceToTargetPositions = 0;
		float sumSquaredDistance = 0;
		for (int i = 0; i < squaredDistanceOfTargetPositionArray.length; i++) {
			float squaredDistance = squaredDistanceOfTargetPositionArray[i];
			if(squaredDistance < minSquaredDistanceToTargetPositions) {
				minSquaredDistanceToTargetPositions = squaredDistance;
			}
			if(squaredDistance > maxSquaredDistanceToTargetPositions) {
				maxSquaredDistanceToTargetPositions = squaredDistance;
			}
			sumSquaredDistance += squaredDistance;
		}
		avgSquaredDistanceToTargetPositions = sumSquaredDistance/numberOfCoveredTargetPositions;
	}
	
	public List<ShipPosition> getTargetPosList() {
		return targetPosList;
	}

	public void setTargetPosList(List<ShipPosition> targetPosList) {
		this.targetPosList = targetPosList;
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

	public void setNumberOfCoveredTargetPositions(int numberOfTargetPositions) {
		this.numberOfCoveredTargetPositions = numberOfTargetPositions;
	}

	public float getAvgSquaredDistanceToTargetPositions() {
		return avgSquaredDistanceToTargetPositions;
	}

	public void setAvgSquaredDistanceToTargetPositions(
			float avgSquaredDistanceToTargetPositions) {
		this.avgSquaredDistanceToTargetPositions = avgSquaredDistanceToTargetPositions;
	}

	public float getMinSquaredDistanceToTargetPositions() {
		return minSquaredDistanceToTargetPositions;
	}

	public void setMinSquaredDistanceToTargetPositions(
			float minSquaredDistanceToTargetPositions) {
		this.minSquaredDistanceToTargetPositions = minSquaredDistanceToTargetPositions;
	}

	public float getMaxSquaredDistanceToTargetPositions() {
		return maxSquaredDistanceToTargetPositions;
	}

	public void setMaxSquaredDistanceToTargetPositions(
			float maxSquaredDistanceToTargetPositions) {
		this.maxSquaredDistanceToTargetPositions = maxSquaredDistanceToTargetPositions;
	}
	
	public String toString() {
		String s = "Segment: ";
		s = s + p1 + " --- " + p2 +", l = "+ length + " deg, d = " + durationInSeconds + " s" + "\n";
		if(targetPosList != null) {
			int i = 0;
			for (ShipPosition targetPos : targetPosList) {
				s = s + "Covered Pos: " + targetPos + 
						", d^2="+ squaredDistanceOfTargetPositionArray[i] + "\n";
				i++;
			}
			s = s + "Squared distance: avg=" + avgSquaredDistanceToTargetPositions +
					", min=" + minSquaredDistanceToTargetPositions +
					", max=" + maxSquaredDistanceToTargetPositions +
					", to segment end=" + squaredDistanceOfSegmentEndToLastTargetPosition + "\n";		
		}
		return s;
	}
	
}
