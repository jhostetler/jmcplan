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

package edu.oregonstate.eecs.mcplan.domains.sailing;

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

public class SailingVisualization
{
	private static final Color TrackColor = Color.lightGray;
	private static final Color WallColor = Color.black;
	
	public final int scale;
	
	public SailingState state = null;
	public SailingFsssModel sim = null;
	private final double tstep_ = 0.2;
	
	private DrawPanel draw_panel_ = null;
	private ControlPanel control_panel_ = null;
	
	private JFrame frame = null;
	
	public SailingVisualization( final SailingFsssModel sim, final int scale )
	{
		if( sim != null ) {
			this.sim = sim;
			this.state = sim.initialState();
		}
		
		this.scale = scale;
		final SailingTerrain[][] circuit = state.terrain;
		
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				@Override
				public void run()
				{
					frame = new JFrame();
					final Container cp = frame.getContentPane();
					cp.setLayout( new BorderLayout() );
					
					draw_panel_ = new DrawPanel( circuit );
					final Dimension d = new Dimension( scale*circuit[0].length + 20, scale*circuit.length + 20 );
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
			updateStateOnEDT( state );
		}
	}
	
	public void init()
	{
		
	}
	
	public void updateState( final int x, final int y, final int w )
	{
		draw_panel_.updateState( x, y, w );
		control_panel_.updateState( x, y, w );
	}
	
	public void updateState( final SailingState s )
	{
		final int x = s.x;
		final int y = s.y;
		final int w = s.w;
		updateState( x, y, w );
		if( s.isTerminal() ) {
			System.out.println( "Terminal" );
		}
	}
	
