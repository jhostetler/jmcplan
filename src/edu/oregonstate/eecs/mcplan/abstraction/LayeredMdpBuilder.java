/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.search.ActionNode;
import edu.oregonstate.eecs.mcplan.search.GameTree;
import edu.oregonstate.eecs.mcplan.search.GameTreeVisitor;
import edu.oregonstate.eecs.mcplan.search.StateNode;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class LayeredMdpBuilder
{
	private static class ActionStateTuple<X, A>
	{
		public final X x;
		public final A a;
		public ActionStateTuple( final X x, final A a )
		{
			this.x = x;
			this.a = a;
		}
		
		@Override
		public boolean equals( final Object obj )
		{
			if( !(obj instanceof ActionStateTuple<?, ?>) ) {
				return false;
			}
			@SuppressWarnings( "unchecked" )
			final ActionStateTuple<X, A> that = (ActionStateTuple<X, A>) obj;
			return x.equals( that.x ) && a.equals( that.a );
		}
		
		@Override
		public int hashCode()
		{
			return 139 * (149 * x.hashCode() + a.hashCode());
		}
	}
	
	private static class EmpiricalState<X, A>
	{
		public X x;
		public double r = 0.0;
		public final Map<ActionStateTuple<X, A>, Double> p = new HashMap<ActionStateTuple<X, A>, Double>();
		
		public EmpiricalState( final X x )
		{
			this.x = x;
		}
		
		@Override
		public boolean equals( final Object obj )
		{
			if( !(obj instanceof EmpiricalState<?, ?>) ) {
				return false;
			}
			@SuppressWarnings( "unchecked" )
			final EmpiricalState<X, A> that = (EmpiricalState<X, A>) obj;
			return x.equals( that.x ); //&& r == that.r && p.equals( that.p );
		}
		
		@Override
		public int hashCode()
		{
			final HashCodeBuilder hb = new HashCodeBuilder( 241, 251 );
			hb.append( x );
//			hb.append( r );
//			hb.append( p );
			return hb.toHashCode();
		}
	}
	
	public <X, A extends VirtualConstructor<A>> void build( final GameTree<X, A> T )
	{
		final DirectedGraph<EmpiricalState<X, A>, DefaultEdge> dag
			= new DefaultDirectedGraph<EmpiricalState<X, A>, DefaultEdge>( DefaultEdge.class );
		
		final LayerMaker<X, A> layers = new LayerMaker<X, A>();
		T.root().accept( layers );
		final int dmax = layers.S_.size();
		final ArrayList<StateNode<X, A>> L_0 = layers.S_.get( 0 );
		assert( L_0.size() == 1 );
		final EmpiricalState<X, A> s0 = new EmpiricalState<X, A>( L_0.get( 0 ).token );
		s0.r = L_0.get( 0 ).r( 0 );
		
		final ArrayList<Map<X, ArrayList<StateNode<X, A>>>> merged
			= new ArrayList<Map<X, ArrayList<StateNode<X, A>>>>();
		final Map<X, ArrayList<StateNode<X, A>>> m0 = new HashMap<X, ArrayList<StateNode<X, A>>>();
		m0.put( L_0.get( 0 ).token, L_0 );
		merged.add( m0 );
		
		for( int d = 0; d < dmax; ++d ) {
			final ArrayList<StateNode<X, A>> layer = layers.S_.get( d );
			final HashMap<X, ArrayList<StateNode<X, A>>> succ = new HashMap<X, ArrayList<StateNode<X, A>>>();
			for( final StateNode<X, A> s : layer ) {
				for( final ActionNode<X, A> a : Fn.in( s.successors() ) ) {
					for( final StateNode<X, A> sprime : Fn.in( a.successors() ) ) {
						ArrayList<StateNode<X, A>> list = succ.get( sprime.token );
						if( list == null ) {
							list = new ArrayList<StateNode<X, A>>();
						}
						list.add( sprime );
					}
				}
			}
			merged.add( succ );
		}
		
		for( int d = 1; d < dmax; ++d ) {
			final Map<X, ArrayList<StateNode<X, A>>> m_0 = merged.get( d - 1 );
			final Map<X, ArrayList<StateNode<X, A>>> m_1 = merged.get( d );
			
			for( final Map.Entry<X, ArrayList<StateNode<X, A>>> e_0 : m_0.entrySet() ) {
				
				for( final Map.Entry<X, ArrayList<StateNode<X, A>>> e_1 : m_1.entrySet() ) {
					
				}
			}
			
			
			final ArrayList<StateNode<X, A>> layer = layers.S_.get( d );
			final HashMap<X, ArrayList<StateNode<X, A>>> succ = new HashMap<X, ArrayList<StateNode<X, A>>>();
			for( final StateNode<X, A> s : layer ) {
				for( final ActionNode<X, A> a : Fn.in( s.successors() ) ) {
					for( final StateNode<X, A> sprime : Fn.in( a.successors() ) ) {
						ArrayList<StateNode<X, A>> list = succ.get( sprime.token );
						if( list == null ) {
							list = new ArrayList<StateNode<X, A>>();
						}
						list.add( sprime );
					}
				}
			}
			merged.add( succ );
		}
	}
	
	private static class LayerMaker<X, A extends VirtualConstructor<A>> implements GameTreeVisitor<X, A>
	{
		private int d_ = 0;
		private final ArrayList<ArrayList<StateNode<X, A>>> S_
			= new ArrayList<ArrayList<StateNode<X, A>>>();
		
		@Override
		public void visit( final StateNode<X, A> s )
		{
			if( S_.size() <= d_ ) {
				S_.add( new HashMap<X, ArrayList<StateNode<X, A>>>() );
			}
			final ArrayList<StateNode<X, A>> S_d = S_.get( d_ );
			S_d.add( s );
			d_ += 1;
			for( final ActionNode<X, A> a : Fn.in( s.successors() ) ) {
				visit( a );
			}
			d_ -= 1;
		}
	
		@Override
		public void visit( final ActionNode<X, A> a )
		{
			for( final StateNode<X, A> s : Fn.in( a.successors() ) ) {
				visit( s );
			}
		}
	}

}
