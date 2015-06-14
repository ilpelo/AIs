package org.pelizzari.ship;

import java.util.ArrayList;
import java.util.Iterator;

import org.pelizzari.gis.DisplacementSequence;

public class HeadingSequence extends ArrayList<Heading> {
	
	public HeadingSequence(DisplacementSequence displSeq) {
		for (Displacement displacement : displSeq) {
			float angle = 
			Heading head = new Heading(displ)
		}
	}
	
	public String toString() {
		String s = "Sequence of Headings: ";
		Iterator<Heading> headItr = this.iterator();
		while(headItr.hasNext()) {
			Heading head = headItr.next();
			s = s + head + " ";
		}
		return s;
	}
}
