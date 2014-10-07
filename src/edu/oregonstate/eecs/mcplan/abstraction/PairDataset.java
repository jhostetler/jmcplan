/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.ReservoirSampleAccumulator;

/**
 * @author jhostetler
 *
 */
public class PairDataset
{
	public final Instances instances;
	public final ArrayList<int[]> matches;
	public final PairDataset.InstanceCombiner combiner;
	
	public PairDataset( final Instances instances, final ArrayList<int[]> matches,
						final PairDataset.InstanceCombiner combiner )
	{
		this.instances = instances;
		this.matches = matches;
		this.combiner = combiner;
	}
	
	// -----------------------------------------------------------------------
	// Pairwise feature constructors
	// -----------------------------------------------------------------------
	
	public static abstract class InstanceCombiner
	{
		public abstract ArrayList<Attribute> attributes();
		public abstract DenseInstance apply( Instance a, Instance b, final int label );
		public abstract double[] apply( final double[] a, final double[] b, final double label );
		public abstract double[] apply( final double[] a, final double[] b );
		public abstract String keyword();
	}
	
	public static class DifferenceFeatures extends InstanceCombiner
	{
		private final ArrayList<Attribute> attributes_;
		
		/**
		 * Attributes must include label.
		 * @param attributes
		 */
		public DifferenceFeatures( final ArrayList<Attribute> attributes )
		{
			attributes_ = attributes;
		}
		
		@Override
		public DenseInstance apply( final Instance a, final Instance b, final int pair_label )
		{
			final double[] phi = new double[a.numValues()];
			assert( a.classIndex() == phi.length - 1 );
			
			for( int i = 0; i < phi.length - 1; ++i ) {
				phi[i] = a.value( i ) - b.value( i );
			}
			phi[phi.length - 1] = pair_label;
			return new DenseInstance( 1.0, phi );
		}
		
		@Override
		public double[] apply( final double[] a, final double[] b, final double label )
		{
			assert( a.length == b.length );
			final double[] phi = new double[a.length + 1];
			
			for( int i = 0; i < phi.length - 1; ++i ) {
				phi[i] = a[i] - b[i];
			}
			phi[phi.length - 1] = label;
			return phi;
		}
		
		@Override
		public double[] apply( final double[] a, final double[] b )
		{
			return Fn.vminus( a, b );
		}
		
		@Override
		public String keyword()
		{
			return "difference";
		}

		@Override
		public ArrayList<Attribute> attributes()
		{
			final ArrayList<Attribute> result = new ArrayList<Attribute>();
			for( final Attribute a : attributes_ ) {
				result.add( (Attribute) a.copy() );
			}
			final ArrayList<String> nominal = new ArrayList<String>();
			nominal.add( "0" );
			nominal.add( "1" );
			result.add( new Attribute( "__label__", nominal ) );
			return result;
		}
	}
	
	public static class SymmetricFeatures extends InstanceCombiner
	{
		private final ArrayList<Attribute> attributes_;
		
		/**
		 * @param attributes Unlabeled attributes of single-instance data.
		 */
		public SymmetricFeatures( final ArrayList<Attribute> attributes )
		{
			attributes_ = attributes;
		}
		
		@Override
		public DenseInstance apply( final Instance a, final Instance b, final int pair_label )
		{
			assert( a.classIndex() == b.classIndex() );
			assert( a.classIndex() == a.numAttributes() - 1 );
			final double[] phi = new double[2*(a.numAttributes() - 1) + 1];
			
			int idx = 0;
			for( int i = 0; i < a.numAttributes() - 1; ++i ) {
				phi[idx++] = a.value( i ) + b.value( i );
				phi[idx++] = a.value( i ) * b.value( i );
			}
			phi[idx++] = pair_label;
			assert( idx == phi.length );
			// FIXME: Make this configurable?
			final double weight = (pair_label == 1 ? 1.0 : 1.0);
			return new DenseInstance( weight, phi );
		}
		
		@Override
		public double[] apply( final double[] a, final double[] b, final double label )
		{
			assert( a.length == b.length );
			final double[] phi = new double[2*a.length + 1];
			int idx = 0;
			for( int i = 0; i < a.length; ++i ) {
				phi[idx++] = a[i] + b[i];
				phi[idx++] = a[i] * b[i];
			}
			phi[idx++] = label;
			assert( idx == phi.length );
			return phi;
		}
		
		@Override
		public double[] apply( final double[] a, final double[] b )
		{
			assert( a.length == b.length );
			final double[] phi = new double[2*a.length];
			int idx = 0;
			for( int i = 0; i < a.length; ++i ) {
				phi[idx++] = a[i] + b[i];
				phi[idx++] = a[i] * b[i];
			}
			return phi;
		}
		
