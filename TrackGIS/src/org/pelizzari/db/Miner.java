package org.pelizzari.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.pelizzari.gis.Box;
import org.pelizzari.gis.Point;
import org.pelizzari.ship.Ship;
import org.pelizzari.ship.ShipPosition;
import org.pelizzari.time.TimeInterval;
import org.pelizzari.time.Timestamp;

public class Miner {

	Connection con;
	
	public Miner() {
		con = DBConnection.getCon();		
	}
	
	public static String convertIntoCommaSeparatedMMSIList(List<Ship> ships) {		
		String mmsiList = "0";
		for (Ship ship : ships) {
			mmsiList = mmsiList + "," + ship.getMmsi();
		}
		
		return mmsiList;
	}

	public List<Ship> getShipsInBoxAndInterval(Box box, 
			   TimeInterval interval) {
		return getShipsInBoxAndInterval(box, interval, null, null);
	}

	
	public List<Ship> getShipsInBoxAndInterval(Box box, 
											   TimeInterval interval, 
											   List<Ship> includeShips,
											   List<Ship> excludeShips) {
		// get mmsi from DB
		String startISODatetime = interval.getStartTsISO();
		String endISODatetime = interval.getEndTsISO();
				
		final String GEO_COND = 
				"and lat >= "+box.getMinLat()+" "+
				"and lat <= "+box.getMaxLat()+" "+
				"and lon >= "+box.getMinLon()+" "+
				"and lon <= "+box.getMaxLon()+" ";

		final String PERIOD_COND = 
				"and date(from_unixtime(ts)) >= '"+startISODatetime+"' "+
				"and date(from_unixtime(ts)) <= '"+endISODatetime+"' ";

		final String INCLUDE_MMSI_COND =
				(includeShips == null) ?
				"" :
				"and mmsi in ("+convertIntoCommaSeparatedMMSIList(includeShips) +")";

		final String EXCLUDE_MMSI_COND =
				(excludeShips == null) ?
				"" :
				"and NOT mmsi in ("+convertIntoCommaSeparatedMMSIList(excludeShips) +")";

		final String SHIP_QUERY = 
				"SELECT distinct mmsi "+
				"FROM wpos "+
			    "WHERE 1=1 "+
				INCLUDE_MMSI_COND+
				EXCLUDE_MMSI_COND+
			    PERIOD_COND+
			    GEO_COND+
			    "order by mmsi";
		
		String shipQuery = SHIP_QUERY;
		System.out.println(shipQuery);
		
		List<Ship> listOfShips = new ArrayList<Ship>();

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(shipQuery);
			ShipPosition pos = null;
			while(rs.next()){
				String mmsi = rs.getString("mmsi");
				Ship ship = new Ship(mmsi);
				listOfShips.add(ship);					
			}
		} catch (SQLException e) {
			System.err.println("Cannot get ships");
			e.printStackTrace();
			System.exit(-1);
		}
		
		return listOfShips;
	}
	
	
	
	
	
}
