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

package edu.oregonstate.eecs.mcplan.domains.cosmic;

import jcosmic.JCosmic;

import com.mathworks.toolbox.javabuilder.Disposable;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;

/**
 * Type translation layer for the Cosmic Matlab <-> Java interface.
 */
public final class CosmicMatlabInterface implements AutoCloseable
{
	public static final class Problem
	{
		public final CosmicParameters params;
		public final CosmicState s0;
		
		public Problem( final CosmicParameters params, final CosmicState s0 )
		{
			this.params = params;
			this.s0 = s0;
		}
	}
	
	public static enum Case
	{
		ieee9,
		ieee39,
		rts96,
		poland
	}
	
	// -----------------------------------------------------------------------
	
	public final JCosmic m;
	
	private MWStructArray opt = null;
	private CosmicParameters params = null;
	
	private String current_case = null;
	
	public CosmicMatlabInterface()
	{
		try {
			m = new JCosmic();
		}
		catch( final MWException ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	private Problem unpack( final Case cosmic_case, final Object[] cosmic0, final int T )
	{
		int t_idx = -1;
		int idx = 0;
		final MWStructArray C = 	(MWStructArray) 	cosmic0[idx++];
		final MWStructArray ps = 	(MWStructArray) 	cosmic0[idx++];
		final MWStructArray index =	(MWStructArray)		cosmic0[idx++];
		opt = 						(MWStructArray)		cosmic0[idx++];
		t_idx = idx;
		final int t = 				((MWNumericArray) 	cosmic0[idx++]).getInt();
//		final MWNumericArray x = 	(MWNumericArray) 	cosmic0[idx++];
//		final MWNumericArray y = 	(MWNumericArray) 	cosmic0[idx++];
		final MWNumericArray event = (MWNumericArray)	cosmic0[idx++];
		
		params = new CosmicParameters( this, cosmic_case, C, ps, index, T );
		
		final CosmicState s0 = new CosmicState( params, ps, t );
		
		((Disposable) cosmic0[t_idx]).dispose();
		
		return new Problem( params, s0 );
	}
	
	public Problem init_case9( final int T )
	{
		assert( current_case == null );
		current_case = "case9";
		
		try {
			// [ C, ps, index, opt, t, x, y, event ] = init_case9();
			final Object[] cosmic0 = m.init_case9( 8 );
			return unpack( Case.ieee9, cosmic0, T );
		}
		catch( final MWException ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	public Problem init_case39( final int T, final CosmicOptions jopt )
	{
		assert( current_case == null );
		current_case = "case39";
		
		try {
			// [ C, ps, index, opt, t, event ] = init_case9();
			final Object[] cosmic0 = m.init_case39( 6, jopt.toMatlab() );
			return unpack( Case.ieee39, cosmic0, T );
		}
		catch( final MWException ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	public Problem init_rts96( final int T, final CosmicOptions jopt )
	{
		assert( current_case == null );
		current_case = "rts96";
		
		try {
			final Object[] cosmic0 = m.init_rts96( 6, jopt.toMatlab() );
			return unpack( Case.rts96, cosmic0, T );
		}
		catch( final MWException ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	public Problem init_poland( final int T, final CosmicOptions jopt )
	{
		assert( current_case == null );
		current_case = "poland";
		
		try {
			// [ C, ps, index, opt, t, event ] = init_case2383();
			final Object[] cosmic0 = m.init_case2383( 6, jopt.toMatlab() );
			return unpack( Case.poland, cosmic0, T );
		}
		catch( final MWException ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	public void show_memory()
	{
		try {
			m.show_memory( new Object[] { } );
		}
		catch( final MWException ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	/**
	 * Execute action 'a' in state 's', simulate 'delta_t' time, and return
	 * the resulting state. The input state is not modified. This is the new
	 * version.
	 * @param s
	 * @param a
	 * @param delta_t
	 * @return
	 */
	public CosmicState take_action2( final String context, final CosmicState s, final CosmicAction a, final double delta_t )
	{
		Object[] cprime = null;
		try( final CosmicState scopy = s.copy() ) {
			
			// [ ps, t, x, y, event ] = take_action( ps, opt, t, x, y, event, a, delta_t )
//			cprime = m.take_action( 5, s.ps, opt, s.t, s.mx, s.my, s.event, a.toMatlab( params, s.t ), delta_t );
//			MWCharArray mcontext = null;
			MWNumericArray ma = null;
			try {
//				mcontext = new MWCharArray( context );
				ma = a.toMatlab( params, scopy.t );
				cprime = m.take_action2( 2, context, scopy.ps, opt, scopy.t, ma, delta_t );
			}
			finally {
//				mcontext.dispose();
				ma.dispose();
			}
			
			final MWStructArray ps_prime = (MWStructArray) cprime[0];
			// Update 'opt' in-place (to maintain state of RNG streams)
			opt = (MWStructArray) cprime[1];
			
			// This is designed to verify that 'sprime' really has its own
			// copy of all of the Cosmic data structures, by seeing if changes
			// to ps_prime affect s.ps
			final int sentinel = 99999;
			final int old_id = M.field_getInt( ps_prime, "bus", 1, new int[] { 1, 1 } );
//			final int old_id = ((MWNumericArray) ps_prime.getField( "bus", 1 )).getInt( new int[] { 1, 1 } );
			M.field_set( ps_prime, "bus", 1, new int[] { 1, 1 }, sentinel );
//			ps_prime.getField( "bus", 1 ).set( new int[] { 1, 1 }, sentinel );
			assert( M.field_getInt( s.ps, "bus", 1, new int[] { 1, 1 } ) != sentinel );
//			assert( ((MWNumericArray) s.ps.getField( "bus", 1 )).getInt( new int[] { 1, 1 } ) != sentinel );
			M.field_set( ps_prime, "bus", 1, new int[] { 1, 1 }, old_id );
//			ps_prime.getField( "bus", 1 ).set( new int[] { 1, 1 }, old_id );
			
			final CosmicState sprime = new CosmicState(	params, ps_prime, scopy.t + delta_t );
			return sprime;
		}
		catch( final MWException ex ) {
			throw new RuntimeException( ex );
		}
//		finally {
//			if( cprime != null ) {
//				// We turned 't' into a primitive 'int' earlier.
//				((Disposable) cprime[1]).dispose();
//			}
//		}
	}
	
	/**
	 * Execute action 'a' in state 's', simulate 'delta_t' time, and return
	 * the resulting state. The input state is not modified. This is the new
	 * version.
	 * @param s
	 * @param a
	 * @param delta_t
	 * @return
	 */
	public CosmicState take_action_iter( final CosmicState s, final CosmicAction a, final double delta_t )
	{
		Object[] cprime = null;
		try( final CosmicState scopy = s.copy() ) {
			
			// [ ps ] = take_action( ps, opt, t, a, delta_t )
			cprime = m.take_action_iter( 1, scopy.ps, opt, scopy.t, a.toMatlab( params, scopy.t ), delta_t );
			
			final MWStructArray ps_prime = (MWStructArray) cprime[0];
			
			// This is designed to verify that 'sprime' really has its own
			// copy of all of the Cosmic data structures, by seeing if changes
			// to ps_prime affect s.ps
			final int sentinel = 99999;
			final int old_id = ((MWNumericArray) ps_prime.getField( "bus", 1 )).getInt( new int[] { 1, 1 } );
			ps_prime.getField( "bus", 1 ).set( new int[] { 1, 1 }, sentinel );
			assert( ((MWNumericArray) s.ps.getField( "bus", 1 )).getInt( new int[] { 1, 1 } ) != sentinel );
			ps_prime.getField( "bus", 1 ).set( new int[] { 1, 1 }, old_id );
			
			final CosmicState sprime = new CosmicState(	params, ps_prime, scopy.t + delta_t );
			return sprime;
		}
		catch( final MWException ex ) {
			throw new RuntimeException( ex );
		}
//		finally {
//			if( cprime != null ) {
//				// We turned 't' into a primitive 'int' earlier.
//				((Disposable) cprime[1]).dispose();
//			}
//		}
	}
	
	/**
	 * @deprecated Use take_action2()
	 * @param s
	 * @param a
	 * @param delta_t
	 * @return
	 */
	@Deprecated
	public CosmicState take_action( final CosmicState s, final CosmicAction a, final double delta_t )
	{
		Object[] cprime = null;
		try( final CosmicState scopy = s.copy() ) {
			
			// [ ps, t, x, y, event ] = take_action( ps, opt, t, x, y, event, a, delta_t )
//			cprime = m.take_action( 5, s.ps, opt, s.t, s.mx, s.my, s.event, a.toMatlab( params, s.t ), delta_t );
			cprime = m.take_action( 5, scopy.ps, opt, scopy.t, scopy.mx, scopy.my,
									scopy.event, a.toMatlab( params, scopy.t ), delta_t );
			
			final MWStructArray ps_prime = (MWStructArray) cprime[0];
			final double t = ((MWNumericArray) cprime[1]).getDouble();
			final MWNumericArray x_prime = (MWNumericArray) cprime[2];
			final MWNumericArray y_prime = (MWNumericArray) cprime[3];
			final MWNumericArray event_prime = (MWNumericArray) cprime[4];
			
			// This is designed to verify that 'sprime' really has its own
			// copy of all of the Cosmic data structures, by seeing if changes
			// to ps_prime affect s.ps
			final int sentinel = 99999;
			final int old_id = ((MWNumericArray) ps_prime.getField( "bus", 1 )).getInt( new int[] { 1, 1 } );
			ps_prime.getField( "bus", 1 ).set( new int[] { 1, 1 }, sentinel );
			assert( ((MWNumericArray) s.ps.getField( "bus", 1 )).getInt( new int[] { 1, 1 } ) != sentinel );
			ps_prime.getField( "bus", 1 ).set( new int[] { 1, 1 }, old_id );
			
			final CosmicState sprime = new CosmicState(
				params, ps_prime, t, x_prime, y_prime, event_prime );
			return sprime;
		}
		catch( final MWException ex ) {
			throw new RuntimeException( ex );
		}
		finally {
			if( cprime != null ) {
				// We turned 't' into a primitive 'int' earlier.
				((Disposable) cprime[1]).dispose();
			}
		}
	}
	
	@Override
	public void close()
	{
		if( opt != null ) {
			opt.dispose();
		}
		
		if( params != null ) {
			params.close();
		}
		
		m.dispose();
	}
}
