package org.pelizzari.gis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pelizzari.ship.ShipPosition;

public class DisplacementSequence extends ArrayList<Displacement> {
	
	/*
	 * Increase the sequence density by splitting "factor" times each original one
	 */
	public DisplacementSequence increaseDisplacements(int factor) {
		DisplacementSequence increasedDisplacementSequence = new DisplacementSequence();
		for (Displacement displ : this) {
			increasedDisplacementSequence.addAll(displ.split(factor));
		}
		return increasedDisplacementSequence;
	}
	
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
