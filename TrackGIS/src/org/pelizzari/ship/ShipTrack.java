package org.pelizzari.ship;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pelizzari.db.DBConnection;
import org.pelizzari.gis.*;
import org.pelizzari.time.TimeInterval;
import org.pelizzari.time.Timestamp;

/**
 * @author andrea@pelizzari.org
 *
 */
public class ShipTrack extends ShipPositionList {
	
	public static final float REFERENCE_SPEED_IN_KNOTS = 10; // knots, used to normalize tracks
	
	final float SEGMENT_PRECISION = 0.01f; // alignment parameter
	// used to read the input file with ship positions
	private static Pattern SHIP_POSITION = Pattern
			.compile("^(.+),(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)$"); // ts,lat,lon
	
	DisplacementSequence displacementSeq; // this sequence has length = length(ShipTrack) - 1;
	HeadingSequence headingSeq; // this sequence has length = length(ShipTrack) - 1;
	ChangeOfHeadingSequence changeOfHeadingSeq; // this sequence has length = length(ShipTrack) - 2;

	// list of segments of this track
	List<ShipTrackSegment> segList = new ArrayList<ShipTrackSegment>();
	// average speed
	float avgSpeed = -1;
	// mmsi
	String mmsi = null;
	
	public ShipTrack() {
		super();
		// nothing
	}


