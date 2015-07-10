package org.pelizzari.ship;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pelizzari.gis.*;
import org.pelizzari.time.TimeInterval;
import org.pelizzari.time.Timestamp;

/**
 * @author andrea@pelizzari.org
 *
 */
public class ShipTrack {

	final float SEGMENT_PRECISION = 0.01f; // alignment parameter
	
	DisplacementSequence displacementSeq; // this sequence has length = length(ShipTrack) - 1;
	HeadingSequence headingSeq; // this sequence has length = length(ShipTrack) - 1;
	ChangeOfHeadingSequence changeOfHeadingSeq; // this sequence has length = length(ShipTrack) - 2;

	private static Pattern SHIP_POSITION = Pattern
			.compile("^(.+),(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)$"); // ts,lat,lon

	// list of positions of this track
	List<ShipPosition> posList = new ArrayList<ShipPosition>();
	// list of segments of this track
	List<ShipTrackSegment> segList = new ArrayList<ShipTrackSegment>();
	// average speed
	float avgSpeed = -1;

	public ShipTrack() {
		// nothing
	}

	public void addPosition(ShipPosition pos) {
		posList.add(pos);
	}

	public void addPosition(int ts, float lat, float lon) {
		Timestamp timestamp = new Timestamp(ts);
		Point point = new Point(lat, lon);
		ShipPosition pos = new ShipPosition(point, timestamp);
		addPosition(pos);
	}

	public ShipPosition getFirstPosition() {
		return posList.get(0);
	}

	public ShipPosition getLastPosition() {
		return posList.get(posList.size() - 1);
	}

