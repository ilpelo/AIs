package org.pelizzari.ship;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pelizzari.db.DBConnection;
import org.pelizzari.gis.Box;
import org.pelizzari.gis.Point;
import org.pelizzari.time.Timestamp;

public class ShipVoyage extends ShipTrack {

	String mmsi;
	
	static final boolean SHOW_ALL_VOYAGES = true;
	
	public ShipVoyage(String mmsi, 
					  Box arrivalArea,
					  String departureISODate, // YYYY-MM-DD
					  int maxVoyageDurationInDays) {
		super();
		this.mmsi = mmsi;
		
		// get positions from DB
		List<ShipPosition> posList = new ArrayList<ShipPosition>();
		Connection con = DBConnection.getCon();
		final String VOYAGE_PERIOD_COND = 
				"and date(from_unixtime(ts)) >= '"+departureISODate+"' "+
				"and date(from_unixtime(ts)) <= ('"+departureISODate+"' "+
				" + INTERVAL "+ maxVoyageDurationInDays +" DAY) ";

		final String TRACK_QUERY = 
				"SELECT mmsi, ts, date(from_unixtime(ts)) as ts_date, lat, lon "+
				"FROM wpos "+
			    "WHERE 1=1 "+
			    "and mmsi = "+mmsi+" "+			
				VOYAGE_PERIOD_COND+
			    "order by ts asc";
		String trackQuery = TRACK_QUERY;
		System.out.println(trackQuery);

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(trackQuery);
			ShipPosition pos = null;
			while(rs.next()){
				float lat = rs.getFloat("lat");
				float lon = rs.getFloat("lon");
				int ts = rs.getInt("ts");
				Point posPoint = new Point(lat, lon);				
				pos = new ShipPosition(posPoint, new Timestamp(ts));
				posList.add(pos);					
			}
		} catch (SQLException e) {
			System.err.println("Cannot get voyage");
			e.printStackTrace();
			System.exit(-1);
		}
		
		// now check if the voyage crosses the arrival area and truncate it
		boolean voyageCrossesArrivalArea = false;
		List<ShipPosition> voyageBetweenDepartureAndArrivalAreas = new ArrayList<ShipPosition>();
		
		Iterator<ShipPosition> posItr = posList.iterator();
		while(posItr.hasNext()) {
			ShipPosition pos = posItr.next();
			voyageBetweenDepartureAndArrivalAreas.add(pos);
			if(arrivalArea.isWithinBox(pos.point)) {
				voyageCrossesArrivalArea = true;
				break;
			}
		}
		if(voyageCrossesArrivalArea || SHOW_ALL_VOYAGES) { // if true, get any voyages
			setPosList(voyageBetweenDepartureAndArrivalAreas);
		}
	}

	public String getMmsi() {
		return mmsi;
	}

	public void setMmsi(String mmsi) {
		this.mmsi = mmsi;
	}
}
