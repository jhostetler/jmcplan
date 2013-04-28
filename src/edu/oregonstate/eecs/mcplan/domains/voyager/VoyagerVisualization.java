/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.RolloutPolicy;
import edu.oregonstate.eecs.mcplan.domains.voyager.policies.BalancedPolicy;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveListener;
import edu.oregonstate.eecs.mcplan.sim.SimultaneousMoveRunner;

/**
 * @author jhostetler
 *
 */
public class VoyagerVisualization extends JFrame
{
	private static class VoyagerCanvas extends Canvas
	{
		private final Color background_color = Color.black;
		private final Color text_color = Color.white;
		private final int scale = 36;
		private final float planet_border_thickness_ = 2.0f;
		
		private VoyagerState s_ = null;
		private final VoyagerParameters params_;
		private final BufferStrategy buffer_strategy_;
		
		public final int map_side;
		
		public VoyagerCanvas( final JFrame host, final VoyagerParameters params )
		{
			params_ = params;
			
			setIgnoreRepaint( true );
			setBackground( Color.black );
			
			host.add( this );
			final int two_buffers = 2;
			createBufferStrategy( two_buffers );
			buffer_strategy_ = getBufferStrategy();
			
			map_side = params.Nsites * scale;
		}
		
		private Color playerColor( final Player p )
		{
			switch( p ) {
				case Neutral: return Color.yellow;
				case Min: return Color.green;
				case Max: return Color.red;
				default: throw new AssertionError();
			}
		}
		
		private Color entityColor( final EntityType type )
		{
			if( type == null ) {
				return background_color;
			}
			switch( type ) {
				case Worker: return Color.blue;
				case Soldier: return Color.orange;
				default: throw new AssertionError();
			}
		}
		
		private int planetRadius( final Planet p )
		{
			return p.capacity*p.capacity / 2;
		}
		
		private String makeShipString( final Spaceship ship )
		{
			final StringBuilder sb = new StringBuilder();
			boolean sep = false;
			for( final EntityType type : EntityType.values() ) {
				if( sep ) {
					sb.append( "/" );
				}
				else {
					sep = true;
				}
				sb.append( ship.population[type.ordinal()] );
			}
			return sb.toString();
		}
		
		private void drawPopulation( final Graphics2D g, final Planet p, final EntityType type,
									 final int x, final int y, final int uh, final int sep )
		{
			final int w25 = 10;
			final int w5 = 4;
			final int w1 = 2;
			int pop = p.population( type );
			final int twentyfives = pop / 25;
			pop -= twentyfives * 25;
			final int fives = pop / 5;
			pop -= fives * 5;
			final int ones = pop;
			g.setColor( entityColor( type ) );
			int xi = x;
			for( int i = 0; i < twentyfives; ++i ) {
				g.fillRect( xi, y, w25, uh );
				xi += w25 + sep;
			}
			for( int i = 0; i < fives; ++i ) {
				g.fillRect( xi, y, w5, uh );
				xi += w5 + sep;
			}
			for( int i = fives; i < fives + ones; ++i ) {
//				g.fillPolygon( new int[] { x, x + uw - sep, x },
//						   	   new int[] { y, y, y + uh }, 3 );
				g.fillRect( xi, y, w1, uh );
				xi += w1 + sep;
			}
		}
		
		public void updateState( final VoyagerState sprime )
		{
			s_ = sprime;
			final Graphics2D g = (Graphics2D) buffer_strategy_.getDrawGraphics();
			
			// Background
			g.setColor( background_color );
			final Rectangle bounds = getBounds();
			g.fillRect( bounds.x, bounds.y, bounds.width, bounds.height );
			
			for( final Planet p : sprime.planets ) {
				final int r = planetRadius( p );
				final int d = 2*r;
				// Need to add Nsites here so that x,y are not negative.
				final int cx = scale * (p.x + params_.Nsites);
				final int cy = scale * (p.y + params_.Nsites);
				final int x = cx - r;
				final int y = cy - r;
				// Production progress
				if( p.nextProduced() != null && p.owner() != Player.Neutral ) {
					g.setColor( entityColor( p.nextProduced() ) );
					g.setStroke( new BasicStroke() );
					final double progress = (double) p.remainingProduction( p.nextProduced() )
											/ (double) p.nextProduced().cost();
					g.fillArc( x, y, d, d, 90, -(int) (360 * progress) );
				}
				// Planet border
				g.setColor( playerColor( p.owner() ) );
				g.setStroke( new BasicStroke( planet_border_thickness_ ) );
				g.drawOval( x, y, d, d );
				// Text stuff
				g.setColor( text_color );
				g.drawString( Integer.toString( p.id ), x + r, y + r );
//				g.drawString( "W |", x, y + 2*r + 2 );
//				g.drawString( "S |", x, y + 2*r + 2 + 10 );
				// Amount of units
				final int uh = 4;
				final int sep = 2;
				int yi = y + d + sep;
				for( final EntityType type : EntityType.values() ) {
					drawPopulation( g, p, type, x, yi, uh, sep );
					yi += uh + sep;
				}
			}
			
			for( final Spaceship ship : sprime.spaceships ) {
				g.setColor( playerColor( ship.owner ) );
				final int x = (int) (scale * (ship.x + params_.Nsites) - 2);
				final int y = (int) (scale * (ship.y + params_.Nsites) - 2);
				g.fillOval( x, y, 4, 4 );
				g.setFont( g.getFont().deriveFont( 10.0f ) );
				g.drawString( makeShipString( ship ), x - 4, y + 5 + 8 );
			}
			
			g.dispose();
			buffer_strategy_.show();
		}
	}
	
