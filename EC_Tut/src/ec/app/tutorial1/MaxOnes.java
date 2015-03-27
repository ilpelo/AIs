package ec.app.tutorial1;

import ec.*;
import ec.simple.*;
import ec.util.*;
import ec.vector.*;

public class MaxOnes extends Problem implements SimpleProblemForm {


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
		if (!(ind instanceof BitVectorIndividual))
			state.output.fatal("Whoa!  It's not a BitVectorIndividual!!!",null);
		BitVectorIndividual ind2 = (BitVectorIndividual)ind;
		int sum=0;        
		for(int x=0; x<ind2.genome.length; x++)
			sum += (ind2.genome[x] ? 1 : 0);
		if (!(ind2.fitness instanceof SimpleFitness))
			state.output.fatal("Whoa!  It's not a SimpleFitness!!!",null);

		((SimpleFitness)ind2.fitness).setFitness(state,
				// ...the fitness...
				((double)sum)/ind2.genome.length,
				///... is the individual ideal?  Indicate here...
				sum == ind2.genome.length);
		ind2.evaluated = true;
	}

}