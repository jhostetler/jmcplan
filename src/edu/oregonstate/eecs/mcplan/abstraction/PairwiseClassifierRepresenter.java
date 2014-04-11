package edu.oregonstate.eecs.mcplan.abstraction;

import java.util.ArrayList;
import java.util.HashMap;

import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import gnu.trove.list.array.TIntArrayList;

/**
 * Represents states by comparing them to the means of clusters created online
 * using a binary classifier.
 * 
 * Because it constructs partitions online, the resulting representation will
 * depend on the order of samples presented.
 * 
 * @author jhostetler
 *
 * @param <S>
 * @param <X>
 */
public final class PairwiseClassifierRepresenter<S extends State, X extends FactoredRepresentation<S>>
	implements Representer<S, ClusterAbstraction<S>>
{
	private final Representer<S, X> repr_;
//	private final ArrayList<Attribute> attributes_;
	private final Classifier classifier_;
	
	private final ArrayList<ClusterAbstraction<S>> clusters_ = new ArrayList<ClusterAbstraction<S>>();
	private final ArrayList<double[]> exemplars_ = new ArrayList<double[]>();
	private final TIntArrayList n_ = new TIntArrayList();
	
	private final HashMap<X, ClusterAbstraction<S>> cluster_map_ = new HashMap<X, ClusterAbstraction<S>>();
	private final boolean duplicates_ = false;
	
	private int neg_idx_ = -1;
	
	// Need an Instances object to add Instance objects to before
	// classification.
	private final Instances dataset_;
	
	public PairwiseClassifierRepresenter( final Representer<S, X> repr,
										  final Instances empty_dataset,
										  final Classifier classifier )
	{
		repr_ = repr;
//		attributes_ = attributes;
		classifier_ = classifier;
		dataset_ = empty_dataset; // WekaUtil.createEmptyInstances( "runtime", attributes );
	}
	
	@Override
	public PairwiseClassifierRepresenter<S, X> create()
	{
		// Note: It is *very* important to call the two-argument
		// Instances constructor, otherwise it takes *forever* even
		// though the Instances object is empty!
		return new PairwiseClassifierRepresenter<S, X>(
			repr_.create(), new Instances( dataset_, 0 ), classifier_ );
	}
	
	public ClusterAbstraction<S> clusterState( final X x )
	{
		try { // try-block for the sake of Weka
			final double[] phi = x.phi();
			// TODO: How to do this step is a big design decision. We might
			// eventually like something formally justified, e.g. the
			// "Chinese restaurant process" approach.
			for( int i = 0; i < clusters_.size(); ++i ) {
				final double[] ex = exemplars_.get( i );
				final Instance instance = features( phi, ex );
				dataset_.add( instance );
				instance.setDataset( dataset_ );
				final double label = classifier_.classifyInstance( instance );
				dataset_.remove( 0 );
				// TODO: Is there a more generic way to find the right label?
				if( 1.0 == label ) {
					final ClusterAbstraction<S> c = clusters_.get( i );
					// Modify exemplar to be cluster mean.
					// TODO: Customizable metric / distance calculation
					final int ni = 1 + n_.get( i );
					n_.set( i, ni );
					for( int j = 0; j < ex.length; ++j ) {
						ex[j] += (phi[j] - ex[j]) / ni;
					}
					return c;
				}
			}
			// No match found in loop -> Make new cluster
			final ClusterAbstraction<S> c = new ClusterAbstraction<S>( exemplars_.size() );
			clusters_.add( c );
			exemplars_.add( phi );
			n_.add( 1 );
			return c;
		}
		catch( final Exception ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	private Instance features( final double[] phi_i, final double[] phi_j )
	{
		assert( phi_i.length == phi_j.length );
		// Feature vector is absolute difference of the two state
		// feature vectors.
		final double[] phi = new double[phi_i.length];
		for( int k = 0; k < phi.length; ++k ) {
			phi[k] = Math.abs( phi_i[k] - phi_j[k] );
		}
		return new DenseInstance( 1.0, phi );
	}
	
	@Override
	public ClusterAbstraction<S> encode( final S s )
	{
		if( s.isTerminal() ) {
			final int idx = neg_idx_--;
			if( neg_idx_ >= 0 ) {
				neg_idx_ = -1;
			}
			return new ClusterAbstraction<S>( idx );
		}
		
		// TODO: This needs to know the tree depth, or else you're building a DAG!
		final X x = repr_.encode( s );
		if( duplicates_ ) {
			return clusterState( x );
		}
		else {
			ClusterAbstraction<S> c = cluster_map_.get( x );
			if( c == null ) {
				c = clusterState( x );
				cluster_map_.put( x, c );
			}
			return c;
		}
	}
}