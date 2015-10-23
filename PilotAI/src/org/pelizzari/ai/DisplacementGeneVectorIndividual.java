package org.pelizzari.ai;

import org.pelizzari.gis.Displacement;
import org.pelizzari.gis.Point;
import org.pelizzari.ship.ShipPosition;
import org.pelizzari.ship.ShipTrack;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.vector.GeneVectorIndividual;
import ec.vector.VectorIndividual;

public class DisplacementGeneVectorIndividual extends GeneVectorIndividual {

	@Override
	public void reset(EvolutionState state, int thread) {
		super.reset(state, thread);
		// replace the last gene (displacement) in order to reach the final destination
		GeneVectorIndividual geneVectorInd = (GeneVectorIndividual) this;
		ShipTrack trackInd = DisplacementSequenceProblem.makeTrack(state, geneVectorInd);
		ShipPosition secondLastPos = trackInd.getPosition(trackInd.getPosList().size()-2);
		Point lastPoint = DisplacementSequenceProblem.endPosition.getPoint();
		Displacement lastDisplacement = secondLastPos.getPoint().computeDisplacement(lastPoint);
		DisplacementGene lastGene = (DisplacementGene) genome[genome.length-1];  
		lastGene.setAllele(lastDisplacement);
	}

}
