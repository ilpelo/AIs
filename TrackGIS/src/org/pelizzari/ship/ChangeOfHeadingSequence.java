package org.pelizzari.ship;

import java.util.ArrayList;
import java.util.Iterator;

public class ChangeOfHeadingSequence extends ArrayList<ChangeOfHeading> {
	
	public String toString() {
		String s = "Changes of Heading: ";
		for(ChangeOfHeading coh : this) {
			s = s + coh + " ";
		}
		return s;
	}
	
}
