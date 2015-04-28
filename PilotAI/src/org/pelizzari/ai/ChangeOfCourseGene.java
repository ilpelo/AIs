/**
 * 
 */
package org.pelizzari.ai;

import org.pelizzari.ship.ChangeOfCourse;

import ec.EvolutionState;
import ec.vector.Gene;

/**
 * @author andrea
 *
 */
public class ChangeOfCourseGene extends Gene {

	public final static float MIN_COURSE = 0f;
	public final static float MAX_COURSE = 360f;
	public final static int MIN_DURATION = 0;
	public final static int MAX_DURATION = 3600*24*1; // one day	
	
	private ChangeOfCourse allele;
	/**
	 * 
	 */
	public ChangeOfCourseGene() {
		// TODO Auto-generated constructor stub
		super();
	}

    public void setup(final EvolutionState state, final Parameter base)
    {
    	super.setup(state, base);    	
    }	
	
	/* (non-Javadoc)
	 * @see ec.vector.Gene#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object otherGene) {
        if (!this.getClass().isInstance(otherGene)) {
            return false;
        }

        ChangeOfCourseGene cocGene = (ChangeOfCourseGene) otherGene;

        return allele.equals(cocGene.allele);
    }

	/* (non-Javadoc)
	 * @see ec.vector.Gene#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = this.getClass().hashCode();
		hash = hash ^ (int) allele.getCourse() ^ allele.getDuration();
		return hash;
	}

	/* (non-Javadoc)
	 * @see ec.vector.Gene#reset(ec.EvolutionState, int)
	 */
	@Override
	public void reset(EvolutionState state, int thread) {
		float course = MIN_COURSE + state.random[thread].nextFloat(true, false) * MAX_COURSE;
		int duration = MIN_DURATION + state.random[thread].nextInt(MAX_DURATION);
		try {
			allele = new ChangeOfCourse(course, duration);
		} catch (Exception e) {
			state.output.fatal(e.toString());
		}
	}
	
	ChangeOfCourse getAllele() {
		return allele;
	}
	
    public Object clone() {
        ChangeOfCourseGene cocGene = (ChangeOfCourseGene) (super.clone());

        return cocGene;
    }

}
