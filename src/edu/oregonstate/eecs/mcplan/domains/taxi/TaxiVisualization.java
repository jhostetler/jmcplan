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
package edu.oregonstate.eecs.mcplan.domains.taxi;

import java.awt.BasicStroke;
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
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

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
public class TaxiVisualization
{
	private static final Color TrackColor = Color.lightGray;
	private static final Color WallColor = Color.black;
	
	public final int scale;
	
	public TaxiState state = null;
	public TaxiSimulator sim = null;
	private final double tstep_ = 0.2;
	
	private DrawPanel draw_panel_ = null;
	private ControlPanel control_panel_ = null;
	
	public TaxiVisualization( final TaxiSimulator sim, final int[][] topology, final ArrayList<int[]> locations, final int scale )
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
					
					draw_panel_ = new DrawPanel( topology, locations );
					final Dimension d = new Dimension( scale*topology.length + 20, scale*topology[0].length + 20 );
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
	
	public void updateState( final int[] taxi, final int passenger, final int[][] other_taxis,
							 final int destination,
							 final boolean illegal, final boolean goal )
	{
		draw_panel_.updateState( taxi, passenger, other_taxis, destination, illegal, goal );
		control_panel_.updateState( taxi, passenger, other_taxis, destination, illegal, goal );
	}
	
	public void updateState( final TaxiState s )
	{
		final int[] taxi = s.taxi;
		final int passenger = s.passenger;
		final int[][] other_taxis = s.other_taxis;
		final int destination = s.destination;
		final boolean illegal = s.illegal_pickup_dropoff;
		final boolean goal = s.goal;
		updateState( taxi, passenger, other_taxis, destination, illegal, goal );
		if( s.goal ) {
			System.out.println( "Goal!" );
		}
		else if( s.illegal_pickup_dropoff ) {
			System.out.println( "Illegal!" );
		}
		else if( s.pickup_success ) {
			System.out.println( "Pickup success!" );
		}
	}
	
