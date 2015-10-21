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
import org.pelizzari.ship.ShipPositionList;
import org.pelizzari.ship.ShipTrack;
import org.pelizzari.time.TimeInterval;
import org.pelizzari.time.Timestamp;


/**
 * Compute the avarage length of a voyage between two areas and normalize the timestamp of
 * the ship position of each track so that the duration is always 24h and the speed is constant.
 * Normalized timestamps are stored in the "norm_ts" column of the "tracks" table.
 * @author andrea@pelizzari.org
 *
 */
public class NormalizeShipTracks {

	static final String YEAR_PERIOD = "SPRING";
	static final Box DEPARTURE_AREA = Areas.getBox("LANZAROTE"); 
	static final Box ARRIVAL_AREA = Areas.getBox("NATAL");
//	static final String YEAR_PERIOD = "WINTER";
//	static final Box DEPARTURE_AREA = Areas.getBox("CAPETOWN"); 
//	static final Box ARRIVAL_AREA = Areas.getBox("REUNION");
//	static final String YEAR_PERIOD = "WINTER";
//	static final Box DEPARTURE_AREA = Areas.getBox("REDSEA"); 
//	static final Box ARRIVAL_AREA = Areas.getBox("GOA");
	static final long INSERT_TS = 1444899117;	
	
	// max percentage of discrepancy from the average track length
	static final float MAX_TRACK_LENGTH_DISCREPANCY = 0.1f;
	//
	final static String REFERENCE_START_DT = MineVoyages.REFERENCE_START_DT; // reference start date of all tracks
	final static int REFERENCE_VOYAGE_DURATION_IN_SEC = MineVoyages.REFERENCE_VOYAGE_DURATION_IN_SEC;
	
	public static void main(String[] args) throws ParseException {

		Miner miner = new Miner();
	
		List<ShipTrack> tracks = miner.getShipTracksFromTracksTable(
				YEAR_PERIOD, DEPARTURE_AREA, ARRIVAL_AREA);
		// compute average
		float sumLengthsInMiles = 0;
		for (ShipTrack track : tracks) {
			// compute average length
			float trackLengthInMiles = track.computeLengthInMiles();
			System.out.println("MMSI "+track.getMmsi()+" length="+trackLengthInMiles);
			sumLengthsInMiles += trackLengthInMiles;			
		}
		float avgLengthInMiles = sumLengthsInMiles/tracks.size();
		System.out.println("Avg. length="+avgLengthInMiles);
		// normalize timestamps based on a fix voyage duration of 24h
		for (ShipTrack track : tracks) {
			float trackLengthInMiles = track.computeLengthInMiles();
			float discrepancyFromAverage = Math.abs((avgLengthInMiles - trackLengthInMiles)/avgLengthInMiles);
			if(discrepancyFromAverage > MAX_TRACK_LENGTH_DISCREPANCY) {
				System.out.println("MMSI "+track.getMmsi()+" high discrepancy="+discrepancyFromAverage);
			}
			int i=0;
			for (ShipPosition pos : track.getPosList()) {
				Timestamp normTs = track.computeNormalizedTime(
						new Timestamp(REFERENCE_START_DT),
						REFERENCE_VOYAGE_DURATION_IN_SEC,
						i);
				track.updateNormalizedShipPositionTimestampInDB(
						DEPARTURE_AREA, ARRIVAL_AREA, YEAR_PERIOD, i, normTs);
				i++;
			}
		}		
		System.out.println("Done");		
	}

}
