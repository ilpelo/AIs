package org.pelizzari.gis;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.jacquet80.minigeo.MapWindow;
import eu.jacquet80.minigeo.POI;
import eu.jacquet80.minigeo.Point;
import eu.jacquet80.minigeo.Segment;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.File;
import javax.imageio.ImageIO;


public class MapPlot {
        private static Pattern POINT = Pattern.compile("^.*?(-?\\d+\\.\\d+)\\s+(-?\\d+\\.\\d+)$");
       
        public static void main(String[] args) throws IOException {
                MapWindow window = new MapWindow();
               
                BufferedReader r = new BufferedReader(new FileReader("C:\\master_data\\france.poly"));
                String line;
                Point cur, prec = null;
                int readCount = 0;
                int errCount = 0;
                while((line = r.readLine()) != null) {
                        readCount++;
                        Matcher m = POINT.matcher(line);
                        if(m.matches()) {
                                double lon = Double.parseDouble(m.group(1));
                                double lat = Double.parseDouble(m.group(2));
                                cur = new Point(lat, lon);
                                if(prec != null) window.addSegment(new Segment(prec, cur, lon<0 ? Color.BLUE : Color.RED));
                                prec = cur;
                        } else errCount++;
                }
               
                window.addPOI(new POI(new Point(48.8567, 2.3508), "Paris"));
               
                System.out.println("Read " + readCount + " lines; ignored " + errCount);
                window.setVisible(true);
                
                // save picture
                try
                {
                    BufferedImage image = new BufferedImage(window.getWidth(), window.getHeight(), BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics2D = image.createGraphics();
                    window.paint(graphics2D);
                    ImageIO.write(image,"jpeg", new File("C:\\master_data\\map.jpeg"));
                }
                catch(Exception exception)
                {
                    //code
                }
        }

}

