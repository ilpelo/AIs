package org.pelizzari.ship;

import java.util.ArrayList;
import java.util.List;

public class ShipVoyage {

	String mmsi;
	List<ShipPosition> posList;
	
	public ShipVoyage(String mmsi, List<ShipPosition> posList) {
		setPosList(posList);
	}

	public List<ShipPosition> getPosList() {
		return posList;
	}

	public void setPosList(List<ShipPosition> posList) {
		this.posList = posList;
	}
	
	
	
}
