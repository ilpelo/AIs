package org.pelizzari.gis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.pelizzari.ship.ShipPosition;
import org.pelizzari.ship.ShipPositionList;
import org.pelizzari.ship.ShipTrack;

import eu.jacquet80.minigeo.MapWindow;
import eu.jacquet80.minigeo.POI;
import eu.jacquet80.minigeo.Point;
import eu.jacquet80.minigeo.Segment;

public class Map extends MapWindow {

	public Map() {
		super();
	}

	// map.addPOI(new POI(new Point(48.8567, 2.3508), "Paris"));

	// public void loadTrack(FileReader fr) {
	// BufferedReader r = new BufferedReader(fr);
	// //BufferedReader r = new BufferedReader(new
	// FileReader("C:\\master_data\\france.poly"));
	// String line;
	// Point cur, prec = null;
	// int readCount = 0;
	// int errCount = 0;
	// try {
	// while((line = r.readLine()) != null) {
	// readCount++;
	// Matcher m = SHIP_POSITION.matcher(line);
	// if(m.matches()) {
	// String ts = m.group(1);
	// double lat = Double.parseDouble(m.group(2));
	// double lon = Double.parseDouble(m.group(3));
	// System.out.println("ts " + ts + " lat " + lat + " lon "+ lon);
	// cur = new Point(lat, lon);
	// if(prec != null) addSegment(new Segment(prec, cur, Color.BLUE));
	// prec = cur;
	// } else errCount++;
	// }
	// System.out.println("Read " + readCount + " lines; ignored " + errCount);
	// } catch (NumberFormatException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	public void plotShipPositions(ShipPositionList posList, Color color) {
		plotShipPositions(posList, color, null, false);
	}
	
	public void plotTrack(ShipTrack track, Color color, String label) {
		plotShipPositions(track, color, label, true);
	}
	
	public void plotShipPositions(ShipPositionList posList, 
								  Color color,
								  String lastPositionLabel,
								  boolean showSegments) {
		Point cur, prec = null;
		try {
			Iterator<ShipPosition> posItr = posList.getPosList().iterator();
			while (posItr.hasNext()) {
				ShipPosition pos = posItr.next();
				double lat = pos.getPoint().lat;
				double lon = pos.getPoint().lon;
				// System.out.println("ts " + ts + " lat " + lat + " lon "+
				// lon);
				cur = new Point(lat, lon);
				//String label = ""+pos.getIndex();
				String label = ""+pos.getTs(); //.getTsMillisec();
				if(lastPositionLabel != null && !posItr.hasNext()) {
					label = lastPositionLabel;
				}
				POI posPoi = new POI(cur, label);
				addPOI(posPoi);                
				if (showSegments && prec != null) {
					addSegment(new Segment(prec, cur, color));
				}
				prec = cur;
			}
			// System.out.println("Read " + readCount + " lines; ignored " +
			// errCount);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void plotBox(Box box, Color color) {
		Point p1 = new Point(box.getMaxLat(), box.getMinLon());
		Point p2 = new Point(box.getMaxLat(), box.getMaxLon());
		Point p3 = new Point(box.getMinLat(), box.getMaxLon());
		Point p4 = new Point(box.getMinLat(), box.getMinLon());
		addSegment(new Segment(p1, p2, color));
		addSegment(new Segment(p2, p3, color));
		addSegment(new Segment(p3, p4, color));
		addSegment(new Segment(p4, p1, color));		
	}
	
	public void saveAsImage(File outputFile) {
		try {
			BufferedImage image = new BufferedImage(getWidth(), getHeight(),
					BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics2D = image.createGraphics();
			paint(graphics2D);
			ImageIO.write(image, "png", outputFile);
			// ImageIO.write(image,"jpeg", new
			// File("C:\\master_data\\map.jpeg"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setVisible(boolean b) {
		super.setVisible(b);
	}
}
