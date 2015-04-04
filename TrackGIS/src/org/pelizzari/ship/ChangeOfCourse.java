package org.pelizzari.ship;


public class ChangeOfCourse {

	static final float COURSE_PRECISION = 0.1f; // degrees
	
	float course; // in degrees (0-359)
	int duration; // in seconds
	
	
	public ChangeOfCourse(float newCourse, int duration)
			throws Exception {
		setDuration(duration);
		setCourse(newCourse);
	}
	
	public float getCourse() {
		return course;
	}

	public void setCourse(float newCourse) throws Exception {
		if (newCourse < 0 || newCourse > 359.9) {
			throw new Exception("Parameter newCourse out of bounds: "
					+ newCourse);
		}
		this.course = newCourse;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) throws Exception {
		if (duration <= 0) {
			throw new Exception("Parameter duration not valid: " + duration);
		}		this.duration = duration;
	}
	
	public String toString() {
		return "[" + String.format("%3.1f°,%ds", course, duration) + "]"; 
	}

}
