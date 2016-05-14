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
	public static final class Case
	{
		public final CosmicParameters params;
		public final CosmicState s0;
		
		public Case( final CosmicParameters params, final CosmicState s0 )
		{
			this.params = params;
			this.s0 = s0;
		}
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
	
	private Case unpack( final Object[] cosmic0, final int T )
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
		
		params = new CosmicParameters( this, C, ps, index, T );
		
		final CosmicState s0 = new CosmicState( params, ps, t );
		
		((Disposable) cosmic0[t_idx]).dispose();
		
		return new Case( params, s0 );
	}
	
	public Case init_case9( final int T )
	{
		assert( current_case == null );
		current_case = "case9";
		
		try {
			// [ C, ps, index, opt, t, x, y, event ] = init_case9();
			final Object[] cosmic0 = m.init_case9( 8 );
			return unpack( cosmic0, T );
		}
		catch( final MWException ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	public Case init_case39( final int T, final CosmicOptions jopt )
	{
		assert( current_case == null );
		current_case = "case39";
		
		try {
			// [ C, ps, index, opt, t, event ] = init_case9();
			final Object[] cosmic0 = m.init_case39( 6, jopt.toMatlab() );
			return unpack( cosmic0, T );
		}
		catch( final MWException ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	public Case init_poland( final int T, final CosmicOptions jopt )
	{
		assert( current_case == null );
		current_case = "poland";
		
		try {
			// [ C, ps, index, opt, t, event ] = init_case2383();
			final Object[] cosmic0 = m.init_case2383( 6, jopt.toMatlab() );
			return unpack( cosmic0, T );
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
	public CosmicState take_action2( final CosmicState s, final CosmicAction a, final double delta_t )
	{
		Object[] cprime = null;
		try( final CosmicState scopy = s.copy() ) {
			
			// [ ps, t, x, y, event ] = take_action( ps, opt, t, x, y, event, a, delta_t )
//			cprime = m.take_action( 5, s.ps, opt, s.t, s.mx, s.my, s.event, a.toMatlab( params, s.t ), delta_t );
			cprime = m.take_action2( 1, scopy.ps, opt, scopy.t, a.toMatlab( params, scopy.t ), delta_t );
			
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
