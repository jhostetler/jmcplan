/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.converters.Saver;
import edu.oregonstate.eecs.mcplan.Pair;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * Utility functions to make working with Weka's horrendous API less painful.
 * 
 * @author jhostetler
 */
public class WekaUtil
{
	public static ArrayList<RealVector> instancesToUnlabeledVectors( final Instances instances )
	{
		final ArrayList<RealVector> result = new ArrayList<RealVector>();
		for( final Instance i : instances ) {
			result.add( new ArrayRealVector( unlabeledFeatures( i ) ) );
		}
		return result;
	}
	
	public static Instances createEmptyInstances( final String name, final ArrayList<Attribute> attributes )
	{
		final Instances instances = new Instances( name, attributes, 0 );
		instances.setClassIndex( attributes.size() - 1 );
		return instances;
	}
	
	public static void addInstance( final Instances instances, final Instance i )
	{
		instances.add( i );
		i.setDataset( instances );
	}
	
	/**
	 * Creates a nominal attribute containing the values {0,1}.
	 * @param name
	 * @return
	 */
	public static Attribute createBinaryNominalAttribute( final String name )
	{
		return createNominalAttribute( name, 2 );
	}
	
	/**
	 * Creates a nominal attribute containing the values {0,...,N}.
	 * @param name
	 * @return
	 */
	public static Attribute createNominalAttribute( final String name, final int N )
	{
		final List<String> values = new ArrayList<String>( N );
		for( int i = 0; i < N; ++i ) {
			values.add( Integer.toString( i ) );
		}
		final Attribute attr = new Attribute( name, values );
		assert( attr.type() == Attribute.NOMINAL );
		return attr;
	}
	
	/**
	 * Creates an Instances object containing the specified feature vector
	 * and with an added "dummy label".
	 * @param attributes
	 * @param features
	 * @return
	 */
	public static Instances createSingletonInstances( final List<Attribute> attributes, final double[] features )
	{
		final ArrayList<Attribute> attr_dummy_label = new ArrayList<Attribute>( attributes );
		attr_dummy_label.add( createBinaryNominalAttribute( "__dummy_label__" ) );
		final double[] features_dummy_label = new double[features.length + 1];
		Fn.memcpy( features_dummy_label, features, features.length );
		final Instance instance = new DenseInstance( 1.0, features_dummy_label );
		final Instances x = new Instances( "__eval__", attr_dummy_label, 1 );
		x.setClassIndex( attr_dummy_label.size() - 1 );
		x.add( instance );
		instance.setDataset( x );
		return x;
	}
	
	/**
	 * Classify a feature vector that is not part of an Instances object.
	 * @param classifier
	 * @param attributes
	 * @param features
	 * @return
	 */
	public static double classify( final Classifier classifier, final List<Attribute> attributes, final double[] features )
	{
		final Instances x = createSingletonInstances( attributes, features );
		try {
			return classifier.classifyInstance( x.get( 0 ) );
		}
		catch( final Exception ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	/**
	 * Classify a feature vector that is not part of an Instances object.
	 * @param classifier
	 * @param attributes
	 * @param features
	 * @return
	 */
	public static double[] distribution( final Classifier classifier, final List<Attribute> attributes, final double[] features )
	{
		final Instances x = createSingletonInstances( attributes, features );
		try {
			return classifier.distributionForInstance( x.get( 0 ) );
		}
		catch( final Exception ex ) {
			throw new RuntimeException( ex );
		}
	}

	/**
	 * Save an Instances object, using the relation name for the file name.
	 * @param root
	 * @param x
	 */
	public static void writeDataset( final File root, final Instances x )
	{
		writeDataset( root, x.relationName(), x );
	}

	/**
	 * Save an Instances object with the specified file name. The '.arff'
	 * extension is added by this function.
	 * 
	 * Adapted from:
	 * http://weka.wikispaces.com/Use+WEKA+in+your+Java+code
	 * @param root
	 * @param filename
	 * @param x
	 */
	public static void writeDataset( final File root, final String filename, final Instances x )
	{
		final File dataset_file = new File( root, filename + ".arff" );
		final Saver saver = new ArffSaver();
		try {
			saver.setFile( dataset_file );
			saver.setInstances( x );
			saver.writeBatch();
		}
		catch( final IOException ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	/**
	 * Returns a list of all Attributes, *including* the class attribute if
	 * it is set. Note that using Instance.enumerateAttributes() will *skip*
	 * the class attribute.
	 * @param instances
	 * @return
	 */
	public static ArrayList<Attribute> extractAttributes( final Instances instances )
	{
		final ArrayList<Attribute> attributes = new ArrayList<Attribute>( instances.numAttributes() );
		for( int i = 0; i < instances.numAttributes(); ++i ) {
			attributes.add( instances.attribute( i ) );
		}
		return attributes;
	}
	
	/**
	 * Load an ARFF dataset.
	 *
	 * Adapted from:
	 * http://weka.wikispaces.com/Use+WEKA+in+your+Java+code
	 * @param file
	 * @return
	 */
	public static Instances readLabeledDataset( final File file )
	{
		try {
			final DataSource source = new DataSource( file.getPath() );
			final Instances data = source.getDataSet();
			// setting class attribute if the data format does not provide this information
			// For example, the XRFF format saves the class attribute information as well
			if( data.classIndex() == -1 ) {
				data.setClassIndex(data.numAttributes() - 1);
			}
			return data;
		}
		catch( final Exception ex ) {
			throw new RuntimeException( ex );
		}
	}

	public static double[] unlabeledFeatures( final Instance i )
	{
		assert( i.dataset() != null );
		assert( i.dataset().classIndex() == i.numAttributes() - 1 );
		final double[] phi = new double[i.numAttributes() - 1];
		for( int j = 0; j < i.numAttributes() - 1; ++j ) {
			phi[j] = i.value( j );
		}
		return phi;
	}

	public static Instance labeledInstanceFromUnlabeledFeatures( final Instances headers, final double[] x )
	{
		assert( x.length == headers.numAttributes() - 1 );
		final double[] labeled = new double[x.length + 1];
		Fn.memcpy( labeled, x, x.length );
		labeled[labeled.length - 1] = Double.NaN;
		final DenseInstance inst = new DenseInstance( 1.0, labeled );
		return inst;
	}
	
	/**
	 * Adds a dummy label and converts to an Instance
	 * @param headers
	 * @param x
	 * @return
	 */
	public static Instance labeledInstanceFromUnlabeledFeatures( final Instances headers, final RealVector x )
	{
		assert( x.getDimension() == headers.numAttributes() - 1 );
		final double[] labeled = new double[x.getDimension() + 1];
		for( int i = 0; i < x.getDimension(); ++i ) {
			labeled[i] = x.getEntry( i );
		}
		labeled[labeled.length - 1] = Double.NaN;
		final DenseInstance inst = new DenseInstance( 1.0, labeled );
		return inst;
	}

	public static Pair<ArrayList<double[]>, int[]> splitLabels( final Instances train )
	{
		assert( train.classAttribute() != null );
		
		final ArrayList<double[]> X = new ArrayList<double[]>();
		final int[] Y = new int[train.size()];
		
		for( int i = 0; i < train.size(); ++i ) {
			final Instance inst = train.get( i );
			final double[] x = new double[train.numAttributes() - 1];
			int idx = 0;
			for( int j = 0; j < train.numAttributes(); ++j ) {
				if( j == train.classIndex() ) {
					Y[i] = (int) inst.classValue();
				}
				else {
					x[idx++] = inst.value( j );
				}
			}
			X.add( x );
		}
		
		return Pair.makePair( X, Y );
	}
}
