package org.pelizzari.db;

import java.awt.Color;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pelizzari.gis.Box;
import org.pelizzari.gis.Map;
import org.pelizzari.gis.Point;
import org.pelizzari.ship.Ship;
import org.pelizzari.ship.ShipPosition;
import org.pelizzari.ship.ShipPositionList;
import org.pelizzari.ship.ShipTrack;
import org.pelizzari.time.TimeInterval;
import org.pelizzari.time.Timestamp;

public class Miner {
	
	final static int MIN_SHIP_TRACK_SIZE = 5;

	Connection con;
	
	public Miner() {
		con = DBConnection.getCon();		
	}
	
	public static String convertIntoCommaSeparatedMMSIList(List<Ship> ships) {		
		String mmsiList = "-1";
		for (Ship ship : ships) {
			mmsiList = mmsiList + "," + ship.getMmsi();
		}
		
		return mmsiList;
	}

	public List<Ship> getShipsInIntervalAndBox(TimeInterval interval, Box box) {
		return getShipsInIntervalAndBox(interval, box, null, null, -1);
	}

	
	String getGeoSQLCondition(Box box) {
		final String GEO_COND = 
				"and lat >= "+box.getMinLat()+" "+
				"and lat <= "+box.getMaxLat()+" "+
				"and lon >= "+box.getMinLon()+" "+
				"and lon <= "+box.getMaxLon()+" ";
		return GEO_COND;
	}

	String getBoxNamesSQLCondition(Box depBox, Box arrBox) {
		final String BOX_NAME_COND = 
				"and dep = '"+depBox.getName()+"' "+		
				"and arr = '"+arrBox.getName()+"' ";		
		return BOX_NAME_COND;
	}
	
	String getPeriodSQLCondition(TimeInterval interval) {
		// get start and end TS of the interval
		String startISODatetime = interval.getStartTsISO();
		String endISODatetime = interval.getEndTsISO();

		final String PERIOD_COND = 
				"and date(from_unixtime(ts)) >= '"+startISODatetime+"' "+
				"and date(from_unixtime(ts)) <= '"+endISODatetime+"' ";	
		return PERIOD_COND;		
	}

	/*
	 * If include=true make "mmsi in (...)", "mmsi NOT in (...)" otherwise  
	 */
	String getMmsiSQLCondition(List<Ship> ships, boolean in) {
		String notStr = in?"":"NOT";
		final String INCLUDE_MMSI_COND =
				(ships == null || ships.isEmpty()) ?
				"" :
				"and "+notStr+" mmsi in ("+convertIntoCommaSeparatedMMSIList(ships) +") ";	
		return INCLUDE_MMSI_COND;
	}
	
