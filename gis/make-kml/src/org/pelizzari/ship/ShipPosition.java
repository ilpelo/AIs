package org.pelizzari.ship;

import org.pelizzari.gis.Point;

public class ShipPosition extends Point {

	int ts;
	public ShipPosition(int ts, float lat, float lon) {
		super(lat, lon);
		this.ts = ts;		
	}
	public int getTs() {
		return ts;
	}
	public void setTs(int ts) {
		this.ts = ts;
	}

}
