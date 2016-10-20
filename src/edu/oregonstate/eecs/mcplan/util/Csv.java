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
package edu.oregonstate.eecs.mcplan.util;

import gnu.trove.list.array.TDoubleArrayList;

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
	public static class Writer implements AutoCloseable
	{
		public final PrintStream out;
		private boolean comma_ = false;
		
		public Writer( final PrintStream out )
		{
			this.out = out;
		}
		
		@Override
		public void close()
		{
			out.close();
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
	
	public static void write( final PrintStream out, final double[] v )
	{
		final Writer writer = new Writer( out );
		for( int i = 0; i < v.length; ++i ) {
			writer.cell( v[i] );
		}
		writer.newline();
	}
	
	public static void write( final PrintStream out, final TDoubleArrayList v )
	{
		final Writer writer = new Writer( out );
		for( int i = 0; i < v.size(); ++i ) {
			writer.cell( v.get( i ) );
		}
		writer.newline();
	}
	
	public static void write( final PrintStream out, final RealVector v )
	{
		final Writer writer = new Writer( out );
		for( int i = 0; i < v.getDimension(); ++i ) {
			writer.cell( v.getEntry( i ) );
		}
		writer.newline();
	}
	
	public static void write( final PrintStream out, final Iterable<RealVector> ts )
	{
		for( final RealVector t : ts ) {
			write( out, t );
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
			String line;
			final TDoubleArrayList values = new TDoubleArrayList();
			while( (line = r.readLine()) != null ) {
				values.add( Double.parseDouble( line ) );
			}
			r.close();
			return new ArrayRealVector( values.toArray() );
		}
		catch( final Exception ex ) {
			throw new RuntimeException( ex );
		}
	}

	
}
