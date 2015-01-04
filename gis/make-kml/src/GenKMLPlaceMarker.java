import java.io.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


//import com.google.marker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GenKMLPlaceMarker {
	public int id;
	public float lat;
	public float lon;

	public static void main(String[]args ){
		Statement stmt;
		ResultSet rs;
		GenKMLPlaceMarker KML = new GenKMLPlaceMarker();
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://localhost:3306/ai";
			Connection con = DriverManager.getConnection(url, "root", "mysql");

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			TransformerFactory tranFactory = TransformerFactory.newInstance(); 
		    Transformer aTransformer = tranFactory.newTransformer(); 

			Document doc = builder.newDocument();
			Element root = doc.createElement("kml");
			root.setAttribute("xmlns", "http://earth.google.com/kml/2.1");
			doc.appendChild(root);
			Element dnode = doc.createElement("Document");
			root.appendChild(dnode);
			
			
			Element tstyle = doc.createElement("Style");
			tstyle.setAttribute("id", "targetStyle");
			Element tistyle = doc.createElement("IconStyle");
			tistyle.setAttribute("id", "targetStyle");
			Element bicon = doc.createElement("Icon");
			Element biconhref = doc.createElement("href");
			biconhref.appendChild(doc.createTextNode("http://maps.google.com/mapfiles/kml/shapes/target.png"));
			tstyle.appendChild(tistyle);
			bicon.appendChild(biconhref);
			tistyle.appendChild(bicon);
			dnode.appendChild(tstyle);
			
			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT * FROM position limit 10");
			while(rs.next()){
				KML.id = rs.getInt("mmsi");
				KML.lat = rs.getFloat("lat");
				KML.lon = rs.getFloat("lon");

				Element placemark = doc.createElement("Placemark");
				dnode.appendChild(placemark);
				Element styleUrl = doc.createElement("styleUrl");
				styleUrl.appendChild(doc.createTextNode( "#targetStyle"));
				placemark.appendChild(styleUrl);
				Element point = doc.createElement("Point");
				Element coordinates = doc.createElement("coordinates");
				coordinates.appendChild(doc.createTextNode(KML.lon+ "," + KML.lat));
				point.appendChild(coordinates);
				placemark.appendChild(point);
			}
			Source src = new DOMSource(doc);
			Result dest = new StreamResult(new File("c:/master_data/PlaceMarkers.kml")); 
			aTransformer.transform(src, dest);
			System.out.println("Completed.....");
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
	}
}
