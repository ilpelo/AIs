package org.pelizzari.ship;

import org.pelizzari.gis.Displacement;

/**
 * This class represents a ship true heading, in degrees.
 * 
 * @author andrea
 * 
 */
public class Heading {

	static final float HEADING_PRECISION = 0.1f; // degrees

	float heading; // in degrees (0-359)

	public Heading(float heading) throws Exception {
		setHeading(heading);
	}

	public Heading(Displacement displ) {
		float lonDiff = displ.deltaLon;
		float latDiff = displ.deltaLat;
		if (Math.abs(lonDiff) < HEADING_PRECISION
				&& Math.abs(latDiff) < HEADING_PRECISION) {
			this.heading = 0;
		} else if (Math.abs(lonDiff) < HEADING_PRECISION) {
			this.heading = latDiff > 0 ? 0 : 180;
		} else if (Math.abs(latDiff) < HEADING_PRECISION) {
			this.heading = lonDiff > 0 ? 90 : 270;
		} else {
			if (lonDiff > 0) {
				this.heading = 90f - (float) Math.toDegrees(Math.atan(latDiff
						/ lonDiff));
			} else {
				this.heading = 270f + (float) Math.toDegrees(Math.atan(latDiff
						/ -lonDiff));
			}
		}
	}

	public void setHeading(float heading) throws Exception {
		if (heading < 0 || heading >= 360) {
			throw new Exception("Parameter heading out of bounds: " + heading);
		}
		this.heading = heading;
	}

	public String toString() {
		return String.format("%3.1f°", heading);
	}

	public boolean equals(Heading heading) {
		double headingDiff = (double) (this.heading - heading.heading);
		return Math.abs(headingDiff) <= HEADING_PRECISION;
	}

}
