package org.pelizzari.ship;

public class ShipTrackSegment {
	ShipPosition p1;
	ShipPosition p2;

	public ShipTrackSegment(ShipPosition p1, ShipPosition p2) {
		this.p1 = p1;
		this.p2 = p2;
	}

	public ShipPosition getP1() {
		return p1;
	}

	public ShipPosition getP2() {
		return p2;
	}
	
	
	
}
