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
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import ch.qos.logback.classic.Logger;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class FsssTest
{
	public static void printDecisionTree( final DataNode<?, ?> dn, final PrintStream out, final int ws )
	{
		for( int i = 0; i < ws; ++i ) {
			out.print( "->" );
		}
		out.println( dn );
		if( dn.split != null ) {
//			out.println( dn.split );
			for( final DataNode<?, ?> succ : Fn.in( dn.split.children() ) ) {
				printDecisionTree( succ, out, ws + 1 );
			}
		}
	}
	
	public static void printTree( final FsssStateNode<?, ?> sn, final PrintStream out, final int ws )
	{
		for( int i = 0; i < ws; ++i ) {
			out.print( "-+" );
		}
		out.println( sn );
		for( final FsssActionNode<?, ?> an : sn.successors() ) {
			printTree( an, out, ws + 1 );
		}
	}
	
	public static void printTree( final FsssActionNode<?, ?> an, final PrintStream out, final int ws )
	{
		for( int i = 0; i < ws; ++i ) {
			out.print( "-+" );
		}
		out.println( an );
		for( final FsssStateNode<?, ?> sn : an.successors() ) {
			printTree( sn, out, ws + 1 );
		}
	}
	
	public static void printTree( final FsssAbstractStateNode<?, ?> sn, final PrintStream out, final int ws )
	{
		for( int i = 0; i < ws; ++i ) {
			out.print( "-+" );
		}
		out.println( sn );
		if( sn != null ) {
			for( final FsssAbstractActionNode<?, ?> an : sn.successors() ) {
				printTree( an, out, ws + 1 );
			}
		}
	}
	
	public static void printTree( final FsssAbstractActionNode<?, ?> an, final PrintStream out, final int ws )
	{
		for( int i = 0; i < ws; ++i ) {
			out.print( "-+" );
		}
		out.println( an );
		
		if( an != null ) {
		
			for( final DataNode<?, ?> dn : an.repr.dt_roots.values() ) {
				printDecisionTree( dn, out, ws + 1 );
			}
			
			for( final FsssAbstractStateNode<?, ?> sn : an.successors() ) {
				printTree( sn, out, ws + 1 );
			}
		}
	}
	
	public static <S extends State, A extends VirtualConstructor<A>>
	FsssAbstractStateNode<S, A> findRoot( final FsssAbstractStateNode<S, A> asn )
	{
		if( asn.predecessor == null ) {
			return asn;
		}
		else {
			return findRoot( asn.predecessor );
		}
	}
	
	public static <S extends State, A extends VirtualConstructor<A>>
	FsssAbstractStateNode<S, A> findRoot( final FsssAbstractActionNode<S, A> aan )
	{
		return findRoot( aan.predecessor );
	}
	
	public static void printAncestorChain( final FsssAbstractStateNode<?, ?> asn )
	{
		final ArrayList<String> strings = new ArrayList<String>();
		strings.add( asn.toString() );
		FsssAbstractActionNode<?, ?> aan_pred = asn.predecessor;
		while( aan_pred != null ) {
			strings.add( aan_pred.toString() );
			final FsssAbstractStateNode<?, ?> asn_pred = aan_pred.predecessor;
			strings.add( asn_pred.toString() );
			aan_pred = asn_pred.predecessor;
		}
		
		for( int i = 0; i < strings.size(); ++i ) {
			for( int indent = 0; indent < i; ++indent ) {
				System.out.print( "=>" );
			}
			System.out.println( strings.get( strings.size() - 1 - i ) );
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static void printDecisionTree( final DataNode<?, ?> dn, final Logger log, final int ws )
	{
		if( log.isDebugEnabled() ) {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps;
			try {
				ps = new PrintStream( baos, true, "utf-8" );
			}
			catch( final UnsupportedEncodingException ex ) {
				throw new RuntimeException( ex );
			}
			ps.println();
			printDecisionTree( dn, ps, ws );
			log.debug( baos.toString() );
		}
	}
	
	public static void printTree( final FsssAbstractStateNode<?, ?> sn, final Logger log, final int ws )
	{
		if( log.isDebugEnabled() ) {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps;
			try {
				ps = new PrintStream( baos, true, "utf-8" );
			}
			catch( final UnsupportedEncodingException ex ) {
				throw new RuntimeException( ex );
			}
			ps.println();
			printTree( sn, ps, ws );
			log.debug( baos.toString() );
		}
	}
	
	// -----------------------------------------------------------------------
	
	public static <S extends State, A extends VirtualConstructor<A>>
	ArrayList<String> validateDecisionTree( final DataNode<S, A> root )
	{
		final ArrayList<String> errors = new ArrayList<String>();
		
		final Set<DataNode<S, A>> dns = new HashSet<DataNode<S, A>>();
		
		final Deque<DataNode<S, A>> stack = new ArrayDeque<DataNode<S, A>>();
		stack.push( root );
		while( !stack.isEmpty() ) {
			final DataNode<S, A> dn = stack.pop();
			if( !dns.add( dn ) ) {
				errors.add( "Multiple paths to " + dn );
			}
			if( dn.split != null ) {
				if( dn.aggregate != null ) {
					errors.add( "Non-null aggregate in split " + dn );
				}
				for( final DataNode<S, A> succ : Fn.in( dn.split.children() ) ) {
					stack.push( succ );
				}
			}
			else if( dn.aggregate == null ) {
				errors.add( "Null aggregate in leaf " + dn );
			}
		}
		
		return errors;
	}
	
	public static <S extends State, A extends VirtualConstructor<A>>
	ArrayList<String> validateAllDecisionTrees( final FsssAbstractStateNode<S, A> asn )
	{
		final ArrayList<String> errors = new ArrayList<String>();
		
		for( final FsssAbstractActionNode<S, A> aan : asn.successors() ) {
			for( final DataNode<S, A> dn : aan.repr.dt_roots.values() ) {
				final ArrayList<String> aan_errors = validateDecisionTree( dn );
				errors.addAll( aan_errors );
				for( final FsssAbstractStateNode<S, A> asn_succ : aan.successors() ) {
					final ArrayList<String> succ_errors = validateAllDecisionTrees( asn_succ );
					errors.addAll( succ_errors );
				}
			}
		}
		
		return errors;
	}
	
	// -----------------------------------------------------------------------
	
	/**
	 * Performs consistency checks on an AFSSS tree.
	 * <p>
	 * The most common reason for failing these checks is incorrect
	 * implementation of Vmin/Vmax in the FsssModel.
	 * @param params
	 * @param root
	 * @param model
	 * @return List of errors
	 */
	public static <S extends State, A extends VirtualConstructor<A>>
	ArrayList<String> validateTree( final FsssParameters params,
									final FsssAbstractStateNode<S, A> root,
									final FsssModel<S, A> model )
	{
		final ArrayList<String> errors = new ArrayList<String>();
		
		final Deque<FsssAbstractStateNode<S, A>> state_stack = new ArrayDeque<FsssAbstractStateNode<S, A>>();
		state_stack.push( root );
		
		while( !state_stack.isEmpty() ) {
			final FsssAbstractStateNode<S, A> asn = state_stack.pop();
			{
				if( !asn.isClosed() ) {
					// These checks compare the ASN value to the average
					// over GSN values. They don't work on closed nodes.
					double model_R = 0;
					double model_U = 0;
					double model_L = 0;
					double model_H = 0;
					for( final FsssStateNode<S, A> gsn : asn.states() ) {
						model_R += model.reward( gsn.s() );
						model_U += model.Vmax( gsn.s() );
						model_L += model.Vmin( gsn.s() );
						model_H += model.heuristic( gsn.s() );
					}
					model_R /= asn.n();
	//				check_U /= asn.n();
	//				check_L /= asn.n();
					model_U /= asn.n();
					model_L /= asn.n();
					model_H /= asn.n();
					
					double check_U = -Double.MAX_VALUE;
					double check_L = -Double.MAX_VALUE;
					if( asn.nsuccessors() > 0 ) {
						for( final FsssAbstractActionNode<S, A> aan : asn.successors() ) {
							check_U = Math.max( check_U, aan.U() );
							check_L = Math.max( check_L, aan.L() );
						}
					}
					else if( asn.isExpanded() && asn.isTerminal() ) {
						check_U = check_L = model_H;
					}
					else {
						check_U = model_U;
						check_L = model_L;
					}
					check_U += model_R;
					check_L += model_R;
					
					if( !Fn.approxEq( 1e-6, asn.R(), model_R ) ) {
						errors.add( "@" + Integer.toHexString( asn.hashCode() )
									+ ": asn.R() [" + asn.R() + "] != model_R [" + model_R + "]" );
					}
					
					if( asn.nvisits() > 0 ) {
						// These give spurious errors in unvisited states due to
						// immediate reward, which is counted by 'check_X', but
						// which is not applied to U/L until backup() is executed.
						if( !Fn.approxEq( 1e-6, asn.U(), check_U ) ) {
							errors.add( "@" + Integer.toHexString( asn.hashCode() )
										+ ": asn.U() [" + asn.U() + "] != check_U [" + check_U + "]" );
						}
						if( !Fn.approxEq( 1e-6, asn.L(), check_L ) ) {
							errors.add( "@" + Integer.toHexString( asn.hashCode() )
										+ ": asn.L() [" + asn.L() + "] != check_L [" + check_L + "]" );
						}
					}
				}
				
				// U() > L()
				if( asn.U() - asn.L() < 0 ) {
					errors.add( "@" + Integer.toHexString( asn.hashCode() )
								+ ": asn.U() [" + asn.U() + "] - asn.L() [" + asn.L() + "] < 0" );
				}

				// Unvisited nodes should have no successors
				if( asn.nvisits() == 0 ) {
					if( asn.nsuccessors() > 0 ) {
						errors.add( "@" + Integer.toHexString( asn.hashCode() )
									+ ": asn.nvisits() == 0 and asn.nsuccessors() [" + asn.nsuccessors() + "] > 0" );
					}
				}
			
			}
			
			for( final FsssAbstractActionNode<S, A> aan : asn.successors() ) {
				final A a = aan.a();
				
				if( !asn.isClosed() ) {
					double model_R = 0;
					for( final FsssStateNode<S, A> gsn : asn.states() ) {
						model_R += model.reward( gsn.s(), a );
					}
					model_R /= asn.n();
					if( !Fn.approxEq( 1e-6, aan.R(), model_R ) ) {
						errors.add( "@" + Integer.toHexString( aan.hashCode() )
									+ ": aan.R() [" + aan.R() + "] != model_R [" + model_R + "]" );
					}
				}
				
				
				double check_U = 0;
				double check_L = 0;
				int check_n = 0;
				for( final FsssAbstractStateNode<S, A> asn_prime : aan.successors() ) {
					state_stack.push( asn_prime );
					
					final int n = asn_prime.n();
					check_U += n*asn_prime.U();
					check_L += n*asn_prime.L();
					check_n += n;
				}
				check_U /= check_n;
				check_U += aan.R();
				check_L /= check_n;
				check_L += aan.R();
				
				// Correct value estimates
				if( !Fn.approxEq( 1e-6, aan.U(), check_U ) ) {
					errors.add( "@" + Integer.toHexString( aan.hashCode() )
								+ ": aan.U() [" + aan.U() + "] != check_U [" + check_U + "]" );
				}
				if( !Fn.approxEq( 1e-6, aan.L(), check_L ) ) {
					errors.add( "@" + Integer.toHexString( aan.hashCode() )
								+ ": aan.L() [" + aan.L() + "] != check_L [" + check_L + "]" );
				}
				
				// Action sampled at least C times
				if( check_n < params.width ) {
					errors.add( "@" + Integer.toHexString( aan.hashCode() )
								+ ": check_n [" + check_n + "] < params.width [" + params.width + "]" );
				}
				
				// Action nodes should always have successors
				if( aan.nsuccessors() == 0 ) {
					errors.add( "@" + Integer.toHexString( aan.hashCode() )
								+ ": aan.nsuccessors() == 0" );
				}
			}
		}
		
		return errors;
	}
}
