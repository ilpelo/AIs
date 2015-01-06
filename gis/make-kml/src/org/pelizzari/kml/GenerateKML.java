package org.pelizzari.kml;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class GenerateKML {

	public static void main(String[]args ){
		Statement stmt;
		ResultSet rs;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://localhost:3306/ai";
			Connection con = DriverManager.getConnection(url, "root", "mysql");
			
			KMLGenerator kmlGenerator = new KMLGenerator();
			
			kmlGenerator.addIconStyle("targetStyle", "http://maps.google.com/mapfiles/kml/shapes/target.png");
			
			stmt = con.createStatement();
			// get top 10 ships
			List<String> top10Ships = new ArrayList<String>();
			rs = stmt.executeQuery("SELECT mmsi, count(*) FROM position group by mmsi order by 2 desc limit 10");
			while(rs.next()){
				String mmsi = rs.getString("mmsi");
				top10Ships.add(mmsi);
			}

			Iterator<String> itr = top10Ships.iterator();
			while(itr.hasNext()) {
				rs = stmt.executeQuery("SELECT * FROM position where mmsi = "+itr.next()+			
						 " order by ts asc");
				ShipPosition pos = null;
				List<ShipPosition> posList = new ArrayList<ShipPosition>();
				while(rs.next()){
					String mmsi = rs.getString("mmsi");
					float lat = rs.getFloat("lat");
					float lon = rs.getFloat("lon");
					pos = new ShipPosition(lat, lon);
					
					kmlGenerator.addPoint("targetStyle", mmsi, pos.lat, pos.lon);
					
					posList.add(pos);					
				}
				kmlGenerator.addLineString("track", posList);
			}
			
			Source src = new DOMSource(kmlGenerator.getDoc());
			
			Result dest = new StreamResult(new File("c:/master_data/PlaceMarkers.kml")); 
			TransformerFactory tranFactory = TransformerFactory.newInstance(); 
		    Transformer aTransformer = tranFactory.newTransformer(); 
			aTransformer.transform(src, dest);
			
			System.out.println("Completed.....");
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
	}
}