		@Override
		public String keyword()
		{
			return "symmetric";
		}

		@Override
		public ArrayList<Attribute> attributes()
		{
			final ArrayList<Attribute> result = new ArrayList<Attribute>();
			for( final Attribute a : attributes_ ) {
				result.add( new Attribute( a.name() + "_sum" ) );
				result.add( new Attribute( a.name() + "_product" ) );
			}
			final ArrayList<String> nominal = new ArrayList<String>();
			nominal.add( "0" );
			nominal.add( "1" );
			result.add( new Attribute( "__label__", nominal ) );
			return result;
		}
	}
	
	public static class ExtendedSymmetricFeatures extends InstanceCombiner
	{
		private final ArrayList<Attribute> attributes_;
		
		/**
		 * @param attributes Unlabeled attributes of single-instance data.
		 */
		public ExtendedSymmetricFeatures( final ArrayList<Attribute> attributes )
		{
			attributes_ = attributes;
		}
		
		@Override
		public DenseInstance apply( final Instance a, final Instance b, final int pair_label )
		{
			assert( a.classIndex() == b.classIndex() );
			assert( a.classIndex() == a.numAttributes() - 1 );
			final double[] phi = new double[3*(a.numAttributes() - 1) + 1];
			
			int idx = 0;
			for( int i = 0; i < a.numAttributes() - 1; ++i ) {
				phi[idx++] = a.value( i ) + b.value( i );
				phi[idx++] = a.value( i ) * b.value( i );
				phi[idx++] = Math.abs( a.value( i ) - b.value( i ) );
			}
			phi[idx++] = pair_label;
			assert( idx == phi.length );
			// FIXME: Make this configurable?
			final double weight = (pair_label == 1 ? 1.0 : 1.0);
			return new DenseInstance( weight, phi );
		}
		
		@Override
		public double[] apply( final double[] a, final double[] b, final double label )
		{
			assert( a.length == b.length );
			final double[] phi = new double[3*a.length + 1];
			int idx = 0;
			for( int i = 0; i < a.length; ++i ) {
				phi[idx++] = a[i] + b[i];
				phi[idx++] = a[i] * b[i];
				phi[idx++] = Math.abs( a[i] - b[i] );
			}
			phi[idx++] = label;
			assert( idx == phi.length );
			return phi;
		}
		
		@Override
		public double[] apply( final double[] a, final double[] b )
		{
			assert( a.length == b.length );
			final double[] phi = new double[3*a.length];
			int idx = 0;
			for( int i = 0; i < a.length; ++i ) {
				phi[idx++] = a[i] + b[i];
				phi[idx++] = a[i] * b[i];
				phi[idx++] = Math.abs( a[i] - b[i] );
			}
			return phi;
		}
		
		@Override
		public String keyword()
		{
			return "exsymmetric";
		}

