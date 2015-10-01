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

import org.pelizzari.ai.DisplacementSequenceProblem;
import org.pelizzari.db.Miner;
import org.pelizzari.gis.Box;
import org.pelizzari.gis.Map;
import org.pelizzari.gis.Point;
import org.pelizzari.kml.KMLGenerator;
import org.pelizzari.ship.Ship;
import org.pelizzari.ship.ShipTrack;
import org.pelizzari.time.TimeInterval;
import org.pelizzari.time.Timestamp;

/**
 * Extract tracks of ships between two areas.
 * @author andrea@pelizzari.org
 *
 */
public class MineVoyages {

	final static String START_DT = "2011-06-01 00:00:00";
	final static int START_PERIOD_IN_DAYS = 4;
	final static int VOYAGE_DURATION_IN_DAYS = 15;
	final static int ANALYSIS_PERIOD_IN_DAYS = 30;
	final static int MAX_SHIPS_TO_ANALYSE = 50;
	final static int MAX_RATE_IN_SECONDS = 60; // max 1 position every 1 minute

	final static String YEAR_PERIOD = "SUMMER";
	
	//// Departure
	//final static Box DEP_BOX = Areas.CAPETOWN;
	final static Box DEP_BOX = Areas.GIBRALTAR;
	//Box depBox = Areas.WEST_ATLANTIC;
					
	/// Arrival
	//Box arrBox = Areas.FINISTERRE;
	//Box arrBox = Areas.SUEZ;
	//Box arrBox = Areas.WEST_ATLANTIC;
	//final static Box ARR_BOX = Areas.REUNION;
	//final static Box ARR_BOX = Areas.GOA;
	//Box arrBox = Areas.NOVASCOTIA;
	//final static Box ARR_BOX = Areas.WEST_ATLANTIC;
	final static Box ARR_BOX = Areas.GUADELOUPE;
	//final static Box ARR_BOX = Areas.RIO;

	final static String OUTPUT_DIR = "c:/master_data/";
	// final String OUTPUT_DIR = "/master_data/";

	final static boolean KML_FILE_WITH_DATES = false;
	final static String OUTPUT_KML_FILE = OUTPUT_DIR+"tracks.kml";
	final static String REFERENCE_START_DT = "2000-01-01 00:00:00"; // reference start date of all tracks
	
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
				
		
		/// Let's mine
		
		Miner miner = new Miner();

		List<ShipTrack> allTracks = new ArrayList<ShipTrack>();
		
		TimeInterval depInterval = new TimeInterval(new Timestamp(START_DT), START_PERIOD_IN_DAYS);
		
		List<Ship> seenShips = new ArrayList<Ship>();
		for (int i = 0; i < ANALYSIS_PERIOD_IN_DAYS/START_PERIOD_IN_DAYS; i++) {
			System.out.println(">>> Period: "+depInterval);
			List<ShipTrack> tracks = miner.getShipTracksInIntervalAndBetweenBoxes(
					DEP_BOX, ARR_BOX, depInterval, VOYAGE_DURATION_IN_DAYS, null, seenShips, MAX_SHIPS_TO_ANALYSE);
			if(tracks != null) {
				for (ShipTrack track : tracks) {
					seenShips.add(new Ship(track.getMmsi()));
				}
				allTracks.addAll(tracks);
				if(allTracks.size() >= MAX_SHIPS_TO_ANALYSE) {
					break;
				}
			}
			depInterval.shiftInterval(START_PERIOD_IN_DAYS);
		}
		
		// reduce position density
		System.out.println(">>> Reducing position density ");
		for (ShipTrack track : allTracks) {
			int nPosBefore = track.getPosList().size();
			track.reducePositionDensity(MAX_RATE_IN_SECONDS);
			int nPosAfter = track.getPosList().size();
			System.out.println(track.getMmsi() + ", positions: " + nPosBefore + " > " + nPosAfter);		
		}		
		
		// Make KML
		kmlGenerator.addBox("Departure", DEP_BOX);
		kmlGenerator.addBox("Arrival", ARR_BOX);
		Map map = new Map();
		for (ShipTrack track : allTracks) {
			map.plotTrack(track, Color.GREEN, track.getMmsi());
			kmlGenerator.addTrack(track, track.getMmsi(), KML_FILE_WITH_DATES);
		}
//		map.setVisible(true);
				
		// compute average length to be used to normalize the tracks
		// Save track files and to DB
//		float avgLength = 0;
//		for (ShipTrack track : allTracks) {
//			avgLength += track.computeLengthInMiles();			
//		}
//		avgLength = avgLength / allTracks.size();
//		System.out.println(">>> Average length: "+avgLength);
		
		// Save to KML
		System.out.println(">>> Saving to KML: "+OUTPUT_KML_FILE);
		kmlGenerator.saveKMLFile(OUTPUT_KML_FILE);

		
		// Save track files and to DB
		for (ShipTrack track : allTracks) {
			String fileName = OUTPUT_DIR+"pos_"+track.getMmsi()+".csv";
			System.out.println(">>> Saving to CSV: "+fileName);
			FileWriter fw = new FileWriter(fileName);
			track.saveTrack(fw);
			fw.close();
			//
			// Normalize tracks (use compute segments to overwrite timestamps)!!!
			track.computeTrackSegmentsAndNormalizeTime(new Timestamp(REFERENCE_START_DT), ShipTrack.REFERENCE_SPEED_IN_KNOTS);
			track.saveTrackToDB(DEP_BOX, ARR_BOX, YEAR_PERIOD);			
		}
		System.out.println("Done\n");
	}
}
