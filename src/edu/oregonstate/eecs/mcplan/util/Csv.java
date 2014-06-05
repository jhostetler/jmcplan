/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
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
	
	public static String encode( final int[] a )
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "[" );
		for( int i = 0; i < a.length; ++i ) {
			if( i > 0 ) {
				sb.append( ";" );
			}
			sb.append( a[i] );
		}
		sb.append( "]" );
		return sb.toString();
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
	
	public static <K, V> void write( final PrintStream out, final Map<K, V> m )
	{
		final Writer writer = new Writer( out );
		writer.cell( "key" ).cell( "value" ).newline();
		for( final Map.Entry<K, V> e : m.entrySet() ) {
			writer.cell( e.getKey() ).cell( e.getValue() ).newline();
		}
	}
	
	public static Map<String, String> readKeyValue( final File f, final boolean headers )
	{
		try {
			final BufferedReader r = new BufferedReader( new FileReader( f ) );
			String line;
			final ArrayList<String[]> rows = new ArrayList<String[]>();
			while( true ) {
				line = r.readLine();
				if( line == null ) {
					break;
				}
				rows.add( line.split( "," ) );
			}
			
			final Map<String, String> m = new HashMap<String, String>();
			for( int i = (headers ? 1 : 0); i < rows.size(); ++i ) {
				final String[] row = rows.get( i );
				m.put( row[0], row[1] );
			}
			
			return m;
		}
		catch( final Exception ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	public static RealMatrix readMatrix( final File f )
	{
		try {
			final BufferedReader r = new BufferedReader( new FileReader( f ) );
			String line;
			final ArrayList<String[]> rows = new ArrayList<String[]>();
			while( true ) {
				line = r.readLine();
				if( line == null ) {
					break;
				}
				rows.add( line.split( "," ) );
			}
			final Array2DRowRealMatrix M = new Array2DRowRealMatrix( rows.size(), rows.get( 0 ).length );
			for( int i = 0; i < rows.size(); ++i ) {
				final String[] row = rows.get( i );
				for( int j = 0; j < row.length; ++j ) {
					M.setEntry( i, j, Double.parseDouble( row[j] ) );
				}
			}
			
			return M;
		}
		catch( final Exception ex ) {
			throw new RuntimeException( ex );
		}
	}
	
	public static RealVector readVector( final File f )
	{
		try {
			final BufferedReader r = new BufferedReader( new FileReader( f ) );
			final String line = r.readLine();
			final String[] entries = line.split( "," );
			final ArrayRealVector v = new ArrayRealVector( entries.length );
			for( int i = 0; i < entries.length; ++i ) {
				v.setEntry( i, Double.parseDouble( entries[i] ) );
			}
			
			return v;
		}
		catch( final Exception ex ) {
			throw new RuntimeException( ex );
		}
	}
}
