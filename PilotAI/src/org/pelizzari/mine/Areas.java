package org.pelizzari.mine;

import org.pelizzari.gis.Box;
import org.pelizzari.gis.Point;

public class Areas {

	// Gibraltar
	static Point gibraltarNW = new Point(37, -10);
	static Point gibraltarSE = new Point(35, -5);
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
	// West Atlantic
	static Point westAtlanticNW = new Point(70, -77);
	static Point westAtlanticSE = new Point(-70, -70);

	
	public final static Box GIBRALTAR = new Box(gibraltarNW, gibraltarSE);
	public final static Box SUEZ = new Box(suezNW, suezSE);
	public final static Box WEST_ATLANTIC= new Box(westAtlanticNW, westAtlanticSE);
	
}
