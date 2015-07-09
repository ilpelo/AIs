package org.pelizzari.ship;

/*
 * WARNING: empty class!!!
 * 
 * 
 */

import java.util.List;

import org.pelizzari.gis.Box;

public class Ship {
	
	String mmsi;
	
	public Ship(String mmsi) {
		this.mmsi = mmsi;
	}

	public static List<ShipVoyage> findVoyages(Box depBox, Box arrBox,  String depDay,
			int duration,  String[] excludeMmsi) {
		return null;
	}
	
	
	public String getMmsi() {
		return mmsi;
	}

	public String toString() {
		return "Ship "+mmsi;
	}
	
}
