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

public class ShowShipTracksCrossingBox {

	final static String START_DT = "2011-03-01 00:00:00";
	final static int ANALYSIS_PERIOD_IN_DAYS = 10;
	final static int SHIPS_LIMIT = 20;
	final static Box BOX = Areas.getBox("GIBRALTAR");
	

	final static String OUTPUT_FILE = "c:/master_data/ShipTracks";
	
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
			
		
		Timestamp startTS = null;
		TimeInterval analysisInterval = null;
		try {
			startTS = new Timestamp(START_DT);
			analysisInterval = new TimeInterval(startTS, ANALYSIS_PERIOD_IN_DAYS);
		} catch (Exception e) {
			System.err.println("error parsing times");
			e.printStackTrace();
		}
		
		/// Let's mine
		
		Miner miner = new Miner();
		
		List<ShipTrack> tracks = miner.getShipTracksInIntervalAndCrossingBox(
				analysisInterval, BOX, SHIPS_LIMIT);		
		
		for (ShipTrack track : tracks) {
			kmlGenerator.addTrack(track, null);			
		}
		
		kmlGenerator.saveKMLFile(OUTPUT_FILE+".kml");
		
		System.out.println("Done\n");
		
	}

}
