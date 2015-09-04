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

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import ch.qos.logback.classic.Logger;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.domains.cards.InfiniteSpanishDeck;
import edu.oregonstate.eecs.mcplan.domains.spbj.SpBjAction;
import edu.oregonstate.eecs.mcplan.domains.spbj.SpBjFsssModel;
import edu.oregonstate.eecs.mcplan.domains.spbj.SpBjState;
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
	
	public static <S extends State, A extends VirtualConstructor<A>>
	ArrayList<String> validateTree( final FsssAbstractStateNode<S, A> root, final FsssModel<S, A> model )
	{
		final ArrayList<String> errors = new ArrayList<String>();
		
		final Deque<FsssAbstractStateNode<S, A>> state_stack = new ArrayDeque<FsssAbstractStateNode<S, A>>();
		state_stack.push( root );
		
		while( !state_stack.isEmpty() ) {
			final FsssAbstractStateNode<S, A> asn = state_stack.pop();
			
//			final S s = asn.exemplar().s();
			
			{
			
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
				else if( asn.nvisits() > 0 && asn.isTerminal() ) {
					check_U = check_L = model_H;
				}
				else {
					check_U = model_U;
					check_L = model_L;
				}
				check_U += model_R;
				check_L += model_R;
				
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
				
				if( asn.U() - asn.L() < 0 ) {
					errors.add( "@" + Integer.toHexString( asn.hashCode() )
								+ ": asn.U() [" + asn.U() + "] - asn.L() [" + asn.L() + "] < 0" );
				}
				
//				if( asn.U() - model_U > 1e-6 ) {
//					errors.add( "@" + Integer.toHexString( asn.hashCode() )
//								+ ": asn.U() [" + asn.U() + "] >~ model_U [" + model_U + "]" );
//				}
//				if( model_L - asn.L() > 1e-6 ) {
//					errors.add( "@" + Integer.toHexString( asn.hashCode() )
//								+ ": asn.L() [" + asn.L() + "] <~ model_L [" + model_L + "]" );
//				}
				if( asn.nvisits() == 0 ) {
					if( asn.nsuccessors() > 0 ) {
						errors.add( "@" + Integer.toHexString( asn.hashCode() )
									+ ": asn.nvisits() == 0 and asn.nsuccessors() [" + asn.nsuccessors() + "] > 0" );
					}
	//				if( asn.U() != model.Vmax( s ) ) {
	//					errors.add( "@" + Integer.toHexString( asn.hashCode() )
	//								+ ": asn.nvisits() == 0 and asn.U() != model.Vmax()" );
	//				}
	//				if( asn.L() != model.Vmin( s ) ) {
	//					errors.add( "@" + Integer.toHexString( asn.hashCode() )
	//								+ ": asn.nvisits() == 0 and asn.L() != model.Vmin()" );
	//				}
				}
			
			}
			
			for( final FsssAbstractActionNode<S, A> aan : asn.successors() ) {
				final A a = aan.a();
				
				double model_R = 0;
				for( final FsssStateNode<S, A> gsn : asn.states() ) {
					model_R += model.reward( gsn.s(), a );
				}
				model_R /= asn.n();
				double check_U = 0;
				double check_L = 0;
				double model_U = 0;
				double model_L = 0;
				int check_n = 0;
				for( final FsssAbstractStateNode<S, A> asn_prime : aan.successors() ) {
					state_stack.push( asn_prime );
					
					final int n = asn_prime.states().size();
					check_U += n*asn_prime.U();
					check_L += n*asn_prime.L();
					model_U += n*model.Vmax( asn_prime.exemplar().s() );
					model_L += n*model.Vmin( asn_prime.exemplar().s() );
					check_n += n;
				}
				check_U /= check_n;
				check_U += aan.R();
				check_L /= check_n;
				check_L += aan.R();
				model_U /= check_n;
				model_U += model_R;
				model_L /= check_n;
				model_L += model_R;
				
				if( aan.isBackedUp() ) {
					if( !Fn.approxEq( 1e-6, aan.U(), check_U ) ) {
						errors.add( "@" + Integer.toHexString( aan.hashCode() )
									+ ": aan.U() [" + aan.U() + "] != check_U [" + check_U + "]" );
					}
					if( !Fn.approxEq( 1e-6, aan.L(), check_L ) ) {
						errors.add( "@" + Integer.toHexString( aan.hashCode() )
									+ ": aan.L() [" + aan.L() + "] != check_L [" + check_L + "]" );
					}
	//				if( !Fn.approxEq( 1e-6, aan.R(), model_R ) ) {
	//					errors.add( "@" + Integer.toHexString( aan.hashCode() )
	//								+ ": aan.R() != model_R [" + model_R + "]" );
	//				}
//					if( aan.U() - model_U > 1e-6 ) {
//						errors.add( "@" + Integer.toHexString( aan.hashCode() )
//									+ ": aan.U() [" + aan.U() + "] >~ model_U [" + model_U + "]" );
//					}
//					if( model_L - aan.L() > 1e-6 ) {
//						errors.add( "@" + Integer.toHexString( aan.hashCode() )
//									+ ": aan.L() [" + aan.L() + "] <~ model_L [" + model_L + "]" );
//					}
				}
				
				if( aan.nsuccessors() == 0 ) {
					errors.add( "@" + Integer.toHexString( aan.hashCode() )
								+ ": aan.nsuccessors() == 0" );
				}
			}
		}
		
		return errors;
	}
	
	// -----------------------------------------------------------------------
	
	/**
	 * @param args
	 */
	public static void main( final String[] args )
	{
		final int width = 10;
		final int depth = 10;
		final RandomGenerator rng = new MersenneTwister( 43 );
		final SpBjFsssModel model = new SpBjFsssModel( rng );
		final InfiniteSpanishDeck deck = new InfiniteSpanishDeck( rng );
		final SpBjState s0 = new SpBjState( deck );
		s0.init();
		final FsssTreeBuilder<SpBjState, SpBjAction> tb
			= new FsssTreeBuilder<SpBjState, SpBjAction>( model, width, depth );
		tb.buildTree( s0 );
	}
}
