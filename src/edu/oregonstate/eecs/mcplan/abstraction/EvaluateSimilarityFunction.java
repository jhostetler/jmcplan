/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import hr.irb.fastRandomForest.FastRandomForest;
import hr.irb.fastRandomForest.NakedFastRandomForest;
import hr.irb.fastRandomForest.NakedFastRandomTree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.RandomAdaptor;

import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.LogitBoost;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.abstraction.Experiments.Configuration;
import edu.oregonstate.eecs.mcplan.ml.ClusterContingencyTable;
import edu.oregonstate.eecs.mcplan.ml.InnerProductKernel;
import edu.oregonstate.eecs.mcplan.ml.KernelPrincipalComponentsAnalysis;
import edu.oregonstate.eecs.mcplan.ml.LinearDiscriminantAnalysis;
import edu.oregonstate.eecs.mcplan.ml.LongHashFunction;
import edu.oregonstate.eecs.mcplan.ml.RadialBasisFunctionKernel;
import edu.oregonstate.eecs.mcplan.ml.RandomForestKernel;
import edu.oregonstate.eecs.mcplan.ml.SequentialProjectionHashLearner;
import edu.oregonstate.eecs.mcplan.ml.StreamingClusterer;
import edu.oregonstate.eecs.mcplan.ml.WekaGlue;
import edu.oregonstate.eecs.mcplan.util.Csv;
import edu.oregonstate.eecs.mcplan.util.CsvConfigurationParser;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.KeyValueStore;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class EvaluateSimilarityFunction
{
	public static int[][] evaluate( final MetricSimilarityFunction f, final ArrayList<PairInstance> test,
								   final double decision_threshold )
	{
		final int[][] cm = new int[2][2];
		int N = 0;
		
		for( final PairInstance i : test ) {
//			if( N % 1000 == 0 ) {
//				System.out.println( "Instance " + N );
//			}
			N += 1;
			final double s = f.similarity( i.a, i.b );
			final double label = (s > decision_threshold ? 1.0 : 0.0);
			
			cm[(int) i.label][(int) label] += 1;
		}
		
		return cm;
	}
	
	public static ClusterContingencyTable evaluateClassifier(
		final Classifier classifier, final Instances test )
	{
		try {
			final Map<Integer, Set<RealVector>> Umap = new TreeMap<Integer, Set<RealVector>>();
			final Map<Integer, Set<RealVector>> Vmap = new TreeMap<Integer, Set<RealVector>>();
			
			final Remove rm_filter = new Remove();
			rm_filter.setAttributeIndicesArray( new int[] { test.classIndex() } );
			rm_filter.setInputFormat( test );
			
			for( final Instance i : test ) {
				rm_filter.input( i );
				final double[] phi = rm_filter.output().toDoubleArray();
//				final double[] phi = WekaUtil.unlabeledFeatures( i );
				
				final int cluster = (int) classifier.classifyInstance( i );
				Set<RealVector> u = Umap.get( cluster );
				if( u == null ) {
					u = new HashSet<RealVector>();
					Umap.put( cluster, u );
				}
				u.add( new ArrayRealVector( phi ) );
				
				final int true_label = (int) i.classValue();
				Set<RealVector> v = Vmap.get( true_label );
				if( v == null ) {
					v = new HashSet<RealVector>();
					Vmap.put( true_label, v );
				}
				v.add( new ArrayRealVector( phi ) );
			}
			
			final ArrayList<Set<RealVector>> U = new ArrayList<Set<RealVector>>();
			for( final Map.Entry<Integer, Set<RealVector>> e : Umap.entrySet() ) {
				U.add( e.getValue() );
			}
			
			final ArrayList<Set<RealVector>> V = new ArrayList<Set<RealVector>>();
			for( final Map.Entry<Integer, Set<RealVector>> e : Vmap.entrySet() ) {
				V.add( e.getValue() );
			}
			
			return new ClusterContingencyTable( U, V );
		}
		catch( final RuntimeException ex ) {
			throw ex;
		}
		catch( final Exception ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	public static ClusterContingencyTable evaluateClustering(
		final MetricSimilarityFunction f, final Instances single, final int max_branching )
	{
		final StreamingClusterer clusterer = new StreamingClusterer( f, max_branching );
		final Map<Integer, Set<RealVector>> Umap = new TreeMap<Integer, Set<RealVector>>();
		final Map<Integer, Set<RealVector>> Vmap = new TreeMap<Integer, Set<RealVector>>();
		
		for( final Instance i : single ) {
			final double[] phi = WekaUtil.unlabeledFeatures( i );
			
			final int cluster = clusterer.clusterState( phi );
			Set<RealVector> u = Umap.get( cluster );
			if( u == null ) {
				u = new HashSet<RealVector>();
				Umap.put( cluster, u );
			}
			u.add( new ArrayRealVector( phi ) );
			
			final int true_label = (int) i.classValue();
			Set<RealVector> v = Vmap.get( true_label );
			if( v == null ) {
				v = new HashSet<RealVector>();
				Vmap.put( true_label, v );
			}
			v.add( new ArrayRealVector( phi ) );
		}
		
		final ArrayList<Set<RealVector>> U = new ArrayList<Set<RealVector>>();
		for( final Map.Entry<Integer, Set<RealVector>> e : Umap.entrySet() ) {
			U.add( e.getValue() );
		}
		
		final ArrayList<Set<RealVector>> V = new ArrayList<Set<RealVector>>();
		for( final Map.Entry<Integer, Set<RealVector>> e : Vmap.entrySet() ) {
			V.add( e.getValue() );
		}
		
		return new ClusterContingencyTable( U, V );
	}
	
	public static Instances transformInstances( final Instances src, final CoordinateTransform transform )
	{
		final ArrayList<Attribute> out_attributes = new ArrayList<Attribute>();
		for( int i = 0; i < transform.outDimension(); ++i ) {
			out_attributes.add( new Attribute( "x" + i ) );
		}
		out_attributes.add( (Attribute) src.classAttribute().copy() );
		final Instances out = new Instances( src.relationName() + "_" + transform.name(), out_attributes, 0 );
		for( int i = 0; i < src.size(); ++i ) {
			final Instance inst = src.get( i );
			final RealVector flat = new ArrayRealVector( WekaUtil.unlabeledFeatures( inst ) );
			final RealVector transformed_vector = transform.encode( flat ).x;
			final double[] transformed = new double[transformed_vector.getDimension() + 1];
			for( int j = 0; j < transformed_vector.getDimension(); ++j ) {
				transformed[j] = transformed_vector.getEntry( j );
			}
			transformed[transformed.length - 1] = inst.classValue();
			final Instance transformed_instance = new DenseInstance( inst.weight(), transformed );
			out.add( transformed_instance );
			transformed_instance.setDataset( out );
		}
		out.setClassIndex( out.numAttributes() - 1 );
		return out;
	}
	
	private static class RealVectorRepresentation extends Representation<RealVector>
	{
		public final RealVector x;
		
		public RealVectorRepresentation( final RealVector x )
		{
			this.x = x;
		}
		
		@Override
		public Representation<RealVector> copy()
		{
			return new RealVectorRepresentation( x );
		}

		@Override
		public boolean equals( final Object obj )
		{
			if( !(obj instanceof RealVectorRepresentation) ) {
				return false;
			}
			final RealVectorRepresentation that = (RealVectorRepresentation) obj;
			return x.equals( that.x );
		}

		@Override
		public int hashCode()
		{
			return x.hashCode();
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static abstract class CoordinateTransform implements Representer<RealVector, RealVectorRepresentation>
	{
		public abstract String name();
		public abstract int outDimension();
	}
	
	private static class KPCA_Identity_Representer extends CoordinateTransform
	{
		private final KernelPrincipalComponentsAnalysis.Transformer<RealVector> transformer;
		
		public KPCA_Identity_Representer( final Configuration config, final ArrayList<RealVector> train )
		{
			final int Nbases = config.getInt( "kpca.Nbases" );
			final KernelPrincipalComponentsAnalysis<RealVector> kpca
				= new KernelPrincipalComponentsAnalysis<RealVector>(
					train, new InnerProductKernel(), 1e-6 );
			kpca.writeModel( config.data_directory, Nbases );
			
			transformer = kpca.makeTransformer( Nbases );
		}
		
		private KPCA_Identity_Representer( final KPCA_Identity_Representer that )
		{
			this.transformer = that.transformer;
		}

		@Override
		public RealVectorRepresentation encode( final RealVector s )
		{
			return new RealVectorRepresentation( transformer.transform( s ) );
		}

		@Override
		public Representer<RealVector, RealVectorRepresentation> create()
		{
			return new KPCA_Identity_Representer( this );
		}
		
		@Override
		public String name()
		{
			return "kpca.identity";
		}

		@Override
		public int outDimension()
		{
			return transformer.outDimension();
		}
	}
	
	private static class KPCA_RBF_Representer extends CoordinateTransform
	{
		private final double rbf_sigma;
		private final KernelPrincipalComponentsAnalysis.Transformer<RealVector> transformer;
		
		public KPCA_RBF_Representer( final Configuration config, final ArrayList<RealVector> train )
		{
			rbf_sigma = config.getDouble( "kpca.rbf.sigma" );
			final int Nbases = config.getInt( "kpca.Nbases" );
			final KernelPrincipalComponentsAnalysis<RealVector> kpca
				= new KernelPrincipalComponentsAnalysis<RealVector>(
					train, new RadialBasisFunctionKernel( rbf_sigma ), 1e-6 );
			kpca.writeModel( config.data_directory, Nbases );
			
			transformer = kpca.makeTransformer( Nbases );
		}
		
		private KPCA_RBF_Representer( final KPCA_RBF_Representer that )
		{
			this.rbf_sigma = that.rbf_sigma;
			this.transformer = that.transformer;
		}

		@Override
		public RealVectorRepresentation encode( final RealVector s )
		{
			return new RealVectorRepresentation( transformer.transform( s ) );
		}

		@Override
		public Representer<RealVector, RealVectorRepresentation> create()
		{
			return new KPCA_RBF_Representer( this );
		}
		
		@Override
		public String name()
		{
			return "kpca.rbf_" + rbf_sigma;
		}

		@Override
		public int outDimension()
		{
			return transformer.outDimension();
		}
	}
	
	private static class KPCA_RandomForest_Representer extends CoordinateTransform
	{
		private final NakedFastRandomForest rf;
		private final int Ntrees;
		private final int max_depth;
		private final KernelPrincipalComponentsAnalysis.Transformer<NakedFastRandomTree[]> transformer;
		private final Instances headers;
		
		public KPCA_RandomForest_Representer( final Configuration config, final Instances train )
		{
			max_depth = config.getInt( "kpca.random_forest.max_depth" );
			Ntrees = config.getInt( "kpca.random_forest.Ntrees" );
			headers = new Instances( train, 0 );
			
			try {
				rf = new NakedFastRandomForest();
				rf.setMaxDepth( max_depth );
				rf.setNumTrees( Ntrees );
				rf.setNumThreads( 1 );
				rf.buildClassifier( train );
			}
			catch( final Exception ex ) {
				throw new RuntimeException( ex );
			}
			
			final ArrayList<NakedFastRandomTree[]> counts = new ArrayList<NakedFastRandomTree[]>();
			for( final Instance i : train ) {
				counts.add( rf.getNodesForInstance( i, max_depth ) );
			}
			
			final RandomForestKernel k = new RandomForestKernel();
			
			final int Nbases = config.getInt( "kpca.Nbases" );
			final KernelPrincipalComponentsAnalysis<NakedFastRandomTree[]> kpca
				= new KernelPrincipalComponentsAnalysis<NakedFastRandomTree[]>( counts, k, 1e-6 );
			kpca.writeModel( config.data_directory, Nbases );
			
			transformer = kpca.makeTransformer( Nbases );
		}
		
		private KPCA_RandomForest_Representer( final KPCA_RandomForest_Representer that )
		{
			this.rf = that.rf;
			this.Ntrees = that.Ntrees;
			this.max_depth = that.max_depth;
			this.transformer = that.transformer;
			this.headers = that.headers;
		}

		@Override
		public RealVectorRepresentation encode( final RealVector x )
		{
			final Instance i = WekaUtil.labeledInstanceFromUnlabeledFeatures( headers, x );
			final NakedFastRandomTree[] nodes = rf.getNodesForInstance( i, max_depth );
			return new RealVectorRepresentation( transformer.transform( nodes ) );
		}

		@Override
		public KPCA_RandomForest_Representer create()
		{
			return new KPCA_RandomForest_Representer( this );
		}
		
		@Override
		public String name()
		{
			return "kpca.random_forest_" + max_depth;
		}

		@Override
		public int outDimension()
		{
			return transformer.outDimension();
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class LDA_Representer extends CoordinateTransform
	{
		private final LinearDiscriminantAnalysis.Transformer transformer;
		private final Instances headers;
		
		public LDA_Representer( final Configuration config, final Instances train )
		{
			headers = new Instances( train, 0 );
			
			final Pair<ArrayList<double[]>, int[]> p = WekaUtil.splitLabels( train );
			final LinearDiscriminantAnalysis lda = new LinearDiscriminantAnalysis(
				p.first, p.second, train.numClasses(), 1e-6 );
			lda.writeModel( config.data_directory );
			
			transformer = lda.makeTransformer();
		}
		
		private LDA_Representer( final LDA_Representer that )
		{
			this.transformer = that.transformer;
			this.headers = that.headers;
		}

		@Override
		public RealVectorRepresentation encode( final RealVector x )
		{
			return new RealVectorRepresentation( transformer.transform( x ) );
		}

		@Override
		public LDA_Representer create()
		{
			return new LDA_Representer( this );
		}
		
		@Override
		public String name()
		{
			return "lda";
		}

		@Override
		public int outDimension()
		{
			return transformer.outDimension();
		}
	}
	
	// -----------------------------------------------------------------------
	
	private static class TransformerChain extends CoordinateTransform
	{
		public final ArrayList<CoordinateTransform> transformers;
		
		public TransformerChain()
		{
			transformers = new ArrayList<CoordinateTransform>();
		}
		
		private TransformerChain( final TransformerChain that )
		{
			transformers = that.transformers;
		}

		@Override
		public TransformerChain create()
		{
			return new TransformerChain( this );
		}

		@Override
		public RealVectorRepresentation encode( final RealVector s )
		{
			RealVector current = s;
			RealVectorRepresentation repr = null;
			for( final CoordinateTransform t : transformers ) {
				repr = t.encode( current );
				current = repr.x;
			}
			return repr;
		}

		@Override
		public String name()
		{
			final StringBuilder sb = new StringBuilder();
			sb.append( "TransformerChain[" );
			for( int i = 0; i < transformers.size(); ++i ) {
				if( i > 0 ) {
					sb.append( ";" );
				}
				sb.append( transformers.get( i ).name() );
			}
			sb.append( "]" );
			return sb.toString();
		}

		@Override
		public int outDimension()
		{
			return transformers.get( transformers.size() - 1 ).outDimension();
		}
	}
	
	private static CoordinateTransform createCoordinateTransform( final Configuration config, final Instances train )
	{
		final String abstraction = config.get( "abstraction.discovery" );
		final String[] stages = abstraction.split( "\\+" );
		final TransformerChain chain = new TransformerChain();
		
		Instances current = train;
		for( int i = 0; i < stages.length; ++i ) {
			final String ab = stages[i];
			final CoordinateTransform next;
			if( "kpca".equals( ab ) ) {
				final String kernel = config.get( "kpca.kernel" );
				if( "rbf".equals( kernel ) ) {
					final ArrayList<RealVector> train_vectors = WekaUtil.instancesToUnlabeledVectors( current );
					next = new KPCA_RBF_Representer( config, train_vectors );
				}
				else if( "identity".equals( kernel ) ) {
					final ArrayList<RealVector> train_vectors = WekaUtil.instancesToUnlabeledVectors( current );
					next = new KPCA_Identity_Representer( config, train_vectors );
				}
				else if( "random_forest".equals( kernel ) ) {
					next = new KPCA_RandomForest_Representer( config, current );
				}
				else {
					throw new IllegalArgumentException( "kpca.kernel = " + kernel );
				}
			}
			else if( "lda".equals( ab ) ) {
				next = new LDA_Representer( config, current );
			}
			else {
				throw new IllegalArgumentException( "abstraction = " + abstraction );
			}
			
			chain.transformers.add( next );
			
			if( i < stages.length - 1 ) {
				current = transformInstances( train, next );
			}
		}
		
		return chain;
	}
	
//	private static Instances transformInstances( final Instances instances, final CoordinateTransform transform )
//	{
//		final Instances result = new Instances( instances, 0 );
//
//		for( final Instance i : instances ) {
//			final double[] x = WekaUtil.unlabeledFeatures( i );
//			final RealVector xprime = transform.encode( new ArrayRealVector( x ) ).x;
//			final double[] r = new double[i.numAttributes()];
//			for( int j = 0; j < xprime.getDimension(); ++j ) {
//				r[j] = xprime.getEntry( j );
//			}
//			r[r.length - 1] = i.classValue();
//			final DenseInstance iprime = new DenseInstance( i.weight(), r );
//			result.add( iprime );
//			iprime.setDataset( result );
//		}
//
//		return result;
//	}
	
	// -----------------------------------------------------------------------
	
	public static abstract class Evaluator
	{
		public abstract Instances prepareInstances( final Instances test );
		public abstract ClusterContingencyTable evaluate( final Instances test );
		public abstract boolean isSensitiveToOrdering();
	}
	
	public static class ClusterEvaluator extends Evaluator
	{
		public final Configuration config;
		public final CoordinateTransform transformer;
		public final MetricSimilarityFunction similarity;
		
		public ClusterEvaluator( final Configuration config, final Instances train )
		{
			this.config = config;
			transformer = createCoordinateTransform( config, train );
				
//			final Instances transformed_train = transformInstances( train, transformer );
//			System.out.println( "[Making PairDataset]" );
//			final int max_pairwise_instances = config.getInt( "training.max_pairwise" );
//			final ArrayList<PairInstance> pair = PairDataset.makePairDataset(
//				config.rng, max_pairwise_instances, transformed_train );
			
//			final RealMatrix M = Csv.readMatrix(
//				new File( new File( config.modelDirectory(), config.model ), "M" + 0 + ".csv" ) );
			
			final RealMatrix M = MatrixUtils.createRealIdentityMatrix( transformer.outDimension() );
			similarity = new MetricSimilarityFunction( M );
			
//			System.out.println( "[Calculating distance quantiles]" );
//			final PairDataset.InstanceCombiner combiner
//					= new PairDataset.DifferenceFeatures( WekaUtil.extractAttributes( transformed_train ) );
//			final QuantileAccumulator q = new QuantileAccumulator(
//				Fn.scalar_multiply( new double[] {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0}, 0.1 ) );
//			for( final PairInstance i : pair ) {
//				final double[] phi = combiner.apply( i.a, i.b );
//				q.add( Math.sqrt( HilbertSpace.inner_prod( phi, M, phi ) ) );
//			}
		}
		
		@Override
		public Instances prepareInstances( final Instances test )
		{
			final Instances transformed_test = transformInstances( test, transformer );
			return transformed_test;
		}

		@Override
		public ClusterContingencyTable evaluate( final Instances test )
		{
			final ClusterContingencyTable ct = evaluateClustering(
				similarity, test, config.getInt( "pairwise_classifier.max_branching" ) );
			return ct;
		}

		@Override
		public boolean isSensitiveToOrdering()
		{
			return true;
		}
	}
	
	public static class MulticlassEvaluator extends Evaluator
	{
		public final Configuration config;
		public final Classifier classifier;
		public final int Nclasses;
		
		public MulticlassEvaluator( final Configuration config, final Instances train )
		{
			this.config = config;
			try {
				Nclasses = train.classAttribute().numValues();
				final String algorithm = config.get( "multiclass.classifier" );
				if( "random_forest".equals( algorithm ) ) {
					final FastRandomForest rf = new FastRandomForest();
					rf.setNumTrees( config.getInt( "multiclass.random_forest.Ntrees" ) );
					rf.setMaxDepth( config.getInt( "multiclass.random_forest.max_depth" ) );
					rf.setNumThreads( 1 );
					classifier = rf;
				}
				else if( "decision_tree".equals( algorithm ) ) {
					final J48 dt = new J48();
					classifier = dt;
				}
				else if( "logistic_regression".equals( algorithm ) ) {
					final Logistic lr = new Logistic();
					lr.setRidge( config.getDouble( "multiclass.logistic_regression.ridge" ) );
					classifier = lr;
				}
				else if( "logit_boost".equals( algorithm ) ) {
					final LogitBoost lb = new LogitBoost();
//					final M5P base = new M5P();
					final REPTree base = new REPTree();
					base.setMaxDepth( 8 );
					lb.setClassifier( base );
					classifier = lb;
				}
				else {
					throw new IllegalArgumentException( "multiclass.classifier = " + algorithm );
				}
				
				final Instances transformed = prepareInstances( train );
				classifier.buildClassifier( transformed );
			}
			catch( final RuntimeException ex ) {
				throw ex;
			}
			catch( final Exception ex ) {
				throw new RuntimeException( ex );
			}
		}

		@Override
		public Instances prepareInstances( final Instances test )
		{
//			final Instances result =
//				Instances.mergeInstances( WekaUtil.allPairwiseProducts( test, false, false ), test );
//			result.setClassIndex( result.numAttributes() - 1 );
//			return result;
			
			final String algorithm = config.get( "multiclass.classifier" );
			if( "logistic_regression".equals( algorithm ) ) {
				return WekaUtil.powerSet( test, 2 );
			}
			else {
				return test;
			}
		}

		@Override
		public ClusterContingencyTable evaluate( final Instances test )
		{
			final ClusterContingencyTable ct = evaluateClassifier( classifier, test );
			return ct;
		}

		@Override
		public boolean isSensitiveToOrdering()
		{
			return false;
		}
	}
	
	public static class HashEvaluator extends Evaluator
	{
		public final Configuration config;
		public final LongHashFunction<RealVector> hash;
		
		public HashEvaluator( final Configuration config, final Instances train )
		{
			this.config = config;
			try {
				final String algorithm = config.get( "hash.algorithm" );
				if( "sequential_projection".equals( algorithm ) ) {
					final int K = config.getInt( "sequential_projection.K" );
					final double eta = config.getDouble( "sequential_projection.eta" );
					final double alpha = config.getDouble( "sequential_projection.alpha" );
					final Instances unlabeled = new Instances( train, 0 );
					final SequentialProjectionHashLearner sph =	WekaGlue.createSequentialProjectionHashLearner(
						config.rng, train, unlabeled, K, eta, alpha );
					sph.run();
					hash = sph;
				}
				else {
					throw new IllegalArgumentException( "hash.algorithm = " + algorithm );
				}
			}
			catch( final RuntimeException ex ) {
				throw ex;
			}
			catch( final Exception ex ) {
				throw new RuntimeException( ex );
			}
		}

		@Override
		public Instances prepareInstances( final Instances test )
		{
			return test;
		}

		@Override
		public ClusterContingencyTable evaluate( final Instances test )
		{
			final ArrayList<Set<RealVector>> U = new ArrayList<Set<RealVector>>();
			final ArrayList<Set<RealVector>> V = new ArrayList<Set<RealVector>>();
			for( int i = 0; i < test.numClasses(); ++i ) {
				V.add( new HashSet<RealVector>() );
			}
			
			final int r = 25;
			final TLongObjectMap<Set<RealVector>> um = new TLongObjectHashMap<Set<RealVector>>();
			for( final Instance inst : test ) {
				final RealVector v = new ArrayRealVector( WekaUtil.unlabeledFeatures( inst ), false );
				final long h = hash.hash( v );
//				System.out.println( "h = " + h );
				
				boolean found = false;
				for( final Set<RealVector> s : U ) {
					found = true;
					for( final RealVector u : s ) {
						final long uh = hash.hash( u );
						final int d = Long.bitCount( h ^ uh );
						if( d > r ) {
							found = false;
							break;
						}
					}
					if( found ) {
						s.add( v );
						break;
					}
				}
				if( !found ) {
					final Set<RealVector> s = new HashSet<RealVector>();
					s.add( v );
					U.add( s );
				}
				
				// The truth
				V.get( (int) inst.classValue() ).add( v );
			}
			
			final ClusterContingencyTable ct = new ClusterContingencyTable( U, V );
			return ct;
		}

		@Override
		public boolean isSensitiveToOrdering()
		{
			return false;
		}
	}
	
	public static class PairSimilarityEvaluator extends Evaluator
	{
		public final Configuration config;
		
		private final Classifier classifier_;
		
		// FIXME: Ugh! Ugly hack because we have to return Instances from
		// prepareInstances()
		private PairDataset pair_dataset_ = null;
		
		public PairSimilarityEvaluator( final Configuration config, final Instances train )
		{
			this.config = config;
			try {
				final String algorithm = config.get( "pair.algorithm" );
				final Instances p = prepareInstances( train );
				if( "decision_tree".equals( algorithm ) ) {
					final J48 dt = new J48();
					dt.buildClassifier( p );
					classifier_ = dt;
				}
				else if( "logit_boost".equals( algorithm ) ) {
					final LogitBoost lb = new LogitBoost();
//					final M5P base = new M5P();
					final REPTree base = new REPTree();
//					base.setMaxDepth( 8 );
					lb.setSeed( config.rng.nextInt() );
					lb.setClassifier( base );
					lb.setNumIterations( 50 );
					lb.buildClassifier( p );
					classifier_ = lb;
				}
				else if( "random_forest".equals( algorithm ) ) {
					final RandomForest rf = new RandomForest();
					final int Ntrees = config.getInt( "pair.random_forest.Ntrees" );
					rf.setNumTrees( Ntrees );
					rf.buildClassifier( p );
					classifier_ = rf;
				}
				else {
					throw new IllegalArgumentException( "pair.algorithm = " + algorithm );
				}
			}
			catch( final RuntimeException ex ) {
				throw ex;
			}
			catch( final Exception ex ) {
				throw new RuntimeException( ex );
			}
		}
		
		@Override
		public Instances prepareInstances( final Instances test )
		{
			final PairDataset.InstanceCombiner combiner
				= new PairDataset.SymmetricFeatures( WekaUtil.extractUnlabeledAttributes( test ) );
			final int positive_pairs = config.getInt( "training.positive_pairs" );
			final int negative_pairs = config.getInt( "training.negative_pairs" );
			pair_dataset_ = PairDataset.makeBalancedPairDataset(
				config.rng, negative_pairs, positive_pairs, test, combiner );
			return pair_dataset_.instances;
		}

		@Override
		public ClusterContingencyTable evaluate( final Instances test )
		{
			final ArrayList<Instance> exemplars = new ArrayList<Instance>();
			final ArrayList<Set<RealVector>> U = new ArrayList<Set<RealVector>>();
			final ArrayList<Set<RealVector>> V = new ArrayList<Set<RealVector>>();
			final int Nclasses = test.numClasses();
			for( int i = 0; i < Nclasses; ++i ) {
				V.add( new HashSet<RealVector>() );
			}
			
			final Instances dummy = WekaUtil.createEmptyInstances( "dummy", pair_dataset_.combiner.attributes() );
			for( final Instance inst : test ) {
				final int c = (int) inst.classValue();
				final RealVector v = new ArrayRealVector( inst.toDoubleArray() );
				V.get( c ).add( v );
				boolean found = false;
				final int[] idx = Fn.range( 0, exemplars.size() );
				Fn.shuffle( config.rng, idx );
				for( final int i : idx ) {
					final Instance x = exemplars.get( i );
					final int xc = (int) x.classValue();
					final Instance p = pair_dataset_.combiner.apply( inst, x, (c == xc ? 1 : 0) );
					WekaUtil.addInstance( dummy, p );
					int prediction;
					try {
						prediction = (int) classifier_.classifyInstance( p );
					}
					catch( final RuntimeException ex ) { throw ex; }
					catch( final Exception ex ) { throw new RuntimeException( ex ); }
					dummy.remove( 0 );
					if( prediction == 1 ) {
						U.get( i ).add( v );
						found = true;
						break;
					}
				}
				if( !found ) {
					exemplars.add( inst );
					final Set<RealVector> s = new HashSet<RealVector>();
					s.add( v );
					U.add( s );
				}
			}

			return new ClusterContingencyTable( U, V );
		}

		@Override
		public boolean isSensitiveToOrdering()
		{
			return false;
		}
		
	}
	
	public static Evaluator createEvaluator( final Configuration config, final Instances train )
	{
		// FIXME: We are checking against a list of names in two places (here and
		// when creating the CoordinateTransform). The only reason to do it here
		// is basically because to evaluate a Classifier we need labeled Instance
		// objects, while to evaluate a clusterer we need unlabeled double[]'s.
		final String abstraction = config.get( "abstraction.discovery" );
		if( "kpca".equals( abstraction ) || "lda".equals( abstraction ) || "kpca+lda".equals( abstraction ) ) {
			return new ClusterEvaluator( config, train );
		}
		else if( "multiclass".equals( abstraction ) ) {
			return new MulticlassEvaluator( config, train );
		}
		else if( "hash".equals( abstraction ) ) {
			return new HashEvaluator( config, train );
		}
		else if( "pair".equals( abstraction ) ) {
			return new PairSimilarityEvaluator( config, train );
		}
		else {
			throw new IllegalArgumentException( "abstraction = " + abstraction );
		}
	}
	
	// -----------------------------------------------------------------------
	
	/**
	 * @param args
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void main( final String[] args ) throws FileNotFoundException, IOException
	{
		final String experiment_file = args[0];
		final File root_directory;
		if( args.length > 1 ) {
			root_directory = new File( args[1] );
		}
		else {
			root_directory = new File( "." );
		}
		final CsvConfigurationParser csv_config = new CsvConfigurationParser( new FileReader( experiment_file ) );
		final String experiment_name = FilenameUtils.getBaseName( experiment_file );
		
		final File expr_directory = new File( root_directory, experiment_name );
		expr_directory.mkdirs();
		
		final Csv.Writer csv = new Csv.Writer( new PrintStream( new FileOutputStream(
			new File( expr_directory, "results.csv" ) ) ) );
		final String[] parameter_headers = new String[] {
			"kpca.kernel", "kpca.rbf.sigma",
			"kpca.random_forest.Ntrees", "kpca.random_forest.max_depth",
			"kpca.Nbases", "multiclass.classifier",
			"multiclass.random_forest.Ntrees", "multiclass.random_forest.max_depth",
			"pairwise_classifier.max_branching", "training.label_noise"
		};
		csv.cell( "domain" ).cell( "abstraction" );
		for( final String p : parameter_headers ) {
			csv.cell( p );
		}
		csv.cell( "Ntrain" ).cell( "Ntest" ).cell( "ami.mean" ).cell( "ami.variance" ).cell( "ami.confidence" ).newline();
		
		for( int expr = 0; expr < csv_config.size(); ++expr ) {
			try {
				final KeyValueStore expr_config = csv_config.get( expr );
				final Configuration config = new Configuration(
					root_directory.getPath(), expr_directory.getName(), expr_config );
				
				System.out.println( "[Loading '" + config.training_data_single + "']" );
				final Instances single = WekaUtil.readLabeledDataset(
					new File( root_directory, config.training_data_single + ".arff" ) );
				
				final Instances train = new Instances( single, 0 );
				final int[] idx = Fn.range( 0, single.size() );
				int instance_counter = 0;
				Fn.shuffle( config.rng, idx );
				final int Ntrain = config.Ntrain_games;
				final double label_noise = config.getDouble( "training.label_noise" );
				final int Nlabels = train.classAttribute().numValues();
				assert( Nlabels > 0 );
				for( int i = 0; i < Ntrain; ++i ) {
					final Instance inst = single.get( idx[instance_counter++] );
					if( label_noise > 0 && config.rng.nextDouble() < label_noise ) {
						int noisy_label = 0;
						do {
							noisy_label = config.rng.nextInt( Nlabels );
						} while( noisy_label == (int) inst.classValue() );
						System.out.println( "Noisy label (" + inst.classValue() + " -> " + noisy_label + ")" );
						inst.setClassValue( noisy_label );
					}
					train.add( inst );
					inst.setDataset( train );
				}
				final int Ntest = config.Ntest_games;
				final Instances test = new Instances( single, 0 );
				while( instance_counter < single.size() && test.size() < Ntest ) {
					final Instance inst = single.get( idx[instance_counter++] );
					test.add( inst );
					inst.setDataset( test );
				}
				
				System.out.println( "[Training]" );
				final Evaluator evaluator = createEvaluator( config, train );
//				final Instances transformed_test = evaluator.prepareInstances( test );
								
				System.out.println( "[Evaluating]" );
	
				final int Nxval = evaluator.isSensitiveToOrdering() ? 10 : 1;
				final MeanVarianceAccumulator ami = new MeanVarianceAccumulator();
				
				for( int xval = 0; xval < Nxval; ++xval ) {
//					transformed_test.randomize( new RandomAdaptor( config.rng ) );
//					final ClusterContingencyTable ct = evaluator.evaluate( transformed_test );
					test.randomize( new RandomAdaptor( config.rng ) );
					final ClusterContingencyTable ct = evaluator.evaluate( test );
					System.out.println( ct );
					final PrintStream ct_out = new PrintStream( new FileOutputStream(
						new File( expr_directory, "ct_" + expr + "_" + xval + ".csv" ) ) );
					ct.writeCsv( ct_out );
					ct_out.close();
					ami.add( ct.adjustedMutualInformation_max() );
				}
				System.out.println( "AMI_max = " + ami.mean() + " (" + ami.confidence() + ")" );
				
				csv.cell( config.domain ).cell( config.get( "abstraction.discovery" ) );
				for( final String p : parameter_headers ) {
					csv.cell( config.get( p ) );
				}
				csv.cell( Ntrain ).cell( Ntest ).cell( ami.mean() ).cell( ami.variance() ).cell( ami.confidence() ).newline();
			}
			catch( final Exception ex ) {
				ex.printStackTrace();
			}
		}
	}

}
