package org.pelizzari.ship;

import java.util.ArrayList;
import java.util.List;

import org.pelizzari.gis.Box;
import org.pelizzari.gis.Point;
import org.pelizzari.time.TimeInterval;
import org.pelizzari.time.Timestamp;

/**
 * Set of ship position ordered by timestamp.
 * @author andrea@pelizzari.org
 *
 */
public class ShipPositionList {
	
	// list of positions of this track
	List<ShipPosition> posList = new ArrayList<ShipPosition>();
	
	public ShipPositionList() {
		
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

	
	/**
	 * Select ship positions only if they are in the time interval of the segment and they are located
	 * in the stripe perpendicular to the segment. 
	 * @param segment
	 * @return
	 * @throws Exception
	 */
	public List<ShipPosition> getPosListInIntervalAndOnStripe(ShipTrackSegment segment) throws Exception {
		List<ShipPosition> filteredPosList = new ArrayList<ShipPosition>();
		TimeInterval interval = segment.getTimeInterval(); 
		for (ShipPosition pos : posList) {
			if(interval.isWithinInterval(pos.getTs()) && 
			   segment.isWithinPerpendicularStripe(pos.point)) {
				filteredPosList.add(pos);
			}
		}
		return filteredPosList;
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
	
	
}
