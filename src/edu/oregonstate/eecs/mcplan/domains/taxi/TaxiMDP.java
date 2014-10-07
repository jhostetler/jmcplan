/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.taxi;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import edu.oregonstate.eecs.mcplan.ActionSpace;
import edu.oregonstate.eecs.mcplan.MarkovDecisionProblem;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.abstraction.WekaUtil;
import edu.oregonstate.eecs.mcplan.dp.SparseValueIterationSolver;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class TaxiMDP extends MarkovDecisionProblem<TaxiState, TaxiAction>
{
	private final TaxiStateSpace S_;
	private final TaxiActionSpace A_;
	
	public TaxiMDP( final TaxiState s )
	{
		S_ = new TaxiStateSpace( s );
		A_ = new TaxiActionSpace();
	}
	
	@Override
	public TaxiStateSpace S()
	{
		return S_;
	}

	@Override
	public ActionSpace<TaxiState, TaxiAction> A()
	{
		return A_;
	}
	
	private final int[][] neighborhood = new int[][] {
		{ 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }
	};
	
	private Pair<ArrayList<int[][]>, ArrayList<Double>> nextStep( final TaxiState current, final int taxi_idx )
	{
		final ArrayList<int[][]> succ = new ArrayList<int[][]>();
		final ArrayList<Double> prob = new ArrayList<Double>();
		final double uniform = 1.0 / (neighborhood.length + 1);
		
		final int[] taxi_pos = current.other_taxis[taxi_idx];
		final int[] new_pos = Fn.copy( taxi_pos );
		
		double stay_prob = uniform;
		
		for( final int[] neighbor : neighborhood ) {
			Fn.vplus_inplace( new_pos, neighbor );
			if( current.isLegalMove( taxi_idx, taxi_pos, new_pos ) ) {
				final int[][] new_other = Fn.copy( current.other_taxis );
				Fn.memcpy( new_other[taxi_idx], new_pos );
				succ.add( new_other );
				prob.add( uniform );
			}
			else {
				// If move is illegal, taxi stays put
				stay_prob += uniform;
			}
			Fn.vminus_inplace( new_pos, neighbor );
		}
		
		// Case where taxi doesn't move
		succ.add( Fn.copy( current.other_taxis ) );
		prob.add( stay_prob );
		
		return Pair.makePair( succ, prob );
	}
	
	private Pair<ArrayList<TaxiState>, ArrayList<Double>> otherMoves( final TaxiState s )
	{
		ArrayList<TaxiState> succ = new ArrayList<TaxiState>();
		ArrayList<Double> prob = new ArrayList<Double>();
		
		succ.add( s );
		prob.add( 1.0 );
		
		ArrayList<TaxiState> others = new ArrayList<TaxiState>();
		ArrayList<Double> other_probs = new ArrayList<Double>();
		
		for( int i = 0; i < s.Nother_taxis; ++i ) {
			for( int si = 0; si < succ.size(); ++si ) {
				// For each successor, compute possible moves for taxi i
				final TaxiState succ_i = succ.get( si );
				final Pair<ArrayList<int[][]>, ArrayList<Double>> p = nextStep( succ_i, i );
				
				// For each possible move of taxi i, create a new state
				for( int j = 0; j < p.first.size(); ++j ) {
					final TaxiState sprime = new TaxiState( succ_i );
					Fn.memcpy( sprime.other_taxis, p.first.get( j ) );
					others.add( sprime );
					other_probs.add( prob.get( si ) * p.second.get( j ) );
				}
			}
			
			// Replace successors with successors including taxi i
			succ = others;
			prob = other_probs;
			others = new ArrayList<TaxiState>();
			other_probs = new ArrayList<Double>();
		}
		
		return Pair.makePair( succ, prob );
	}

	@Override
	public Pair<ArrayList<TaxiState>, ArrayList<Double>> sparseP( final TaxiState s,
			final TaxiAction a )
	{
		final TaxiState sprime = new TaxiState( s );
		if( a instanceof MoveAction ) {
			final MoveAction move = (MoveAction) a;
			final int[] new_taxi = Fn.copy( sprime.taxi );
			new_taxi[0] += move.dx;
			new_taxi[1] += move.dy;
			if( s.isLegalMove( s.taxi, new_taxi ) ) {
				Fn.memcpy( sprime.taxi, new_taxi );
			}
		}
		else if( a instanceof PickupAction ) {
			if( s.passenger != TaxiState.IN_TAXI && Arrays.equals( s.locations.get( s.passenger ), s.taxi ) ) {
				sprime.passenger = TaxiState.IN_TAXI;
			}
		}
		else if( a instanceof PutdownAction ) {
			if( s.passenger == TaxiState.IN_TAXI && Arrays.equals( s.locations.get( s.destination ), s.taxi ) ) {
				sprime.passenger = s.destination;
			}
		}
		else {
			throw new IllegalArgumentException( a.toString() );
		}
		
		return otherMoves( sprime );
	}

	@Override
	public double[] P( final TaxiState s, final TaxiAction a )
	{
		throw new UnsupportedOperationException( "Use sparseP()" );
	}

	@Override
	public double P( final TaxiState s, final TaxiAction a, final TaxiState sprime )
	{
		throw new UnsupportedOperationException( "Use sparseP()" );
	}
	
	@Override
	public double R( final TaxiState s )
	{
		return 0.0;
	}

	@Override
	public double R( final TaxiState s, final TaxiAction a )
	{
		if( a instanceof MoveAction ) {
			return -1;
		}
		else if( a instanceof PickupAction ) {
			if( s.passenger != TaxiState.IN_TAXI ) {
				if( Arrays.equals( s.locations.get( s.passenger ), s.taxi ) ) {
					return -1;
				}
			}
			return -10 - 1;
		}
		else if( a instanceof PutdownAction ) {
			if( s.passenger == TaxiState.IN_TAXI ) {
				if( Arrays.equals( s.locations.get( s.destination ), s.taxi ) ) {
					return 20 - 1;
				}
			}
			return -10 - 1;
		}
		else {
			throw new IllegalArgumentException( a.toString() );
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static void main( final String[] argv )
	{
		final int Nother_taxis = 2;
		final double slip = 0.1;
		final double discount = 0.9;
		final TaxiState template = TaxiWorlds.dietterich2000( Nother_taxis, slip );
		final TaxiMDP mdp = new TaxiMDP( template );
		final int Nfeatures = new PrimitiveTaxiRepresentation( template ).phi().length;
		final SparseValueIterationSolver<TaxiState, TaxiAction> vi
			= new SparseValueIterationSolver<TaxiState, TaxiAction>( mdp, discount );
		vi.run();
		
		final PrimitiveTaxiRepresenter repr = new PrimitiveTaxiRepresenter( template );
		final ArrayList<Attribute> attr = new ArrayList<Attribute>();
		attr.addAll( repr.attributes() );
		attr.add( WekaUtil.createNominalAttribute( "__label__", mdp.A().cardinality() ) );
		final Instances instances = WekaUtil.createEmptyInstances( "taxi_" + Nother_taxis + "_pistar", attr );
		final Policy<TaxiState, TaxiAction> pistar = vi.pistar();
		final Generator<TaxiState> g = mdp.S().generator();
		while( g.hasNext() ) {
			final TaxiState s = g.next();
			pistar.setState( s, 0L );
			final TaxiAction astar = pistar.getAction();
			final double[] phi = new double[Nfeatures + 1];
			Fn.memcpy( phi, new PrimitiveTaxiRepresentation( s ).phi(), Nfeatures );
			phi[Nfeatures] = mdp.A().index( astar );
			WekaUtil.addInstance( instances, new DenseInstance( 1.0, phi ) );
		}
		
		WekaUtil.writeDataset( new File( "." ), instances );
	}

}
