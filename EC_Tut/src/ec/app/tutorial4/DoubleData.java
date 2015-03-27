package ec.app.tutorial4;

import ec.util.*;
import ec.*;
import ec.gp.*;

public class DoubleData extends GPData {
	public double x; // return value

	public void copyTo(final GPData gpd) // copy my stuff to another DoubleData
	{
		((DoubleData) gpd).x = x;
	}
}