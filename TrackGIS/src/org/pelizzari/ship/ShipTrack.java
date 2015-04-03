package org.pelizzari.ship;

import java.awt.Color;
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
	
	final float SEGMENT_PRECISION = 0.5f;
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
			                System.out.println("ts " + ts + " lat " + lat + " lon "+ lon);			                
			                pos = new ShipPosition(new Point((float)lat, (float)lon), new Timestamp(ts));
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
			if(isSegment(p1, p2, p3, SEGMENT_PRECISION)) {
				// points are aligned, ignore p2: p3 becomes the 2nd point of the triplet
				p2 = p3;
			} else {
				// points are not aligned, add p2 to the list: shift p1 and p2 
				reducedPosList.add(p2);
				p1 = p2;
				p2 = p3;
			}
			p3 = null;
		}
		reducedPosList.add(p2);
		posList = reducedPosList;
	}
	
	/*
	 * Checks if the difference of the slope between (p1, p2) and (p1, p3) is the same, with a given precision:
	 * slope(p1, p2) - slope(p1, p3) <= precision
	 */
	public boolean isSegment(ShipPosition p1, ShipPosition p2, ShipPosition p3, float precision) {		
		return Math.abs((p1.point.lat - p2.point.lat) * (p1.point.lon - p3.point.lon)
						-
						(p1.point.lat - p3.point.lat) * (p1.point.lon - p2.point.lon)) <= precision;
	}
	
	public List<ChangeOfCourse> computeChangeOfCourseSequence(ShipTrack track) {
		List<ChangeOfCourse> changeOfCourseList = new ArrayList<ChangeOfCourse>();
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
			ChangeOfCourse changeOfCourse;
			try {
				changeOfCourse = p1.computeChangeOfCourse(p2);
				changeOfCourseList.add(changeOfCourse);
			} catch (Exception e) {
				System.err.println("Error in computeChangeOfCourseSequence: "+e);
			}
		}
		return changeOfCourseList;
	}

	
	public List<ShipPosition> getPosList() {
		return posList;
	}

	public void setPosList(List<ShipPosition> posList) {
		this.posList = posList;
	}

	public String toString() {
		String s = "ShipTrack: ";
		Iterator<ShipPosition> posItr = posList.iterator();
		while(posItr.hasNext()) {
			ShipPosition pos = posItr.next();
			s = s + pos + " ";
		}
		return s;
	}
}
