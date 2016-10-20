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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * An action node in an Abstract FSSS tree.
 * @param <S>
 * @param <A>
 */
public class FsssAbstractActionNode<S extends State, A extends VirtualConstructor<A>> implements AutoCloseable
{
	private static final ch.qos.logback.classic.Logger Log = LoggerManager.getLogger( "log.search" );
	
	public final int depth;
	public final FsssAbstractStateNode<S, A> predecessor;
	private final FsssModel<S, A> model;
//	private final FsssAbstraction<S, A> abstraction;
	private final A a;
	public final ClassifierRepresenter<S, A> repr;
//	public final RefineablePartitionTreeRepresenter<S, A> repr;
//	public final Representer<S, ? extends Representation<S>> repr;
	
	public final ArrayList<FsssActionNode<S, A>> actions = new ArrayList<FsssActionNode<S, A>>();
	private final MeanVarianceAccumulator R = new MeanVarianceAccumulator();
	
	private double U = Double.NaN;
	private double L = Double.NaN;
	
	private final MeanVarianceAccumulator Ubar = new MeanVarianceAccumulator();
	private final MeanVarianceAccumulator Lbar = new MeanVarianceAccumulator();
	
	private boolean backed_up = false;
	
	private int n = 0;
	
	public FsssAbstractActionNode( final FsssAbstractStateNode<S, A> predecessor,
								   final FsssModel<S, A> model, final FsssAbstraction<S, A> abstraction,
								   final A a, final ClassifierRepresenter<S, A> repr )
	{
		this.depth = predecessor.depth;
		this.predecessor = predecessor;
		this.model = model;
//		this.abstraction = abstraction;
		this.a = a;
		this.repr = repr;
	}
	
	@Override
	public void close()
	{
		actions.clear();
		actions.trimToSize();
	}
	
	public boolean isBackedUp()
	{
		return backed_up;
	}
	
