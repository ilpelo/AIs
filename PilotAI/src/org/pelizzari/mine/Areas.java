package org.pelizzari.mine;

import java.util.HashMap;
import java.util.Map;

import org.pelizzari.gis.Box;
import org.pelizzari.gis.Point;

public class Areas {

	// Gibraltar
	static Point gibraltarNW = new Point(37, -6);
	static Point gibraltarSE = new Point(35, -5);
	// Finisterre
	static Point finisterreNW = new Point(44, -10);
	static Point finisterreSE = new Point(43, -8);
	// Copenhagen
	static Point copenhagenNW = new Point(56, 12);
	static Point copenhagenSE = new Point(55, 14);
	// Channel
	static Point channelNW = new Point(51, 1);
	static Point channelSE = new Point(50.5f, 2.5f);
	// New York
	static Point nyNW = new Point(44, -77);
	static Point nySE = new Point(40, -70);
	// Rio de Janeiro
	static Point rioNW = new Point(-21, -46);
	static Point rioSE = new Point(-26, -40);
	// South Africa
	static Point saNW = new Point(-32, 17);
	static Point saSE = new Point(-36, 20);
	// Suez
	static Point suezNW = new Point(32, 31.5f);
	static Point suezSE = new Point(29.5f, 33);
	// Golf of Aden
	static Point goaNW = new Point(18, 50);
	static Point goaSE = new Point(9, 51);
	static Point goaPOI = new Point(13.70f, 50.98f);
	// West Atlantic
	static Point westAtlanticNW = new Point(70, -77);
	static Point westAtlanticSE = new Point(-70, -70);
	// Nova Scotia
	static Point novascotiaNW = new Point(50, -72);
	static Point novascotiaSE = new Point(45f, -55);
	// Cape Town
	static Point capetownNW = new Point(-33.5f, 17);
	static Point capetownSE = new Point(-35.5f, 19);
	// Reunion
	static Point reunionNW = new Point(-20, 52);
	static Point reunionSE = new Point(-27, 57);
	// Guadeloupe
	static Point guadeloupeNW = new Point(17.27f, -62.9f);
	static Point guadeloupeSE = new Point(15.8f, -59.7f);
	// Calais
	static Point calaisNW = new Point(51.33f, 0.95f);
	static Point calaisSE = new Point(50.61f, 2.58f);
	// Red Sea
	static Point redseaNW = new Point(20, 38);
	static Point redseaSE = new Point(17, 42);
	static Point redseaPOI = new Point(20, 38.86f);
	// Lanzarote
	static Point lanzaroteNW = new Point(29, -14);
	static Point lanzaroteSE = new Point(27, 12);
	// Natal
	static Point natalNW = new Point(-5, -36);
	static Point natalSE = new Point(-7, -33);
		
	public static Map<String, Box> boxes = new HashMap<String, Box>();
	
	static {
		boxes.put("GIBRALTAR", new Box(gibraltarNW, gibraltarSE, "GIBRALTAR"));
		boxes.put("FINISTERRE", new Box(finisterreNW, finisterreSE, "FINISTERRE"));
		boxes.put("NOVASCOTIA", new Box(novascotiaNW, novascotiaSE, "NOVASCOTIA"));
		boxes.put("SUEZ", new Box(suezNW, suezSE, "SUEZ"));
		boxes.put("CAPETOWN", new Box(capetownNW, capetownSE, "CAPETOWN"));
		boxes.put("REUNION", new Box(reunionNW, reunionSE, "REUNION"));
		boxes.put("WEST_ATLANTIC", new Box(westAtlanticNW, westAtlanticSE, "WEST_ATLANTIC"));
		boxes.put("RIO", new Box(rioNW, rioSE, "RIO"));
		boxes.put("GOA", new Box(goaNW, goaSE, "GOA", goaPOI));
		boxes.put("GUADELOUPE", new Box(guadeloupeNW, guadeloupeSE, "GUADELOUPE"));		
		String areaName = "CALAIS";
		boxes.put(areaName, new Box(calaisNW, calaisSE, areaName));		
		areaName = "REDSEA";
		boxes.put(areaName, new Box(redseaNW, redseaSE, areaName, redseaPOI));		
		areaName = "LANZAROTE";
		boxes.put(areaName, new Box(lanzaroteNW, lanzaroteSE, areaName));		
		areaName = "NATAL";
		boxes.put(areaName, new Box(natalNW, natalSE, areaName));		
	}
	
	public static Box getBox(String boxName) {
		return boxes.get(boxName);
	}
	
}
