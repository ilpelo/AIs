package org.pelizzari.kml;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.pelizzari.ship.*;
import org.pelizzari.gis.*;

public class GenerateKML {

	static Box depBox, arrBox;
	final static int 
			VOYAGE_DURATION_IN_DAYS = 15, // 8 deg/day
			ANALYSIS_PERIOD_IN_DAYS = 5;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final String OUTPUT_FILE = "c:/master_data/PlaceMarkers.kml";
		// final String OUTPUT_FILE = "/master_data/PlaceMarkers.kml";

		try {
			// Class.forName("com.mysql.jdbc.Driver");
			// String url = "jdbc:mysql://localhost:3306/ai";
			// Connection con = DriverManager.getConnection(url, "root",
			// "mysql");

			KMLGenerator kmlGenerator = new KMLGenerator();

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

			
			depBox = new Box(gibraltarNW, gibraltarSE);
			//depBox = new Box(channelNW, channelSE);
			
			//arrBox = new Box(nyNW, nySE);
			//arrBox = new Box(rioNW, rioSE);
			//arrBox = new Box(saNW, saSE);
			//arrBox = new Box(copenhagenNW, copenhagenSE);
			arrBox = new Box(suezNW, suezSE);

			/*
			 * Point galiziaNW = new Point(45, -13); Point galiziaSE = new
			 * Point(42, -7); Box arrBox = new Box(galiziaNW, galiziaSE);
			 */

			kmlGenerator.addBox("Departure", depBox);
			kmlGenerator.addBox("Arrival", arrBox);

			String[] excludeMmsi = null;
			for (int i = 0; i < ANALYSIS_PERIOD_IN_DAYS; i++) {
				String departureDay = "2011-03-" + String.format("%02d", i+1);
				String[] mmsiList = addTracks(kmlGenerator, departureDay, excludeMmsi);
				// add mmsi to the exclude list
				if(mmsiList != null && mmsiList.length > 0) {
					if(excludeMmsi != null) {
						String[] newExcludeMmsi = Arrays.copyOf(excludeMmsi, 
																excludeMmsi.length + mmsiList.length);
						System.arraycopy(mmsiList, 0, newExcludeMmsi, excludeMmsi.length, mmsiList.length);
						excludeMmsi = newExcludeMmsi;
					} else {
						excludeMmsi = mmsiList;
					}
						
				}
			}

			Source src = new DOMSource(kmlGenerator.getDoc());

			Result dest = new StreamResult(new File(OUTPUT_FILE));
			TransformerFactory tranFactory = TransformerFactory.newInstance();
			Transformer aTransformer = tranFactory.newTransformer();
			aTransformer.transform(src, dest);

			System.out.println("Completed.....");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static String[] addTracks(KMLGenerator kmlGenerator,
									 String depDay,
									 String[] excludeMmsi) {
		List<ShipVoyage> voyages = Ship.findVoyages(depBox, arrBox, depDay,
				VOYAGE_DURATION_IN_DAYS, excludeMmsi);

		String[] mmsiList = null;
		if (voyages.size() == 0) {
			System.out.println("No voyages");
		} else {
			mmsiList = new String[voyages.size()];
			int i = 0;
			Iterator<ShipVoyage> itr = voyages.iterator();
			while (itr.hasNext()) {
				ShipVoyage voyage = itr.next();
				mmsiList[i++] = voyage.getMmsi();
				List<ShipPosition> positions = voyage.getPosList();
				if (positions != null) {
					Iterator<ShipPosition> posItr = positions.iterator();
					while (posItr.hasNext()) {
						ShipPosition pos = posItr.next();
						int ts = pos.getTs();
						Date date = new Date(ts * 1000);
						kmlGenerator.addPoint("targetStyle", "", 
								//date.toString(),
								pos.lat, pos.lon);
					}
					kmlGenerator.addLineString(voyage);
				}
			}
		}
		return mmsiList;
	}
}
