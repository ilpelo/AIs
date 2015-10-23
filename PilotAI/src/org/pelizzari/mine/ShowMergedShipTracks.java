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
import org.pelizzari.ship.ShipPosition;
import org.pelizzari.ship.ShipPositionList;
import org.pelizzari.ship.ShipTrack;
import org.pelizzari.time.TimeInterval;
import org.pelizzari.time.Timestamp;


/**
 * Show on the map the merged positions of several ship tracks stored in the "tracks" table.
 * Result is saved in c:/master_data/MergedShipTracks.kml
 * @author andrea@pelizzari.org
 *
 */
public class ShowMergedShipTracks {

	static final String YEAR_PERIOD = "SPRING";
	static final Box DEPARTURE_AREA = Areas.getBox("LANZAROTE"); 
	static final Box ARRIVAL_AREA = Areas.getBox("NATAL");

//	static final String YEAR_PERIOD = "WINTER";
//	static final Box DEPARTURE_AREA = Areas.getBox("CAPETOWN"); 
//	static final Box ARRIVAL_AREA = Areas.getBox("REUNION");

//	static final String YEAR_PERIOD = "WINTER";
//	static final Box DEPARTURE_AREA = Areas.getBox("REDSEA"); 
//	static final Box ARRIVAL_AREA = Areas.getBox("GOA");

	static final long INSERT_TS = 1444915221;
	
	final static String OUTPUT_FILE = "c:/master_data/MergedShipTracks";
	final static boolean WITH_TRACKS = true;
	final static boolean WITH_DATES = false;
	
	
	public static void main(String[] args) {

		KMLGenerator kmlGenerator = null;
		try {
			kmlGenerator = new KMLGenerator();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		kmlGenerator.addIconStyle("poiStyle",
				"http://maps.google.com/mapfiles/kml/shapes/target.png");
				//"http://maps.google.com/mapfiles/kml/shapes/placemark_circle.png");
				//"http://maps.google.com/mapfiles/kml/shapes/star.png");
		final int HUE_LEVELS = 20;
		kmlGenerator.addColoredStyles("pointStyle", 
				"http://maps.google.com/mapfiles/kml/shapes/placemark_circle.png",
				HUE_LEVELS, false);
		kmlGenerator.addWaypointStyle("waypointStyle");

		
		kmlGenerator.addBox(DEPARTURE_AREA);
		kmlGenerator.addPoint("poiStyle",  
				  "DEP POI",
				  DEPARTURE_AREA.getPoi().lat, 
				  DEPARTURE_AREA.getPoi().lon);

		kmlGenerator.addBox(ARRIVAL_AREA);
		kmlGenerator.addPoint("poiStyle",  
				  "ARR POI",
				  ARRIVAL_AREA.getPoi().lat, 
				  ARRIVAL_AREA.getPoi().lon);
		
		Miner miner = new Miner();
		ShipPositionList posList = miner.getMergedShipTracksInPeriodAndBetweenBoxes(
				YEAR_PERIOD, DEPARTURE_AREA, ARRIVAL_AREA, INSERT_TS); 
		
		long firstTSInMillis = posList.getFirstPosition().getTs().getTsMillisec();
		long voyageDurationInMillis = posList.getLastPosition().getTs().getTsMillisec()-firstTSInMillis;
		for (ShipPosition pos : posList.getPosList()) {
			long elapsedTime = pos.getTs().getTsMillisec()-firstTSInMillis;
			String style = "pointStyle"+(int)(elapsedTime*1.0f/voyageDurationInMillis*HUE_LEVELS);
			kmlGenerator.addPoint(style,  
								  "", //+pos.getTs(),
								  pos.getPoint().lat, 
								  pos.getPoint().lon);
		}
		
		if(WITH_TRACKS) {
			List<ShipTrack> tracks = miner.getShipTracksFromTracksTable(
					YEAR_PERIOD, DEPARTURE_AREA, ARRIVAL_AREA);
			for (ShipTrack track : tracks) {
				// add tracks with dates
				kmlGenerator.addTrack(track, null, WITH_DATES);
			}
		}

		kmlGenerator.saveKMLFile(OUTPUT_FILE+".kml");

		System.out.println("Done");
		System.out.println(OUTPUT_FILE+".kml\n");
		
	}

}
