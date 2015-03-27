package ec.app.tutorial4;
import ec.*;
import ec.gp.*;
import ec.util.*;

public class Y extends GPNode
    {
    public String toString() { return "y"; }

    public int expectedChildren() { return 0; }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem)
        {
        DoubleData rd = ((DoubleData)(input));
        rd.x = ((MultiValuedRegression)problem).currentY;
        }
    }