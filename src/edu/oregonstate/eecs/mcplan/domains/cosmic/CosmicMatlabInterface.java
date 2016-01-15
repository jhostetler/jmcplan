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
		final MWNumericArray x = 	(MWNumericArray) 	cosmic0[idx++];
		final MWNumericArray y = 	(MWNumericArray) 	cosmic0[idx++];
		final MWNumericArray event = (MWNumericArray)	cosmic0[idx++];
		
		params = new CosmicParameters( this, C, ps, index, T );
		
		final CosmicState s0 = new CosmicState( params, ps, t, x, y, event );
		
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
			// [ C, ps, index, opt, t, x, y, event ] = init_case9();
			final Object[] cosmic0 = m.init_case39( 8, jopt.toMatlab() );
			return unpack( cosmic0, T );
		}
		catch( final MWException ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	public CosmicState take_action( final CosmicState s, final CosmicAction a, final double delta_t )
	{
		Object[] cprime = null;
		try {
			// [ ps, t, x, y, event ] = take_action( ps, opt, t, x, y, event, a, delta_t )
			cprime = m.take_action( 5, s.ps, opt, s.t, s.mx, s.my, s.event, a.toMatlab( params, s.t ), delta_t );
			
			final double t = ((MWNumericArray) cprime[1]).getDouble();
			
			return new CosmicState( params,
									(MWStructArray) cprime[0],
									t,
									(MWNumericArray) cprime[2],
									(MWNumericArray) cprime[3],
									(MWNumericArray) cprime[4] );
		}
		catch( final MWException ex ) {
			if( ex.getMessage().startsWith( "E_" + CosmicError.NotConverged.toString() ) ) {
				final CosmicState s_terminal = s.copy();
				s_terminal.setError( CosmicError.NotConverged );
				return s_terminal;
			}
			else {
				throw new RuntimeException( ex );
			}
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