	private static class CanvasUpdater implements SimultaneousMoveListener<VoyagerState, VoyagerEvent>
	{
		private final VoyagerCanvas canvas_;
		private final int sleep_;
		
		public CanvasUpdater( final VoyagerCanvas canvas, final int sleep )
		{
			canvas_ = canvas;
			sleep_ = sleep;
		}
		
		@Override
		public <P extends Policy<VoyagerState, VoyagerEvent>> void startState(
				final VoyagerState s, final ArrayList<P> policies )
		{
			canvas_.updateState( s );
		}

		@Override
		public void preGetAction( final int player )
		{ }

		@Override
		public void postGetAction( final int player, final VoyagerEvent action )
		{ }

		@Override
		public void onActionsTaken( final VoyagerState sprime )
		{
			canvas_.updateState( sprime );
			if( sleep_ > 0 ) {
				try { Thread.sleep( sleep_ ); } catch( final Exception e ) { }
			}
		}

		@Override
		public void endState( final VoyagerState s )
		{ }
	}
	
	// -----------------------------------------------------------------------
	
	private final VoyagerCanvas canvas_;
	private final int sleep_;
	
	public VoyagerVisualization( final VoyagerParameters params, final Dimension dim, final int sleep )
	{
		super( "Voyager" );
		
		sleep_ = sleep;
		
		final Container cp = getContentPane();
		cp.setPreferredSize( dim );
		cp.setLayout( null );
		
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		pack();
		setResizable( false );
		setVisible( true );

		canvas_ = new VoyagerCanvas( this, params );
		canvas_.setBounds( 0, 0, dim.width, dim.height );
	}
	
	public void display( final VoyagerState state )
	{
		canvas_.updateState( state );
	}
	
	public void attach( final SimultaneousMoveRunner<VoyagerState, VoyagerEvent> runner )
	{
		runner.addListener( new CanvasUpdater( canvas_, sleep_ ) );
	}
	
	public static void main( final String[] args )
	{
		final VoyagerParameters params = new VoyagerParameters.Builder().finish();
		final VoyagerVisualization vis = new VoyagerVisualization( params, new Dimension( 720, 720 ), 100 );
		final VoyagerInstance instance = new VoyagerInstance( params, 42 );
		final RandomGenerator master_rng = new MersenneTwister( instance.seed );
		
		final ArrayList<AnytimePolicy<VoyagerState, VoyagerEvent>> policies
			= new ArrayList<AnytimePolicy<VoyagerState, VoyagerEvent>>();
		policies.add( new BalancedPolicy( Player.Min, new MersenneTwister( master_rng.nextInt() ),
										  0.8, 2.0, 0.1 ) );
		final VoyagerActionGenerator action_gen = new VoyagerActionGenerator();
		@SuppressWarnings( "unchecked" )
		final List<Policy<VoyagerState, VoyagerEvent>> rollout_policies = Arrays.asList(
			(Policy<VoyagerState, VoyagerEvent>) new BalancedPolicy(
				Player.Min, new MersenneTwister( master_rng.nextInt() ), 0.8, 2.0, 0.1 ),
			(Policy<VoyagerState, VoyagerEvent>) new BalancedPolicy(
				Player.Max, new MersenneTwister( master_rng.nextInt() ), 0.8, 2.0, 0.1 ) );
		policies.add( new RolloutPolicy<VoyagerState, VoyagerEvent>(
			instance.simulator(), action_gen, 1.0, rollout_policies,
			new ControlMctsVisitor() {
				@Override public double terminal( final VoyagerState s ) {
					final Player winner = Voyager.winner( s );
					if( winner == Player.Max ) {
						return 1;
					}
					else {
						return 0;
					}
				}
			} ) );
//		policies.add( new RandomPolicy<VoyagerState, VoyagerEvent>(
//			Player.Max.ordinal(), master_rng.nextInt(), action_gen ) );
//		policies.add( new BalancedPolicy( Player.Max, new MersenneTwister( master_rng.nextInt() ),
//										  0.5, 0.6, 0.1 ) );
//		policies.add( new RandomPolicy<VoyagerState, VoyagerEvent>( 1, 44, new VoyagerActions() ) );
		final SimultaneousMoveRunner<VoyagerState, VoyagerEvent> runner
			= new SimultaneousMoveRunner<VoyagerState, VoyagerEvent>( instance.simulator(), policies, 4096, 8000 );
		vis.attach( runner );
		runner.run();
	}
}
