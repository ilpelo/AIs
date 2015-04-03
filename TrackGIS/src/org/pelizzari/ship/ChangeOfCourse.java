package org.pelizzari.ship;


public class ChangeOfCourse {

	static final float COURSE_PRECISION = 0.1f; // degrees
	
	float oldCourse, newCourse; // in degrees (0-359)
	int duration; // in seconds
	
	public ChangeOfCourse(float oldCourse, float newCourse, int duration)
			throws Exception {
		setDuration(duration);
		setOldCourse(oldCourse);
		setNewCourse(newCourse);
	}
	
	public ChangeOfCourse(float newCourse, int duration)
			throws Exception {
		setDuration(duration);
		setNewCourse(newCourse);
	}
	
	public float getOldCourse() {
		return oldCourse;
	}

	public void setOldCourse(float oldCourse) throws Exception {
		if (oldCourse < 0 || oldCourse > 359.9) {
			throw new Exception("Parameter oldCourse out of bounds: "
					+ oldCourse);
		}
		this.oldCourse = oldCourse;
	}

	public float getNewCourse() {
		return newCourse;
	}

	public void setNewCourse(float newCourse) throws Exception {
		if (newCourse < 0 || newCourse > 359.9) {
			throw new Exception("Parameter newCourse out of bounds: "
					+ newCourse);
		}
		this.newCourse = newCourse;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) throws Exception {
		if (duration <= 0) {
			throw new Exception("Parameter duration not valid: " + newCourse);
		}		this.duration = duration;
	}

}
