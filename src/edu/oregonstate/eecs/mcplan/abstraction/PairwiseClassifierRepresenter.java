/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

import java.util.ArrayList;
import java.util.Arrays;

import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.util.Fn;
import gnu.trove.list.array.TIntArrayList;

/**
 * @author jhostetler
 *
 */
public class PairwiseClassifierRepresenter<S extends State>
	implements Representer<FactoredRepresentation<S>, Representation<S>>
{
	private final PairDataset.InstanceCombiner combiner_;
	private final Classifier classifier_;
	
	private final ArrayList<ClusterAbstraction<S>> clusters_ = new ArrayList<ClusterAbstraction<S>>();
	private final ArrayList<double[]> exemplars_ = new ArrayList<double[]>();
	private final TIntArrayList n_ = new TIntArrayList();
	
	private final Instances dummy_;
	
	public PairwiseClassifierRepresenter(
		final PairDataset.InstanceCombiner combiner, final Classifier classifier )
	{
		combiner_ = combiner;
		classifier_ = classifier;
		dummy_ = WekaUtil.createEmptyInstances( "dummy", combiner_.attributes() );
	}
	
	@Override
	public PairwiseClassifierRepresenter<S> create()
	{
		return new PairwiseClassifierRepresenter<S>( combiner_, classifier_ );
	}
	
	public ClusterAbstraction<S> clusterState( final double[] phi )
	{
//		System.out.println( "\tcluster " + (count_++) +": size = " + clusters_.size() );
		
		final int none = -1;
		try { // try-block for the sake of Weka
			final int[] idx = Fn.range( 0, exemplars_.size() );
			int cluster = none;
			for( final int i : idx ) {
				final double[] x = exemplars_.get( i );
				if( Arrays.equals( x, phi ) ) {
					cluster = i;
					break;
				}
				// NaN for "unknown label"
				final Instance p = new DenseInstance( 1.0, combiner_.apply( phi, x, Double.NaN ) );
				WekaUtil.addInstance( dummy_, p );
				final int prediction = (int) classifier_.classifyInstance( p );
				dummy_.remove( 0 );
				if( prediction == 1 ) {
					cluster = i;
					break;
				}
			}
			
			if( cluster != none ) {
				final ClusterAbstraction<S> c = clusters_.get( cluster );
				final int ni = 1 + n_.get( cluster );
				n_.set( cluster, ni );
				return c;
			}
			else {
				// No match found in loop -> Make new cluster
				// FIXME: Added string hint for debugging; this has non-trivial overhead
				final ClusterAbstraction<S> c = new ClusterAbstraction<S>( clusters_.size() ); //, Arrays.toString( phi ) );
				clusters_.add( c );
				exemplars_.add( Arrays.copyOf( phi, phi.length ) );
				n_.add( 1 );
				return c;
			}
		}
		catch( final RuntimeException ex ) 	{ throw ex; }
		catch( final Exception ex ) 		{ throw new RuntimeException( ex ); }
	}
	
	@Override
	public Representation<S> encode( final FactoredRepresentation<S> x )
	{
		final Representation<S> c = clusterState( x.phi() );
		return c;
	}
}
