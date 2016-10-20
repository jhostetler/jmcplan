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
package edu.oregonstate.eecs.mcplan.domains.firegirl;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author jhostetler
 *
 */
public class FireGirlDiamondSquareAlgorithm
{
	private final int min_val = 0;
	private final int max_val = 120;
	private final double roughness = 0.5;
	
	private double wiggle_range = (0.5 * (max_val - min_val));
	
	private final double[][] timber_val;
	private final double[][] fuel_load;
	
	private final RandomGenerator rng;
	private final FireGirlParameters params;
	
	private double[][] temp_val = null;
	private double[][] temp_fuel = null;
	
	public FireGirlDiamondSquareAlgorithm( final RandomGenerator rng, final FireGirlParameters params )
	{
		this.rng = rng;
		this.params = params;
		
		assert( params.width >= 32 );
		assert( params.height >= 32 );
		
		timber_val = new double[params.width][params.height];
		fuel_load = new double[params.width][params.height];
	}
	
	public void run()
	{
		temp_val = new double[params.width + 1][params.height + 1];
		temp_fuel = new double[params.width + 1][params.height + 1];
		
		for( int i = 0; i < params.width + 1; ++i ) {
			for( int j = 0; j < params.height + 1; ++j ) {
				temp_val[i][j] = min_val + (max_val - min_val)*rng.nextDouble();
				temp_fuel[i][j] = min_val + (max_val - min_val)*rng.nextDouble();
			}
		}
		
		for( final int scale : new int[] { 16, 8, 4, 2 } ) {
			diamond( scale );
			square( scale );
			wiggle_range *= roughness;
		}
		
		// Average corner values to get the nice, power-of-two final grid size.
		for( int i = 0; i < params.width; ++i ) {
			for( int j = 0; j < params.height; ++j ) {
				timber_val[i][j] = 0.25 * (temp_val[i][j] + temp_val[i+1][j] + temp_val[i][j+1] + temp_val[i+1][j+1]);
				fuel_load[i][j] = 0.25 * (temp_fuel[i][j] + temp_fuel[i+1][j] + temp_fuel[i][j+1] + temp_fuel[i+1][j+1]);
			}
		}
		
		// Allow garbage collection
		temp_val = null;
		temp_fuel = null;
	}
	
	public short[][] getStandAge()
	{
		final short[][] r = new short[params.width][params.height];
		for( int i = 0; i < params.width; ++i ) {
			for( int j = 0; j < params.height; ++j ) {
				r[i][j] = (short) ((int) timber_val[i][j]);
			}
		}
		return r;
	}
	
	public short[][] getFuelLoad()
	{
		final short[][] r = new short[params.width][params.height];
		for( int i = 0; i < params.width; ++i ) {
			for( int j = 0; j < params.height; ++j ) {
				r[i][j] = (short) ((int) fuel_load[i][j]);
			}
		}
		return r;
	}
	
	// -----------------------------------------------------------------------
	
	private void diamond( final int scale )
	{
		// Note: Loop orders interchanged for efficiency
		// FireGirl_DS_alg.py(87)
		final int half_scale = scale / 2;
		assert( half_scale * 2 == scale );
		
		for( int i = half_scale; i < params.width + 1; i += scale ) {
			for( int j = 0; j < params.height + 1; j += scale ) {
				final double val_timb = 0.5 * (temp_val[i-half_scale][j] + temp_val[i+half_scale][j]);
				final double val_fuel = 0.5 * (temp_fuel[i-half_scale][j] + temp_fuel[i+half_scale][j]);
				
				temp_val[i][j] = wiggle( val_timb, min_val, max_val, wiggle_range );
				temp_fuel[i][j] = wiggle( val_fuel, min_val, max_val, wiggle_range );
			}
		}
		
		for( int i = 0; i < params.width + 1; i += scale ) {
			for( int j = half_scale; j < params.height + 1; j += scale ) {
				final double val_timb = 0.5 * (temp_val[i][j+half_scale] + temp_val[i][j-half_scale]);
				final double val_fuel = 0.5 * (temp_fuel[i][j+half_scale] + temp_fuel[i][j-half_scale]);
				
				temp_val[i][j] = wiggle( val_timb, min_val, max_val, wiggle_range );
				temp_fuel[i][j] = wiggle( val_fuel, min_val, max_val, wiggle_range );
			}
		}
	}
	
	private void square( final int scale )
	{
		final int half_scale = scale / 2;
		assert( half_scale * 2 == scale );
		
		for( int i = half_scale; i < params.width + 1; i += scale ) {
			for( int j = half_scale; j < params.height + 1; j += scale ) {
				final double val_timb = 0.25 * (temp_val[i-half_scale][j] + temp_val[i+half_scale][j]
												+ temp_val[i][j-half_scale] + temp_val[i][j+half_scale]);
				final double val_fuel = 0.25 * (temp_fuel[i-half_scale][j] + temp_fuel[i+half_scale][j]
												+ temp_fuel[i][j-half_scale] + temp_fuel[i][j+half_scale]);
				
				temp_val[i][j] = val_timb; //wiggle( val_timb, min_val, max_val, wiggle_range );
				temp_fuel[i][j] = val_fuel; //wiggle( val_fuel, min_val, max_val, wiggle_range );
			}
		}
	}
	
	private double wiggle( final double current_val, final int min, final int max, final double range )
	{
		double top = current_val + (0.5 * range);
		double bottom = top - range;
		top = Math.min( top, max );
		bottom = Math.max( bottom, min );
		return bottom + (top - bottom)*rng.nextDouble();
	}
}