	/*
	 * Loads a track from an input CSV file: see pattern SHIP_POSITION
	 */
	public void loadTrack(FileReader fr) {
		BufferedReader r = new BufferedReader(fr);
		ShipPosition pos = null;
		String line;
		int readCount = 0;
		int errCount = 0;
		try {
			while ((line = r.readLine()) != null) {
				readCount++;
				Matcher m = SHIP_POSITION.matcher(line);
				if (m.matches()) {
					long ts = Long.parseLong(m.group(1));
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

	
	public void saveTrack(FileWriter fw) {
		BufferedWriter w = new BufferedWriter(fw);
		String line;
		int writeCount = 0;
		//int errCount = 0;
		try {
			for (ShipPosition pos : getPosList()) {
				line = pos.getTs().getTsMillisec() + "," + pos.point.lat + "," + pos.point.lon + "\n";
				w.write(line);
				writeCount++;
			}
			System.out.println("Written " + writeCount + " lines");
			w.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveTrackToDB(Box depBox, Box arrBox, String yearPeriod) {
		Connection con = DBConnection.getCon();
		int writeCount = 0;
		//int errCount = 0;
				
		final String TRACK_INSERT = 
				"INSERT INTO tracks (mmsi, source, period, dep, arr, ts, lat, lon) "+
				"VALUES ("+
			    getMmsi() + ", " +
				"null, " + // source
				"'" + yearPeriod+ "', " +
				"'" + depBox.getName() + "', " +
				"'" + arrBox.getName() + "', ";
		
		try {
			for (ShipPosition pos : getPosList()) {
				String values = pos.getTs().getTsMillisec()/1000 + "," + pos.point.lat + "," + pos.point.lon;
				String insert = TRACK_INSERT + values + ")";
				Statement stmt = con.createStatement();
				stmt.executeUpdate(insert);	
				writeCount++;
			}
			System.out.println("Written to DB " + writeCount + " lines");
		} catch (SQLException e) {
			System.err.println("Cannot write track to DB");
			e.printStackTrace();
		}
	}	
	
	
	/**
	 * Remove from track the positions so that the rate does not go over the given one. 
	 * @param maxRateIn
	 */
	public void reducePositions(float maxRateInPosPerHour) {
		
	}
	
	public void removeAlignedPositions() {
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
		setPosList(reducedPosList);
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

	/**
	 * Reconstruct the track based on a the given sequence of displacements and firstPosition.
	 * Compute Timestamps based on the given speed.
	 * @param startPosition
	 * @param displSeq
	 * @param speed
	 * @return
	 */
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

	/**
	 * Reconstruct the track based on a the given sequence of displacements and firstPosition.
	 * Set the timestamps according to the cumulative distance and the effective duration of the voyage.
	 * @param startPosition
	 * @param endTs
	 * @param displSeq
	 * @return
	 */
	public static ShipTrack reconstructShipTrack(ShipPosition startPosition,
													Timestamp endTs, 
													DisplacementSequence displSeq) {
		// first reconstruct track based on displacement; do not bother about timestamps
		ShipTrack reconstructedTrack = new ShipTrack();
		reconstructedTrack.addPosition(startPosition);
		ShipPosition pos = startPosition;
		for(Displacement displ : displSeq) {
			ShipPosition nextPos = pos.computeNextPosition(displ);
			reconstructedTrack.addPosition(nextPos);
			pos = nextPos;
		}
		// set the timestamps: use the cumulative distance from the start to compute the elapsed time 
		float totalLengthInMiles = reconstructedTrack.computeLengthInMiles();
		long startTsInMillisec = startPosition.getTs().getTsMillisec();
		long totalDurationInMillisec = endTs.getTsMillisec() - startTsInMillisec;
		ShipPosition prevPos = null;
		float cumulativeDistanceInMiles = 0;
		for(ShipPosition pos1 : reconstructedTrack.getPosList()) {
			if (prevPos != null) {
				float distanceInMiles = prevPos.getPoint().distanceInMiles(pos1.getPoint());
				cumulativeDistanceInMiles += distanceInMiles;
				long relativeDurationInMillisec = (long) (totalDurationInMillisec * cumulativeDistanceInMiles / totalLengthInMiles);
				pos1.setTs(startTsInMillisec + relativeDurationInMillisec);
			}
			prevPos = pos1;
		}
		// force last position to have the same timestamp as the last position of the target track
		reconstructedTrack.getLastPosition().setTs(endTs.getTsMillisec());
		return reconstructedTrack;
	}	
	

	/**
	 * Compute the list of segments corresponding to the positions of this track.
	 *
	 * WARNING: overwrite the position timestamps to match the speed parameter
	 * and start journey on the reference timestamp. 
	 *
	 * @param speed
	 * @return
	 */
	public List<ShipTrackSegment> computeTrackSegmentsAndNormalizeTime(Timestamp referenceStartTS, 
																	   float speed) {
		if(segList.size() == 0) {
			ShipPosition prevPos = null;
			for (ShipPosition pos : posList) {
				if (prevPos != null) {
					segList.add(new ShipTrackSegment(prevPos, pos, speed));
				} else {
					pos.setTs(referenceStartTS);
				}
				prevPos = pos;
			}
		}
		return segList;
	}
	
	/**
	 * Compute the list of segments corresponding to the positions of this track.
	 * Keep the position timestamps.
	 *
	 * @return
	 */
	public List<ShipTrackSegment> computeTrackSegments() {
		if(segList.size() == 0) {
			ShipPosition prevPos = null;
			for (ShipPosition pos : posList) {
				if (prevPos != null) {
					segList.add(new ShipTrackSegment(prevPos, pos));
				} 
				prevPos = pos;
			}
		}
		return segList;
	}
	
	public List<ShipTrackSegment> getSegList() {
		return segList;
	}

	public float computeLengthInMiles() {
		float lengthInMiles = 0;
		ShipPosition prevPos = null;
		for (ShipPosition pos : posList) {
			if (prevPos != null) {
				lengthInMiles += prevPos.point.distanceInMiles(pos.point); // in nm
			}
			prevPos = pos;
		}
		return lengthInMiles;
	}
	
	// in knots (miles/hours)
	public float computeAverageSpeed() {
		if(avgSpeed != -1) {
			return avgSpeed;
		}
		float distance = computeLengthInMiles();
		float duration = (getLastPosition().ts.getTsMillisec() - getFirstPosition().ts.getTsMillisec())
				/ 3600000f; // in hours
		avgSpeed = distance / duration;
		return avgSpeed;
	}
	
	public TrackError computeTrackError(ShipPositionList trainingPosList, boolean debug) throws Exception {
		TrackError trackError = new TrackError(this, debug);
		trackError.computeSegmentErrorVector(trainingPosList);
		trackError.computeStatsForFitness();
		return trackError;		
	}

	public TrackError computeTrackError(ShipPositionList trainingPosList) throws Exception {
		return computeTrackError(trainingPosList, false);		
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
	
	/*
	 * Normalize timestamps: first position at Epoch 00:00, last at Epoch 24:00
	 */
//	public void timeNormalize() {
//		long firstTSMillisec = getFirstPosition().getTs().getTsMillisec();
//		long lastTSMillisec = getLastPosition().getTs().getTsMillisec();
//		long durationInMillisec = lastTSMillisec - firstTSMillisec;
//		for (ShipPosition pos : getPosList()) {
//			long tsMillisec = pos.getTs().getTsMillisec();
//			long deltaTsMillisec = tsMillisec - firstTSMillisec;
//			long newTsMillisec = (long)((float)deltaTsMillisec/(float)durationInMillisec*
//					Timestamp.ONE_DAY_IN_MILLISEC); // norm to 24h
//			pos.setTs(new Timestamp(newTsMillisec));
//		}	
//	}
	
	/*
	 * Normalize timestamps based on the track distance and fixed speed: first position at Epoch 00:00, last at Epoch 24:00
	 */
//	public void normalize(float speedInKnots) {
//		float totalDistance = computeTotalDistance();
//		
//		long firstTSMillisec = getFirstPosition().getTs().getTsMillisec();
//		long lastTSMillisec = getLastPosition().getTs().getTsMillisec();
//		long durationInMillisec = lastTSMillisec - firstTSMillisec;
//		for (ShipPosition pos : getPosList()) {
//			long tsMillisec = pos.getTs().getTsMillisec();
//			long deltaTsMillisec = tsMillisec - firstTSMillisec;
//			long newTsMillisec = (long)((float)deltaTsMillisec/(float)durationInMillisec*
//					Timestamp.ONE_DAY_IN_MILLISEC); // norm to 24h
//			pos.setTs(new Timestamp(newTsMillisec));
//		}	
//	}



	public String getMmsi() {
		return mmsi;
	}

	public void setMmsi(String mmsi) {
		this.mmsi = mmsi;
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
