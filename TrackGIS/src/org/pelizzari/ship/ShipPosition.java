package org.pelizzari.ship;

import org.pelizzari.gis.*;

public class ShipPosition {
	Point point;
	Timestamp ts;
	
	public ShipPosition(Point point, Timestamp ts) {
		this.point = point;
		this.ts = ts;
	}
}
