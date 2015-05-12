package org.pelizzari.ship;


public class ChangeOfCourse {

	static final float COURSE_PRECISION = 0.1f; // degrees
	
	float course; // in degrees (0-359)
	int distance; // in nautical miles
	
	
	public ChangeOfCourse(float newCourse, int distance)
			throws Exception {
		setDistance(distance);
		setCourse(newCourse);
	}
	
	public float getCourse() {
		return course;
	}

	public void setCourse(float newCourse) throws Exception {
		if (newCourse < 0 || newCourse >= 360) {
			throw new Exception("Parameter newCourse out of bounds: "
					+ newCourse);
		}
		this.course = newCourse;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) throws Exception {
		if (distance <= 0) {
			throw new Exception("Parameter distance not valid: " + distance);
		}		this.distance = distance;
	}
	
	public String toString() {
		return "[" + String.format("%3.1f°,%dnm", course, distance) + "]"; 
	}
	
	public boolean equals(ChangeOfCourse coc) {
		return course == coc.course && distance == coc.distance;
	}

}
