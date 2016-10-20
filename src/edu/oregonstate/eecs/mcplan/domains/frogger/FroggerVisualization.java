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
package edu.oregonstate.eecs.mcplan.domains.frogger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.sim.EpisodeListener;

/**
 * @author jhostetler
 *
 */
public class FroggerVisualization
{
	private static final Color TrackColor = Color.lightGray;
	private static final Color WallColor = Color.black;
	
	public final int scale;
	
	public FroggerState state = null;
	public FroggerSimulator sim = null;
	private final double tstep_ = 0.2;
	
	private DrawPanel draw_panel_ = null;
	private ControlPanel control_panel_ = null;
	
	public FroggerVisualization( final FroggerSimulator sim, final FroggerParameters params, final int scale )
	{
		if( sim != null ) {
			this.sim = sim;
			this.state = sim.state();
		}
		
		this.scale = scale;
		
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				@Override
				public void run()
				{
					final JFrame frame = new JFrame();
					final Container cp = frame.getContentPane();
					cp.setLayout( new BorderLayout() );
					
					draw_panel_ = new DrawPanel( params );
					final Dimension d = new Dimension( scale*params.road_length + 20, scale*(params.lanes + 2) + 20 );
					draw_panel_.setPreferredSize( d );
					draw_panel_.setSize( d );
					cp.add( draw_panel_, BorderLayout.CENTER );
					
					control_panel_ = new ControlPanel();
					cp.add( control_panel_, BorderLayout.SOUTH );
					
					frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
					frame.setResizable( false );
					frame.pack();
					frame.setVisible( true );
				}
			} );
		}
		catch( final Exception ex ) {
			throw new RuntimeException( ex );
		}
		
		if( sim != null ) {
			updateStateOnEDT( sim.state() );
		}
	}
	
	public void init()
	{
		
	}
	
	public void updateState( final int x, final int y, final Tile[][] grid )
	{
		draw_panel_.updateState( x, y, grid );
		control_panel_.updateState( x, y );
	}
	
	public void updateState( final FroggerState s )
	{
		final int x = s.frog_x;
		final int y = s.frog_y;
		final Tile[][] grid = new Tile[s.params.lanes + 2][s.params.road_length];
		for( int i = 0; i < s.params.lanes + 2; ++i ) {
			for( int j = 0; j < s.params.road_length; ++j ) {
				grid[i][j] = s.grid[i][j];
			}
		}
		updateState( x, y, grid );
		if( s.goal ) {
			System.out.println( "Goal!" );
		}
		else if( s.squashed ) {
			System.out.println( "Squashed!" );
		}
	}
	
	public void updateStateOnEDT( final FroggerState s )
	{
		final int x = s.frog_x;
		final int y = s.frog_y;
		final Tile[][] grid = new Tile[s.params.lanes + 2][s.params.road_length];
		for( int i = 0; i < s.params.lanes + 2; ++i ) {
			for( int j = 0; j < s.params.road_length; ++j ) {
				grid[i][j] = s.grid[i][j];
			}
		}
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				@Override
				public void run() {
					updateState( x, y, grid );
				}
			} );
		}
		catch( final Exception ex ) {
			throw new RuntimeException( ex );
		}
		if( s.isTerminal() ) {
			
		}
	}
	
	public Updater updater( final int sleep )
	{
		return new Updater( sleep );
	}
	
	private class Updater implements EpisodeListener<FroggerState, FroggerAction>
	{
		private final int sleep_;
		
		public Updater( final int sleep )
		{
			sleep_ = sleep;
		}

		@Override
		public <P extends Policy<FroggerState, JointAction<FroggerAction>>>
		void startState( final FroggerState s, final double[] r, final P pi )
		{
			updateStateOnEDT( s );
		}

		@Override
		public void preGetAction()
		{ }

		@Override
		public void postGetAction( final JointAction<FroggerAction> action )
		{ }

		@Override
		public void onActionsTaken( final FroggerState sprime, final double[] r )
		{
			updateStateOnEDT( sprime );
			if( sleep_ > 0 ) {
				try { Thread.sleep( sleep_ ); } catch( final Exception e ) { }
			}
		}

		@Override
		public void endState( final FroggerState s )
		{ }
	}
	
	private class UserActionN extends AbstractAction
	{
		public UserActionN() { super( "^" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<FroggerAction>( new MoveAction( 0, 1 ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionE extends AbstractAction
	{
		public UserActionE() { super( ">" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<FroggerAction>( new MoveAction( 1, 0 ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionS extends AbstractAction
	{
		public UserActionS() { super( "v" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<FroggerAction>( new MoveAction( 0, -1 ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionW extends AbstractAction
	{
		public UserActionW() { super( "<" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<FroggerAction>( new MoveAction( -1, 0 ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionSit extends AbstractAction
	{
		public UserActionSit() { super( "S" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<FroggerAction>( new MoveAction( 0, 0 ) ) );
			updateState( sim.state() );
		}
	}
	
	private class ControlPanel extends JPanel
	{
		private final JLabel x_ = new JLabel( "X: " );
		private final JLabel y_ = new JLabel( "Y: " );
//		private final JLabel goal_ = new JLabel( "Goal? " );
//		private final JLabel crashed_ = new JLabel( "Crashed? " );
		
		public ControlPanel()
		{
			final JPanel controls = new JPanel();
			controls.setPreferredSize( new Dimension( 120, 120 ) );
			final LayoutManager layout = new GridLayout( 3, 3 );
			controls.setLayout( layout );
			controls.add( new JPanel() );
			controls.add( makeButton( new UserActionN() ) );
			controls.add( new JPanel() );
			controls.add( makeButton( new UserActionW() ) );
			controls.add( makeButton( new UserActionSit() ) );
			controls.add( makeButton( new UserActionE() ) );
			controls.add( new JPanel() );
			controls.add( makeButton( new UserActionS() ) );
			controls.add( new JPanel() );
			add( controls );
			
			final JPanel telemetry = new JPanel();
			telemetry.setLayout( new BoxLayout( telemetry, BoxLayout.Y_AXIS ) );
			telemetry.add( x_ );
			telemetry.add( y_ );
			add( telemetry );
		}
		
		public void updateState( final int x, final int y )
		{
			x_.setText( "X: " + x );
			y_.setText( "Y: " + y );
			
//			if( sim != null ) {
//				System.out.println( "Eval: " + Arrays.toString(
//					new SectorEvaluator( sim.state().terminal_velocity() ).evaluate( sim ) ) );
//			}
		}
		
		private JButton makeButton( final Action action )
		{
			final JButton b = new JButton( action );
			b.setMargin( new Insets( 2, 2, 2, 2 ) );
			return b;
		}
	}
	
	private class DrawPanel extends JPanel
	{
		private Tile[][] grid_ = null;
		
		private int car_x = 0;
		private int car_y = 0;
		
		public DrawPanel( final FroggerParameters params )
		{
			grid_ = new Tile[params.lanes + 2][params.road_length];
			for( int j = 0; j < params.road_length; ++j ) {
				grid_[0][j] = Tile.Start;
				grid_[params.lanes + 1][j] = Tile.Goal;
				for( int i = 0; i < params.lanes; ++i ) {
					grid_[i+1][j] = Tile.Empty;
				}
			}
		}
		
		@Override
		public void paintComponent( final Graphics graphics )
		{
			final Graphics2D g = (Graphics2D) graphics.create();
			
			// Invert y-axis
			g.translate( 0, this.getHeight() );
			g.scale( 1, -1 );
			
			// Outer boundary
			g.setColor( WallColor );
			g.fillRect( 0, 0, this.getWidth(), this.getHeight() );
			
			// Display margins and scaling
			g.translate( 10, 10 );
			g.scale( scale, scale );
			
			for( int i = 0; i < grid_.length; ++i ) {
				for( int j = 0; j < grid_[i].length; ++j ) {
					final Tile t = grid_[i][j];
					switch( t ) {
					case Car:
						g.setColor( Color.black );
						break;
					case Empty:
						g.setColor( Color.lightGray );
						break;
					case Goal:
						g.setColor( Color.red );
						break;
					case Start:
						g.setColor( Color.green );
						break;
					}
					g.fillRect( j, i, 1, 1 );
				}
			}
			
			// Car
			g.setColor( Color.blue );
			g.scale( 0.25, 0.25 );
			g.fillRect( 4*car_x + 1, 4*car_y + 1, 2, 2 );
			
			g.dispose();
		}
		
		private void updateState( final int car_x, final int car_y, final Tile[][] grid )
		{
			this.car_x = car_x;
			this.car_y = car_y;
			grid_ = grid;
			repaint();
		}
	}
	
	
	public static void main( final String[] argv )
	{
		final RandomGenerator rng = new MersenneTwister( 46 );
		
		final FroggerParameters params = new FroggerParameters();
		final FroggerState state = new FroggerState( params );
		final FroggerSimulator sim = new FroggerSimulator( rng, state );
		final int scale = 20;
		final FroggerVisualization vis = new FroggerVisualization( sim, params, scale );
	}
}
