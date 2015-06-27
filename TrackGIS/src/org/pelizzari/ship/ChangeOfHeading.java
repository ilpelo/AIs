package org.pelizzari.ship;

/**
 * Angle between two consecutive ship track segments.
 * @author andrea@pelizzari.org
 *
 */
public class ChangeOfHeading {

	float changeOfHeading; // in degrees +/-180

	public ChangeOfHeading(Heading head1, Heading head2) {
		float coh = head2.heading - head1.heading;
		if(coh > 180) {
			coh = -360f + coh;
		} else {
			if(coh < -180) {
				coh = -(360f + coh);
			}
		}
		changeOfHeading = coh;		
	}

	public String toString() {
		return String.format("%3.1f", changeOfHeading);
	}

	public boolean equals(ChangeOfHeading heading) {
		double changeOfHeadingDiff = (double) (this.changeOfHeading - heading.changeOfHeading);
		return Math.abs(changeOfHeadingDiff) <= Heading.HEADING_PRECISION;
	}

}
