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

public class ShipTrack {
	
	final float SEGMENT_PRECISION = 0.01f; // alignment parameter

	
    private static Pattern SHIP_POSITION =
    		Pattern.compile("^(.+),(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)$"); // ts,lat,lon
    
	List<ShipPosition> posList = new ArrayList<ShipPosition>();
		
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
		return posList.get(posList.size()-1);
	}
	
	/*
	 * Loads a track from a 3 column input CSV file: lat, lon, ts
	 */
	public void loadTrack(FileReader fr) {
        BufferedReader r = new BufferedReader(fr);
        //BufferedReader r = new BufferedReader(new FileReader("C:\\master_data\\france.poly"));
        ShipPosition pos = null;
        String line;
        int readCount = 0;
        int errCount = 0;
        try {
			while((line = r.readLine()) != null) {
			        readCount++;
			        Matcher m = SHIP_POSITION.matcher(line);
			        if(m.matches()) {
			        		int ts = Integer.parseInt(m.group(1));
			                double lat = Double.parseDouble(m.group(2));
			                double lon = Double.parseDouble(m.group(3));
			                //System.out.println("ts " + ts + " lat " + lat + " lon "+ lon);			                
			                pos = new ShipPosition(new Point((float)lat, (float)lon), new Timestamp(ts));
			                pos.setIndex(readCount);
			                posList.add(pos);
			        } else errCount++;			
			}
            System.out.println("Read " + readCount + " lines; ignored " + errCount);
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
		while(posItr.hasNext()) {
			ShipPosition pos = posItr.next();
			if(p1 == null) {
				p1 = pos;
				reducedPosList.add(p1);
				continue;
			}
			if(p2 == null) {
				p2 = pos;
				continue;
			}
			p3 = pos;
			// the triplet (p1, p2, p3) is set; check if the points are aligned
			if(p1.isAligned(p2, p3, SEGMENT_PRECISION)) {
				// points are aligned, ignore p2: p3 becomes the 2nd point of the triplet
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
	

	public ChangeOfCourseSequence computeChangeOfCourseSequence() {
		ChangeOfCourseSequence changeOfCourseSequence = new ChangeOfCourseSequence();
		ShipPosition p1 = null;
		ShipPosition p2 = null;
		Iterator<ShipPosition> posItr = posList.iterator();
		while(posItr.hasNext()) {
			ShipPosition pos = posItr.next();
			if(p1 == null) {
				p1 = pos;
				continue;
			} 
			p2 = pos;
			ChangeOfCourse changeOfCourse = null;
			try {
				changeOfCourse = p1.computeChangeOfCourse(p2);
				if(changeOfCourse == null) throw new Exception("coc is null");
				changeOfCourseSequence.add(changeOfCourse);
			} catch (Exception e) {
				System.err.println("Error in computeChangeOfCourseSequence: "+e);
			}
			p1 = p2;
		}
		return changeOfCourseSequence;
	}
	
	public ShipTrack getInterpolatedTrack(int timePeriod) {
		ShipPosition posFirst = getFirstPosition();
		//int trackSize = posList.size();
		ShipPosition posLast = getLastPosition();
		int maxTs = posLast.ts.getTs();
		ShipTrack interpolatedTrack = new ShipTrack();
		interpolatedTrack.addPosition(posFirst);
		for (int t = posFirst.ts.getTs()+timePeriod; t < maxTs; t += timePeriod) {
			ShipPosition pos = getInterpolatedPosition(t);
			interpolatedTrack.addPosition(pos);
		}
		interpolatedTrack.addPosition(posLast);
		return interpolatedTrack;		
	}

	public ShipPosition getInterpolatedPosition(int ts) {
		ShipPosition posFirst = getFirstPosition();
		//int trackSize = posList.size();
		ShipPosition posLast = getLastPosition();
		ShipPosition pos = null;
		if(ts < posFirst.ts.getTs() || ts > posLast.ts.getTs()) {
			System.err.println("getInterpolatedPosition: ts out of bounds");
			return null;
		}
		ShipPosition posBefore = posFirst;
		ShipPosition posAfter = posList.get(1);
		int i = 2;
		while(ts > posAfter.ts.getTs()) {
			posBefore = posAfter;
			posAfter = posList.get(i);
			i++;
		}
		float r = (float) (ts - posBefore.ts.getTs()) / (float) (posAfter.ts.getTs() - posBefore.ts.getTs());
		float lat = posBefore.point.lat + (posAfter.point.lat - posBefore.point.lat) * r;
		float lon = posBefore.point.lon + (posAfter.point.lon - posBefore.point.lon) * r;
		pos = new ShipPosition(new Point(lat, lon), new Timestamp(ts));
		pos.setIndex(ts);
		return pos;
	}
	
	public static ShipTrack reconstructShipTrack(ShipPosition startPosition,
										  ChangeOfCourseSequence cocSeq,
										  float speed) {
		ShipTrack reconstructedTrack = new ShipTrack();
		reconstructedTrack.addPosition(startPosition);
		ShipPosition pos = startPosition;
		for(ChangeOfCourse coc : cocSeq) {
			ShipPosition nextPos = pos.computeNextPosition(coc, speed);
			reconstructedTrack.addPosition(nextPos);
			pos = nextPos;
		}
		return reconstructedTrack;
	}
	
	public float getAverageSpeed() {
		float distance = 0;
		ShipPosition prevPos = null;
		for(ShipPosition pos : posList) {
			if(prevPos != null) {
				distance += prevPos.point.distanceInMiles(pos.point); // in nm
			}
			prevPos = pos;
		}
		float duration = (getLastPosition().ts.getTs() - getFirstPosition().ts.getTs())/3600f; // in hours
		return distance / duration;
	}
	
	public float getCourseError(ChangeOfCourseSequence cocSeq) {
		float error = 1;
		
		return error;
	}
	
	public List<ShipPosition> getPosList() {
		return posList;
	}

	public void setPosList(List<ShipPosition> posList) {
		this.posList = posList;
	}

	public String toString() {
		String s = "ShipTrack:\n";
		for(ShipPosition pos : posList) {
			s = s + pos + "\n";			
		}
		return s;
	}
}
