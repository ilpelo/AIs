package org.pelizzari.ship;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pelizzari.gis.*;
import org.pelizzari.db.*;

public class Ship {

	public Ship() {
		// TODO Auto-generated constructor stub
	}
	
	public static List<ShipVoyage> findVoyages(Box departureArea,
											   Box arrivalArea,
											   String departureISODate, // YYYY-MM-DD
											   int maxVoyageDurationInDays) {
		
		List<ShipVoyage> voyages = new ArrayList<ShipVoyage>();
		
		final String DEPARTURE_PERIOD_COND = 
				"and date(from_unixtime(ts)) = '"+departureISODate+"' ";
		
		final String DEPARTURE_AREA_COND = 
			"and lat between "+departureArea.getMinLat()+" and "+departureArea.getMaxLat()+" "+
			"and lon between "+departureArea.getMinLon()+" and "+departureArea.getMaxLon()+" ";

		final String SHIP_DEPARTURE_QUERY = 
				"SELECT distinct mmsi "+
				"FROM wpos "+
			    "WHERE 1=1 "+
			    DEPARTURE_PERIOD_COND +
				DEPARTURE_AREA_COND;
		
		Connection con = null;
		try {
			con = new DBConnection().getCon();
		} catch (ClassNotFoundException | SQLException e) {
			System.err.println("Cannot make DB connection");
			e.printStackTrace();
			System.exit(-1);
		}		
		
		Statement stmt;
		ResultSet rs;
		try {
			stmt = con.createStatement();
			List<String> ships = new ArrayList<String>();
			String shipQuery = SHIP_DEPARTURE_QUERY;
			System.out.println(shipQuery);
			rs = stmt.executeQuery(shipQuery);
			while(rs.next()){
				String mmsi = rs.getString("mmsi");
				ships.add(mmsi);
				System.out.println(mmsi);
			}
			// get track of selected ships
			Iterator<String> itr = ships.iterator();
			while(itr.hasNext()) {
				String mmsi = itr.next();
				List<ShipPosition> posList = new ArrayList<ShipPosition>();
				voyages.add(new ShipVoyage(mmsi, posList));
			}
		} catch (SQLException e) {
			System.err.println("Cannot select ships");
			e.printStackTrace();
			System.exit(-1);
		}
		

		return voyages;
	}

}
