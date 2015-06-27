package org.pelizzari.ship;

import java.util.ArrayList;
import java.util.Iterator;

import org.pelizzari.gis.Displacement;
import org.pelizzari.gis.DisplacementSequence;

public class HeadingSequence extends ArrayList<Heading> {
	
	public HeadingSequence(DisplacementSequence displSeq) {
		super();
		for (Displacement displacement : displSeq) {
			Heading head = new Heading(displacement);
			this.add(head);
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
