package org.pelizzari.ship;

import org.pelizzari.gis.Point;

public class ShipPosition extends Point {

	int ts;
	public ShipPosition(int ts, float lat, float lon) {
		super(lat, lon);
		this.ts = ts;		
	}

}
