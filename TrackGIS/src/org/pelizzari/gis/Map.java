package org.pelizzari.gis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import eu.jacquet80.minigeo.MapWindow;
import eu.jacquet80.minigeo.POI;
import eu.jacquet80.minigeo.Point;
import eu.jacquet80.minigeo.Segment;

public class Map extends MapWindow {

    private static Pattern SHIP_POSITION =
    		Pattern.compile("^(.+),(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)$");
	
	public Map() {
		super();		
	}
	
    //map.addPOI(new POI(new Point(48.8567, 2.3508), "Paris"));
	
	public void loadTrack(FileReader fr) {
        BufferedReader r = new BufferedReader(fr);
        //BufferedReader r = new BufferedReader(new FileReader("C:\\master_data\\france.poly"));
        String line;
        Point cur, prec = null;
        int readCount = 0;
        int errCount = 0;
        try {
			while((line = r.readLine()) != null) {
			        readCount++;
			        Matcher m = SHIP_POSITION.matcher(line);
			        if(m.matches()) {
			        		String ts = m.group(1);
			                double lat = Double.parseDouble(m.group(2));
			                double lon = Double.parseDouble(m.group(3));
			                System.out.println("ts " + ts + " lat " + lat + " lon "+ lon);			                
			                cur = new Point(lat, lon);
			                if(prec != null) addSegment(new Segment(prec, cur, Color.BLUE));
			                prec = cur;
			        } else errCount++;			
			}
            System.out.println("Read " + readCount + " lines; ignored " + errCount);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void saveAsImage(File outputFile) {
        try
        {
            BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = image.createGraphics();
            paint(graphics2D);
            ImageIO.write(image,"png", outputFile);
            //ImageIO.write(image,"jpeg", new File("C:\\master_data\\map.jpeg"));
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }		
	}
}
