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
	
	public Case init_case9()
	{
		assert( current_case == null );
		current_case = "case9";
		
		Object[] cosmic0 = null;
		try {
			// [ C, ps, opt, t, x, y, event ] = init_case9();
			cosmic0 = m.init_case9( 7 );
			
			final MWStructArray C = (MWStructArray) cosmic0[0];
			
			final MWStructArray ps = (MWStructArray) cosmic0[1];
			opt = (MWStructArray) cosmic0[2];
			final int t = ((MWNumericArray) cosmic0[3]).getInt();
			final MWNumericArray x = (MWNumericArray) cosmic0[4];
			final MWNumericArray y = (MWNumericArray) cosmic0[5];
			final MWNumericArray event = (MWNumericArray) cosmic0[6];
			
			final int nx = x.getDimensions()[0];
			final int ny = y.getDimensions()[0];
			
			// FIXME: Remove hardcoded time horizon
			final double T = 30;
			params = new CosmicParameters( this, C, ps, nx, ny, T );
			
			final CosmicState s0 = new CosmicState( params, ps, t, x, y, event );
			
			return new Case( params, s0 );
		}
		catch( final MWException ex ) {
			throw new RuntimeException( ex );
		}
		finally {
			if( cosmic0 != null ) {
				// We turned 't' into a primitive 'int' earlier, so we no
				// longer need the Matlab object.
				((Disposable) cosmic0[3]).dispose();
			}
		}
	}
	
	public CosmicState take_action( final CosmicState s, final CosmicAction a, final double delta_t )
	{
		Object[] cprime = null;
		try {
			// [ ps, t, x, y, event ] = take_action( ps, opt, t, x, y, event, a, delta_t )
			cprime = m.take_action( 5, s.ps, opt, s.t, s.x, s.y, s.event, a.toMatlab( params, s.t ), delta_t );
			
			final double t = ((MWNumericArray) cprime[1]).getDouble();
			
			return new CosmicState( params,
									(MWStructArray) cprime[0],
									t,
									(MWNumericArray) cprime[2],
									(MWNumericArray) cprime[3],
									(MWNumericArray) cprime[4] );
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