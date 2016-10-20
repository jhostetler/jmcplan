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

import javax.swing.JFrame;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.JointPolicy;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.RandomPolicy;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.sim.Episode;
import edu.oregonstate.eecs.mcplan.sim.EpisodeListener;

/**
 * @author jhostetler
 *
 */
public class VoyagerVisualization<A extends VirtualConstructor<A>> extends JFrame
{
	private static class VoyagerCanvas extends Canvas
	{
		private final Color background_color = Color.black;
		private final Color text_color = Color.white;
		private final int scale = 36;
		private final float planet_border_thickness_ = 2.0f;
		
		private VoyagerState s_ = null;
		private final BufferStrategy buffer_strategy_;
		
		private final VoyagerParameters params_;
		private final int logical_half_width_;
		private final int logical_half_height_;
		
		public VoyagerCanvas( final JFrame host, final VoyagerParameters params,
							  final int logical_width, final int logical_height )
		{
			params_ = params;
			logical_half_width_ = logical_width / 2;
			logical_half_height_ = logical_height / 2;
			
			setIgnoreRepaint( true );
			setBackground( Color.black );
			
			host.add( this );
			final int two_buffers = 2;
			createBufferStrategy( two_buffers );
			buffer_strategy_ = getBufferStrategy();
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
		
		private Color entityColor( final Unit type )
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
			for( final Unit type : Unit.values() ) {
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
		
		private void drawPopulation( final Graphics2D g, final Planet p, final Player player, final Unit type,
									 final int x, final int y, final int uh, final int sep )
		{
			final int reflect = (player == Player.Min ? -1 : 1);
			final int w25 = 10;
			final int w5 = 4;
			final int w1 = 2;
			int pop = p.population( player, type );
			final int twentyfives = pop / 25;
			pop -= twentyfives * 25;
			final int fives = pop / 5;
			pop -= fives * 5;
			final int ones = pop;
			g.setColor( entityColor( type ) );
			int xoff = 0;
			for( int i = 0; i < twentyfives; ++i ) {
				g.fillRect( x + reflect*xoff, y, w25, uh );
				xoff += w25 + sep;
			}
			for( int i = 0; i < fives; ++i ) {
				g.fillRect( x + reflect*xoff, y, w5, uh );
				xoff += w5 + sep;
			}
			for( int i = fives; i < fives + ones; ++i ) {
//				g.fillPolygon( new int[] { x, x + uw - sep, x },
//						   	   new int[] { y, y, y + uh }, 3 );
				g.fillRect( x + reflect*xoff, y, w1, uh );
				xoff += w1 + sep;
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
				final int cx = scale * (1 + p.x + logical_half_width_);
				final int cy = scale * (1 + p.y + logical_half_height_);
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
				for( final Unit type : Unit.values() ) {
					drawPopulation( g, p, Player.Min, type, cx, yi, uh, sep );
					drawPopulation( g, p, Player.Max, type, cx, yi, uh, sep );
					yi += uh + sep;
				}
			}
			
			for( final Spaceship ship : sprime.spaceships ) {
				g.setColor( playerColor( ship.owner ) );
				final int x = (int) (scale * (1 + ship.x + logical_half_width_) - 2);
				final int y = (int) (scale * (1 + ship.y + logical_half_height_) - 2);
				g.fillOval( x, y, 4, 4 );
				g.setFont( g.getFont().deriveFont( 10.0f ) );
				g.drawString( makeShipString( ship ), x - 4, y + 5 + 8 );
			}
			
			g.dispose();
			buffer_strategy_.show();
		}
	}
	
	private static class CanvasUpdater<A extends VirtualConstructor<A>> implements EpisodeListener<VoyagerState, A>
	{
		private final VoyagerCanvas canvas_;
		private final int sleep_;
		
		public CanvasUpdater( final VoyagerCanvas canvas, final int sleep )
		{
			canvas_ = canvas;
			sleep_ = sleep;
		}
		
		@Override
		public <P extends Policy<VoyagerState, JointAction<A>>> void startState(
				final VoyagerState s, final P pi )
		{
			canvas_.updateState( s );
		}

		@Override
		public void preGetAction()
		{ }

		@Override
		public void postGetAction( final JointAction<A> action )
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
	
	// FIXME: Should derive 'dim' from map information.
	public VoyagerVisualization( final VoyagerParameters params, final Dimension dim,
								 final int width, final int height, final int sleep )
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

		canvas_ = new VoyagerCanvas( this, params, width, height );
		canvas_.setBounds( 0, 0, dim.width, dim.height );
	}
	
	public void display( final VoyagerState state )
	{
		canvas_.updateState( state );
	}
	
	public void attach( final Episode<VoyagerState, A> episode )
	{
		episode.addListener( new CanvasUpdater<A>( canvas_, sleep_ ) );
	}
	
	public static void main( final String[] args )
	{
		final VoyagerParameters params = new VoyagerParameters.Builder().master_seed( 641 ).finish();
		final VoyagerState s0 = Maps.Generic9( params );
		final VoyagerInstance instance = new VoyagerInstance( params, s0 );
		final VoyagerVisualization<VoyagerAction> vis
			= new VoyagerVisualization<VoyagerAction>( params, new Dimension( 720, 720 ), s0.width, s0.height, 0 );
		
		final ArrayList<AnytimePolicy<VoyagerState, VoyagerAction>> policies
			= new ArrayList<AnytimePolicy<VoyagerState, VoyagerAction>>();
//		policies.add( new BalancedPolicy( Player.Min, instance.nextSeed(), 0.8, 2.0, 0.1 ) );
//		policies.add( new BalancedPolicy( Player.Max, instance.nextSeed(), 0.7, 1.5, 0 ) );
		final VoyagerActionGenerator action_gen = new VoyagerActionGenerator();
		policies.add( new RandomPolicy<VoyagerState, VoyagerAction>(
			Player.Min.ordinal(), instance.nextSeed(), action_gen.create() ) );
		policies.add( new RandomPolicy<VoyagerState, VoyagerAction>(
			Player.Max.ordinal(), instance.nextSeed(), action_gen.create() ) );
		
//		@SuppressWarnings( "unchecked" )
//		final List<Policy<VoyagerState, ? extends UndoableAction<VoyagerState>>> rollout_policies;
//		rollout_policies.add( new BalancedPolicy( Player.Min, instance.nextSeed(), 0.8, 2.0, 0.1 ) );
//		rollout_policies.add( new BalancedPolicy( Player.Max, instance.nextSeed(), 0.8, 2.0, 0.1 ) );
//		policies.add( new RolloutPolicy<VoyagerState, VoyagerStateToken>(
//			instance.simulator(), action_gen, 1.0, rollout_policies,
//			new ControlMctsVisitor( Player.Max ) ) );
		
//		policies.add( new BalancedPolicy( Player.Max, new MersenneTwister( master_rng.nextInt() ),
//										  0.5, 0.6, 0.1 ) );
//		policies.add( new RandomPolicy<VoyagerState, VoyagerEvent>( 1, 44, new VoyagerActions() ) );
		
		final JointPolicy<VoyagerState, VoyagerAction> joint_policy
			= new JointPolicy<VoyagerState, VoyagerAction>( policies );
		
		final Episode<VoyagerState, VoyagerAction> episode
			= new Episode<VoyagerState, VoyagerAction>( instance.simulator(), joint_policy );
		if( vis != null ) {
			vis.attach( episode );
		}
		episode.run();
		System.exit( 0 );
	}
}
