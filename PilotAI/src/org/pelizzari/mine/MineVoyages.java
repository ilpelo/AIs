package org.pelizzari.mine;

import java.applet.AppletStub;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

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

	// consider this period to detect ships with a position in the departure area
	final static int START_PERIOD_IN_DAYS = 4;
	final static int MAX_RATE_IN_SECONDS = 600; // max 1 position every 10 minutes
	
	static String START_DT; // = "2011-01-01 00:00:00";
	static int VOYAGE_DURATION_IN_DAYS; // = 15;
	static int ANALYSIS_PERIOD_IN_DAYS; // = 10;
	static int MAX_SHIPS_TO_ANALYSE; // = 50;
	static String YEAR_PERIOD; // = "WINTER";
	static String[] EXCLUDE_MMSI_LIST; // = {};
	
	// GIB-Guadalupe SUMMER 2011-06-01 2 months
	//static final String[] EXCLUDE_MMSI_LIST = {"247456000", "247601000", "247585000", "636090262", 
	//	"235051085", "235010170", "375443000", "235054581"};
	
	//// Departure
	static Box DEP_BOX; // = Areas.CAPETOWN;
	static Box ARR_BOX; // = Areas.REUNION;
	final static String OUTPUT_DIR = "c:/master_data/";
	static String OUTPUT_KML_FILE; // = OUTPUT_DIR+"tracks.kml";
		
	final static boolean KML_FILE_WITH_DATES = false;
	public final static String REFERENCE_START_DT = "2000-01-01 00:00:00"; // reference start date of all tracks
	public final static int REFERENCE_VOYAGE_DURATION_IN_DAYS = 1;
	
	static void loadProps(String absolutePathToPropFile) {
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream(absolutePathToPropFile);

			prop.load(input);
			START_DT = prop.getProperty("start_dt") + " 00:00:00";
			VOYAGE_DURATION_IN_DAYS = Integer.parseInt(prop.getProperty("voyage_duration_in_days"));
			ANALYSIS_PERIOD_IN_DAYS = Integer.parseInt(prop.getProperty("analysis_period_in_days"));
			MAX_SHIPS_TO_ANALYSE = Integer.parseInt(prop.getProperty("max_ships_to_analyse"));
			YEAR_PERIOD = prop.getProperty("year_period");
			EXCLUDE_MMSI_LIST = prop.getProperty("exclude_mmsi_list").split(",");
			DEP_BOX = getBox(prop, "dep_box");
			ARR_BOX = getBox(prop, "arr_box");
		} catch (IOException ex) {
			System.err.println("Cannot read properties: "+absolutePathToPropFile);
			ex.printStackTrace();			
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		OUTPUT_KML_FILE = OUTPUT_DIR + DEP_BOX.getName() + "_" + ARR_BOX.getName() + "_" +
				YEAR_PERIOD + "_tracks.kml";
	}
	
	static Box getBox(Properties prop, String boxParam) {		
//		// lat1,lon1,lat2,lon2 : ne (lat, lon), sw (lat, lon)
//		String[] boxCoordStr = prop.getProperty(boxParam).split(",");
//		float[] boxCoord = new float[boxCoordStr.length];
//		for (int i = 0; i < boxCoordStr.length; i++) {
//			boxCoord[i] = Float.parseFloat(boxCoordStr[i]);			
//		}
//		Box box = new Box(new Point(boxCoord[0], boxCoord[1]),
//						  new Point(boxCoord[2], boxCoord[3]));
		Box box = Areas.getBox(boxParam);
		return box;
	}
	
	
	public static void main(String[] args) throws Exception {

		if(args.length != 1) {
			System.err.println("Usage: prog properties_file");
			System.exit(-1);
		}
		String absPathPropFile = args[0];
		loadProps(absPathPropFile);
		
		Date startDate = new java.util.Date();
		
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
		// add the list of ships to be excluded to the seenShip arraylist in order to filter them out
		for (String mmsi : EXCLUDE_MMSI_LIST) {
			seenShips.add(new Ship(mmsi));
		}		
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
		kmlGenerator.addBox(DEP_BOX);
		kmlGenerator.addBox(ARR_BOX);
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
			// Normalize tracks (overwrite timestamps)!!!
			track.computeTrackSegmentsAndNormalizeTimestamps(new Timestamp(REFERENCE_START_DT), REFERENCE_VOYAGE_DURATION_IN_DAYS);
			track.saveTrackToDB(DEP_BOX, ARR_BOX, YEAR_PERIOD, startDate.getTime()/1000);			
		}
		Date endDate = new Date();
		long duration  = endDate.getTime() - startDate.getTime();
		long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
		System.out.println("Done, duration (min): "+diffInMinutes);
		
		System.exit(0);
	}
}
