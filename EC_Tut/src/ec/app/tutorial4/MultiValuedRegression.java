package ec.app.tutorial4;

import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

public class MultiValuedRegression extends GPProblem implements
		SimpleProblemForm {
	public static final String P_DATA = "data";

	public double currentX;
	public double currentY;

	public void setup(final EvolutionState state, final Parameter base) {
		// very important, remember this
		super.setup(state, base);

		// verify our input is the right class (or subclasses from it)
		if (!(input instanceof DoubleData))
			state.output.fatal("GPData class must subclass from "
					+ DoubleData.class, base.push(P_DATA), null);
	}

	public void evaluate(final EvolutionState state, final Individual ind,
			final int subpopulation, final int threadnum) {
		if (!ind.evaluated) // don't bother reevaluating
		{
			DoubleData input = (DoubleData) (this.input);

			int hits = 0;
			double sum = 0.0;
			double expectedResult;
			double result;
			for (int y = 0; y < 10; y++) {
				currentX = state.random[threadnum].nextDouble();
				currentY = state.random[threadnum].nextDouble();
				expectedResult = currentX * currentX * currentY + currentX
						* currentY + currentY;
				((GPIndividual) ind).trees[0].child.eval(state, threadnum,
						input, stack, ((GPIndividual) ind), this);

				result = Math.abs(expectedResult - input.x);
				if (result <= 0.01)
					hits++;
				sum += result;
			}

			// the fitness better be KozaFitness!
			KozaFitness f = ((KozaFitness) ind.fitness);
			f.setStandardizedFitness(state, sum);
			f.hits = hits;
			ind.evaluated = true;
		}
	}
}
