package org.pelizzari.mine;

import java.awt.Color;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.pelizzari.db.Miner;
import org.pelizzari.gis.Box;
import org.pelizzari.gis.Map;
import org.pelizzari.gis.Point;
import org.pelizzari.kml.KMLGenerator;
import org.pelizzari.ship.Ship;
import org.pelizzari.ship.ShipTrack;
import org.pelizzari.time.TimeInterval;
import org.pelizzari.time.Timestamp;

public class MineVoyages {

	final static String START_DT = "2011-03-01 00:00:00";
	final static int START_PERIOD_IN_DAYS = 4;
	final static int VOYAGE_DURATION_IN_DAYS = 15;
	final static int ANALYSIS_PERIOD_IN_DAYS = 4;

	final static String OUTPUT_FILE = "c:/master_data/PlaceMarkers.kml";
	// final String OUTPUT_FILE = "/master_data/PlaceMarkers.kml";

	
	public static void main(String[] args) throws Exception {

		KMLGenerator kmlGenerator = null;
		try {
			kmlGenerator = new KMLGenerator();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		kmlGenerator.addIconStyle("targetStyle",
				//"http://maps.google.com/mapfiles/kml/shapes/target.png");
				"http://maps.google.com/mapfiles/kml/shapes/placemark_circle.png");
		
		// Gibraltar
		Point gibraltarNW = new Point(37, -10);
		Point gibraltarSE = new Point(35, -5);
		// Copenhagen
		Point copenhagenNW = new Point(56, 12);
		Point copenhagenSE = new Point(55, 14);
		// Channel
		Point channelNW = new Point(51, 1);
		Point channelSE = new Point(50.5f, 2.5f);
		// New York
		Point nyNW = new Point(44, -77);
		Point nySE = new Point(40, -70);
		// Rio de Janeiro
		Point rioNW = new Point(-21, -46);
		Point rioSE = new Point(-26, -40);
		// South Africa
		Point saNW = new Point(-32, 17);
		Point saSE = new Point(-36, 20);
		// Suez
		Point suezNW = new Point(32, 31.5f);
		Point suezSE = new Point(29.5f, 33);
		// Golf of Aden
		Point goaNW = new Point(18, 50);
		Point goaSE = new Point(9, 51);
		

		//// Departure
		Box depBox = null;
		
		depBox = new Box(gibraltarNW, gibraltarSE);
		//depBox = new Box(channelNW, channelSE);
		//depBox = new Box(suezNW, suezSE);	
						
		/// Arrival
		Box arrBox = null;
		
		//arrBox = new Box(nyNW, nySE);
		//arrBox = new Box(rioNW, rioSE);
		//arrBox = new Box(saNW, saSE);
		//arrBox = new Box(copenhagenNW, copenhagenSE);
		//arrBox = new Box(goaNW, goaSE);
		//arrBox = new Box(gibraltarNW, gibraltarSE);
		arrBox = new Box(suezNW, suezSE);
				
		
		/// Let's mine
		
		Miner miner = new Miner();

		List<ShipTrack> allTracks = new ArrayList<ShipTrack>();
		
		TimeInterval depInterval = new TimeInterval(new Timestamp(START_DT), START_PERIOD_IN_DAYS);

		for (int i = 0; i < ANALYSIS_PERIOD_IN_DAYS/START_PERIOD_IN_DAYS; i++) {
			depInterval.shiftInterval(i*START_PERIOD_IN_DAYS);
			System.out.println(">>> Period: "+depInterval);
			List<ShipTrack> tracks = miner.getShipTracksInIntervalAndBetweenBoxes(
					depBox, arrBox, depInterval, VOYAGE_DURATION_IN_DAYS, null, null, 100);
			if(tracks != null) {
				allTracks.addAll(tracks);			
			}
		}
		
		
		kmlGenerator.addBox("Departure", depBox);
		kmlGenerator.addBox("Arrival", arrBox);
		Map map = new Map();
		for (ShipTrack track : allTracks) {
			map.plotTrack(track, Color.GREEN, track.getMmsi());
			kmlGenerator.addTrack(track, track.getMmsi());
		}
//		map.setVisible(true);
		
		kmlGenerator.saveKMLFile(OUTPUT_FILE);
		
		System.out.println("Done\n");
		
	}

}
