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
	static final int MMSI_LABEL_SKIP_INTERVAL = 5;
	static final String[] COLOR_SCALE = {
		"ffffff",
		"ff0000",
		"00ff00",
		"0000ff",
		"ffff00",
		"ff00ff",
		"c0c0c0",
		"808080",
		"800000",
		"808000",
		"008000",
		"800080",
		"008080",
		"000080",
		"ff4500",
		"7fff00",
		"ff1493",
		"d2691e",
		"ff6347",
		"f0e68c"
		};

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

	/**
	 * @param styleName
	 * @param iconHrefURL
	 * @param greenHueLevel 0-(maxHueLevels-1)
	 */
	public void addThickLineStyle(String styleName, String iconHrefURL) {
		Element style = doc.createElement("Style");
		Element lineStyle = doc.createElement("LineStyle");
		style.setAttribute("id", styleName);
		// color aabbggrr hex
		Element color = doc.createElement("color");
		color.appendChild(doc.createTextNode("aa00ff00"));
		lineStyle.appendChild(color);
		// line width
		Element width = doc.createElement("width");
		width.appendChild(doc.createTextNode("10"));
		lineStyle.appendChild(width);		
		// HREF to icon
		Element icon = doc.createElement("Icon");
		Element iconHref = doc.createElement("href");
		iconHref.appendChild(doc.createTextNode(iconHrefURL));
		icon.appendChild(iconHref);
		lineStyle.appendChild(icon);
		//
		style.appendChild(lineStyle);
		docNode.appendChild(style);
	}
	
	/**
	 * @param styleName
	 * @param iconHrefURL
	 * @param greenHueLevel 0-(maxHueLevels-1)
	 */
	public void addGradientStyle(
			String styleName, String iconHrefURL, int hueLevel, int maxHueLevels) {
		int greenHueLevel = 255*hueLevel/(maxHueLevels-1);
		Element style = doc.createElement("Style");
		// tstyle.setAttribute("id", styleName);
		Element iconStyle = doc.createElement("IconStyle");
		style.setAttribute("id", styleName+hueLevel);
		// color aabbggrr hex
		Element color = doc.createElement("color");
		String green = String.format("%02x", greenHueLevel);
		color.appendChild(doc.createTextNode("ff66"+green+"ff"));
		iconStyle.appendChild(color);
		// HREF to icon
		Element icon = doc.createElement("Icon");
		Element iconHref = doc.createElement("href");
		iconHref.appendChild(doc.createTextNode(iconHrefURL));
		icon.appendChild(iconHref);
		iconStyle.appendChild(icon);
		style.appendChild(iconStyle);
		docNode.appendChild(style);
	}

	/**
	 * @param styleName
	 * @param iconHrefURL
	 * @param greenHueLevel 0-(maxHueLevels-1)
	 */
	public void addColorScaleStyle(
			String styleName, String iconHrefURL, int level) {
		Element style = doc.createElement("Style");
		// tstyle.setAttribute("id", styleName);
		Element iconStyle = doc.createElement("IconStyle");
		style.setAttribute("id", styleName+level);
		// color aabbggrr hex
		Element color = doc.createElement("color");
		if(level >= COLOR_SCALE.length) {
			level = COLOR_SCALE.length-1;
		}
		String colorValue = "ff"+COLOR_SCALE[level];
		color.appendChild(doc.createTextNode(colorValue));
		iconStyle.appendChild(color);
		// HREF to icon
		Element icon = doc.createElement("Icon");
		Element iconHref = doc.createElement("href");
		iconHref.appendChild(doc.createTextNode(iconHrefURL));
		icon.appendChild(iconHref);
		iconStyle.appendChild(icon);
		style.appendChild(iconStyle);
		docNode.appendChild(style);
	}
	
	/**
	 * Add maxHueLevels of greens named coloredStyleName1, coloredStyleName2, ...
	 * @param coloredStyleName
	 * @param iconHrefURL
	 * @param maxHueLevels
	 */
	public void addColoredStyles(
			String coloredStyleName, String iconHrefURL, int maxHueLevels, boolean gradient) {
		for (int i = 0; i < maxHueLevels; i++) {
			if(gradient) {
				addGradientStyle(coloredStyleName, iconHrefURL, i, maxHueLevels);
			} else {
				addColorScaleStyle(coloredStyleName, iconHrefURL, i);
			}
		}
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

	public void addLineString(String mmsi, List<ShipPosition> posList, String style) {
		Element placemark = doc.createElement("Placemark");
		docNode.appendChild(placemark);
		//
		Element name = doc.createElement("name");
		name.appendChild(doc.createTextNode(mmsi));
		placemark.appendChild(name);
		//
		Element styleUrl = doc.createElement("styleUrl");
		styleUrl.appendChild(doc.createTextNode( "#" + style));
		placemark.appendChild(styleUrl);
		//
		Element altitudeMode = doc.createElement("altitudeMode");
		altitudeMode.appendChild(doc.createTextNode("absolute"));
		placemark.appendChild(altitudeMode);		
		//
		Element altitude = doc.createElement("altitude");
		altitude.appendChild(doc.createTextNode("1000"));
		placemark.appendChild(altitude);
		//
		Element lineString = doc.createElement("LineString");
		lineString.appendChild(altitude);
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

	public void addBox(Box box) {
		Element placemark = doc.createElement("Placemark");
		docNode.appendChild(placemark);
		Element name = doc.createElement("name");
		name.appendChild(doc.createTextNode(box.getName()));
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
		int lastPos = positions.size()-1;
		for (ShipPosition pos : positions) {
			long ts = pos.getTs().getTsMillisec();
			Date date = new Date(ts);
			String posLabel = withDates?date.toString():"";
			if(i % MMSI_LABEL_SKIP_INTERVAL == 0 || i == lastPos) {
				posLabel = posLabel + " " + track.getMmsi(); 
			}
			addPoint("targetStyle",  
					posLabel,
					pos.getPoint().lat, 
					pos.getPoint().lon);
			i++;
		}
		addThickLineStyle("trackLineStyle", 
				"http://maps.google.com/mapfiles/kml/shapes/target.png");
		
		addLineString(label, track.getPosList(), "trackLineStyle");
//		Comment trackDescription = doc.createComment(track.toString());
//		Element element = doc.getDocumentElement();
//		element.getParentNode().insertBefore(trackDescription, element);
	}

	
	public void addTrack(ShipTrack track, String label) {
		addTrack(track, label, false);
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