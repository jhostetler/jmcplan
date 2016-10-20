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
package edu.oregonstate.eecs.mcplan.domains.planetwars;

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

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.RandomPolicy;
import edu.oregonstate.eecs.mcplan.sim.Episode;
import edu.oregonstate.eecs.mcplan.sim.EpisodeListener;

/**
 * @author jhostetler
 *
 */
public class PwVisualization extends JFrame
{
	private static class PwVisualizationCanvas extends Canvas
	{
		private final Color background_color = Color.black;
		private final Color text_color = Color.white;
		private final int scale = 36;
		private final float planet_border_thickness = 2.0f;
		
		private PwState s = null;
		private final BufferStrategy buffer_strategy;
		
		private final PwGame game;
		private final int logical_half_width;
		private final int logical_half_height;
		
		private final ArrayList<Color> colors = new ArrayList<Color>();
		
		public PwVisualizationCanvas( final JFrame host, final PwGame game,
							  		  final int logical_width, final int logical_height )
		{
			this.game = game;
			logical_half_width = logical_width / 2;
			logical_half_height = logical_height / 2;
			
			setIgnoreRepaint( true );
			setBackground( Color.black );
			
			host.add( this );
			final int two_buffers = 2;
			createBufferStrategy( two_buffers );
			buffer_strategy = getBufferStrategy();
			
			colors.add( Color.blue );
			colors.add( Color.orange );
			colors.add( Color.cyan );
			colors.add( Color.magenta );
			while( colors.size() < game.Nunits() ) {
				for( int i = 0; i < 4; ++i ) {
					colors.add( colors.get( colors.size() - 4 ).brighter() );
				}
			}
		}
		
		private Color playerColor( final PwPlayer p )
		{
			switch( p ) {
				case Neutral: return Color.yellow;
				case Min: return Color.green;
				case Max: return Color.red;
				default: throw new AssertionError();
			}
		}
		
		private Color entityColor( final PwUnit type )
		{
			if( type == null ) {
				return background_color;
			}
			else {
				return colors.get( type.id );
			}
		}
		
		private int planetRadius( final PwPlanet p )
		{
			return 15; //p.capacity*p.capacity / 2;
		}
		
		private String makePopulationString( final int[] population )
		{
			final StringBuilder sb = new StringBuilder();
			boolean sep = false;
			for( final PwUnit type : game.units() ) {
				if( sep ) {
					sb.append( "/" );
				}
				else {
					sep = true;
				}
				sb.append( population[type.id] );
			}
			return sb.toString();
		}
		
		private void drawPopulation( final Graphics2D g, final PwPlanet p, final PwPlayer player, final PwUnit type,
									 final int x, final int y, final int uh, final int sep )
		{
			final int reflect = (player == PwPlayer.Min ? -1 : 1);
			final int shift = (player == PwPlayer.Min ? -1 : 0);
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
				g.fillRect( x + shift*w25 + reflect*xoff, y, w25, uh );
				xoff += w25 + sep;
			}
			for( int i = 0; i < fives; ++i ) {
				g.fillRect( x + shift*w5 + reflect*xoff, y, w5, uh );
				xoff += w5 + sep;
			}
			for( int i = fives; i < fives + ones; ++i ) {
//				g.fillPolygon( new int[] { x, x + uw - sep, x },
//						   	   new int[] { y, y, y + uh }, 3 );
				g.fillRect( x + shift*w1 + reflect*xoff, y, w1, uh );
				xoff += w1 + sep;
			}
		}
		
