package org.pelizzari.ship;

import java.util.ArrayList;
import java.util.List;

import org.pelizzari.gis.*;

public class ShipTrack {
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

	
}
