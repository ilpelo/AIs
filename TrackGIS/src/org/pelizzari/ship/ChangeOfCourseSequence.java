package org.pelizzari.ship;

import java.util.ArrayList;
import java.util.Iterator;

public class ChangeOfCourseSequence extends ArrayList<ChangeOfCourse> {
	
	public String toString() {
		String s = "Changes of Course: ";
		Iterator<ChangeOfCourse> cocItr = this.iterator();
		while(cocItr.hasNext()) {
			ChangeOfCourse coc = cocItr.next();
			s = s + coc + " ";
		}
		return s;
	}
}