	/**
	 * Get the list of ships present in the tracks table.
	 * @param yearPeriod
	 * @param depBox
	 * @param arrBox
	 * @return
	 */
	public List<Ship> getShipsWithTracks(String yearPeriod, Box depBox, Box arrBox, long insertTs) {

		final String SHIP_QUERY = 
				"select distinct mmsi " +
				"from tracks "+
				"where period = '" + yearPeriod + "' " +
				"and dep = '" + depBox.getName() + "' " +
				"and arr = '" + arrBox.getName() +  "' " +
				((insertTs == -1)?"":"and insert_ts = " + insertTs + " ") + 				
				"order by mmsi asc";

		String shipQuery = SHIP_QUERY;
		System.out.println(shipQuery);

		List<Ship> listOfShips = new ArrayList<Ship>();

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(shipQuery);
			while (rs.next()) {
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
	
	/**
	 * Get list of ships having at least one position in the box during the given time interval.
	 * @param interval
	 * @param box
	 * @param includeShips
	 * @param excludeShips
	 * @param limitShips
	 * @return
	 */
	public List<Ship> getShipsInIntervalAndBox(TimeInterval interval, 
											   Box box, 
											   List<Ship> includeShips,
											   List<Ship> excludeShips,
											   int limitShips) {
				
		final String GEO_COND = getGeoSQLCondition(box);

		final String PERIOD_COND = getPeriodSQLCondition(interval);

		final String INCLUDE_MMSI_COND = getMmsiSQLCondition(includeShips, true);		

		final String EXCLUDE_MMSI_COND = getMmsiSQLCondition(excludeShips, false);
		
		final String LIMIT_SHIPS = (limitShips > 0)?"limit "+limitShips+" ":"";		

		final String SHIP_QUERY = 
				"SELECT distinct mmsi "+
				"FROM wpos "+
			    "WHERE 1=1 "+
				INCLUDE_MMSI_COND+
				EXCLUDE_MMSI_COND+
			    PERIOD_COND+
			    GEO_COND+
			    LIMIT_SHIPS;
		
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

	/*
	 * Loads a merged track between 2 areas from the TRACKS table in the db
	 */
	public ShipPositionList getMergedShipTracksInPeriodAndBetweenBoxes(
			String yearPeriod, Box depBox, Box arrBox) {
		return getMergedShipTracksInPeriodAndBetweenBoxes(yearPeriod, depBox, arrBox, -1);
	}
	
	/*
	 * Loads a merged track between 2 areas from the TRACKS table in the db
	 */
	public ShipPositionList getMergedShipTracksInPeriodAndBetweenBoxes(
			String yearPeriod, Box depBox, Box arrBox, long insertTs) {
		Connection con = DBConnection.getCon();
		int readCount = 0;				
		final String FUSED_TRACK_SELECT = 
				"select norm_ts, lat, lon " +
				"from tracks " +
				"where period = '" + yearPeriod + "' " +
				"and dep = '" + depBox.getName() + "' " +
				"and arr = '" + arrBox.getName() + "' " + 
				((insertTs == -1)?"":"and insert_ts = " + insertTs + " ") + 
				"order by ts asc";
		System.out.println("Fused Track Query: " + FUSED_TRACK_SELECT);			
		
		ShipPositionList mergedTrack = new ShipPositionList();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(FUSED_TRACK_SELECT);
			ShipPosition pos = null;
			while(rs.next()) {
				float lat = rs.getFloat("lat");
				float lon = rs.getFloat("lon");
				int ts = rs.getInt("norm_ts"); // in sec
				Point posPoint = new Point(lat, lon);				
				pos = new ShipPosition(posPoint, new Timestamp((long)ts*1000));
				mergedTrack.addPosition(pos);
				readCount++;
			}
			System.out.println("Read " + readCount + " positions");			
		} catch (SQLException e) {
			System.err.println("Cannot get track positions from TRACKS table");
			e.printStackTrace();
			System.exit(-1);
		}
		return mergedTrack;
	}
	
	List<ShipPosition> getShipPositions(String posQuery) {
		List<ShipPosition> posList = new ArrayList<ShipPosition>();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(posQuery);
			ShipPosition pos = null;
			while(rs.next()){
				float lat = rs.getFloat("lat");
				float lon = rs.getFloat("lon");
				int ts = rs.getInt("ts");
				Point posPoint = new Point(lat, lon);				
				pos = new ShipPosition(posPoint, new Timestamp((long)ts*1000));
				posList.add(pos);					
			}
		} catch (SQLException e) {
			System.err.println("Cannot get ship positions");
			e.printStackTrace();
			System.exit(-1);
		}
		return posList;
	}
	
	
	/**
	 * Get ship positions from the wpos table
	 * @param interval
	 * @param box
	 * @param includeShips
	 * @param excludeShips
	 * @param limitPositions
	 * @return
	 */
	public List<ShipPosition> getShipPositionsInIntervalAndBox(
				   TimeInterval interval,  
				   Box box, 
				   List<Ship> includeShips,
				   List<Ship> excludeShips,
				   int limitPositions) {

		final String GEO_COND = (box == null)?" ":getGeoSQLCondition(box);
		
		final String PERIOD_COND = getPeriodSQLCondition(interval);
		
		final String INCLUDE_MMSI_COND = getMmsiSQLCondition(includeShips, true);		
		
		final String EXCLUDE_MMSI_COND = getMmsiSQLCondition(excludeShips, false);
		
		final String LIMIT_POS = (limitPositions > 0)?"limit "+limitPositions+" ":"";		

		final String SHIP_POSITION_QUERY = 
				"SELECT ts, date(from_unixtime(ts)) as ts_date, lat, lon "+
				"FROM wpos "+
			    "WHERE 1=1 "+
				INCLUDE_MMSI_COND+
				EXCLUDE_MMSI_COND+
				PERIOD_COND+
				GEO_COND+
				"order by ts asc "+
				LIMIT_POS;
		
		String posQuery = SHIP_POSITION_QUERY;
		System.out.println(posQuery);
		
		List<ShipPosition> posList = getShipPositions(SHIP_POSITION_QUERY);
		return posList;
	}
	
	
	public List<ShipTrack> getShipTracksFromTracksTable(
			String yearPeriod, 
			Box depBox,
			Box arrBox,
			long insertTs) {
		List<Ship> ships = getShipsWithTracks(yearPeriod, depBox, arrBox, insertTs);
		List<ShipTrack> tracks = new ArrayList<ShipTrack>();
		for (Ship ship : ships) {
			ShipTrack track = new ShipTrack();
			track.setMmsi(ship.getMmsi());
			List<Ship> oneShip = new ArrayList<Ship>();
			oneShip.add(ship);
			List<ShipPosition> posList = getShipPositionsFromTracksTable(
					depBox, arrBox, oneShip, null, -1, insertTs);
			track.setPosList(posList);
			tracks.add(track);
		}		
		return tracks;
	}
	
	public List<ShipPosition> getShipPositionsFromTracksTable(
			   Box depBox,
			   Box arrBox,
			   List<Ship> includeShips,
			   List<Ship> excludeShips,
			   int limitPositions,
			   long insertTs) {
	
	final String INCLUDE_MMSI_COND = getMmsiSQLCondition(includeShips, true);		
	
	final String EXCLUDE_MMSI_COND = getMmsiSQLCondition(excludeShips, false);
	
	final String BOX_NAMES_COND = getBoxNamesSQLCondition(depBox, arrBox);
	
	final String INSERT_TS_COND = ((insertTs == -1)?"":"and insert_ts = " + insertTs + " ");
	
	final String LIMIT_POS = (limitPositions > 0)?"limit "+limitPositions+" ":"";

	final String SHIP_POSITION_QUERY = 
			"SELECT ts, date(from_unixtime(ts)) as ts_date, lat, lon "+
			"FROM tracks "+
		    "WHERE 1=1 "+
			INCLUDE_MMSI_COND+
			EXCLUDE_MMSI_COND+
			BOX_NAMES_COND+
			INSERT_TS_COND+
			"order by ts asc "+ // this is important: it sorts all positions regardless of mmsi
								// tracks must have been speed normalized!
			LIMIT_POS;
	
	String posQuery = SHIP_POSITION_QUERY;
	System.out.println(posQuery);
	
	List<ShipPosition> posList = getShipPositions(SHIP_POSITION_QUERY);
	
	return posList;
}
	
	
	/*
	 * Return the track of a ship that goes from a departure area to an arrival area in a given time interval.
	 * Warning: if one of the boxes is null, return the full track
	 */
	public ShipTrack getShipTrackInIntervalAndBetweenBoxes(Ship ship,
														   TimeInterval interval,
														   Box departureBox,
														   Box arrivalBox) {
		// get positions from DB
		List<Ship> shipList = new ArrayList<Ship>();
		shipList.add(ship);
		List<ShipPosition> posList = getShipPositionsInIntervalAndBox(interval, null, shipList, null, -1);
				
		ShipTrack track = new ShipTrack();
		
		// if boxes are null, return full track
		if(departureBox == null || arrivalBox == null) {
			track.setPosList(posList);
			return track;
		}

		
		// now check if the voyage crosses the departure and arrival areas and truncate it
		boolean trackCrossedDepartureArea = false;
		boolean trackCrossedArrivalArea = false;
		List<ShipPosition> trackBetweenDepartureAndArrivalBoxes = new ArrayList<ShipPosition>();
		
		Iterator<ShipPosition> posItr = posList.iterator();
		boolean inDepartureArea = false;
		boolean recordTrack = false;
		while(posItr.hasNext()) {
			ShipPosition pos = posItr.next();
			boolean posInDepartureArea = departureBox.isWithinBox(pos.getPoint());
			if (posInDepartureArea) {
				if(!trackCrossedDepartureArea) { // pos in departure area for the first time, start recording the track
					trackCrossedDepartureArea = true;
					recordTrack = true;
				} else { // another pos in departure area
					if(!inDepartureArea) { // the ship came back to the departure area, ignore previous positions
						trackBetweenDepartureAndArrivalBoxes.clear();
					}
				}
			}
			if(recordTrack) {
				trackBetweenDepartureAndArrivalBoxes.add(pos);				
			}
			if(arrivalBox.isWithinBox(pos.getPoint())) {
				trackCrossedArrivalArea = true;
				break;
			}
			inDepartureArea = posInDepartureArea;
		}

		if(!trackCrossedDepartureArea) { // if false, ship does not cross the departure area, something is wrong!
			System.err.println("WARN: "+ship+" does not cross departure "+arrivalBox);
			return null;
		}
		
		if(!trackCrossedArrivalArea) { // if false, ship does not cross the arrival area, something is wrong!
			System.err.println("WARN: "+ship+" does not cross arrival "+arrivalBox);
			return null;
		}

		if(trackBetweenDepartureAndArrivalBoxes.size() < MIN_SHIP_TRACK_SIZE) { // discard if positions are too few
			System.err.println("WARN: "+ship+" track is too short, positions: "+trackBetweenDepartureAndArrivalBoxes.size());
			return null;
		}
				
		track.setPosList(trackBetweenDepartureAndArrivalBoxes);						

//		if(normalizeTime) {
//			track.timeNormalize();		
//		}

		return track;
	}
	
	
	/**
	 * Return the list of tracks of ships that go from a departure area to an arrival area in a given time interval.
	 * Warning: if one of the boxes is null, return the full track
	 * @param departureBox
	 * @param arrivalBox
	 * @param depInterval period of time in which the ship is in the dep. area (at least 1 position)
	 * @param voyageDurationInDays max duration of the voyage
	 * @param includeShips
	 * @param excludeShips
	 * @return
	 * @throws Exception
	 */
	public List<ShipTrack> getShipTracksInIntervalAndBetweenBoxes(
														 Box departureBox,
														 Box arrivalBox,
														 TimeInterval depInterval,
														 int voyageDurationInDays,
														 List<Ship> includeShips,
														 List<Ship> excludeShips,
														 int limitTracks
														 ) throws Exception {
		// get ships that were in the departureBox
		List<Ship> departingShips = getShipsInIntervalAndBox(depInterval, 
															 departureBox, 
															 includeShips,
															 excludeShips,
															 -1);
		
		if(departingShips.isEmpty()) {
			System.out.println("WARN: no departing ships found");
			return null;
		}
		
		// among the departing ships, get those that were in the arrivalBox
		TimeInterval analysisInterval = new TimeInterval(depInterval.getStartTs(), 
											voyageDurationInDays+depInterval.getDurationInDays());
		List<Ship> arrivingShips = getShipsInIntervalAndBox(analysisInterval, 
															arrivalBox, 
															departingShips, 
															null, 
															limitTracks);

		if(arrivingShips.isEmpty()) {
			System.out.println("WARN: no arriving ships found");
			return null;
		}

		System.out.println("Arriving: ");
		for (Ship ship : arrivingShips) {
			System.out.print(ship+" ");
			System.out.println("");
		}			
		
		List<ShipTrack> tracks = new ArrayList<ShipTrack>();
		for (Ship ship : arrivingShips) {
			ShipTrack track = getShipTrackInIntervalAndBetweenBoxes(ship, 
																	analysisInterval,
																	departureBox,
																	arrivalBox);
			if(track != null) {
				track.setMmsi(ship.getMmsi());
				tracks.add(track);
			}
		}
				
		return tracks;						
	}

	/**
	 * Return the list of tracks of ships that go from a departure area to an arrival area in a given time interval.
	 * Warning: if one of the boxes is null, return the full track
	 * @param departureBox
	 * @param arrivalBox
	 * @param depInterval period of time in which the ship is in the dep. area (at least 1 position)
	 * @param voyageDurationInDays max duration of the voyage
	 * @param includeShips
	 * @param excludeShips
	 * @return
	 * @throws Exception
	 */
	public List<ShipTrack> getShipTracksInIntervalAndCrossingBox(TimeInterval interval,
																 Box box,
																 int limitTracks
																 ) throws Exception {
		// get ships that were in the box
		List<Ship> ships = getShipsInIntervalAndBox(interval, 
													box, 
													null,
													null,
													limitTracks
													);
		
		if(ships.isEmpty()) {
			System.out.println("WARN: no ships found");
			return null;
		}
				
		List<ShipTrack> tracks = new ArrayList<ShipTrack>();
		for (Ship ship : ships) {
			ShipTrack track = getShipTrackInIntervalAndBetweenBoxes(ship, 
																	interval,
																	null,
																	null);
			if(track != null) {
				track.setMmsi(ship.getMmsi());
				tracks.add(track);
			}
		}
				
		return tracks;						
	}
	
	
}
