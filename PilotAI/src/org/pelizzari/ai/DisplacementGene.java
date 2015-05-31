/**
 * 
 */
package org.pelizzari.ai;

import org.pelizzari.gis.Displacement;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.vector.Gene;

/**
 * @author andrea
 *
 */
public class DisplacementGene extends Gene {

	public final static float PARAM_MAX_DELTA = 10f; // max absolute value of change of coordinate (lat or lon)
											   // in decimal degree for each step 
	
	private Displacement allele;
	/**
	 * 
	 */
	public DisplacementGene() {
		// TODO Auto-generated constructor stub
		super();
	}

    public void setup(final EvolutionState state, final Parameter base)
    {
    	super.setup(state, base);
    	// get the maximum displacement and use it to initialize an allele
    	//int maxDisplacement = state.parameters.getInt(base.pop().push("max-gene"), null);
    	//state.output.fatal("Max displ: "+ maxDisplacement);
    }	
	
	/* (non-Javadoc)
	 * @see ec.vector.Gene#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object otherGene) {
        if (!this.getClass().isInstance(otherGene)) {
            return false;
        }
        DisplacementGene posGene = (DisplacementGene) otherGene;
        return allele.equals(posGene.allele);
    }

	/* (non-Javadoc)
	 * @see ec.vector.Gene#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = this.getClass().hashCode();
		hash = hash ^ (int) (allele.deltaLat * 1000) ^ (int) (allele.deltaLon * 1000);
		return hash;
	}

	/* (non-Javadoc)
	 * @see ec.vector.Gene#reset(ec.EvolutionState, int)
	 */
	@Override
	public void reset(EvolutionState state, int thread) {
		// set to a random value in [-MAX_DELTA, +MAX_DELTA] interval both for lat and lon
		float deltaLat = - PARAM_MAX_DELTA + state.random[thread].nextFloat(true, true) * PARAM_MAX_DELTA * 2;
		float deltaLon = - PARAM_MAX_DELTA + state.random[thread].nextFloat(true, true) * PARAM_MAX_DELTA * 2;;
		try {
			allele = new Displacement(deltaLat, deltaLon);
		} catch (Exception e) {
			state.output.fatal("Resetting Dispacement gene:"+ e.toString());
		}
	}
	
	Displacement getAllele() {
		return allele;
	}
	
    public Object clone() {
        DisplacementGene displGene = (DisplacementGene) (super.clone());
        return displGene;
    }
    
    public String toString() {
    	return allele.toString();
    }

}
