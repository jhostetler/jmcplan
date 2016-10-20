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

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.util.Fn;



/**
 * @author jhostetler
 *
 */
public final class FireGirlParameters
{
	public final double[] growth_model_2 = new double[] {
			0, 0.357, 0.731, 1.122, 1.532, 1.963, 2.416, 2.895, 3.4, 3.935,
			4.502, 5.104, 5.744, 6.424, 7.148, 7.919, 8.741, 9.616, 10.55, 11.544,
			12.603, 13.73, 14.928, 16.201, 17.549, 18.977, 20.485, 22.075, 23.746, 25.499,
			27.331, 29.24, 31.223, 33.275, 35.39, 37.562, 39.783, 42.043, 44.335, 46.648,
			48.971, 51.294, 53.607, 55.899, 58.159, 60.38, 62.552, 64.667, 66.719, 68.702,
			70.611, 72.443, 74.196, 75.867, 77.457, 78.965, 80.393, 81.741, 83.014, 84.212,
			85.339, 86.398, 87.392, 88.326, 89.201, 90.023, 90.794, 91.518, 92.198, 92.838,
			93.44, 94.007, 94.542, 95.047, 95.526, 95.979, 96.41, 96.82, 97.211, 97.585,
			97.942, 98.285, 98.615, 98.933, 99.24, 99.537, 99.825, 100.105, 100.377, 100.643,
			100.902, 101.156, 101.405, 101.649, 101.889, 102.125, 102.358, 102.588, 102.814, 103.039,
			103.261, 103.481, 103.699, 103.915, 104.13, 104.343, 104.556, 104.767, 104.976, 105.185,
			105.394, 105.601, 105.808, 106.014, 106.219, 106.424, 106.628, 106.833, 107.036, 107.239,
			107.442, 107.645, 107.848, 108.05, 108.252, 108.454, 108.655, 108.857, 109.058, 109.259,
			109.461, 109.662, 109.862, 110.063, 110.264, 110.465, 110.665, 110.866, 111.066, 111.267,
			111.467, 111.668, 111.868, 112.068, 112.268, 112.469, 112.669, 112.869, 113.069, 113.269,
			113.47, 113.67, 113.87, 114.07, 114.27, 114.47, 114.67, 114.87, 115.07, 115.27,
			115.47, 115.671, 115.871, 116.071, 116.271, 116.471, 116.671, 116.871, 117.071, 117.271,
			117.471, 117.671, 117.871, 118.071, 118.271, 118.471, 118.671, 118.871, 119.071, 119.271,
			119.471, 119.671, 119.871, 120.071, 120.271, 120.471, 120.671, 120.871, 121.071, 121.271,
			121.471, 121.671, 121.871, 122.071, 122.271, 122.471, 122.671, 122.871, 123.071, 123.271,
			123.471, 123.671, 123.871, 124.071, 124.271, 124.471, 124.671, 124.871, 125.071, 125.271,
			125.471, 125.671, 125.871, 126.071, 126.271, 126.471, 126.671, 126.871, 127.071, 127.271,
			127.471, 127.671, 127.871, 128.071, 128.271, 128.471, 128.671, 128.871, 129.071, 129.271,
			129.471, 129.671, 129.871, 130.071, 130.271, 130.471, 130.671, 130.871, 131.071, 131.271,
			131.471, 131.671, 131.871, 132.071, 132.271, 132.471, 132.671, 132.871, 133.071, 133.271,
			133.471, 133.671, 133.871, 134.071, 134.271, 134.471, 134.671, 134.871, 135.071, 135.271,
			135.471, 135.671, 135.871, 136.071, 136.271, 136.471, 136.671, 136.871, 137.071, 137.271,
			137.471, 137.671, 137.871, 138.071, 138.271, 138.471, 138.671, 138.871, 139.071, 139.271,
			139.471, 139.671, 139.871, 140.071, 140.271, 140.471, 140.671, 140.871, 141.071, 141.271,
			141.471, 141.671, 141.871, 142.071, 142.271, 142.471, 142.671, 142.871, 143.071, 143.271,
			143.471 };
	
	// -----------------------------------------------------------------------
	
	public FireGirlParameters(
		final int T, final double discount,
		final FactoredRepresenter<FireGirlState, ? extends FactoredRepresentation<FireGirlState>> base_repr )
	{
		this.T = T;
		this.discount = discount;
		this.base_repr = base_repr;
		
		assert( Fn.isPowerOf2( width ) );
		assert( Fn.isPowerOf2( height ) );
	}
	
	public final int T;
	public final double discount;
	public final FactoredRepresenter<FireGirlState, ? extends FactoredRepresentation<FireGirlState>> base_repr;
	
	public final int width = 128;
	public final int height = 128;
	
	public final int max_age = 300;
	
	public final int window_NW = 43;
	public final int window_SE = 86;
	
	// Note: ignition_prob is an int in [0, 100] in Python code
	public final double ignition_prob = 1.0;
	
	public final int temp_summer_high = 90;
	public final int temp_winter_low = -90;
	public final double temp_var = 10.0;
	
	public final double wind_mean = 10.0;
	
	public final double fire_param_inputscale = 10;
	public final double fire_param_outputscale = 10;
	public final double fire_param_zeroadjust = 15;
	public final double fire_param_smoothness = 0.4;
	public final int fire_param_reach = 1;
	
	public final int min_spread_windtemp = 0;
	public final int min_spread_fuel = 10;
	
	public final double crownfire_param_inputscale = 10;
	public final double crownfire_param_outputscale = 1;
	public final double crownfire_param_zeroadjust = 5;
	public final double crownfire_param_smoothness = 1;
	
	public final int fire_average_end_day = 2;
	public final double fire_suppression_rate = 0.05;
	// NOTE: Deviation from Python. Made costs negative
	public final int fire_suppression_cost_per_cell = -400;
	// NOTE: Deviation from Python. Made costs negative
	public final int fire_suppression_cost_per_day = -2000;
	// NOTE: Deviation from Python. Moved this from body of doFire() because
	// I'm using it to calculate value bounds in FireGirlFsssModel.
	public final int fire_iter_cap = 5000;
	
	public final double growth_timber_constant = 22.0;
	public final double growth_fuel_accumulation = 2;
	
	/**
	 * Note that this is actually a map from tree age -> timber value.
	 */
	public final double[] growth_model = growth_model_2;
	public final double max_growth = Fn.max( Fn.derivative( growth_model ) );
	
	public final int logging_block_width = 10;
	public final int logging_min_value = 50;
	public final int logging_slash_remaining = 10;
	public final double logging_percentOfIncrement = 0.95;
	public final int logging_max_cuts = 30;
}
