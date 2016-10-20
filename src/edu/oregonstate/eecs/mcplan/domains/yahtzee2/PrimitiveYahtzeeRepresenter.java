/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.yahtzee2;

import java.util.ArrayList;

import org.apache.commons.math3.util.FastMath;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.abstraction.PairDataset.InstanceCombiner;
import edu.oregonstate.eecs.mcplan.abstraction.WekaUtil;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class PrimitiveYahtzeeRepresenter implements FactoredRepresenter<YahtzeeState, FactoredRepresentation<YahtzeeState>>
{
	private static final ArrayList<Attribute> attributes_ = new ArrayList<Attribute>();
	private static final int Nattributes_ = Hand.Nfaces + 1 + (2 * YahtzeeScores.values().length) + 2;
	static {
		for( int i = 0; i < Hand.Nfaces; ++i ) {
			attributes_.add( new Attribute( "n" + (i+1) ) );
		}
		attributes_.add( new Attribute( "rerolls" ) );
		for( final YahtzeeScores category : YahtzeeScores.values() ) {
			attributes_.add( new Attribute( "filled_" + category ) );
		}
		for( final YahtzeeScores category : YahtzeeScores.values() ) {
			attributes_.add( new Attribute( "score_" + category ) );
		}
		attributes_.add( new Attribute( "yahtzee_bonus" ) );
		attributes_.add( new Attribute( "upper_bonus" ) );
	}
	
	public static class SmartPairFeatures extends InstanceCombiner
	{
		private final ArrayList<Attribute> attributes_ = new ArrayList<Attribute>();
		
		/**
		 * @param attributes Unlabeled attributes of single-instance data.
		 */
		public SmartPairFeatures()
		{
			for( int i = 0; i < Hand.Nfaces; ++i ) {
				attributes_.add( new Attribute( "n" + (i+1) + "_distance" ) );
			}
			attributes_.add( new Attribute( "dice_entropy_sum" ) );
			attributes_.add( new Attribute( "dice_entropy_product" ) );
			attributes_.add( new Attribute( "rerolls_distance" ) );
			for( final YahtzeeScores category : YahtzeeScores.values() ) {
				attributes_.add( new Attribute( "filled_xor_" + category ) );
				attributes_.add( new Attribute( "filled_and_" + category ) );
			}
			attributes_.add( new Attribute( "upper_hamming_distance" ) );
			attributes_.add( new Attribute( "lower_hamming_distance" ) );
//			for( final YahtzeeScores category : YahtzeeScores.values() ) {
//				attributes_.add( new Attribute( "score_distance_" + category ) );
//			}
			attributes_.add( new Attribute( "yahtzee_bonus_distance" ) );
			attributes_.add( new Attribute( "upper_bonus_distance" ) );
			
			for( final YahtzeeScores category : YahtzeeScores.values() ) {
				attributes_.add( new Attribute( "potential_distance_" + category ) );
			}
			
			attributes_.add( WekaUtil.createBinaryNominalAttribute( "__label__" ) );
		}
		
		private double entropy( final int[] counts )
		{
			final double[] p = new double[counts.length];
			Fn.memcpy_as_double( p, counts, counts.length );
			Fn.normalize_inplace( p );
			double h = 0;
			for( final double x : p ) {
				if( x > 0 ) {
					h -= x * FastMath.log( 2, x );
				}
			}
			return h;
		}
		
		@Override
		public DenseInstance apply( final Instance a, final Instance b, final int pair_label )
		{
			assert( a.classIndex() == b.classIndex() );
			assert( a.classIndex() == a.numAttributes() - 1 );
			final double[] phi = new double[attributes_.size()];
			
			int s = 0;
			int t = 0;
			
			final int[] ha = new int[Hand.Nfaces];
			final int[] hb = new int[Hand.Nfaces];
			for( int i = 0; i < Hand.Nfaces; ++i ) {
				ha[i] = (int) a.value( s );
				hb[i] = (int) b.value( s );
				phi[t++] = Math.abs( a.value( s ) - b.value( s ) );
				s += 1;
			}
			phi[t++] = entropy( ha ) + entropy( hb );
			phi[t++] = entropy( ha ) * entropy( hb );
			
			phi[t++] = Math.abs( a.value( s ) - b.value( s ) );
			s += 1;
			
			int hU = 0;
			int hL = 0;
			for( final YahtzeeScores category : YahtzeeScores.values() ) {
				final int x = (int) a.value( s );
				assert( x == 0 || x == 1 );
				final int y = (int) b.value( s );
				assert( y == 0 || y == 1 );
				phi[t++] = x ^ y;
				phi[t++] = x & y;
				if( category.isUpper() ) {
					hU += x ^ y;
				}
				else {
					hL += x ^ y;
				}
				s += 1;
			}
			phi[t++] = hU;
			phi[t++] = hL;
			
//			for( final YahtzeeScores category : YahtzeeScores.values() ) {
//				phi[t++] = Math.abs( a.value( s ) - b.value( s ) );
//				s += 1;
//			}
			phi[t++] = Math.abs( a.value( s ) - b.value( s ) );
			s += 1;
			phi[t++] = Math.abs( a.value( s ) - b.value( s ) );
			s += 1;
			
			for( final YahtzeeScores category : YahtzeeScores.values() ) {
				final int score_a = category.score( new Hand( ha ) );
				final int score_b = category.score( new Hand( hb ) );
				phi[t++] = Math.abs( score_a - score_b );
			}
			
			phi[t++] = pair_label;
			assert( t == phi.length );
			// FIXME: Make this configurable?
			final double weight = (pair_label == 1 ? 1.0 : 1.0);
			return new DenseInstance( weight, phi );
		}
		
		@Override
		public double[] apply( final double[] a, final double[] b, final double label )
		{
			return null;
		}
		
		@Override
		public double[] apply( final double[] a, final double[] b )
		{
			return null;
		}
		
		@Override
		public String keyword()
		{
			return "yahtzee_smart";
		}

		@Override
		public ArrayList<Attribute> attributes()
		{
			return attributes_;
		}
	}
	
	public static Instances toAugmentedPair( final Instances single )
	{
		final ArrayList<Attribute> pair_attributes = new ArrayList<Attribute>();
		
		return null;
	}
	
	@Override
	public PrimitiveYahtzeeRepresenter create()
	{
		return new PrimitiveYahtzeeRepresenter();
	}

	@Override
	public PrimitiveYahtzeeState encode( final YahtzeeState s )
	{
		return new PrimitiveYahtzeeState( s );
	}
	
	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes_;
	}
	
	@Override
	public String toString()
	{
		return "PrimitiveYahtzeeRepresenter";
	}

}
