/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.pats;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class PatsStateNode<S extends State, A> implements AbstractionGraph.Listener<S, A>
{
	private final PatsActionNode<S, A> parent;
	public final Representation<S> x;
	private final BoundedValueModel<S, A> model;
	
	private final LinkedHashMap<A, PatsActionNode<S, A>> successors = new LinkedHashMap<>();
	
	// FIXME: elements needs to either be a Multiset, or we need to count
	// multiplicity separately.
	/*package*/ final ArrayList<BoundedStateNode<S, A>> elements = new ArrayList<>();
	private final ActionSet<A> abstraction = null;
	
	private final boolean terminal 	= false;
	private final boolean backed_up 	= false;
	private final boolean pure 		= true;
	private final boolean freed 		= false;
	
	public final int depth;
	
	private final MeanVarianceAccumulator R = new MeanVarianceAccumulator();
	
	private double U = Double.NaN;
	private double L = Double.NaN;
	
	
	public PatsStateNode( final Representation<S> x, final PatsActionNode<S, A> parent,
						  final BoundedValueModel<S, A> model )
	{
		this.parent = parent;
		this.x = x;
		this.model = model;
		this.depth = parent.depth() - 1;
	}
	
	public int n()
	{
		return elements.size();
	}
	
	public PatsActionNode<S, A> successor( final A a )
	{
		return successors.get( a );
	}
	
	public Iterable<PatsActionNode<S, A>> successors()
	{
		return Iterables.unmodifiableIterable( successors.values() );
	}
	
	public int Nsuccessors()
	{
		return successors.size();
	}
	
	/*package*/ void addElement( final BoundedStateNode<S, A> ss )
	{
		elements.add( ss );
	}
	
	public boolean isTerminal()
	{
		return depth == 0 || terminal;
	}
	
	private boolean isExpanded()
	{
		return Nsuccessors() > 0;
	}
	
	public boolean isPure()
	{
		return pure;
	}
	
//	public boolean isReadyToClose()
//	{
//		return isExpanded() && (isTerminal() || isPure());
//	}
	
//	public boolean isActive()
//	{
//		return isExpanded() && !isTerminal() && !isPure();
//	}
	
	public boolean isClosed()
	{
		return freed;
	}
	
	public double R()
	{
		return R.mean();
	}
	
	public double U()
	{
		return U;
	}
	
	public double L()
	{
		return L;
	}
	
	public void backup()
	{
		assert( Nsuccessors() > 0 );
		double max_u = -Double.MAX_VALUE;
		double max_l = -Double.MAX_VALUE;
		for( final PatsActionNode<S, A> an : successors() ) {
			final double u = an.U();
			if( u > max_u ) {
				max_u = u;
			}
			
			final double l = an.L();
			if( l > max_l ) {
				max_l = l;
			}
		}
		U = R.mean() + max_u;
		L = R.mean() + max_l;
		
		// FIXME: Where whould we do this?
		for( final BoundedStateNode<S, A> gsn : elements ) {
			gsn.backup();
		}
		
		backed_up = true;
	}
	
	/**
	 * Returns all actions that achieve the maximum value of U(s, a).
	 * @return
	 */
	public ArrayList<PatsActionNode<S, A>> greatestUpperBound()
	{
		final ArrayList<PatsActionNode<S, A>> best = new ArrayList<>();
		double Ustar = -Double.MAX_VALUE;
		for( final PatsActionNode<S, A> an : successors() ) {
			final double U = an.U();
			if( U > Ustar ) {
				Ustar = U;
				best.clear();
				best.add( an );
			}
			else if( U >= Ustar ) {
				best.add( an );
			}
		}
		return best;
	}
	
	/**
	 * Returns all actions that achieve the maximum value of L(s, a).
	 * @return
	 */
	public ArrayList<PatsActionNode<S, A>> greatestLowerBound()
	{
		final ArrayList<PatsActionNode<S, A>> best = new ArrayList<>();
		double Lstar = -Double.MAX_VALUE;
		for( final PatsActionNode<S, A> an : successors() ) {
			final double L = an.L();
			if( L > Lstar ) {
				Lstar = L;
				best.clear();
				best.add( an );
			}
			else if( L >= Lstar ) {
				best.add( an );
			}
		}
		return best;
	}
	
	private PatsActionNode<S, A> createSuccessor( final A a )
	{
		for( final PatsActionNode<S, A> bn : successors() ) {
			if( bn.a().equals( a ) ) {
				throw new IllegalStateException( "Successor already exists for '" + a + "'" );
			}
		}
		final PatsActionNode<S, A> an = new PatsActionNode<S, A>( a, this, model, abstraction );
	}

	@Override
	public void updateAbstraction( final ActionSet<A> an )
	{
		if( isExpanded() ) {
			for( final A a : Sets.difference( an, abstraction ) ) {
				
			}
		}
	}

	@Override
	public void updateAbstraction( final StateAbstraction<S> X, final ArrayList<Representation<S>> changed )
	{ }
	
	private void transferSubtreeStructureTo( final PatsStateNode<S, A> subtree )
	{
		//
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
}
