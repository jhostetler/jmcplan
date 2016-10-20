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
package edu.oregonstate.eecs.mcplan.domains.fuelworld;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author jhostetler
 *
 */
public class FuelWorldConsole
{
	private static class OrbitGame extends JFrame
	{
		private static JPanel ThePanel = new JPanel() {
			@Override
			public void paint( final Graphics graphics )
			{
				final Graphics2D g = (Graphics2D) graphics.create();
				
				g.translate( getWidth() / 2, getHeight() / 2 );
				
				final double scale = 6;
				g.scale( scale, scale );
				g.setStroke( new BasicStroke( 0.5f ) );
				
				final int Naltitudes = 5;
				int Nsectors = 6;
				final int[] subdivisions = new int[Naltitudes];
				for( int i = Naltitudes - 1; i >= 0; --i ) {
					subdivisions[i] = Nsectors;
					Nsectors *= 2;
				}
//				final int[] subdivisions = new int[] { 30, 24, 18, 12, 6 };
				final Color[] colors = new Color[] { Color.darkGray, Color.blue.darker().darker().darker(),
													 Color.blue.darker().darker(), Color.blue, Color.cyan.darker() };
//				final int[] radii = new int[] { 12, 8, 6, 4, 2 };
				final int[] radii = new int[] { 80, 57, 40, 28, 20 };
				for( int i = 0; i < subdivisions.length; ++i ) {
					drawSubdividedCircle( g, subdivisions.length - i - 1, radii[i], subdivisions[i], colors[i] );
					if( i < subdivisions.length - 1 ) {
						// Rotate half the width of a sector at the current level,
						// so that the centers line up.
						g.rotate( Math.PI / subdivisions[i] );
//						g.rotate( ((double) subdivisions[i + 1]) / subdivisions[i] * 2*Math.PI );
					}
				}
				
				g.setColor( Color.black );
				final int planet_radius = (int) (radii[radii.length - 1] / Math.sqrt( 2 ));
				g.fillOval( -planet_radius, -planet_radius, 2*planet_radius, 2*planet_radius );
			}
			
			private void drawSubdividedCircle( final Graphics2D graphics, final int level,
											   final int r, final int n, final Color c )
			{
				Graphics2D g = (Graphics2D) graphics.create();
				
				g.setColor( c );
				g.fillArc( -r, -r, 2*r, 2*r, 0, 360 );
				
				final double theta = 360.0 / n;
				double error = 0.0;
				
				if( level > 0 ) {
					
					for( int i = 0; i < n; ++i ) {
						final double round_theta = Math.round( theta + error );
//						if( (i - ((int) Math.pow( 2, level - 1))) % ((int) Math.pow( 2, level )) == 0 ) {
						if( i % ((int) Math.pow( 2, level )) == 0 ) {
							g.setColor( Color.lightGray );
							g.fillArc( -r, -r, 2*r, 2*r, 0, (int) round_theta );
						}
						if( (i - ((int) Math.pow( 2, level - 1))) % ((int) Math.pow( 2, level )) == 0 ) {
							g.setColor( Color.blue );
							g.fillArc( -r, -r, 2*r, 2*r, 0, (int) round_theta );
						}
						error = theta - round_theta;
						g.rotate( theta / 360.0 * 2*Math.PI );
					}
					
					g.dispose();
					g = (Graphics2D) graphics.create();
					error = 0.0;
				}
				
				for( int i = 0; i < n; ++i ) {
					final double round_theta = Math.round( theta + error );
//					if( i % 2 == 0 ) {
//						g.setColor( c );
//					}
//					else {
//						g.setColor( Color.white );
//					}
//					g.fillArc( -r, -r, 2*r, 2*r, 0, (int) round_theta );

					g.setColor( Color.black );
					g.drawLine( 0, 0, r, 0 );
					
					error = theta - round_theta;
					g.rotate( theta / 360.0 * 2*Math.PI );
				}
				
				g.setColor( Color.black );
				g.drawArc( -r, -r, 2*r, 2*r, 0, 360 );
				
				g.dispose();
			}
			
		};
		
		public OrbitGame()
		{
			setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
			setSize( new Dimension( 720, 720 ) );
			setContentPane( ThePanel );
		}
	}
	
	/**
	 * @param args
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public static void main( final String[] args ) throws NumberFormatException, IOException
	{
//		final int seed = 42;
//		final RandomGenerator rng = new MersenneTwister( seed );
//		final FuelWorldState s = FuelWorldState.createDefaultWithChoices( rng );
//		final FuelWorldSimulator sim = new FuelWorldSimulator( s );
//		final FuelWorldActionGenerator actions = new FuelWorldActionGenerator();
//
//		while( !s.isTerminal() ) {
//			System.out.println( s );
//			actions.setState( sim.state(), sim.t(), sim.turn() );
//			final ArrayList<FuelWorldAction> action_list = Fn.takeAll( actions );
//			for( int i = 0; i < action_list.size(); ++i ) {
//				System.out.println( "" + i + ": " + action_list.get( i ) );
//			}
//			final BufferedReader cin = new BufferedReader( new InputStreamReader( System.in ) );
//			final int choice = Integer.parseInt( cin.readLine() );
//			final FuelWorldAction a = action_list.get( choice );
//			sim.takeAction( new JointAction<FuelWorldAction>( a ) );
//			System.out.println( "Reward: " + Arrays.toString( sim.reward() ) );
//		}
		
		final OrbitGame game = new OrbitGame();
		game.setVisible( true );
	}

}