	public void updateStateOnEDT( final TaxiState s )
	{
		final int[] taxi = s.taxi;
		final int passenger = s.passenger;
		final int[][] other_taxis = s.other_taxis;
		final int destination = s.destination;
		final boolean illegal = s.illegal_pickup_dropoff;
		final boolean goal = s.goal;
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				@Override
				public void run() {
					updateState( taxi, passenger, other_taxis, destination, illegal, goal );
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
	
	private class Updater implements EpisodeListener<TaxiState, TaxiAction>
	{
		private final int sleep_;
		
		public Updater( final int sleep )
		{
			sleep_ = sleep;
		}

		@Override
		public <P extends Policy<TaxiState, JointAction<TaxiAction>>>
		void startState( final TaxiState s, final double[] r, final P pi )
		{
			updateStateOnEDT( s );
		}

		@Override
		public void preGetAction()
		{ }

		@Override
		public void postGetAction( final JointAction<TaxiAction> action )
		{ }

		@Override
		public void onActionsTaken( final TaxiState sprime, final double[] r )
		{
			updateStateOnEDT( sprime );
			if( sleep_ > 0 ) {
				try { Thread.sleep( sleep_ ); } catch( final Exception e ) { }
			}
		}

		@Override
		public void endState( final TaxiState s )
		{ }
	}
	
	private class UserActionN extends AbstractAction
	{
		public UserActionN() { super( "^" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<TaxiAction>( new MoveAction( 0, 1 ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionE extends AbstractAction
	{
		public UserActionE() { super( ">" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<TaxiAction>( new MoveAction( 1, 0 ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionS extends AbstractAction
	{
		public UserActionS() { super( "v" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<TaxiAction>( new MoveAction( 0, -1 ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionW extends AbstractAction
	{
		public UserActionW() { super( "<" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<TaxiAction>( new MoveAction( -1, 0 ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionCoast extends AbstractAction
	{
		public UserActionCoast() { super( "C" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<TaxiAction>( new MoveAction( 0, 0 ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionPickup extends AbstractAction
	{
		public UserActionPickup() { super( "U" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<TaxiAction>( new PickupAction() ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionPutdown extends AbstractAction
	{
		public UserActionPutdown() { super( "D" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<TaxiAction>( new PutdownAction() ) );
			updateState( sim.state() );
		}
	}
	
	private class ControlPanel extends JPanel
	{
		private final JLabel x_ = new JLabel( "X: " );
		private final JLabel y_ = new JLabel( "Y: " );
		private final JLabel pass_ = new JLabel( "Pass: " );
		private final JLabel dest_ = new JLabel( "Destination: " );
		private final JLabel goal_ = new JLabel( "Goal? " );
//		private final JLabel crashed_ = new JLabel( "Crashed? " );
		
		public ControlPanel()
		{
			setLayout( new BorderLayout() );
			
			final JPanel controls = new JPanel();
			controls.setPreferredSize( new Dimension( 120, 120 ) );
			final LayoutManager layout = new GridLayout( 3, 3 );
			controls.setLayout( layout );
			controls.add( new JPanel() );
			controls.add( makeButton( new UserActionN() ) );
			controls.add( makeButton( new UserActionPickup() ) );
			controls.add( makeButton( new UserActionW() ) );
			controls.add( makeButton( new UserActionCoast() ) );
			controls.add( makeButton( new UserActionE() ) );
			controls.add( new JPanel() );
			controls.add( makeButton( new UserActionS() ) );
			controls.add( makeButton( new UserActionPutdown() ) );
			add( controls, BorderLayout.CENTER );
			
			final JPanel telemetry = new JPanel();
			telemetry.setLayout( new BoxLayout( telemetry, BoxLayout.Y_AXIS ) );
			telemetry.add( x_ );
			telemetry.add( y_ );
			telemetry.add( pass_ );
			telemetry.add( dest_ );
			telemetry.add( goal_ );
//			telemetry.add( laps_to_go_ );
			add( telemetry, BorderLayout.EAST );
		}
		
		public void updateState( final int[] taxi, final int passenger, final int[][] other_taxis,
							 final int destination,
							 final boolean illegal, final boolean goal )
		{
			x_.setText( "X: " + taxi[0] );
			y_.setText( "Y: " + taxi[1] );
			pass_.setText( "Pass: " + passenger );
			dest_.setText( "Destination: " + destination );
			goal_.setText( "Goal? " + goal );
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
		private final int[][] topology_;
		private final ArrayList<int[]> locations_;
		
		private int[] taxi = new int[] { 0, 0 };
		private int passenger = 0;
		private int[][] other_taxis = new int[0][0];
		private int destination = 0;
		private final boolean passenger_in_taxi = false;
		private boolean illegal = false;
		private boolean goal = false;
		
		public DrawPanel( final int[][] topology, final ArrayList<int[]> locations )
		{
			topology_ = topology;
			locations_ = locations;
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
			
			// Background
			g.setColor( Color.lightGray );
			g.fillRect( 0, 0, topology_.length, topology_[0].length );
			
			// Grid lines
			final BasicStroke thin = new BasicStroke( 0.05f );
			g.setColor( Color.darkGray );
			g.setStroke( thin );
			for( int x = 1; x < topology_.length; ++x ) {
				g.drawLine( x, 0, x, topology_[0].length );
			}
			for( int y = 1; y < topology_[0].length; ++y ) {
				g.drawLine( 0, y, topology_.length, y );
			}
			
			// Car
			g.setColor( Color.blue );
			g.fillRect( taxi[0], taxi[1], 1, 1 );
			
			// Other taxis
			g.setColor( Color.red );
			for( final int[] other : other_taxis ) {
				g.fillRect( other[0], other[1], 1, 1 );
			}
			
			// Walls
			g.setColor( Color.black );
			final BasicStroke thick = new BasicStroke( 0.2f );
			g.setStroke( thick );
			for( int i = 0; i < topology_.length; ++i ) {
				for( int j = 0; j < topology_[i].length; ++j ) {
					final int t = topology_[i][j];
					if( (t & TaxiState.wall_right) != 0 ) {
						g.drawLine( i + 1, j, i + 1, j + 1 );
					}
					if( (t & TaxiState.wall_up) != 0 ) {
						g.drawLine( i, j + 1, i + 1, j + 1 );
					}
				}
			}
			
			// Locations
			g.setFont( g.getFont().deriveFont( 1.0f ) );
			for( int i = 0; i < locations_.size(); ++i ) {
				final int[] loc = locations_.get( i );
				final AffineTransform t = g.getTransform();
				g.translate( loc[0], loc[1] );
				g.scale( 1, -1 );
				g.drawString( " " + i, 0, 0 );
				g.setTransform( t );
			}
			
			g.dispose();
		}
		
		public void updateState( final int[] taxi, final int passenger, final int[][] other_taxis,
							 final int destination,
							 final boolean illegal, final boolean goal )
		{
			this.taxi = taxi;
			this.passenger = passenger;
			this.other_taxis = other_taxis;
			this.destination = destination;
			this.illegal = illegal;
			this.goal = goal;
			repaint();
		}
	}
	
	
	public static void main( final String[] argv )
	{
		final RandomGenerator rng = new MersenneTwister( 42 );
		
		final int Nother_taxis = 1;
		final double slip = 0.1;
		final TaxiState state = TaxiWorlds.dietterich2000( rng, Nother_taxis, slip );
		final int T = 100;
		final TaxiSimulator sim = new TaxiSimulator( rng, state, slip, T );
		final int scale = 20;
		final TaxiVisualization vis = new TaxiVisualization( sim, state.topology, state.locations, scale );
	}
}
