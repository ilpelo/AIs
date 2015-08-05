package org.pelizzari.mine;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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

	final static String START_DT = "2011-01-01 00:00:00";
	final static String YEAR_PERIOD = "WINTER";
	final static int START_PERIOD_IN_DAYS = 4;
	final static int VOYAGE_DURATION_IN_DAYS = 20;
	final static int ANALYSIS_PERIOD_IN_DAYS = 30;
	final static int MAX_SHIPS_TO_ANALYSE = 10;

	final static String OUTPUT_DIR = "c:/master_data/";
	// final String OUTPUT_DIR = "/master_data/";

	final static String OUTPUT_KML_FILE = OUTPUT_DIR+"tracks.kml";
	
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
				
		//// Departure
		//Box depBox = Areas.GIBRALTAR;
		Box depBox = Areas.WEST_ATLANTIC;
						
		/// Arrival
		//Box arrBox = Areas.SUEZ;
		//Box arrBox = Areas.WEST_ATLANTIC;
		//Box arrBox = Areas.RIO;
		Box arrBox = Areas.GIBRALTAR;
		
		/// Let's mine
		
		Miner miner = new Miner();

		List<ShipTrack> allTracks = new ArrayList<ShipTrack>();
		
		TimeInterval depInterval = new TimeInterval(new Timestamp(START_DT), START_PERIOD_IN_DAYS);
		
		List<Ship> seenShips = new ArrayList<Ship>();
		for (int i = 0; i < ANALYSIS_PERIOD_IN_DAYS/START_PERIOD_IN_DAYS; i++) {
			depInterval.shiftInterval(START_PERIOD_IN_DAYS);
			System.out.println(">>> Period: "+depInterval);
			List<ShipTrack> tracks = miner.getShipTracksInIntervalAndBetweenBoxes(
					depBox, arrBox, depInterval, VOYAGE_DURATION_IN_DAYS, null, seenShips, MAX_SHIPS_TO_ANALYSE);
			if(tracks != null) {
				for (ShipTrack track : tracks) {
					seenShips.add(new Ship(track.getMmsi()));
				}
				allTracks.addAll(tracks);
			}
		}
		
		// Make KML
		kmlGenerator.addBox("Departure", depBox);
		kmlGenerator.addBox("Arrival", arrBox);
		Map map = new Map();
		for (ShipTrack track : allTracks) {
			map.plotTrack(track, Color.GREEN, track.getMmsi());
			kmlGenerator.addTrack(track, track.getMmsi());
		}
//		map.setVisible(true);
		kmlGenerator.saveKMLFile(OUTPUT_KML_FILE);
		
		// Save track files and to DB
		for (ShipTrack track : allTracks) {
			FileWriter fw = new FileWriter(OUTPUT_DIR+"pos_"+track.getMmsi()+".csv");
			track.saveTrack(fw);
			fw.close();
			//
			// Normalize tracks (use compute segments)!!!
			track.saveTrackToDB(depBox, arrBox, YEAR_PERIOD);			
		}		
		System.out.println("Done\n");
	}

}
