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
package edu.oregonstate.eecs.mcplan.search.pats;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.google.common.collect.Iterables;

import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class PatsActionNode<S extends State, A> implements AutoCloseable, AbstractionGraph.Listener<S, A>
{
	private static final ch.qos.logback.classic.Logger Log = LoggerManager.getLogger( "log.search" );
	
	public final PatsStateNode<S, A> predecessor;
	private final BoundedValueModel<S, A> model;
	private final A a;
	
	private StateAbstraction<S> abstraction = null;
	
	private final LinkedHashMap<Representation<S>, PatsStateNode<S, A>> successors = new LinkedHashMap<>();
	
	public final ArrayList<BoundedActionNode<S, A>> elements = new ArrayList<BoundedActionNode<S, A>>();
	private final MeanVarianceAccumulator R = new MeanVarianceAccumulator();
	
	private double U = Double.NaN;
	private double L = Double.NaN;
	
	private final MeanVarianceAccumulator Ubar = new MeanVarianceAccumulator();
	private final MeanVarianceAccumulator Lbar = new MeanVarianceAccumulator();
	
	private boolean backed_up = false;
	
	private final int n = 0;
	
	public PatsActionNode( final A a, final PatsStateNode<S, A> predecessor,
						   final BoundedValueModel<S, A> model, final StateAbstraction<S> abstraction )
	{
		this.a = a;
		this.predecessor = predecessor;
		this.model = model;
		this.abstraction = abstraction;
	}
	
	public PatsActionNode<S, A> makeEmptyCopyBelow( final PatsStateNode<S, A> copy_predecessor )
	{
		return new PatsActionNode<>( a(), copy_predecessor, model, abstraction );
	}
	
	/*package*/ void addElement( final BoundedActionNode<S, A> sa )
	{
		elements.add( sa );
	}
	
	public Iterable<PatsStateNode<S, A>> successors()
	{
		return Iterables.unmodifiableIterable( successors.values() );
	}
	
	protected final PatsStateNode<S, A> successor( final S s )
	{
		return successor( abstraction.encode( s ) );
	}
	
	protected final PatsStateNode<S, A> successor( final Representation<S> x )
	{
		return successors.get( x );
	}
	
	public int Nsuccessors()
	{
		return successors.size();
	}
	
	public final int depth()
	{
		return predecessor.depth;
	}
	
	@Override
	public void updateAbstraction( final StateAbstraction<S> X, final ArrayList<Representation<S>> changed )
	{
		// Split the subtrees
		for( final Representation<S> changed_set : changed ) {
			// Save a reference to the obsolete subtree and remove it from the tree
			final PatsStateNode<S, A> old_subtree = successor( changed_set );
			successors.remove( changed_set );
			// Partition its elements according to the new representation
			final ArrayList<PatsStateNode<S, A>> new_subtrees = new ArrayList<>();
			for( final BoundedStateNode<S, A> ss : old_subtree.elements ) {
				final Representation<S> sx = X.encode( ss.s() );
				// Ensure successor node exists
				PatsStateNode<S, A> succ = successor( sx );
				if( succ == null ) {
					succ = new PatsStateNode<>( sx, this, model );
					successors.put( sx, succ );
					new_subtrees.add( succ );
				}
				
				succ.addElement( ss );
			}
			// Recursively split the new subtrees, using the old subtree as template
			for( final PatsStateNode<S, A> new_subtree : new_subtrees ) {
				transferSubtreeStructure( old_subtree, new_subtree );
			}
		}
	}
	
	private void transferSubtreeStructureTo( final PatsActionNode<S, A> subtree )
	{
		// 'subtree' contains a subset of the elements in 'this'
		// We need to initialize the children of 'subtree' with the same
		// structure as the children of 'this', and populate them with the
		// elements of the children of 'this' that are children of elements of 'subtrees',
		// preferably without evaluating the classifier.
		
		
		for( final PatsStateNode<S, A> tsucc : this.successors() ) {
			for( final BoundedActionNode<S, A> sub_element : subtree.elements ) {
				if( tsucc.elements.contains( sub_element ) ) {
					// 'sub_element' belong in the child of 'subtree'
					// corresponding to 'tsucc'
					
				}
			}
		}
		
		for( final BoundedStateNode<S, A> ss : subtree.elements ) {
			for( final BoundedActionNode<S, A> sa : ss.successors() ) {
				final PatsActionNode<S, A> tsucc = this.successor( sa.a() );
				PatsActionNode<S, A> a = subtree.successor( tsucc.a() );
				if( a == null ) {
					a = tsucc.makeEmptyCopyBelow( subtree );
				}
				a.addElement( sa );
			}
		}
		
		
		for( final PatsActionNode<S, A> a : subtree.successors() ) {
			this.successor( a.a() ).transferSubtreeStructureTo( a );
		}
		
		// FIXME: Need to de-allocate old nodes someplace
	}
	
	private void transferSubtreeStructureTo( final PatsStateNode<S, A> template, final PatsStateNode<S, A> subtree )
	{
		for( final BoundedStateNode<S, A> ss : subtree.elements ) {
			for( final BoundedActionNode<S, A> sa : ss.successors() ) {
				final PatsActionNode<S, A> tsucc = template.successor( sa.a() );
				PatsActionNode<S, A> a = subtree.successor( tsucc.a() );
				if( a == null ) {
					a = new PatsActionNode<>( tsucc.a(), subtree, tsucc.model, tsucc.abstraction );
				}
				a.addElement( sa );
			}
		}
		
		
		for( final PatsActionNode<S, A> a : subtree.successors() ) {
			transferSubtreeStructure( template.successor( a.a() ), a );
		}
		
//		for( final BoundedActionNode<S, A> sa : template.elements ) {
//			// FIXME: subtree_parent.elements is currently a list, making this a O(n) operation
//			if( subtree_parent.elements.contains( sa.parent ) ) {
//
//			}
//		}
	}
	
//	private void transferSubtreeStructure( final PatsStateNode<S, A> template, final PatsStateNode<S, A> subtree )
//	{
//		for( final BoundedStateNode<S, A> ss : )
//
//		for( final PatsActionNode<S, A> tsucc : template.successors() ) {
//			final PatsActionNode<S, A> dup = tsucc.createEmptyInstance();
//		}
//	}
	
	private PatsStateNode<S, A> ensureSuccessor( final Representation<S> x )
	{
		final PatsStateNode<S, A> succ = successor( sx );
		if( succ == null ) {
			
		}
	}

	@Override
	public void updateAbstraction( final ActionSet<A> Y )
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void close()
	{
		elements.clear();
		elements.trimToSize();
	}
	
	public boolean isBackedUp()
	{
		return backed_up;
	}
	
	public boolean isPure()
	{
		for( final PatsStateNode<S, A> asn : successors() ) {
			if( !asn.isPure() ) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isActive()
	{
		for( final PatsStateNode<S, A> asn : successors() ) {
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
		  .append( "; actions.size(): " ).append( elements.size() )
		  .append( "]" );
		
		// TODO: Debugging code
		for( final BoundedActionNode<S, A> gan : elements ) {
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
		for( final BoundedActionNode<S, A> gan : elements ) {
			acc.add( gan.U() );
		}
		return acc.variance();
	}
	
	public double Lvar()
	{
		final MeanVarianceAccumulator acc = new MeanVarianceAccumulator();
		for( final BoundedActionNode<S, A> gan : elements ) {
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
	
	public ArrayList<PatsStateNode<S, A>> sstar()
	{
		final ArrayList<PatsStateNode<S, A>> sstar = new ArrayList<>();
		double bstar = -Double.MAX_VALUE;
		for( final PatsStateNode<S, A> s : successors() ) {
			final double b = s.U() - s.L();
			assert( b >= 0 );
			if( b > bstar ) {
				bstar = b;
				sstar.clear();
				sstar.add( s );
			}
			else if( b == bstar ) {
				sstar.add( s );
			}
		}
		
		assert( sstar != null );
		return sstar;
	}
	
	public void backup()
	{
		assert( nsuccessors() > 0 );
		
		backed_up = true;
		
		double u = 0;
		double l = 0;
		int N = 0;
		for( final PatsStateNode<S, A> sn : successors() ) {
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
		
		// Backup the ground tree
		// FIXME: It seems like bad design for this to happen here.
		for( final BoundedActionNode<S, A> gan : elements ) {
			gan.backup();
		}
	}
	
	

	

}
