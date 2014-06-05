/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racegrid;

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
public class RacegridVisualization
{
	private static final Color TrackColor = Color.lightGray;
	private static final Color WallColor = Color.black;
	
	public final int scale;
	
	public RacegridState state = null;
	public RacegridSimulator sim = null;
	private final double tstep_ = 0.2;
	
	private DrawPanel draw_panel_ = null;
	private ControlPanel control_panel_ = null;
	
	public RacegridVisualization( final RacegridSimulator sim, final TerrainType[][] circuit, final int scale )
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
			updateStateOnEDT( sim.state() );
		}
	}
	
	public void init()
	{
		
	}
	
	public void updateState( final int x, final int y, final int dx, final int dy )
	{
		draw_panel_.updateState( x, y, dx, dy );
		control_panel_.updateState( x, y, dx, dy );
	}
	
	public void updateState( final RacegridState s )
	{
		final int x = s.x;
		final int y = s.y;
		final int dx = s.dx;
		final int dy = s.dy;
		updateState( x, y, dx, dy );
		if( s.goal ) {
			System.out.println( "Goal!" );
		}
		else if( s.crashed ) {
			System.out.println( "Crashed!" );
		}
	}
	
	public void updateStateOnEDT( final RacegridState s )
	{
		final int x = s.x;
		final int y = s.y;
		final int dx = s.dx;
		final int dy = s.dy;
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				@Override
				public void run() {
					updateState( x, y, dx, dy );
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
	
	private class Updater implements EpisodeListener<RacegridState, RacegridAction>
	{
		private final int sleep_;
		
		public Updater( final int sleep )
		{
			sleep_ = sleep;
		}

		@Override
		public <P extends Policy<RacegridState, JointAction<RacegridAction>>>
		void startState( final RacegridState s, final double[] r, final P pi )
		{
			updateStateOnEDT( s );
		}

		@Override
		public void preGetAction()
		{ }

		@Override
		public void postGetAction( final JointAction<RacegridAction> action )
		{ }

		@Override
		public void onActionsTaken( final RacegridState sprime, final double[] r )
		{
			updateStateOnEDT( sprime );
			if( sleep_ > 0 ) {
				try { Thread.sleep( sleep_ ); } catch( final Exception e ) { }
			}
		}

		@Override
		public void endState( final RacegridState s )
		{ }
	}
	
	private class UserActionN extends AbstractAction
	{
		public UserActionN() { super( "^" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<RacegridAction>( new AccelerateAction( 0, 1 ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionE extends AbstractAction
	{
		public UserActionE() { super( ">" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<RacegridAction>( new AccelerateAction( 1, 0 ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionS extends AbstractAction
	{
		public UserActionS() { super( "v" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<RacegridAction>( new AccelerateAction( 0, -1 ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionW extends AbstractAction
	{
		public UserActionW() { super( "<" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<RacegridAction>( new AccelerateAction( -1, 0 ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionNE extends AbstractAction
	{
		public UserActionNE() { super( "/" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<RacegridAction>( new AccelerateAction( 1, 1 ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionSE extends AbstractAction
	{
		public UserActionSE() { super( "\\" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<RacegridAction>( new AccelerateAction( 1, -1 ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionSW extends AbstractAction
	{
		public UserActionSW() { super( "/" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<RacegridAction>( new AccelerateAction( -1, -1 ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionNW extends AbstractAction
	{
		public UserActionNW() { super( "\\" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<RacegridAction>( new AccelerateAction( -1, 1 ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionCoast extends AbstractAction
	{
		public UserActionCoast() { super( "C" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<RacegridAction>( new AccelerateAction( 0, 0 ) ) );
			updateState( sim.state() );
		}
	}
	
	private class ControlPanel extends JPanel
	{
		private final JLabel x_ = new JLabel( "X: " );
		private final JLabel y_ = new JLabel( "Y: " );
		private final JLabel dx_ = new JLabel( "dX: " );
		private final JLabel dy_ = new JLabel( "dY: " );
		private final JLabel speed_ = new JLabel( "Speed: " );
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
			controls.add( makeButton( new UserActionCoast() ) );
			controls.add( makeButton( new UserActionE() ) );
			controls.add( makeButton( new UserActionSW() ) );
			controls.add( makeButton( new UserActionS() ) );
			controls.add( makeButton( new UserActionSE() ) );
			add( controls );
			
			final JPanel telemetry = new JPanel();
			telemetry.setLayout( new BoxLayout( telemetry, BoxLayout.Y_AXIS ) );
			telemetry.add( x_ );
			telemetry.add( y_ );
			telemetry.add( dx_ );
			telemetry.add( dy_ );
			telemetry.add( speed_ );
//			telemetry.add( sector_ );
//			telemetry.add( laps_to_go_ );
			add( telemetry );
		}
		
		public void updateState( final int x, final int y, final int dx, final int dy )
		{
			x_.setText( "X: " + x );
			y_.setText( "Y: " + y );
			dx_.setText( "dX: " + dx );
			dy_.setText( "dY: " + dy );
			final double speed = Math.sqrt( dx*dx + dy*dy );
			speed_.setText( "Speed: " + speed );
//			sector_.setText( "Sector: " + sector );
//			laps_to_go_.setText( "Laps to go: " + laps_to_go );
			
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
		private final TerrainType[][] terrain_;
		
		private int car_x = 0;
		private int car_y = 0;
		private int car_dx = 0;
		private int car_dy = 0;
		
		public DrawPanel( final TerrainType[][] terrain )
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
					final TerrainType t = terrain_[i][j];
					switch( t ) {
					case Wall:
						g.setColor( Color.black );
						break;
					case Track:
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
			g.fillRect( car_x, car_y, 1, 1 );
			
			g.dispose();
		}
		
		private void updateState( final int car_x, final int car_y,
								  final int car_dx, final int car_dy )
		{
			this.car_x = car_x;
			this.car_y = car_y;
			this.car_dx = car_dx;
			this.car_dy = car_dy;
			repaint();
		}
	}
	
	
	public static void main( final String[] argv )
	{
		final RandomGenerator rng = new MersenneTwister( 42 );
		
		final TerrainType[][] circuit = RacegridCircuits.barto_bradke_singh_SmallTrack();
		final RacegridState state = new RacegridState( circuit );
		final double slip = 0.0;
		final RacegridSimulator sim = new RacegridSimulator( rng, state, slip );
		final int scale = 20;
		final RacegridVisualization vis = new RacegridVisualization( sim, circuit, scale );
	}
}
