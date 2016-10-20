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
package edu.oregonstate.eecs.mcplan.domains.tamarisk;

import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.abstraction.PairDataset.InstanceCombiner;
import edu.oregonstate.eecs.mcplan.abstraction.WekaUtil;

/**
 * @author jhostetler
 *
 */
public class IndicatorTamariskRepresenter implements FactoredRepresenter<TamariskState, FactoredRepresentation<TamariskState>>
{
	private final ArrayList<Attribute> attributes_;

	public IndicatorTamariskRepresenter( final TamariskParameters p )
	{
		attributes_ = new ArrayList<Attribute>();
		for( int r = 0; r < p.Nreaches; ++r ) {
			for( int h = 0; h < p.Nhabitats; ++h ) {
				attributes_.add( new Attribute( "r" + r + "h" + h + "_Native" ) );
				attributes_.add( new Attribute( "r" + r + "h" + h + "_Tamarisk" ) );
			}
		}
	}
	
	private IndicatorTamariskRepresenter( final IndicatorTamariskRepresenter that )
	{
		attributes_ = that.attributes_;
	}
	
	@Override
	public IndicatorTamariskRepresenter create()
	{
		return new IndicatorTamariskRepresenter( this );
	}

	@Override
	public IndicatorTamariskRepresentation encode( final TamariskState s )
	{
		return new IndicatorTamariskRepresentation( s );
	}
	
	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes_;
	}
	
	@Override
	public String toString()
	{
		return "IndicatorTamariskRepresenter";
	}
	
	public static class SmartPairFeatures extends InstanceCombiner
	{
		private final ArrayList<Attribute> attributes_ = new ArrayList<Attribute>();
		private final TamariskParameters params_;
		
		/**
		 * @param attributes Unlabeled attributes of single-instance data.
		 */
		public SmartPairFeatures( final TamariskParameters params )
		{
			params_ = params;
			for( int i = 0; i < params_.Nreaches; ++i ) {
				attributes_.add( new Attribute( "r" + i + "_Nnative_sum" ) );
				attributes_.add( new Attribute( "r" + i + "_Nnative_product" ) );
//				attributes_.add( new Attribute( "r" + i + "_Nnative_distance" ) );
				attributes_.add( new Attribute( "r" + i + "_Ntamarisk_sum" ) );
				attributes_.add( new Attribute( "r" + i + "_Ntamarisk_product" ) );
//				attributes_.add( new Attribute( "r" + i + "_Ntamarisk_distance" ) );
			}
			attributes_.add( WekaUtil.createBinaryNominalAttribute( "__label__" ) );
		}
		
		@Override
		public DenseInstance apply( final Instance a, final Instance b, final int pair_label )
		{
			assert( a.classIndex() == b.classIndex() );
			assert( a.classIndex() == a.numAttributes() - 1 );
			
			final double[] phi = new double[attributes_.size()];
			int idx = 0;
			int ai = 0;
			int bi = 0;
			for( int r = 0; r < params_.Nreaches; ++r ) {
				int Nnative_a = 0;
				int Nnative_b = 0;
				int Ntamarisk_a = 0;
				int Ntamarisk_b = 0;
				for( int h = 0; h < params_.Nhabitats; ++h ) {
					Nnative_a += (int) a.value( ai++ );
					Nnative_b += (int) b.value( bi++ );
					Ntamarisk_a += (int) a.value( ai++ );
					Ntamarisk_b += (int) b.value( bi++ );
				}
				phi[idx++] = Nnative_a + Nnative_b;
				phi[idx++] = Nnative_a * Nnative_b;
//				phi[idx++] = Math.abs( Nnative_a - Nnative_b );
				phi[idx++] = Ntamarisk_a + Ntamarisk_b;
				phi[idx++] = Ntamarisk_a * Ntamarisk_b;
//				phi[idx++] = Math.abs( Ntamarisk_a - Ntamarisk_b );
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
			
			final double[] phi = new double[attributes_.size()];
			int idx = 0;
			int ai = 0;
			int bi = 0;
			for( int r = 0; r < params_.Nreaches; ++r ) {
				int Nnative_a = 0;
				int Nnative_b = 0;
				int Ntamarisk_a = 0;
				int Ntamarisk_b = 0;
				for( int h = 0; h < params_.Nhabitats; ++h ) {
					Nnative_a += (int) a[ai++];
					Nnative_b += (int) b[bi++];
					Ntamarisk_a += (int) a[ai++];
					Ntamarisk_b += (int) b[bi++];
				}
				phi[idx++] = Nnative_a + Nnative_b;
				phi[idx++] = Nnative_a * Nnative_b;
//				phi[idx++] = Math.abs( Nnative_a - Nnative_b );
				phi[idx++] = Ntamarisk_a + Ntamarisk_b;
				phi[idx++] = Ntamarisk_a * Ntamarisk_b;
//				phi[idx++] = Math.abs( Ntamarisk_a - Ntamarisk_b );
			}
			
			phi[idx++] = label;
			assert( idx == phi.length );
			return phi;
		}
		
		@Override
		public double[] apply( final double[] a, final double[] b )
		{
			return null;
		}
		
		@Override
		public String keyword()
		{
			return "tamarisk_smart";
		}

		@Override
		public ArrayList<Attribute> attributes()
		{
			return attributes_;
		}
	}
}