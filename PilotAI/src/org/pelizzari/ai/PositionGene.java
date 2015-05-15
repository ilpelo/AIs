/**
 * 
 */
package org.pelizzari.ai;

import org.pelizzari.gis.Point;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.vector.Gene;

/**
 * @author andrea
 *
 */
public class PositionGene extends Gene {

	public final static float MAX_DELTA = 10f; // max absolute value of change of coordinate (lat or lon)
											   // in decimal degree for each step 
	
	private Point allele;
	/**
	 * 
	 */
	public PositionGene() {
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

        PositionGene posGene = (PositionGene) otherGene;

        return allele.equals(posGene.allele);
    }

	/* (non-Javadoc)
	 * @see ec.vector.Gene#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = this.getClass().hashCode();
		hash = hash ^ (int) (allele.lat * 1000) ^ (int) (allele.lon * 1000);
		return hash;
	}

	/* (non-Javadoc)
	 * @see ec.vector.Gene#reset(ec.EvolutionState, int)
	 */
	@Override
	public void reset(EvolutionState state, int thread) {
		float course = MIN_COURSE + state.random[thread].nextFloat(true, false) * MAX_COURSE;
		int distance = MIN_DISTANCE + state.random[thread].nextInt(MAX_DISTANCE);
		try {
			allele = new ChangeOfCourse(course, distance);
		} catch (Exception e) {
			state.output.fatal(e.toString());
		}
	}
	
	ChangeOfCourse getAllele() {
		return allele;
	}
	
    public Object clone() {
        PositionGene cocGene = (PositionGene) (super.clone());

        return cocGene;
    }

}