	public boolean isPure()
	{
		for( final FsssAbstractStateNode<S, A> asn : successors() ) {
			if( !asn.isPure() ) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isActive()
	{
		for( final FsssAbstractStateNode<S, A> asn : successors() ) {
			if( asn.isActive() ) {
				return true;
			}
		}
		return false;
	}
	
	public double R()
	{
		return R.mean();
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "[" ).append( "@" ).append( Integer.toHexString( System.identityHashCode( this )  ) )
		  .append( ": " ).append( a )
		  .append( "; R.mean(): " ).append( R.mean() )
		  .append( "; U: " ).append( U )
		  .append( "; L: " ).append( L )
		  .append( "; actions.size(): " ).append( actions.size() )
		  .append( "]" );
		
		// TODO: Debugging code
		for( final FsssActionNode<S, A> gan : actions ) {
			sb.append( "; " ).append( gan );
		}
		
		return sb.toString();
	}
	
	public A a()
	{
		return a;
	}
	
	public double U()
	{
		return U;
	}
	
	public double L()
	{
		return L;
	}
	
	public double Uvar()
	{
		final MeanVarianceAccumulator acc = new MeanVarianceAccumulator();
		for( final FsssActionNode<S, A> gan : actions ) {
			acc.add( gan.U() );
		}
		return acc.variance();
	}
	
	public double Lvar()
	{
		final MeanVarianceAccumulator acc = new MeanVarianceAccumulator();
		for( final FsssActionNode<S, A> gan : actions ) {
			acc.add( gan.L() );
		}
		return acc.variance();
	}
	
	/**
	 * Number of ground state successors that have been sampled.
	 * @return
	 */
	public int n()
	{
		return n;
	}
	
	public FsssAbstractStateNode<S, A> sstar()
	{
		FsssAbstractStateNode<S, A> sstar = null;
		double bstar = -Double.MAX_VALUE;
		for( final FsssAbstractStateNode<S, A> s : successors() ) {
			final double b = s.U() - s.L();
			assert( b >= 0 );
			if( b > bstar ) {
				bstar = b;
				sstar = s;
			}
		}
		
		if( sstar == null ) {
			FsssTest.printTree( FsssTest.findRoot( this ), System.out, 1 );
			System.out.println( this );
		}
		
		assert( sstar != null );
		return sstar;
	}
	
	public FsssAbstractStateNode<S, A> sstar_random()
	{
		final ArrayList<FsssAbstractStateNode<S, A>> sstar = new ArrayList<FsssAbstractStateNode<S, A>>();
		double bstar = -Double.MAX_VALUE;
		for( final FsssAbstractStateNode<S, A> s : successors() ) {
			final double b = s.U() - s.L();
			
			if( b < 0 ) {
				Log.error( "! AAN: " + this );
				Log.error( "! ASN: " + s );
				Log.error( "! s.U() [" + s.U() + "] - s.L() [" + s.L() + "] < 0" );
				throw new IllegalStateException( "s.U() - s.L() < 0" );
			}
			
			if( b > bstar ) {
				bstar = b;
				sstar.clear();
				sstar.add( s );
			}
			else if( b >= bstar ) {
				sstar.add( s );
			}
		}
		
		if( sstar.isEmpty() ) {
			return null;
		}
		else {
			return sstar.get( model.rng().nextInt( sstar.size() ) );
		}
	}
	
	public void backup()
	{
		assert( nsuccessors() > 0 );
		
		backed_up = true;
		
		double u = 0;
		double l = 0;
		int N = 0;
		for( final FsssAbstractStateNode<S, A> sn : successors() ) {
			if( sn == null ) {
				Log.debug( "\t\t! AAN.backup(): null successor skipped" );
				// We think it is safe to skip null successors, because they're
				// about to be pruned.
				continue;
			}
			
			final int n = sn.n();
			N += n;
			u += n * sn.U();
			l += n * sn.L();
		}
		U = R.mean();
		L = R.mean();
		assert( N > 0 );
		U += model.discount() * u / N;
		L += model.discount() * l / N;
		
		// Note: I originally considered it an error if any GANs had no
		// successors. However, that can happen if sampling is stopped early.
		for( final FsssActionNode<S, A> gan : actions ) {
			if( gan.nsuccessors() > 0 ) {
				gan.backup();
			}
		}
	}
	
	public Iterable<FsssAbstractStateNode<S, A>> successors()
	{
		return Fn.in( repr.classes() );
	}
	
	protected FsssAbstractStateNode<S, A> successor( final S s )
	{
		return repr.classify( s ).aggregate;
	}
	
	public int nsuccessors()
	{
		return repr.nclasses();
	}
	
	public void addGroundActionNode( final FsssActionNode<S, A> gan )
	{
		assert( !backed_up );
		assert( gan.a().equals( this.a() ) );
		actions.add( gan );
		R.add( gan.r );
		Ubar.add( gan.U() );
		Lbar.add( gan.L() );
		U = Ubar.mean();
		L = Lbar.mean();
	}
	
	public void sample( final int width, final Budget budget )
	{
		while( n < width ) {
			if( budget.isExceeded() ) {
				break;
			}
			
			final int Na = (int) Math.ceil( width / (double) actions.size() );
			assert( Na > 0 );
			assert( Na <= width );
			int N = 0;
			for( final FsssActionNode<S, A> gan : actions ) {
				if( gan.nsuccessors() < Na ) {
					final FsssStateNode<S, A> gsn = gan.sample();
					final FsssAbstractStateNode<S, A> asn = repr.addTrainingSample( this, gsn.s() );
					asn.addGroundStateNode( gsn );
				}
				N += gan.nsuccessors();
			}
			n = N;
		}
	}
	
	// FIXME: This method is exactly the same as sample() except that it
	// builds the 'added' list. This duplication has already led to bugs!
	public Map<FsssAbstractStateNode<S, A>, ArrayList<FsssStateNode<S, A>>>
	upSample( final int width, final Budget budget )
	{
		final Map<FsssAbstractStateNode<S, A>, ArrayList<FsssStateNode<S, A>>> added
			= new HashMap<FsssAbstractStateNode<S, A>, ArrayList<FsssStateNode<S, A>>>();
		while( n < width ) {
			if( budget.isExceeded() ) {
				break;
			}
			
			final int Na = (int) Math.ceil( width / (double) actions.size() );
			assert( Na > 0 );
			assert( Na <= width );
			int N = 0;
			for( final FsssActionNode<S, A> gan : actions ) {
				if( gan.nsuccessors() < Na ) {
					final FsssStateNode<S, A> gsn = gan.sample();
					final FsssAbstractStateNode<S, A> asn = repr.addTrainingSample( this, gsn.s() );
					asn.addGroundStateNode( gsn );
					
					ArrayList<FsssStateNode<S, A>> asn_added = added.get( asn );
					if( asn_added == null ) {
						asn_added = new ArrayList<FsssStateNode<S, A>>();
						added.put( asn, asn_added );
					}
					asn_added.add( gsn );
				}
				N += gan.nsuccessors();
			}
			n = N;
		}
		return added;
	}
	
	public void splitSuccessor( final FsssAbstractStateNode<S, A> asn, final ArrayList<FsssAbstractStateNode<S, A>> parts )
	{
		for( final FsssAbstractStateNode<S, A> p : parts ) {
			for( final FsssStateNode<S, A> gsn : p.states() ) {
				p.buildSubtree2( gsn, asn );
			}
		}
	}
	
	public void buildSubtree2( final FsssActionNode<S, A> gan, final FsssAbstractActionNode<S, A> old_aan )
	{
		assert( old_aan.actions.contains( gan ) );
		for( final FsssStateNode<S, A> gsn : gan.successors() ) {
			final FsssAbstractStateNode<S, A> asn = requireSuccessor( gsn );
			final FsssAbstractStateNode<S, A> old_succ = old_aan.successor( gsn.s() );
			
			if( old_succ == null ) {
				System.out.println( "\t! old_aan = " + old_aan );
				System.out.println( "\t! gsn = " + gsn );
				System.out.println( "\t\t old_succ = null" );
			}
			if( old_succ.isExpanded() ) {
				asn.buildSubtree2( gsn, old_succ );
			}
		}
	}
	
	public FsssAbstractStateNode<S, A> requireSuccessor( final FsssStateNode<S, A> gsn )
	{
		final FsssAbstractStateNode<S, A> asn = repr.addTrainingSampleAsExistingNode( this, gsn );
		Log.trace( "\trequireSuccessor(): {}", gsn );
		Log.trace( "\t\t-> {}", asn );
		asn.addGroundStateNode( gsn );
		return asn;
	}
}
