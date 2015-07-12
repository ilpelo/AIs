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
import org.pelizzari.ship.ShipTrack;
import org.pelizzari.time.TimeInterval;
import org.pelizzari.time.Timestamp;

public class Miner {
	
	final static int MIN_SHIP_TRACK_SIZE = 5;
	final static int MAX_SHIPS_TO_ANALYSE = 5;

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
	 * If normalizeTime, set TS of first position to 00:00 and TS of last position to 24:00
	 */
	public ShipTrack getShipTrackInIntervalAndBetweenBoxes(Ship ship,
														   TimeInterval interval,
														   Box departureBox,
														   Box arrivalBox,
														   boolean normalizeTime) {
		// get positions from DB
		final String PERIOD_COND = getPeriodSQLCondition(interval);
		final String MMSI_COND = "and mmsi = "+ship.getMmsi()+" ";		
		
		final String SHIP_POSITION_QUERY = 
				"SELECT ts, date(from_unixtime(ts)) as ts_date, lat, lon "+
				"FROM wpos "+
			    "WHERE 1=1 "+
				MMSI_COND+
				PERIOD_COND+
			    "order by ts asc ";
	
		String posQuery = SHIP_POSITION_QUERY;
		System.out.println(posQuery);

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
			System.err.println("Cannot get ship track");
			e.printStackTrace();
			System.exit(-1);
		}
		
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
			System.err.println("ERROR: "+ship+" does not cross departure "+arrivalBox);
			return null;
		}
		
		if(!trackCrossedArrivalArea) { // if false, ship does not cross the arrival area, something is wrong!
			System.err.println("ERROR: "+ship+" does not cross arrival "+arrivalBox);
			return null;
		}

		if(trackBetweenDepartureAndArrivalBoxes.size() < MIN_SHIP_TRACK_SIZE) { // discard if positions are too few
			System.err.println("WARN: "+ship+" track is too short, positions: "+trackBetweenDepartureAndArrivalBoxes.size());
			return null;
		}
				
		track.setPosList(trackBetweenDepartureAndArrivalBoxes);						
		if(normalizeTime) {
			long firstTSMillisec = track.getFirstPosition().getTs().getTsMillisec();
			long lastTSMillisec = track.getLastPosition().getTs().getTsMillisec();
			long durationInMillisec = lastTSMillisec - firstTSMillisec;
			for (ShipPosition pos : track.getPosList()) {
				long tsMillisec = pos.getTs().getTsMillisec();
				long newTsMillisec = (tsMillisec - firstTSMillisec)/(long)durationInMillisec*3600000l*24l; // norm to 24h
				pos.setTs(new Timestamp(newTsMillisec));
			}			
		}
		return track;
	}
	
	public List<ShipTrack> getShipTracksInIntervalAndBetweenBoxes(
														 Box departureBox,
														 Box arrivalBox,
														 TimeInterval depInterval,
														 TimeInterval analysisInterval,
														 List<Ship> includeShips,
														 List<Ship> excludeShips,
														 int limitTracks) {
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
		//List<Ship> arrivingShips = departingShips;
		List<Ship> arrivingShips = getShipsInIntervalAndBox(analysisInterval, 
															arrivalBox, 
															departingShips, 
															null, 
															MAX_SHIPS_TO_ANALYSE);

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
																	arrivalBox,
																	true);
			if(track != null) {
				track.setMmsi(ship.getMmsi());
				tracks.add(track);
			}
		}
				
		return tracks;						
	}
		
}
