/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import java.io.PrintStream;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * @author jhostetler
 *
 */
public class Csv
{
	public static class Writer
	{
		public final PrintStream out;
		private boolean comma_ = false;
		
		public Writer( final PrintStream out )
		{
			this.out = out;
		}
		
		public Writer cell( final Object o )
		{
			if( comma_ ) {
				out.print( "," );
			}
			else {
				comma_ = true;
			}
			
			out.print( o );
			return this;
		}
		
		public Writer cell( final String s )
		{
			if( comma_ ) {
				out.print( "," );
			}
			else {
				comma_ = true;
			}
			
			out.print( s );
			return this;
		}
		
		public Writer cell( final double d )
		{
			if( comma_ ) {
				out.print( "," );
			}
			else {
				comma_ = true;
			}
			
			out.print( d );
			return this;
		}
		
		public Writer cell( final int i )
		{
			if( comma_ ) {
				out.print( "," );
			}
			else {
				comma_ = true;
			}
			
			out.print( i );
			return this;
		}
		
		public Writer row( final RealVector v )
		{
			for( int i = 0; i < v.getDimension(); ++i ) {
				if( comma_ ) {
					out.print( "," );
				}
				comma_ = true;
				out.print( v.getEntry( i ) );
			}
			return newline();
		}
		
		public Writer newline()
		{
			out.println();
			comma_ = false;
			return this;
		}
	}
	
	public static void write( final PrintStream out, final RealMatrix m )
	{
		final Writer writer = new Writer( out );
		for( int i = 0; i < m.getRowDimension(); ++i ) {
			for( int j = 0; j < m.getColumnDimension(); ++j ) {
				writer.cell( m.getEntry( i, j ) );
			}
			writer.newline();
		}
	}
}