	/*
	 * Loads a track from a 3 column input CSV file: lat, lon, ts
	 */
	public void loadTrack(FileReader fr) {
		BufferedReader r = new BufferedReader(fr);
		// BufferedReader r = new BufferedReader(new
		// FileReader("C:\\master_data\\france.poly"));
		ShipPosition pos = null;
		String line;
		int readCount = 0;
		int errCount = 0;
		try {
			while ((line = r.readLine()) != null) {
				readCount++;
				Matcher m = SHIP_POSITION.matcher(line);
				if (m.matches()) {
					int ts = Integer.parseInt(m.group(1));
					double lat = Double.parseDouble(m.group(2));
					double lon = Double.parseDouble(m.group(3));
					// System.out.println("ts " + ts + " lat " + lat + " lon "+
					// lon);
					pos = new ShipPosition(new Point((float) lat, (float) lon),
							new Timestamp(ts));
					pos.setIndex(readCount);
					addPosition(pos);
				} else
					errCount++;
			}
			System.out.println("Read " + readCount + " lines; ignored "
					+ errCount);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	
	public void reducePositions() {
		List<ShipPosition> reducedPosList = new ArrayList<ShipPosition>();
		ShipPosition p1 = null;
		ShipPosition p2 = null;
		ShipPosition p3 = null;
		Iterator<ShipPosition> posItr = posList.iterator();
		while (posItr.hasNext()) {
			ShipPosition pos = posItr.next();
			if (p1 == null) {
				p1 = pos;
				reducedPosList.add(p1);
				continue;
			}
			if (p2 == null) {
				p2 = pos;
				continue;
			}
			p3 = pos;
			// the triplet (p1, p2, p3) is set; check if the points are aligned
			if (p1.isAligned(p2, p3, SEGMENT_PRECISION)) {
				// points are aligned, ignore p2: p3 becomes the 2nd point of
				// the triplet
				p2 = p3;
			} else {
				// points are not aligned, add p2 to the list: shift p1 and p2
				reducedPosList.add(p2);
				p1 = p2;
				p2 = p3;
			}
		}
		reducedPosList.add(p2);
		posList = reducedPosList;
	}

	// old version with angle and distance (in fact this is not a change of course!)
//	private ChangeOfCourseSequence computeChangeOfCourseSequence() {
//		ChangeOfCourseSequence changeOfCourseSequence = new ChangeOfCourseSequence();
//		ShipPosition p1 = null;
//		ShipPosition p2 = null;
//		Iterator<ShipPosition> posItr = posList.iterator();
//		while (posItr.hasNext()) {
//			ShipPosition pos = posItr.next();
//			if (p1 == null) {
//				p1 = pos;
//				continue;
//			}
//			p2 = pos;
//			ChangeOfCourse changeOfCourse = null;
//			try {
//				changeOfCourse = p1.computeChangeOfCourse(p2);
//				if (changeOfCourse == null)
//					throw new Exception("coc is null");
//				changeOfCourseSequence.add(changeOfCourse);
//			} catch (Exception e) {
//				System.err.println("Error in computeChangeOfCourseSequence: "
//						+ e);
//			}
//			p1 = p2;
//		}
//		return changeOfCourseSequence;
//	}

	
	public HeadingSequence computeHeadingSequence() {
		if(headingSeq != null) {
			return headingSeq;
		}
		if(displacementSeq == null) {
			computeDisplacements();
		}
		headingSeq = new HeadingSequence(displacementSeq);		
		return headingSeq;
	}

	public ChangeOfHeadingSequence computeChangeOfHeadingSequence() {
		if(changeOfHeadingSeq != null) {
			return changeOfHeadingSeq;
		}
		if(headingSeq == null) {
			computeHeadingSequence();
		}
		changeOfHeadingSeq = new ChangeOfHeadingSequence();
		Heading head1 = null;
		Heading head2 = null;
		for (Heading heading : headingSeq) {
			if (head1 == null) {
				head1 = heading;
				continue;
			}
			head2 = heading;
			ChangeOfHeading coh = new ChangeOfHeading(head1, head2);
			changeOfHeadingSeq.add(coh);
			head1 = head2;
		}
		return changeOfHeadingSeq;
	}
	
	public DisplacementSequence computeDisplacements() {
		if(displacementSeq != null) {
			return displacementSeq;
		}
		DisplacementSequence displSeq = new DisplacementSequence();
		ShipPosition p1 = null;
		ShipPosition p2 = null;
		Iterator<ShipPosition> posItr = posList.iterator();
		while (posItr.hasNext()) {
			ShipPosition pos = posItr.next();
			if (p1 == null) {
				p1 = pos;
				continue;
			}
			p2 = pos;
			Displacement displ = null;
			try {
				displ = p1.computeDisplacement(p2);
				if (displ == null)
					throw new Exception("Displacement is null");
				displSeq.add(displ);
			} catch (Exception e) {
				System.err.println("Error in computeDisplacements: " + e);
			}
			p1 = p2;
		}
		displacementSeq = displSeq;
		return displSeq;
	}

	public ShipTrack getInterpolatedTrack(int timePeriod) {
		ShipPosition posFirst = getFirstPosition();
		// int trackSize = posList.size();
		ShipPosition posLast = getLastPosition();
		long maxTs = posLast.ts.getTsMillisec();
		ShipTrack interpolatedTrack = new ShipTrack();
		interpolatedTrack.addPosition(posFirst);
		for (long t = posFirst.ts.getTsMillisec() + timePeriod; t < maxTs; t += timePeriod) {
			ShipPosition pos = getInterpolatedPosition(t);
			interpolatedTrack.addPosition(pos);
		}
		interpolatedTrack.addPosition(posLast);
		return interpolatedTrack;
	}

	public ShipPosition getInterpolatedPosition(long ts) {
		ShipPosition posFirst = getFirstPosition();
		// int trackSize = posList.size();
		ShipPosition posLast = getLastPosition();
		ShipPosition pos = null;
		if (ts < posFirst.ts.getTsMillisec() || ts > posLast.ts.getTsMillisec()) {
			System.err.println("getInterpolatedPosition: ts out of bounds");
			return null;
		}
		ShipPosition posBefore = posFirst;
		ShipPosition posAfter = posList.get(1);
		int i = 2;
		while (ts > posAfter.ts.getTsMillisec()) {
			posBefore = posAfter;
			posAfter = posList.get(i);
			i++;
		}
		float r = (float) (ts - posBefore.ts.getTsMillisec())
				/ (float) (posAfter.ts.getTsMillisec() - posBefore.ts.getTsMillisec());
		float lat = posBefore.point.lat
				+ (posAfter.point.lat - posBefore.point.lat) * r;
		float lon = posBefore.point.lon
				+ (posAfter.point.lon - posBefore.point.lon) * r;
		pos = new ShipPosition(new Point(lat, lon), new Timestamp(ts));
		pos.setIndex(ts);
		return pos;
	}

//	public static ShipTrack reconstructShipTrack(ShipPosition startPosition,
//			ChangeOfCourseSequence cocSeq, float speed) {
//		ShipTrack reconstructedTrack = new ShipTrack();
//		reconstructedTrack.addPosition(startPosition);
//		ShipPosition pos = startPosition;
//		for (ChangeOfCourse coc : cocSeq) {
//			ShipPosition nextPos = pos.computeNextPosition(coc, speed);
//			reconstructedTrack.addPosition(nextPos);
//			pos = nextPos;
//		}
//		return reconstructedTrack;
//	}

	public static ShipTrack reconstructShipTrack(ShipPosition startPosition,
												 DisplacementSequence displSeq,
												 float speed) {
		ShipTrack reconstructedTrack = new ShipTrack();
		reconstructedTrack.addPosition(startPosition);
		ShipPosition pos = startPosition;
		for (Displacement displ : displSeq) {
			ShipPosition nextPos = pos.computeNextPosition(displ, speed);
			reconstructedTrack.addPosition(nextPos);
			pos = nextPos;
		}
		return reconstructedTrack;
	}
	
	public List<ShipTrackSegment> computeTrackSegments(float speed) {
		if(segList.size() == 0) {
			//List<ShipTrackSegment> segments = new ArrayList<ShipTrackSegment>();
			ShipPosition prevPos = null;
			for (ShipPosition pos : posList) {
				if (prevPos != null) {
					segList.add(new ShipTrackSegment(prevPos, pos, speed));
				}
				prevPos = pos;
			}
		}
		return segList;
	}

	
	public List<ShipTrackSegment> getSegList() {
		return segList;
	}

	// in knots (miles/hours)
	public float computeAverageSpeed() {
		if(avgSpeed != -1) {
			return avgSpeed;
		}
		float distance = 0;
		ShipPosition prevPos = null;
		for (ShipPosition pos : posList) {
			if (prevPos != null) {
				distance += prevPos.point.distanceInMiles(pos.point); // in nm
			}
			prevPos = pos;
		}
		float duration = (getLastPosition().ts.getTsMillisec() - getFirstPosition().ts.getTsMillisec())
				/ 3600f; // in hours
		avgSpeed = distance / duration;
		return avgSpeed;
	}
	
	public TrackError computeTrackError(ShipTrack targetTrack, boolean debug) throws Exception {
		TrackError trackError = new TrackError(this, debug);
		trackError.computeSegmentErrorVector(targetTrack);
		trackError.computeStatsForFitness();
		return trackError;		
	}

	public TrackError computeTrackError(ShipTrack targetTrack) throws Exception {
		return computeTrackError(targetTrack, false);		
	}
	
	public int countChangeOfHeadingOverLimit(float thresholdAngle) {
		int cohOverLimitCount = 0;
		if(changeOfHeadingSeq == null) {
			computeChangeOfHeadingSequence();
		}
		for (ChangeOfHeading coh : changeOfHeadingSeq) {
			if(Math.abs(coh.changeOfHeading) > thresholdAngle) {
				cohOverLimitCount++;
			}
		}
		return cohOverLimitCount;
	}

	public ChangeOfHeadingSequence getChangeOfHeadingSeq() {
		return changeOfHeadingSeq;
	}

	public void setChangeOfHeadingSeq(ChangeOfHeadingSequence changeOfHeadingSeq) {
		this.changeOfHeadingSeq = changeOfHeadingSeq;
	}

	public List<ShipPosition> getPosListInInterval(TimeInterval interval) {
		List<ShipPosition> filteredPosList = new ArrayList<ShipPosition>();
		for (ShipPosition pos : posList) {
			if(interval.isWithinInterval(pos.getTs())) {
				filteredPosList.add(pos);
			}
		}
		return filteredPosList;
	}	
		
	
	/**
	 * Return the list of positions that have a timestamp within the interval and are within the box, 
	 */
	public List<ShipPosition> getPosListInIntervalAndBox(TimeInterval interval, Box box) {
		List<ShipPosition> filteredPosList = new ArrayList<ShipPosition>();
		for (ShipPosition pos : posList) {
			if(interval.isWithinInterval(pos.getTs()) && box.isWithinBox(pos.point)) {
				filteredPosList.add(pos);
			}
		}
		return filteredPosList;
	}


	public List<ShipPosition> getPosListInIntervalAndBoxAndCloseToSegment(TimeInterval interval,
																	Box box,
																	Point p1,
																	Point p2,
																	float maxSquaredDistance) {
		List<ShipPosition> filteredPosList = new ArrayList<ShipPosition>();
		for (ShipPosition pos : posList) {
			if(interval.isWithinInterval(pos.getTs()) && box.isWithinBox(pos.point)) {
				float squaredDistance = pos.point.approxSquaredDistanceToSegment(p1, p2);
				if(squaredDistance <= maxSquaredDistance) {
					filteredPosList.add(pos);
				}
			}
		}
		return filteredPosList;
	}	
	
	public List<ShipPosition> getPosList() {
		return posList;
	}

	public void setPosList(List<ShipPosition> posList) {
		this.posList = posList;
	}

	public float getAvgSpeed() {
		return avgSpeed;
	}

	public void setAvgSpeed(float avgSpeed) {
		this.avgSpeed = avgSpeed;
	}
	
	/* Print all characteristics of this ShipTrack
	 */
	public String toString() {
		String s = "ShipTrack:\n";
		for (ShipPosition pos : posList) {
			s = s + pos + "\n";
		}
		DisplacementSequence displSeq = computeDisplacements();
		s = s + displSeq + "\n";
		HeadingSequence headSeq = computeHeadingSequence();
		s = s + headSeq + "\n";
		ChangeOfHeadingSequence cohSeq = computeChangeOfHeadingSequence();
		s = s + cohSeq + "\n";
		float speed = computeAverageSpeed();
		s = s + "Avg. speed = " + speed + "\n";
		return s;
	}
}
