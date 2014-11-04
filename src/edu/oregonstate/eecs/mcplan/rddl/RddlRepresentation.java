/**
 * 
 */
package edu.oregonstate.eecs.mcplan.rddl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import rddl.RDDL.LCONST;
import rddl.RDDL.PVAR_NAME;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import gnu.trove.map.TObjectIntMap;

/**
 * @author jhostetler
 *
 */
public class RddlRepresentation extends FactoredRepresentation<RDDLState>
{
	private final double[] phi;
	
	public RddlRepresentation( final RddlSpec spec, final rddl.State s )
	{
//		System.out.println( "RddlRepresentation:" );
//		phi = Fn.copy( spec.state_defaults );
		phi = new double[spec.Nstate_vars];
		for( final Map.Entry<PVAR_NAME, HashMap<ArrayList<LCONST>, Object>> e : s._state.entrySet() ) {
			final PVAR_NAME name = e.getKey();
//			System.out.println( "\t" + name );
			final TObjectIntMap<ArrayList<LCONST>> assign_map = spec.state_var_indices.get( name );
//			for( final ArrayList<LCONST> k : assign_map.keySet() ) {
//				System.out.println( "\t\t" + k + " => " + assign_map.get( k ) );
//			}
			for( final Map.Entry<ArrayList<LCONST>, Object> b : e.getValue().entrySet() ) {
				final ArrayList<LCONST> a = b.getKey();
				final Object v = b.getValue();
				
				final int idx = assign_map.get( a );
				
				phi[idx] = spec.valueToDouble( name, v );
			}
		}
		
//		System.out.println( Arrays.toString( phi ) );
	}
	
	private RddlRepresentation( final RddlRepresentation that )
	{
		this.phi = that.phi;
	}
	
	@Override
	public double[] phi()
	{
		return phi;
	}

	@Override
	public RddlRepresentation copy()
	{
		return new RddlRepresentation( this );
	}

	@Override
	public boolean equals( final Object obj )
	{
		final RddlRepresentation that = (RddlRepresentation) obj;
		return Arrays.equals( phi, that.phi );
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( phi );
	}

}
