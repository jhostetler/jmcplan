/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.racetrack;

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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.sim.EpisodeListener;

/**
 * @author jhostetler
 *
 */
public class RacetrackVisualization
{
	private static final Color OffTrackColor = Color.green;
	private static final Color TrackColor = Color.lightGray;
	private static final Color WallColor = Color.black;
	
	public final Circuit circuit;
	public final double scale;
	
	public RacetrackState state = null;
	public RacetrackSimulator sim = null;
	private final double tstep_ = 0.2;
	
	private DrawPanel draw_panel_ = null;
	private ControlPanel control_panel_ = null;
	
	public RacetrackVisualization( final Circuit circuit, final RacetrackSimulator sim, final double scale )
	{
		if( sim != null ) {
			this.sim = sim;
			this.state = sim.state();
		}
		
		this.circuit = circuit;
		this.scale = scale;
		
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				@Override
				public void run()
				{
					final JFrame frame = new JFrame();
					final Container cp = frame.getContentPane();
					cp.setLayout( new BorderLayout() );
					
					draw_panel_ = new DrawPanel();
					final Dimension d = new Dimension( (int) scale*circuit.width + 20, (int) scale*circuit.height + 20 );
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
	}
	
	public void init()
	{
		
	}
	
	public void updateState( final double x, final double y, final double dx, final double dy,
							 final double theta, final int sector, final int laps_to_go )
	{
		draw_panel_.updateState( x, y, dx, dy, theta );
		control_panel_.updateState( x, y, dx, dy, theta, sector, laps_to_go );
	}
	
	public void updateState( final RacetrackState s )
	{
		final double x = s.car_x;
		final double y = s.car_y;
		final double dx = s.car_dx;
		final double dy = s.car_dy;
		final double theta = s.car_theta;
		final int sector = s.sector;
		final int laps_to_go = s.laps_to_go;
		updateState( x, y, dx, dy, theta, sector, laps_to_go );
	}
	
	public void updateStateOnEDT( final RacetrackState s )
	{
		final double x = s.car_x;
		final double y = s.car_y;
		final double dx = s.car_dx;
		final double dy = s.car_dy;
		final double theta = s.car_theta;
		final int sector = s.sector;
		final int laps_to_go = s.laps_to_go;
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				@Override
				public void run() {
					updateState( x, y, dx, dy, theta, sector, laps_to_go );
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
	
	private class Updater implements EpisodeListener<RacetrackState, RacetrackAction>
	{
		private final int sleep_;
		
		public Updater( final int sleep )
		{
			sleep_ = sleep;
		}

		@Override
		public <P extends Policy<RacetrackState, JointAction<RacetrackAction>>>
		void startState( final RacetrackState s, final P pi )
		{
			updateStateOnEDT( s );
		}

		@Override
		public void preGetAction()
		{ }

		@Override
		public void postGetAction( final JointAction<RacetrackAction> action )
		{ }

		@Override
		public void onActionsTaken( final RacetrackState sprime )
		{
			updateStateOnEDT( sprime );
			if( sleep_ > 0 ) {
				try { Thread.sleep( sleep_ ); } catch( final Exception e ) { }
			}
		}

		@Override
		public void endState( final RacetrackState s )
		{ }
	}
	
	private class UserActionN extends AbstractAction
	{
		public UserActionN() { super( "N" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			final double theta = state.car_theta;
			sim.takeAction( new JointAction<RacetrackAction>(
				new AccelerateAction( state.adhesion_limit*Math.cos( theta ), state.adhesion_limit*Math.sin( theta ) ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionE extends AbstractAction
	{
		public UserActionE() { super( "E" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			final double theta = state.car_theta - (Math.PI / 2);
			sim.takeAction( new JointAction<RacetrackAction>(
				new AccelerateAction( state.adhesion_limit*Math.cos( theta ), state.adhesion_limit*Math.sin( theta ) ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionS extends AbstractAction
	{
		public UserActionS() { super( "S" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			final double theta = state.car_theta + (Math.PI);
			sim.takeAction( new JointAction<RacetrackAction>(
				new AccelerateAction( state.adhesion_limit*Math.cos( theta ), state.adhesion_limit*Math.sin( theta ) ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionW extends AbstractAction
	{
		public UserActionW() { super( "W" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			final double theta = state.car_theta + (Math.PI / 2);
			sim.takeAction( new JointAction<RacetrackAction>(
				new AccelerateAction( state.adhesion_limit*Math.cos( theta ), state.adhesion_limit*Math.sin( theta ) ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionNE extends AbstractAction
	{
		public UserActionNE() { super( "NE" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			final double theta = state.car_theta - (Math.PI / 4);
			sim.takeAction( new JointAction<RacetrackAction>(
				new AccelerateAction( state.adhesion_limit*Math.cos( theta ), state.adhesion_limit*Math.sin( theta ) ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionSE extends AbstractAction
	{
		public UserActionSE() { super( "SE" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			final double theta = state.car_theta - (3 * Math.PI / 4);
			sim.takeAction( new JointAction<RacetrackAction>(
				new AccelerateAction( state.adhesion_limit*Math.cos( theta ), state.adhesion_limit*Math.sin( theta ) ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionSW extends AbstractAction
	{
		public UserActionSW() { super( "SW" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			final double theta = state.car_theta + (3 * Math.PI / 4);
			sim.takeAction( new JointAction<RacetrackAction>(
				new AccelerateAction( state.adhesion_limit*Math.cos( theta ), state.adhesion_limit*Math.sin( theta ) ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionNW extends AbstractAction
	{
		public UserActionNW() { super( "NW" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			final double theta = state.car_theta + (Math.PI / 4);
			sim.takeAction( new JointAction<RacetrackAction>(
				new AccelerateAction( state.adhesion_limit*Math.cos( theta ), state.adhesion_limit*Math.sin( theta ) ) ) );
			updateState( sim.state() );
		}
	}
	
	private class UserActionCoast extends AbstractAction
	{
		public UserActionCoast() { super( "C" ); }
		
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			sim.takeAction( new JointAction<RacetrackAction>( new AccelerateAction( 0, 0 ) ) );
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
		private final JLabel sector_ = new JLabel( "Sector: " );
		private final JLabel laps_to_go_ = new JLabel( "Laps to go: " );
		
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
			telemetry.add( sector_ );
			telemetry.add( laps_to_go_ );
			add( telemetry );
		}
		
		public void updateState( final double x, final double y, final double dx, final double dy, final double theta,
								 final int sector, final int laps_to_go )
		{
			x_.setText( "X: " + x );
			y_.setText( "Y: " + y );
			dx_.setText( "dX: " + dx );
			dy_.setText( "dY: " + dy );
			final double speed = Math.sqrt( dx*dx + dy*dy );
			speed_.setText( "Speed: " + speed );
			sector_.setText( "Sector: " + sector );
			laps_to_go_.setText( "Laps to go: " + laps_to_go );
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
		private double car_x = 0;
		private double car_y = 0;
		private double car_dx = 0;
		private double car_dy = 0;
		private double car_theta = 0;
		
		@Override
		public void paintComponent( final Graphics graphics )
		{
			final Graphics2D g = (Graphics2D) graphics.create();
			g.setColor( OffTrackColor );
			g.translate( 0, this.getHeight() );
			g.scale( 1, -1 );
			g.fillRect( 0, 0, this.getWidth(), this.getHeight() );
			
			g.translate( 10, 10 );
			g.scale( scale, scale );
			Geometry geom = circuit.track.getExteriorRing();
			g.setColor( TrackColor );
			drawJtsPolygon( geom, g, true );
	
			g.setColor( OffTrackColor );
			for( int i = 0; i < circuit.track.getNumInteriorRing(); ++i ) {
				geom = circuit.track.getInteriorRingN( i );
				drawJtsPolygon( geom, g, true );
			}
	
			g.setColor( WallColor );
//			g.setStroke( new BasicStroke( 3 ) );
			drawJtsLineString( circuit.wall.getExteriorRing(), g );
			for( int i = 0; i < circuit.wall.getNumInteriorRing(); ++i ) {
				drawJtsLineString( circuit.wall.getInteriorRingN( i ), g );
			}
			
			g.setColor( Color.blue );
			g.drawLine( (int) car_x, (int) car_y, (int) (car_x + car_dx * tstep_), (int) (car_y + car_dy * tstep_) );
			
			for( int i = 0; i < circuit.sectors.size(); ++i ) {
				final Polygon sector = circuit.sectors.get( i );
				drawJtsPolygon( sector, g, false );
			}
			
			g.translate( car_x, car_y );
			g.rotate( car_theta );
			g.setColor( Color.red );
			g.fillRect( -2, -1, 4, 2 );
			
			g.dispose();
		}
		
		private void updateState( final double car_x, final double car_y,
								  final double car_dx, final double car_dy,
								  final double car_theta )
		{
			this.car_x = car_x;
			this.car_y = car_y;
			this.car_dx = car_dx;
			this.car_dy = car_dy;
			this.car_theta = car_theta;
			repaint();
		}
	}
	
	private void drawJtsLineString( final Geometry geom, final Graphics2D g )
	{
		final Coordinate[] coords = geom.getCoordinates();
		for( int j = 1; j < coords.length; ++j ) {
			g.drawLine( (int) coords[j - 1].x, (int) coords[j - 1].y, (int) coords[j].x, (int) coords[j].y );
		}
	}
	
	private void drawJtsPolygon( final Geometry geom, final Graphics2D g, final boolean fill )
	{
		final Coordinate[] coords = geom.getCoordinates();
		final int[] poly_x = new int[coords.length];
		final int[] poly_y = new int[coords.length];
		for( int j = 0; j < coords.length; ++j ) {
			poly_x[j] = (int) coords[j].x;
			poly_y[j] = (int) coords[j].y;
		}
		if( fill ) {
			g.fillPolygon( poly_x, poly_y, coords.length );
		}
		else {
			g.drawPolygon( poly_x, poly_y, coords.length );
		}
	}
	
	public static void main( final String[] argv )
	{
//		final GeometryFactory gfact = new GeometryFactory();
//		final LinearRing ring = gfact.createLinearRing( new Coordinate[] {
//			new Coordinate( 0, 0 ),
//			new Coordinate( 0, 10 ),
//			new Coordinate( 10, 10 ),
//			new Coordinate( 10, 0 ),
//			new Coordinate( 0, 0 )
//		} );
//		final LinearRing hole = gfact.createLinearRing( new Coordinate[] {
//			new Coordinate( 8, 0 ),
//			new Coordinate( 8, 10 ),
//			new Coordinate( 8, 10 ),
//			new Coordinate( 8, 0 ),
//			new Coordinate( 8, 0 )
//		} );
//		final Polygon poly = gfact.createPolygon( ring, new LinearRing[] { hole } );
//		final LineString line = gfact.createLineString( new Coordinate[] {
//			new Coordinate( 5, 5 ), new Coordinate( 15, 5 )
//		} );
//		System.out.println( line.intersection( poly.getBoundary() ) );
		
		final RandomGenerator rng = new MersenneTwister( 42 );
//		final Circuit circuit = Circuits.DragStrip( 2000, 100 );
//		final Circuit circuit = Circuits.Donut( 50, 70, 16 );
		final Circuit circuit = Circuits.PaperClip( 500, 200 );
		final RacetrackState state = new RacetrackState( circuit );
		final double control_noise = 0.1;
		final RacetrackSimulator sim = new RacetrackSimulator( rng, state, control_noise );
		final double scale = 2;
		final RacetrackVisualization vis = new RacetrackVisualization( circuit, sim, scale );
	}
}
