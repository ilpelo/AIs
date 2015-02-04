package org.pelizzari.kml;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
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

	/**
	 * @param args
	 */
	public static void main(String[]args ){
		Statement stmt;
		ResultSet rs;
		
		final String FIRST_DEPARTURE_DAY = "2011-03-17 ";
		final int VOYAGE_DURATION_IN_DAYS = 10; // 8 deg/day
		
//		final String BEFORE_DEPARTURE_PERIOD_COND = 
//				"and date(from_unixtime(ts)) <= "+FIRST_DEPARTURE_DAY;
//
//		final String AFTER_ARRIVAL_PERIOD_COND = 
//				"and date(from_unixtime(ts)) >= ("+LAST_DEPARTURE_DAY+
//				" + INTERVAL "+ VOYAGE_DURATION_IN_DAYS +" DAY) ";

//		final String DEPARTURE_PERIOD_COND = 
//				"and date(from_unixtime(ts)) >= "+FIRST_DEPARTURE_DAY+
//				"and date(from_unixtime(ts)) <= "+LAST_DEPARTURE_DAY;
//
//		final String ARRIVAL_PERIOD_COND = 
//				"and date(from_unixtime(ts)) >= ("+FIRST_DEPARTURE_DAY+
//				" + INTERVAL "+ VOYAGE_DURATION_IN_DAYS +" DAY) "+
//				"and date(from_unixtime(ts)) <= ("+LAST_DEPARTURE_DAY+
//				" + INTERVAL "+ VOYAGE_DURATION_IN_DAYS +" DAY) ";
//		
//		final String VOYAGE_PERIOD_COND = 
//				"and date(from_unixtime(ts)) >= "+FIRST_DEPARTURE_DAY+
//				"and date(from_unixtime(ts)) <= ("+LAST_DEPARTURE_DAY+
//				" + INTERVAL "+ VOYAGE_DURATION_IN_DAYS +" DAY) ";		
//
//		final String GIBRALTAR_COND = 
//				"and lat between 30 and 40 "+
//				"and lon between -15 and -5 ";
//		
//		final String NEWYORK_COND = 
//				"and lat between 40 and 41 "+
//				"and lon between -80 and -70 ";
//		
//		// select ships with more positions in area/period
//		final String SHIP_COUNT_POS_QUERY = 
//				"SELECT mmsi, count(*) "+
//				"FROM wpos "+
//			    "WHERE 1=1 "+
//			    DEPARTURE_PERIOD_COND +
//				GIBRALTAR_COND +
//				//NEWYORK_COND +
//				"group by mmsi order by 2 desc limit 10";
//
//		final String SHIP_DEPARTURE_QUERY = 
//				"SELECT distinct mmsi "+
//				"FROM wpos "+
//			    "WHERE 1=1 "+
//			    DEPARTURE_PERIOD_COND +
//				GIBRALTAR_COND;
//		
//		final String SHIP_ARRIVAL_QUERY = 
//				"SELECT distinct mmsi "+
//				"FROM wpos "+
//			    "WHERE 1=1 "+
//			    NEWYORK_COND+
//			    ARRIVAL_PERIOD_COND;			    
//
//		// select ships that made the specific voyage
//		final String SHIP_VOYAGE_QUERY = 
//				"SELECT distinct mmsi "+
//				"FROM wpos "+
//			    "WHERE 1=1 "+
//			    NEWYORK_COND+
//			    ARRIVAL_PERIOD_COND+
//			    "and mmsi in ("+
//			    	SHIP_DEPARTURE_QUERY+
//					")";
//		
//		final String TRACK_QUERY_SELECT_FROM_WHERE = 
//				"SELECT mmsi, ts, date(from_unixtime(ts)) as ts_date, lat, lon "+
//				"FROM wpos "+
//			    "WHERE 1=1 "+
//			    VOYAGE_PERIOD_COND+ // select position in the reference period
//			    "and NOT ( 1=1 "+   // but not after arrival
//			    		   ARRIVAL_PERIOD_COND+
//			    		   "and NOT ( 1=1 "+
//			    		              NEWYORK_COND+
//			    		            ") "+
//			    		 ") "+
//			    "and NOT ( 1=1 "+    // and not before departure
//			    		   DEPARTURE_PERIOD_COND+
//			    		   "and NOT ( 1=1 "+
//			    		              GIBRALTAR_COND+
//			    		            ") "+
//			    		 ") ";
//		
//		final String TRACK_QUERY_ORDER_LIMIT = 
//				" order by ts desc limit 1000";

		final String OUTPUT_FILE = "c:/master_data/PlaceMarkers.kml";
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://localhost:3306/ai";
			Connection con = DriverManager.getConnection(url, "root", "mysql");
			
			KMLGenerator kmlGenerator = new KMLGenerator();
			
			kmlGenerator.addIconStyle("targetStyle", "http://maps.google.com/mapfiles/kml/shapes/target.png");
			
			Point gibraltarNW = new Point(40, -15); // 40
			Point gibraltarSE = new Point(35, -5); // 30
			Box depBox = new Box(gibraltarNW, gibraltarSE);
			Point nyNW = new Point(44, -77);
			Point nySE = new Point(40, -70);
			Box arrBox = new Box(nyNW, nySE);
/*			Point galiziaNW = new Point(45, -13);
			Point galiziaSE = new Point(42, -7);
			Box arrBox = new Box(galiziaNW, galiziaSE);*/
			kmlGenerator.addBox("Departure", depBox);
			kmlGenerator.addBox("Arrival", arrBox);
			
			List<ShipVoyage> voyages = Ship.findVoyages(depBox, arrBox, 
							FIRST_DEPARTURE_DAY, VOYAGE_DURATION_IN_DAYS);
			
			if(voyages.size() == 0) {
				System.out.println("No voyages");
			} else {
				Iterator<ShipVoyage> itr = voyages.iterator();
				while(itr.hasNext()) {
					ShipVoyage voyage = itr.next();
					List<ShipPosition> positions = voyage.getPosList();
					if(positions != null) {
						Iterator<ShipPosition> posItr = positions.iterator();
						while(posItr.hasNext()) {
							ShipPosition pos = posItr.next();
							int ts = pos.getTs();
							Date date = new Date(ts * 1000);
							kmlGenerator.addPoint("targetStyle", date.toString(), pos.lat, pos.lon);					
						}
						kmlGenerator.addLineString(voyage);
					}
				}
			}

			Source src = new DOMSource(kmlGenerator.getDoc());
			
			Result dest = new StreamResult(new File(OUTPUT_FILE)); 
			TransformerFactory tranFactory = TransformerFactory.newInstance(); 
		    Transformer aTransformer = tranFactory.newTransformer(); 
			aTransformer.transform(src, dest);
			
			System.out.println("Completed.....");
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
	}
}
