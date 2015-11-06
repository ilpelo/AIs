package org.pelizzari.mine;

import java.util.HashMap;
import java.util.Map;

import org.pelizzari.gis.Box;
import org.pelizzari.gis.Point;

public class Areas {

	// Gibraltar
	static Point gibraltarNW = new Point(37, -6);
	static Point gibraltarSE = new Point(35, -5);
	static Point gibraltarPOI = new Point(36, -5.5f);
	// Finisterre
	static Point finisterreNW = new Point(44, -10);
	static Point finisterreSE = new Point(43, -8);
	// Copenhagen
	static Point copenhagenNW = new Point(56, 12);
	static Point copenhagenSE = new Point(55, 14);
	// Channel
	static Point channelNW = new Point(50.22f, -5.3f);
	static Point channelSE = new Point(49.24f, -2.45f);
	static Point channelPOI = new Point(50, -2.5f);
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
	static Point novascotiaNW = new Point(47.5f, -58);
	static Point novascotiaSE = new Point(45f, -55);
	static Point novascotiaPOI = new Point(46.75f, -58);
	// Cape Town
	static Point capetownNW = new Point(-33.5f, 17);
	static Point capetownSE = new Point(-35.5f, 19);
	static Point capetownPOI = new Point(-33.5f, 17);
	// Reunion
	static Point reunionNW = new Point(-20, 52);
	static Point reunionSE = new Point(-27, 57);
	static Point reunionPOI = new Point(-21.62f, 54.76f);
	// Guadeloupe
	static Point guadeloupeNW = new Point(17.27f, -62.9f);
	static Point guadeloupeSE = new Point(15.8f, -59.7f);
	static Point guadeloupePOI = new Point(16, -59.7f);
	// Calais
	static Point calaisNW = new Point(51.33f, 0.95f);
	static Point calaisSE = new Point(50.61f, 2.58f);
	// Red Sea
	static Point redseaNW = new Point(20, 38);
	static Point redseaSE = new Point(17, 42);
	static Point redseaPOI = new Point(20, 38.86f);
	// Lanzarote
	static Point lanzaroteNW = new Point(29.5f, -14);
	static Point lanzaroteSE = new Point(27, -12);
	static Point lanzarotePOI = new Point(29.4f, -12.5f);
	
	// Natal
	static Point natalNW = new Point(-5, -36);
	static Point natalSE = new Point(-8.5f, -31);
	static Point natalPOI = new Point(-8, -33.6f);
		
	public static Map<String, Box> boxes = new HashMap<String, Box>();
	
	static {
		boxes.put("GIBRALTAR", new Box(gibraltarNW, gibraltarSE, "GIBRALTAR", gibraltarPOI));
		boxes.put("FINISTERRE", new Box(finisterreNW, finisterreSE, "FINISTERRE"));
		boxes.put("NOVASCOTIA", new Box(novascotiaNW, novascotiaSE, "NOVASCOTIA", novascotiaPOI));
		boxes.put("SUEZ", new Box(suezNW, suezSE, "SUEZ"));
		boxes.put("CAPETOWN", new Box(capetownNW, capetownSE, "CAPETOWN", capetownPOI));
		boxes.put("REUNION", new Box(reunionNW, reunionSE, "REUNION", reunionPOI));
		boxes.put("WEST_ATLANTIC", new Box(westAtlanticNW, westAtlanticSE, "WEST_ATLANTIC"));
		boxes.put("RIO", new Box(rioNW, rioSE, "RIO"));
		boxes.put("GOA", new Box(goaNW, goaSE, "GOA", goaPOI));
		boxes.put("GUADELOUPE", new Box(guadeloupeNW, guadeloupeSE, "GUADELOUPE", guadeloupePOI));		
		String areaName = "CALAIS";
		boxes.put(areaName, new Box(calaisNW, calaisSE, areaName));		
		areaName = "REDSEA";
		boxes.put(areaName, new Box(redseaNW, redseaSE, areaName, redseaPOI));		
		areaName = "LANZAROTE";
		boxes.put(areaName, new Box(lanzaroteNW, lanzaroteSE, areaName, lanzarotePOI));		
		areaName = "NATAL";
		boxes.put(areaName, new Box(natalNW, natalSE, areaName, natalPOI));		
		areaName = "CHANNEL";
		boxes.put(areaName, new Box(channelNW, channelSE, areaName, channelPOI));		
	}
	
	public static Box getBox(String boxName) {
		return boxes.get(boxName);
	}
	
}
