package org.pelizzari.ai;

import ec.*;
import ec.simple.*;
import ec.util.*;
import ec.vector.*;

public class LatVector extends Problem implements SimpleProblemForm {

	public final float[] TRACK = {32f, 35f, 38f};

	// ind is the individual to be evaluated.
	// We're given state and threadnum primarily so we
	// have access to a random number generator
	// (in the form:  state.random[threadnum] ) 
	// and to the output facility
	public void evaluate(final EvolutionState state,
			final Individual ind,
			final int subpopulation,
			final int threadnum)
	{
		if (ind.evaluated) return;   //don't evaluate the individual if it's already evaluated
		if (!(ind instanceof FloatVectorIndividual))
			state.output.fatal("Whoa!  It's not a FloatVectorIndividual!!!",null);
		FloatVectorIndividual ind2 = (FloatVectorIndividual)ind;
		// compute fitness
		float mse=0; // mean square error        
		for(int x=0; x<ind2.genome.length; x++) {
			float diff = ind2.genome[x] - TRACK[x];
			mse += diff * diff;
		}
		if (!(ind2.fitness instanceof SimpleFitness))
			state.output.fatal("Whoa!  It's not a SimpleFitness!!!",null);

		((SimpleFitness)ind2.fitness).setFitness(state,
				// ...the fitness... (negative!)
				-mse,
				///... is the individual ideal?  Indicate here...
				mse < 0.1);
		ind2.evaluated = true;
	}

}