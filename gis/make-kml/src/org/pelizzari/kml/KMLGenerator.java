/**
 * 
 */
package org.pelizzari.kml;

import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.pelizzari.ship.ShipPosition;
import org.pelizzari.ship.ShipVoyage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author andrea
 *
 */
public class KMLGenerator {
	
	Document doc;
	Element docNode;

	/**
	 * @throws ParserConfigurationException 
	 * 
	 */
	public KMLGenerator() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
 
		doc = builder.newDocument();
		Element root = doc.createElement("kml");
		root.setAttribute("xmlns", "http://earth.google.com/kml/2.1");
		doc.appendChild(root);
		docNode = doc.createElement("Document");
		root.appendChild(docNode);
	}
	
	public void addIconStyle(String iconStyleName, String iconHrefURL) {
		Element style = doc.createElement("Style");
		//tstyle.setAttribute("id", styleName);
		Element iconStyle = doc.createElement("IconStyle");
		style.setAttribute("id", iconStyleName);
		Element icon = doc.createElement("Icon");
		Element iconHref = doc.createElement("href");
		iconHref.appendChild(doc.createTextNode(iconHrefURL));
		icon.appendChild(iconHref);
		iconStyle.appendChild(icon);
		style.appendChild(iconStyle);
		docNode.appendChild(style);
	}
	
	public void addPoint(String iconStyleName, String mmsi, float lat, float lon) {
		Element placemark = doc.createElement("Placemark");		
		docNode.appendChild(placemark);
		Element name = doc.createElement("name");
		name.appendChild(doc.createTextNode(mmsi));
		placemark.appendChild(name);
		Element styleUrl = doc.createElement("styleUrl");
		styleUrl.appendChild(doc.createTextNode( "#" + iconStyleName));
		placemark.appendChild(styleUrl);
		Element point = doc.createElement("Point");
		Element coordinates = doc.createElement("coordinates");
		coordinates.appendChild(doc.createTextNode(lon+ "," + lat));
		point.appendChild(coordinates);
		placemark.appendChild(point);
	}

	public void addLineString(ShipVoyage voyage) {
		addLineString(voyage.getMmsi(), voyage.getPosList());
	}

	public void addLineString(String mmsi, List<ShipPosition> posList) {
		Element placemark = doc.createElement("Placemark");		
		docNode.appendChild(placemark);
		Element name = doc.createElement("name");
		name.appendChild(doc.createTextNode(mmsi));
		placemark.appendChild(name);
//		Element styleUrl = doc.createElement("styleUrl");
//		styleUrl.appendChild(doc.createTextNode( "#" + iconStyleName));
//		placemark.appendChild(styleUrl);
		Element lineString = doc.createElement("LineString");
		Element coordinates = doc.createElement("coordinates");
		String coordList = "";
		Iterator<ShipPosition> itr = posList.iterator();
		while(itr.hasNext()) {
			ShipPosition pos = itr.next();
			coordList = coordList + " " + pos.lon+ "," + pos.lat;
		}
		coordinates.appendChild(doc.createTextNode(coordList));
		lineString.appendChild(coordinates);
		placemark.appendChild(lineString);
	}	

	public Document getDoc() {
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}
	
}
