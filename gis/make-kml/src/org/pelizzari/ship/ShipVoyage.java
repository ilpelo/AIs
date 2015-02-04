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

public class ShipVoyage {

	String mmsi;
	List<ShipPosition> posList;
	
	public ShipVoyage(String mmsi, 
					  Box arrivalArea,
					  String departureISODate, // YYYY-MM-DD
					  int maxVoyageDurationInDays) {
		this.mmsi = mmsi;
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
				pos = new ShipPosition(ts, lat, lon);
				posList.add(pos);					
			}
		} catch (SQLException e) {
			System.err.println("Cannot get voyage");
			e.printStackTrace();
			System.exit(-1);
		}
		boolean voyageCrossesArrivalArea = false;
		List<ShipPosition> voyageBetweenDepartureAndArrivalAreas = new ArrayList<ShipPosition>();
		
		Iterator<ShipPosition> posItr = posList.iterator();
		while(posItr.hasNext()) {
			ShipPosition pos = posItr.next();
			voyageBetweenDepartureAndArrivalAreas.add(pos);
			if(arrivalArea.isWithinBox(pos)) {
				voyageCrossesArrivalArea = true;
				break;
			}
		}
		if(voyageCrossesArrivalArea || true) {
			this.posList = voyageBetweenDepartureAndArrivalAreas;
		}
	}

	public List<ShipPosition> getPosList() {
		return posList;
	}

	public void setPosList(List<ShipPosition> posList) {
		this.posList = posList;
	}

	public String getMmsi() {
		return mmsi;
	}

	public void setMmsi(String mmsi) {
		this.mmsi = mmsi;
	}
}
