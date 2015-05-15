package org.pelizzari.gis;

import java.util.ArrayList;
import java.util.Iterator;

public class DisplacementSequence extends ArrayList<Displacement> {
	
	public String toString() {
		String s = "Displacements: ";
		Iterator<Displacement> displItr = this.iterator();
		while(displItr.hasNext()) {
			Displacement displ = displItr.next();
			s = s + displ + " ";
		}
		return s;
	}
}
