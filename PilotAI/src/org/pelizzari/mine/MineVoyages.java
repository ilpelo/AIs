package org.pelizzari.mine;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.pelizzari.db.Miner;
import org.pelizzari.gis.Box;
import org.pelizzari.gis.Point;
import org.pelizzari.ship.Ship;
import org.pelizzari.time.TimeInterval;
import org.pelizzari.time.Timestamp;

public class MineVoyages {

	final static String START_DT = "2011-03-01 00:00:00";
	final static int START_PERIOD_IN_DAYS = 10;

	final static int ANALYSIS_PERIOD_IN_DAYS = 30;
	
	public static void main(String[] args) {
		
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
		// Golf of Aden
		Point goaNW = new Point(18, 50);
		Point goaSE = new Point(9, 51);
		

		//// Departure
		
		//depBox = new Box(gibraltarNW, gibraltarSE);
		//depBox = new Box(channelNW, channelSE);
		Box depBox = new Box(suezNW, suezSE);	
		
		Timestamp startTS1 = null;
		Timestamp startTS2 = null;
		TimeInterval depInterval = null;
		try {
			startTS1 = new Timestamp(START_DT);
			startTS2 = new Timestamp(startTS1.getTs()+START_PERIOD_IN_DAYS*3600*24*1000);
			depInterval = new TimeInterval(startTS1, startTS2);
		} catch (Exception e) {
			System.err.println("error parsing times");
			e.printStackTrace();
		}
		
		/// Arrival
		
		//arrBox = new Box(nyNW, nySE);
		//arrBox = new Box(rioNW, rioSE);
		//arrBox = new Box(saNW, saSE);
		//arrBox = new Box(copenhagenNW, copenhagenSE);
		//arrBox = new Box(goaNW, goaSE);
		Box arrBox = new Box(gibraltarNW, gibraltarSE);
				
		
		/// Let's mine
		
		Miner miner = new Miner();
		
		// get the ships that were present in the departure area
		List<Ship> shipsInDepBox = miner.getShipsInBoxAndInterval(depBox, depInterval);
		for (Ship ship : shipsInDepBox) {
			System.out.print(ship+",");
			System.out.println("");
		}

		// check ships that were present in the arrival area afterwards
		Timestamp endTS = new Timestamp(startTS1.getTs()+1000); //ANALYSIS_PERIOD_IN_DAYS*3600*24*1000);
		TimeInterval arrInterval = null;
		try {
			arrInterval = new TimeInterval(startTS1, endTS);
		} catch (Exception e) {
			System.err.println("error making arrival interval");
			e.printStackTrace();
		}
		List<Ship> shipsInArrBox = miner.getShipsInBoxAndInterval(arrBox, arrInterval, shipsInDepBox, null);
		for (Ship ship : shipsInArrBox) {
			System.out.print(ship+",");
			System.out.println("");
		}		
				
		System.out.println("Done\n");

		
		
	}

}