		@Override
		public ArrayList<Attribute> attributes()
		{
			final ArrayList<Attribute> result = new ArrayList<Attribute>();
			for( final Attribute a : attributes_ ) {
				result.add( new Attribute( a.name() + "_sum" ) );
				result.add( new Attribute( a.name() + "_product" ) );
				result.add( new Attribute( a.name() + "_distance") );
			}
			final ArrayList<String> nominal = new ArrayList<String>();
			nominal.add( "0" );
			nominal.add( "1" );
			result.add( new Attribute( "__label__", nominal ) );
			return result;
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static <S, X extends FactoredRepresentation<S>, A extends VirtualConstructor<A>>
	ArrayList<PairInstance> makePairDataset(
		final RandomGenerator rng, final int max_pairwise_instances, final Instances single )
	{
		final ReservoirSampleAccumulator<PairInstance> negative
			= new ReservoirSampleAccumulator<PairInstance>( rng, max_pairwise_instances );
		final ReservoirSampleAccumulator<PairInstance> positive
			= new ReservoirSampleAccumulator<PairInstance>( rng, max_pairwise_instances );
		
		for( int i = 0; i < single.size(); ++i ) {
			for( int j = i + 1; j < single.size(); ++j ) {
				final Instance ii = single.get( i );
				final Instance ij = single.get( j );
				final int label;
				if( ii.classValue() == ij.classValue() ) {
					label = 1;
					if( positive.acceptNext() ) {
						final PairInstance pair_instance = new PairInstance(
							ii.toDoubleArray(), ij.toDoubleArray(), label );
						positive.addPending( pair_instance );
					}
				}
				else {
					label = 0;
					if( negative.acceptNext() ) {
						final PairInstance pair_instance = new PairInstance(
							ii.toDoubleArray(), ij.toDoubleArray(), label );
						negative.addPending( pair_instance );
					}
				}
			}
		}
		
		final ArrayList<PairInstance> result = new ArrayList<PairInstance>( negative.n() + positive.n() );
		result.addAll( negative.samples() );
		result.addAll( positive.samples() );
		return result;
	}
	
	public static <S, X extends FactoredRepresentation<S>, A extends VirtualConstructor<A>>
	Instances makePairDataset( final RandomGenerator rng, final int max_pairwise_instances,
							   final Instances single, final InstanceCombiner combiner )
	{
//		final int max_pairwise = config.getInt( "training.max_pairwise" );
		final ReservoirSampleAccumulator<Instance> negative
			= new ReservoirSampleAccumulator<Instance>( rng, max_pairwise_instances );
		final ReservoirSampleAccumulator<Instance> positive
			= new ReservoirSampleAccumulator<Instance>( rng, max_pairwise_instances );
		
		for( int i = 0; i < single.size(); ++i ) {
//			if( i % 100 == 0 ) {
//				System.out.println( "i = " + i );
//			}
			for( int j = i + 1; j < single.size(); ++j ) {
				final Instance ii = single.get( i );
				final Instance ij = single.get( j );
				final int label;
				if( ii.classValue() == ij.classValue() ) {
					label = 1;
					if( positive.acceptNext() ) {
						final Instance pair_instance = combiner.apply( ii, ij, label );
						positive.addPending( pair_instance );
					}
				}
				else {
					label = 0;
					if( negative.acceptNext() ) {
						final Instance pair_instance = combiner.apply( ii, ij, label );
						negative.addPending( pair_instance );
					}
				}
			}
		}
		
		final int N = Math.min( negative.samples().size(), positive.samples().size() );
		final String dataset_name = "train_" + combiner.keyword() + "_" + max_pairwise_instances;
		final Instances x = new Instances( dataset_name, combiner.attributes(), 2*N );
		x.setClassIndex( x.numAttributes() - 1 );
		for( final Instance ineg : negative.samples() ) {
			x.add( ineg );
		}
		for( final Instance ipos : positive.samples() ) {
			x.add( ipos );
		}
		
		return x;
//		return new PairDataset( x, combiner );
	}
	
	/**
	 * Constructs one positive pair and one negative pair involving each
	 * data point in 'single'.
	 * @param rng
	 * @param max_pairwise_instances
	 * @param single
	 * @param combiner
	 * @return
	 */
	public static <S, X extends FactoredRepresentation<S>, A extends VirtualConstructor<A>>
	PairDataset makeBalancedPairDataset( final RandomGenerator rng,
										 final int negative_per_instance, final int positive_per_instance,
										 final Instances single, final InstanceCombiner combiner )
	{
		final int Nnegative = negative_per_instance * single.size();
		final int Npositive = positive_per_instance * single.size();
//		final int max_pairwise = config.getInt( "training.max_pairwise" );
		final ReservoirSampleAccumulator<Pair<Instance, int[]>> negative
			= new ReservoirSampleAccumulator<Pair<Instance, int[]>>( rng, Nnegative );
		final ReservoirSampleAccumulator<Pair<Instance, int[]>> positive
			= new ReservoirSampleAccumulator<Pair<Instance, int[]>>( rng, Npositive );
		
		for( int i = 0; i < single.size(); ++i ) {
//			if( i % 100 == 0 ) {
//				System.out.println( "i = " + i );
//			}
			for( int j = i + 1; j < single.size(); ++j ) {
				final Instance ii = single.get( i );
				final Instance ij = single.get( j );
				final int label;
				if( ii.classValue() == ij.classValue() ) {
					label = 1;
					if( positive.acceptNext() ) {
						final Instance pair_instance = combiner.apply( ii, ij, label );
						positive.addPending( Pair.makePair( pair_instance, new int[] { i, j } ) );
					}
				}
				else {
					label = 0;
					if( negative.acceptNext() ) {
						final Instance pair_instance = combiner.apply( ii, ij, label );
						negative.addPending( Pair.makePair( pair_instance, new int[] { i, j } ) );
					}
				}
			}
		}
		
		final int N = Math.min( negative.samples().size(), positive.samples().size() );
		final String dataset_name = "train_" + combiner.keyword() + "_" + Nnegative + "x" + Npositive;
		final Instances x = new Instances( dataset_name, combiner.attributes(), Nnegative + Npositive );
		x.setClassIndex( x.numAttributes() - 1 );
		final ArrayList<int[]> matches = new ArrayList<int[]>();
		for( final Pair<Instance, int[]> ineg : negative.samples() ) {
			WekaUtil.addInstance( x, ineg.first );
			matches.add( ineg.second );
		}
		for( final Pair<Instance, int[]> ipos : positive.samples() ) {
			WekaUtil.addInstance( x, ipos.first );
			matches.add( ipos.second );
		}
		
		return new PairDataset( x, matches, combiner );
	}
	
	/**
	 * Constructs one positive pair and one negative pair involving each
	 * data point in 'single'.
	 * @param rng
	 * @param max_pairwise_instances
	 * @param single
	 * @param combiner
	 * @return
	 */
	public static <S, X extends FactoredRepresentation<S>, A extends VirtualConstructor<A>>
	PairDataset makePlausiblePairDataset( final RandomGenerator rng,
										 final int negative_per_instance, final int positive_per_instance,
										 final Instances single, final InstanceCombiner combiner,
										 final Fn.Function2<Boolean, Instance, Instance> plausible_p )
	{
		final int Nnegative = negative_per_instance * single.size();
		final int Npositive = positive_per_instance * single.size();
//		final int max_pairwise = config.getInt( "training.max_pairwise" );
		final ReservoirSampleAccumulator<Pair<Instance, int[]>> negative
			= new ReservoirSampleAccumulator<Pair<Instance, int[]>>( rng, Nnegative );
		final ReservoirSampleAccumulator<Pair<Instance, int[]>> positive
			= new ReservoirSampleAccumulator<Pair<Instance, int[]>>( rng, Npositive );
		
		for( int i = 0; i < single.size(); ++i ) {
//			if( i % 100 == 0 ) {
//				System.out.println( "i = " + i );
//			}
			for( int j = i + 1; j < single.size(); ++j ) {
				final Instance ii = single.get( i );
				final Instance ij = single.get( j );
				
				if( !plausible_p.apply( ii, ij ) ) {
//					System.out.println( "Not plausible: " + ii + " != " + ij );
					continue;
				}
				
//				System.out.println( "! Plausible: " + ii + " == " + ij );
				
				final int label;
				if( ii.classValue() == ij.classValue() ) {
					label = 1;
					if( positive.acceptNext() ) {
						final Instance pair_instance = combiner.apply( ii, ij, label );
						positive.addPending( Pair.makePair( pair_instance, new int[] { i, j } ) );
					}
				}
				else {
					label = 0;
					if( negative.acceptNext() ) {
						final Instance pair_instance = combiner.apply( ii, ij, label );
						negative.addPending( Pair.makePair( pair_instance, new int[] { i, j } ) );
					}
				}
			}
		}
		
		final int N = Math.min( negative.samples().size(), positive.samples().size() );
		final String dataset_name = "train_" + combiner.keyword()
								  + "_" + negative.samples().size() + "x" + positive.samples().size();
		final Instances x = new Instances( dataset_name, combiner.attributes(), Nnegative + Npositive );
		x.setClassIndex( x.numAttributes() - 1 );
		final ArrayList<int[]> matches = new ArrayList<int[]>();
		for( final Pair<Instance, int[]> ineg : negative.samples() ) {
			WekaUtil.addInstance( x, ineg.first );
			matches.add( ineg.second );
		}
		for( final Pair<Instance, int[]> ipos : positive.samples() ) {
			WekaUtil.addInstance( x, ipos.first );
			matches.add( ipos.second );
		}
		
		return new PairDataset( x, matches, combiner );
	}
	
	// -----------------------------------------------------------------------

	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
		int idx = 0;
		final String single_filename = args[idx++];
		System.out.println( "single_filename = " + single_filename );
		final String keyword = args[idx++];
		System.out.println( "keyword = " + keyword );
		final int seed = Integer.parseInt( args[idx++] );
		System.out.println( "seed = " + seed );
		final int max_pairwise_instances = Integer.parseInt( args[idx++] );
		System.out.println( "max_pairwise_instances = " + max_pairwise_instances );
		
		final File single_file = new File( single_filename );
		System.out.println( "Opening '" + single_file.getAbsolutePath() + "'" );
		assert( single_file.exists() );
		final Instances single = WekaUtil.readLabeledDataset( single_file );
		final ArrayList<Attribute> single_attributes = WekaUtil.extractAttributes( single );
		
		final InstanceCombiner combiner;
		if( "difference".equals( keyword ) ) {
			combiner = new DifferenceFeatures( single_attributes );
		}
		else if( "symmetric".equals( keyword ) ) {
			combiner = new SymmetricFeatures( single_attributes );
		}
		else {
			throw new IllegalArgumentException( "Unknown keyword '" + keyword + "'" );
		}
		
//		final String pair_name = FilenameUtils.getBaseName( single_filename )
//								 + "_" + keyword + "_" + max_pairwise_instances;
		final RandomGenerator rng = new MersenneTwister( seed );
		System.out.println( "Making dataset..." );
		final Instances pair_instances = makePairDataset( rng, max_pairwise_instances, single, combiner );
		System.out.println( "Writing dataset..." );
		WekaUtil.writeDataset( single_file.getParentFile(), pair_instances );
	}

}