		public void updateState( final PwState sprime )
		{
			s = sprime;
			final Graphics2D g = (Graphics2D) buffer_strategy.getDrawGraphics();
			
			// Background
			g.setColor( background_color );
			final Rectangle bounds = getBounds();
			g.fillRect( bounds.x, bounds.y, bounds.width, bounds.height );
			
			for( final PwPlanet p : sprime.planets ) {
				final int r = planetRadius( p );
				final int d = 2*r;
				// Need to add Nsites here so that x,y are not negative.
				final int cx = scale * (1 + p.position_x + logical_half_width);
				final int cy = scale * (1 + p.position_y + logical_half_height);
				final int x = cx - r;
				final int y = cy - r;
				// Production progress
				if( p.nextProduced() != null && p.owner() != PwPlayer.Neutral ) {
					g.setColor( entityColor( p.nextProduced() ) );
					g.setStroke( new BasicStroke() );
					final double progress = (double) p.remainingProduction( p.nextProduced() )
											/ (double) p.nextProduced().cost;
					g.fillArc( x, y, d, d, 90, -(int) (360 * progress) );
				}
				// Planet border
				g.setColor( playerColor( p.owner() ) );
				g.setStroke( new BasicStroke( planet_border_thickness ) );
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
				for( final PwUnit type : game.units() ) {
					drawPopulation( g, p, PwPlayer.Min, type, cx, yi, uh, sep );
					drawPopulation( g, p, PwPlayer.Max, type, cx, yi, uh, sep );
					yi += uh + sep;
				}
				g.drawString( Integer.toString( p.getSetup() ), x + r, yi + uh + sep );
			}
			
			g.setFont( g.getFont().deriveFont( 10.0f ) );
			for( final PwRoute route : sprime.routes ) {
				for( int i = 0; i < route.length; ++i ) {
					for( final PwPlayer player : PwPlayer.competitors ) {
						if( route.occupiedAB( i, player ) ) {
							g.setColor( playerColor( player ) );
							final double[] ab_loc = route.locationAB( i );
							final int x = (int) (scale * (1 + ab_loc[0] + logical_half_width) - 1);
							final int y = (int) (scale * (1 + ab_loc[1] + logical_half_height) - 1);
							g.fillOval( x, y, 4, 4 );
							g.drawString( makePopulationString( route.populationAB( i, player ) ), x - 4, y + 5 + 8 );
						}
						
						if( route.occupiedBA( i, player ) ) {
							g.setColor( playerColor( player ) );
							final double[] ba_loc = route.locationBA( i );
							final int x = (int) (scale * (1 + ba_loc[0] + logical_half_width) - 1);
							final int y = (int) (scale * (1 + ba_loc[1] + logical_half_height) - 1);
							g.fillOval( x, y, 4, 4 );
							g.drawString( makePopulationString( route.populationBA( i, player ) ), x - 4, y + 5 + 8 );
						}
					}
				}
			}
			
			g.dispose();
			buffer_strategy.show();
		}
	}
	
	private static class CanvasUpdater implements EpisodeListener<PwState, PwEvent>
	{
		private final PwVisualizationCanvas canvas;
		private final int sleep;
		
		public CanvasUpdater( final PwVisualizationCanvas canvas, final int sleep )
		{
			this.canvas = canvas;
			this.sleep = sleep;
		}
		
		@Override
		public <P extends Policy<PwState, JointAction<PwEvent>>> void startState(
				final PwState s, final double[] r, final P pi )
		{
			canvas.updateState( s );
		}

		@Override
		public void preGetAction()
		{ }

		@Override
		public void postGetAction( final JointAction<PwEvent> action )
		{ }
		
		@Override
		public void onActionsTaken( final PwState sprime, final double[] r )
		{
			canvas.updateState( sprime );
			if( sleep > 0 ) {
				try { Thread.sleep( sleep ); } catch( final Exception e ) { }
			}
		}

		@Override
		public void endState( final PwState s )
		{ }
	}
	
	// -----------------------------------------------------------------------
	
	private final PwVisualizationCanvas canvas;
	private final int sleep;
	
	// FIXME: Should derive 'dim' from map information.
	public PwVisualization( final PwGame game, final Dimension dim,
								 final int width, final int height, final int sleep )
	{
		super( "Planet Wars!" );
		
		this.sleep = sleep;
		
		final Container cp = getContentPane();
		cp.setPreferredSize( dim );
		cp.setLayout( null );
		
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		pack();
		setResizable( false );
		setVisible( true );

		canvas = new PwVisualizationCanvas( this, game, width, height );
		canvas.setBounds( 0, 0, dim.width, dim.height );
	}
	
	public void display( final PwState state )
	{
		canvas.updateState( state );
	}
	
	public void attach( final Episode<PwState, PwEvent> episode )
	{
		episode.addListener( new CanvasUpdater( canvas, sleep ) );
	}
	
	public static void main( final String[] args )
	{
		final RandomGenerator rng = new MersenneTwister( 43 );
		final PwGame game = PwGame.PlanetWarsBasic( rng );
		final PwState s0 = PwMaps.Generic9( game );
		final PwVisualization vis
			= new PwVisualization( game, new Dimension( 720, 720 ), s0.width, s0.height, 1000 );
		final PwSimulator sim = new PwSimulator( game, s0 );
		
		final ActionGenerator<PwState, JointAction<PwEvent>> action_gen = game.actions;
		final Policy<PwState, JointAction<PwEvent>> joint_policy
			= new RandomPolicy<PwState, JointAction<PwEvent>>( rng, action_gen.create() );
		
		final Episode<PwState, PwEvent> episode
			= new Episode<PwState, PwEvent>( sim, joint_policy );
		if( vis != null ) {
			vis.attach( episode );
		}
		episode.run();
		System.exit( 0 );
	}
}