	public void updateStateOnEDT( final SailingState s )
	{
//		System.out.println( "[Visualization]: " + s );
		
		final int x = s.x;
		final int y = s.y;
		final int w = s.w;
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				@Override
				public void run() {
					updateState( x, y, w );
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
	
	private class Updater implements EpisodeListener<SailingState, SailingAction>
	{
		private final int sleep_;
		
		public Updater( final int sleep )
		{
			sleep_ = sleep;
		}

		@Override
		public <P extends Policy<SailingState, JointAction<SailingAction>>>
		void startState( final SailingState s, final double[] r, final P pi )
		{
			updateStateOnEDT( s );
		}

		@Override
		public void preGetAction()
		{ }

		@Override
		public void postGetAction( final JointAction<SailingAction> action )
		{ }

		@Override
		public void onActionsTaken( final SailingState sprime, final double[] r )
		{
			updateStateOnEDT( sprime );
			if( sleep_ > 0 ) {
				try { Thread.sleep( sleep_ ); } catch( final Exception e ) { }
			}
		}

		@Override
		public void endState( final SailingState s )
		{
			frame.setVisible( false );
			frame.dispose();
		}
	}
	
	private void takeAction( final SailingAction a )
	{
		final double r = sim.reward( state, a );
		System.out.println( "reward = " + r );
		state = sim.sampleTransition( state, a );
		updateState( state );
	}
	
	private class UserActionN extends AbstractAction
	{
		public UserActionN() { super( "^" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			takeAction( new SailingAction( 2 ) );
		}
	}
	
	private class UserActionE extends AbstractAction
	{
		public UserActionE() { super( ">" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			takeAction( new SailingAction( 0 ) );
		}
	}
	
	private class UserActionS extends AbstractAction
	{
		public UserActionS() { super( "v" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			takeAction( new SailingAction( 6 ) );
		}
	}
	
	private class UserActionW extends AbstractAction
	{
		public UserActionW() { super( "<" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			takeAction( new SailingAction( 4 ) );
		}
	}
	
	private class UserActionNE extends AbstractAction
	{
		public UserActionNE() { super( "/" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			takeAction( new SailingAction( 1 ) );
		}
	}
	
	private class UserActionSE extends AbstractAction
	{
		public UserActionSE() { super( "\\" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			takeAction( new SailingAction( 7 ) );
		}
	}
	
	private class UserActionSW extends AbstractAction
	{
		public UserActionSW() { super( "/" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			takeAction( new SailingAction( 5 ) );
		}
	}
	
	private class UserActionNW extends AbstractAction
	{
		public UserActionNW() { super( "\\" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			takeAction( new SailingAction( 3 ) );
		}
	}
	
	private class ControlPanel extends JPanel
	{
		private final JLabel x_ = new JLabel( "X: " );
		private final JLabel y_ = new JLabel( "Y: " );
		private final JLabel w_ = new JLabel( "W: " );
//		private final JLabel goal_ = new JLabel( "Goal? " );
//		private final JLabel crashed_ = new JLabel( "Crashed? " );
		
		public ControlPanel()
		{
			final JPanel controls = new JPanel();
			controls.setPreferredSize( new Dimension( 120, 120 ) );
			final LayoutManager layout = new GridLayout( 3, 3 );
			controls.setLayout( layout );
			controls.add( makeButton( new UserActionNW() ) );
			controls.add( makeButton( new UserActionN() ) );
			controls.add( makeButton( new UserActionNE() ) );
			controls.add( makeButton( new UserActionW() ) );
			controls.add( new JPanel() );
			controls.add( makeButton( new UserActionE() ) );
			controls.add( makeButton( new UserActionSW() ) );
			controls.add( makeButton( new UserActionS() ) );
			controls.add( makeButton( new UserActionSE() ) );
			add( controls );
			
			final JPanel telemetry = new JPanel();
			telemetry.setLayout( new BoxLayout( telemetry, BoxLayout.Y_AXIS ) );
			telemetry.add( x_ );
			telemetry.add( y_ );
			telemetry.add( w_ );
			add( telemetry );
		}
		
		public void updateState( final int x, final int y, final int w )
		{
			x_.setText( "X: " + x );
			y_.setText( "Y: " + y );
			w_.setText( "W: " + w );
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
		private final SailingTerrain[][] terrain_;
		
		private int x = 0;
		private int y = 0;
		private int w = 0;
		
		public DrawPanel( final SailingTerrain[][] terrain )
		{
			terrain_ = terrain;
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
			
			for( int i = 0; i < terrain_.length; ++i ) {
				for( int j = 0; j < terrain_[i].length; ++j ) {
					final SailingTerrain t = terrain_[i][j];
					switch( t ) {
					case Land:
						g.setColor( Color.black );
						break;
					case Water:
						g.setColor( Color.cyan );
						break;
					}
					g.fillRect( j, i, 1, 1 );
				}
			}
			
			// Car
			g.setColor( Color.red );
			g.fillRect( x, y, 1, 1 );
			
			g.dispose();
		}
		
		private void updateState( final int x, final int y, final int w )
		{
			this.x = x;
			this.y = y;
			this.w = w;
			repaint();
		}
	}
	
	
	public static void main( final String[] argv )
	{
		final RandomGenerator rng = new MersenneTwister( 41 );
		final int V = 1;
		final int T = 30;
		final int dim = 10;
		final int scale = 1;
		final double slip = 0.1;
		final double p = 0.2;
		
//		final SailingState.Factory state = new SailingWorlds.EmptyRectangleFactory( V, T, 10, 10 );
////		final SailingState state = SailingWorlds.squareIsland( T, 15, 5 );
////		final SailingState.Factory state = new SailingWorlds.RandomObstaclesFactory( rng, p, T, 10 );
//		final SailingFsssModel model = new SailingFsssModel( rng, state );
//		final int viz_scale = 10;
//		final SailingVisualization vis = new SailingVisualization( model, viz_scale );
		
		final RandomGenerator world_rng = new MersenneTwister( 43 );
		final SailingState.Factory state_factory = new SailingWorlds.RandomObstaclesFactory( p, V, T, dim );
		final SailingFsssModel model = new SailingFsssModel( world_rng, state_factory );
		for( int i = 0; i < 2; ++i ) {
			SailingState s = model.initialState();
			System.out.println( "Initial state:" );
			System.out.println( s );
			final SailingFsssModel sim_model = (SailingFsssModel) model.create( rng );
			while( !s.isTerminal() ) {
				final SailingAction a = new SailingAction( rng.nextInt( SailingState.Nwind_directions ) );
				s = sim_model.sampleTransition( s, a );
				System.out.println( s );
			}
		}
		
	}
}
