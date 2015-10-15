package org.pelizzari.kml;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.pelizzari.gis.Box;
import org.pelizzari.ship.ShipPosition;
import org.pelizzari.ship.ShipTrack;
import org.pelizzari.ship.ShipTrackSegment;
import org.pelizzari.ship.ShipVoyage;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author andrea
 *
 */
public class KMLGenerator {
	Document doc;
	Element docNode;
	
	// print position with MMSI and then skip ... positions
	static final int MMSI_LABEL_SKIP_INTERVAL = 15;

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
		// tstyle.setAttribute("id", styleName);
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
		styleUrl.appendChild(doc.createTextNode("#" + iconStyleName));
		placemark.appendChild(styleUrl);
		Element point = doc.createElement("Point");
		Element coordinates = doc.createElement("coordinates");
		coordinates.appendChild(doc.createTextNode(lon + "," + lat));
		point.appendChild(coordinates);
		placemark.appendChild(point);
	}

	public void addLineString(String mmsi, List<ShipPosition> posList) {
		Element placemark = doc.createElement("Placemark");
		docNode.appendChild(placemark);
		Element name = doc.createElement("name");
		name.appendChild(doc.createTextNode(mmsi));
		placemark.appendChild(name);
		// Element styleUrl = doc.createElement("styleUrl");
		// styleUrl.appendChild(doc.createTextNode( "#" + iconStyleName));
		// placemark.appendChild(styleUrl);
		Element lineString = doc.createElement("LineString");
		Element coordinates = doc.createElement("coordinates");
		String coordList = "";
		Iterator<ShipPosition> itr = posList.iterator();
		while (itr.hasNext()) {
			ShipPosition pos = itr.next();
			coordList = coordList + " " + pos.getPoint().lon + "," + pos.getPoint().lat;
		}
		coordinates.appendChild(doc.createTextNode(coordList));
		lineString.appendChild(coordinates);
		placemark.appendChild(lineString);
	}

	public void addBox(String title, Box box) {
		Element placemark = doc.createElement("Placemark");
		docNode.appendChild(placemark);
		Element name = doc.createElement("name");
		name.appendChild(doc.createTextNode(title));
		placemark.appendChild(name);
		Element lineString = doc.createElement("LineString");
		Element coordinates = doc.createElement("coordinates");
		String coordList = box.getMinLon() + "," + box.getMaxLat() + " "
				+ box.getMaxLon() + "," + box.getMaxLat() + " "
				+ box.getMaxLon() + "," + box.getMinLat() + " "
				+ box.getMinLon() + "," + box.getMinLat() + " "
				+ box.getMinLon() + "," + box.getMaxLat() + " ";
		coordinates.appendChild(doc.createTextNode(coordList));
		lineString.appendChild(coordinates);
		placemark.appendChild(lineString);
	}
	
//	public void addComment(String comment) {
//	}
	
	public void addTrack(ShipTrack track, String label, boolean withDates) {
		if (track == null) {
			System.err.println("addTrack: track is null");
			return;
		}
		List<ShipPosition> positions = track.getPosList();
		
		int i = 0;
		for (ShipPosition pos : positions) {
			long ts = pos.getTs().getTsMillisec();
			Date date = new Date(ts);
			String posLabel = withDates?date.toString():"";
			if(i % MMSI_LABEL_SKIP_INTERVAL == 0) {
				posLabel = posLabel + " " + track.getMmsi(); 
			}
			addPoint("targetStyle",  
					posLabel,
					pos.getPoint().lat, 
					pos.getPoint().lon);
			i++;
		}
		addLineString(label, track.getPosList());
//		Comment trackDescription = doc.createComment(track.toString());
//		Element element = doc.getDocumentElement();
//		element.getParentNode().insertBefore(trackDescription, element);
	}

	public void addColoredTrainingSet(ShipTrack track) {
		if (track == null) {
			System.err.println("addColoredTrainingSet: track is null");
			return;
		}
		List<ShipTrackSegment> segments = track.getSegList();
		
		int i = 0;
		for (ShipTrackSegment seg : segments) {
			addPoint("targetStyle",  
					posLabel,
					pos.getPoint().lat, 
					pos.getPoint().lon);
			i++;
		}
		addLineString(label, track.getPosList());
//		Comment trackDescription = doc.createComment(track.toString());
//		Element element = doc.getDocumentElement();
//		element.getParentNode().insertBefore(trackDescription, element);
	}
	
	
	public void addTrack(ShipTrack track, String label) {
		addTrack(track, label, true);
	}
	
	public void saveKMLFile(String file) {
		Source src = new DOMSource(getDoc());
		File kmlFile = new File(file);
		Result dest = new StreamResult(kmlFile);
		TransformerFactory tranFactory = TransformerFactory.newInstance();
		Transformer aTransformer;
		try {
			aTransformer = tranFactory.newTransformer();
			aTransformer.transform(src, dest);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Document getDoc() {
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}
}