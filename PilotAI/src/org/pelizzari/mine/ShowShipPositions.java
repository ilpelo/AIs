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
import org.pelizzari.ship.ShipTrack;
import org.pelizzari.time.TimeInterval;
import org.pelizzari.time.Timestamp;


/**
 * Show on the map the positions in a given time range.
 * Result is saved in c:/master_data/ShipPos.kml
 * @author andrea@pelizzari.org
 *
 */
public class ShowShipPositions {

	final static String START_DT = "2011-03-01 00:00:00";
	final static int ANALYSIS_PERIOD_IN_DAYS = 3;

	final static String OUTPUT_FILE = "c:/master_data/ShipPos";
	
	public static void main(String[] args) {

		KMLGenerator kmlGenerator = null;
		try {
			kmlGenerator = new KMLGenerator();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		kmlGenerator.addIconStyle("targetStyle",
				//"http://maps.google.com/mapfiles/kml/shapes/target.png");
				//"http://maps.google.com/mapfiles/kml/shapes/placemark_circle.png");
				"http://maps.google.com/mapfiles/kml/shapes/star.png");
	
		
		Timestamp startTS = null;
		TimeInterval analysisInterval = null;
		try {
			startTS = new Timestamp(START_DT);
			analysisInterval = new TimeInterval(startTS, ANALYSIS_PERIOD_IN_DAYS);
		} catch (Exception e) {
			System.err.println("error parsing times");
			e.printStackTrace();
		}
		
		Box box = new Box(new Point(70, -60), new Point(-70, 80));
		
		/// Let's mine
		
		Miner miner = new Miner();
		
		List<ShipPosition> posList = miner.getShipPositionsInIntervalAndBox(
												analysisInterval,
												box,
												null,
												null,
												100000);
		
		for (ShipPosition pos : posList) {
			kmlGenerator.addPoint("targetStyle",  
								  "",
								  pos.getPoint().lat, 
								  pos.getPoint().lon);
		}
		kmlGenerator.saveKMLFile(OUTPUT_FILE+".kml");
		
		System.out.println("Done\n");
		
	}

}
