package org.pelizzari.ai;

import java.awt.Color;

import org.pelizzari.gis.*;
import org.pelizzari.ship.*;

public class PilotAI {

	public static void main(String[] args) {
		final float[] TRACK_LAT = {32f, 32.1f, 32f, 32f};
		final float[] TRACK_LON = {-10f, -11f, -13f, -24f};
		
		ShipTrack track = new ShipTrack();
		for (int i = 0; i < TRACK_LON.length; i++) {
			Point p = new Point(TRACK_LAT[i], TRACK_LON[i]);
			Timestamp ts = new Timestamp(100000+i*3600);
			track.addPosition(new ShipPosition(p, ts));
		}
		Map map = new Map();

		System.out.println(track);
		map.plotTrack(track, Color.BLACK);
		track.reducePositions();
		System.out.println(track);		
		map.plotTrack(track, Color.BLUE);
		map.setVisible(true);
	}

}
