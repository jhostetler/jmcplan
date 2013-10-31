package edu.oregonstate.eecs.mcplan.abstraction;

import java.util.ArrayList;
import java.util.HashMap;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.Representer;

public final class Aggregator<S, X extends FactoredRepresentation<S>>
	implements Representer<S, AggregateState<S>>
{
	private final Representer<S, X> repr_;
	private final ArrayList<Attribute> attributes_;
	private final Classifier classifier_;
	
	private final ArrayList<AggregateState<S>> clusters_ = new ArrayList<AggregateState<S>>();
	private final ArrayList<X> exemplars_ = new ArrayList<X>();
	
	private final HashMap<X, AggregateState<S>> cluster_map_ = new HashMap<X, AggregateState<S>>();
	
	// Need an Instances object to add Instance objects to before
	// classification.
	private final Instances dataset_;
	
	public Aggregator( final Representer<S, X> repr,
					   final ArrayList<Attribute> attributes,
					   final Classifier classifier )
	{
		repr_ = repr;
		attributes_ = attributes;
		classifier_ = classifier;
		dataset_ = WekaUtil.createEmptyInstances( "runtime", attributes );
	}
	
	@Override
	public Aggregator<S, X> create()
	{
		return new Aggregator<S, X>( repr_.create(), attributes_, classifier_ );
	}
	
	public AggregateState<S> clusterState( final X x )
	{
		try {
			// TODO: How to do this step is a big design decision. We might
			// eventually like something formally justified, e.g. the
			// "Chinese restaurant process" approach.
			for( int i = 0; i < clusters_.size(); ++i ) {
				final X ex = exemplars_.get( i );
				final Instance instance = features( x, ex );
				dataset_.add( instance );
				instance.setDataset( dataset_ );
				final double label = classifier_.classifyInstance( instance );
				dataset_.remove( 0 );
				// TODO: Is there a more generic way to find the right label?
				if( 1.0 == label ) {
					final AggregateState<S> c = clusters_.get( i );
					c.add( x );
					// TODO: Adjust exemplar element?
//						System.out.println( "! Aggregator hit!" );
					return c;
				}
			}
			final AggregateState<S> c = new AggregateState<S>();
			c.add( x );
			clusters_.add( c );
			exemplars_.add( x );
			return c;
		}
		catch( final Exception ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	private Instance features( final X xi, final X xj )
	{
		final double[] phi_i = xi.phi();
		final double[] phi_j = xj.phi();
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
	public AggregateState<S> encode( final S s )
	{
		// TODO: This needs to know the tree depth, or else you're building a DAG!
		final X x = repr_.encode( s );
		AggregateState<S> c = cluster_map_.get( x );
		if( c == null ) {
			c = clusterState( x );
			cluster_map_.put( x, c );
		}
		return c;
	}
}