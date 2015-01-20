package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;

public class FsssAbstractStateNode<S extends State, A extends VirtualConstructor<A>>
{
	public final int depth;
	public final FsssAbstractActionNode<S, A> predecessor;
	private final FsssModel<S, A> model;
	private final FsssAbstraction<S, A> abstraction;
	private final Representation<S> x;
//	private final Representer<S, ? extends Representation<S>> repr;
	private final ArrayList<FsssStateNode<S, A>> states = new ArrayList<FsssStateNode<S, A>>();
	private int nvisits = 0;
	
	private double U;
	private double L;
	
	private final Map<A, FsssAbstractActionNode<S, A>> successors = new HashMap<A, FsssAbstractActionNode<S, A>>();
	
	public FsssAbstractStateNode( final FsssAbstractActionNode<S, A> predecessor,
								  final FsssModel<S, A> model, final FsssAbstraction<S, A> abstraction,
								  final Representation<S> x )
	{
		this.depth = predecessor.depth - 1;
		this.predecessor = predecessor;
		this.model = model;
		this.abstraction = abstraction;
		this.x = x;
		this.U = model.Vmax();
		this.L = model.Vmin();
	}
	
	public FsssAbstractStateNode( final int depth,
								  final FsssModel<S, A> model, final FsssAbstraction<S, A> abstraction,
								  final Representation<S> x, final ArrayList<FsssStateNode<S, A>> states )
	{
		this.depth = depth;
		this.predecessor = null;
		this.model = model;
		this.abstraction = abstraction;
		this.x = x;
		this.U = model.Vmax();
		this.L = model.Vmin();
		for( final FsssStateNode<S, A> s : states ) {
			addGroundStateNode( s );
		}
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "[@" ).append( Integer.toHexString( System.identityHashCode( this ) ) )
		  .append( ": " ).append( x )
		  .append( "; states.size(): " ).append( states.size() )
		  .append( "; U: " ).append( U )
		  .append( "; L: " ).append( L )
		  .append( "]" );
		
		// TODO: Debugging code
		for( final FsssStateNode<S, A> gsn : states ) {
			sb.append( "; " ).append( gsn.s() );
		}
		
		return sb.toString();
	}
	
	public ArrayList<FsssStateNode<S, A>> states()
	{
		return states;
	}
	
	public void addGroundStateNode( final FsssStateNode<S, A> gsn )
	{
//		System.out.println( "ASN: addGroundStateNode(): states.size() = " + states.size() );
		states.add( gsn );
		
		// TODO: Debugging code
		final ArrayList<A> exemplar_actions = Fn.takeAll( model.actions( exemplar().s() ) );
		final ArrayList<A> new_actions = Fn.takeAll( model.actions( gsn.s() ) );
		if( exemplar_actions.size() != new_actions.size() ) {
			System.out.println( "! In " + this + ": Unequal action sets" );
			System.out.println( "\t " + exemplar() + " -> " + exemplar_actions );
			System.out.println( "\t " + gsn + " -> " + new_actions );
			assert( false );
		}
		else {
			for( int i = 0; i < exemplar_actions.size(); ++i ) {
				if( !exemplar_actions.get( i ).equals( new_actions.get( i ) ) ) {
					System.out.println( "! In " + this + ": Unequal action sets" );
					System.out.println( "\t " + exemplar() + " -> " + exemplar_actions );
					System.out.println( "\t " + gsn + " -> " + new_actions );
					assert( false );
					break;
				}
			}
		}
	}
	
	/**
	 * Number of constituent ground states.
	 * @return
	 */
	public int n()
	{
		return states.size();
	}
	
	public int nvisits()
	{
		return nvisits;
	}
	
	public void visit()
	{
		nvisits += 1;
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
		double max_u = -Double.MAX_VALUE;
		double max_l = -Double.MAX_VALUE;
		for( final FsssAbstractActionNode<S, A> an : successors() ) {
			final double u = an.U();
			if( u > max_u ) {
				max_u = u;
			}
			
			final double l = an.L();
			if( l > max_l ) {
				max_l = l;
			}
		}
		U = max_u;
		L = max_l;
		
		for( final FsssStateNode<S, A> gsn : states ) {
			gsn.backup();
		}
	}
	
	public FsssAbstractActionNode<S, A> astar()
	{
		FsssAbstractActionNode<S, A> astar = null;
		double ustar = -Double.MAX_VALUE;
		for( final FsssAbstractActionNode<S, A> an : successors() ) {
			final double u = an.U();
			if( u > ustar ) {
				ustar = u;
				astar = an;
			}
		}
//		assert( astar != null );
		return astar;
	}
	
	public Iterable<FsssAbstractActionNode<S, A>> successors()
	{
		return successors.values();
	}
	
	public FsssAbstractActionNode<S, A> successor( final A a )
	{
		return successors.get( a );
	}
	
	public void expand( final Iterable<A> actions, final int width )
	{
		createActionNodes( actions );
		sample( width );
	}
	
	public void sample( final int width )
	{
		for( final FsssAbstractActionNode<S, A> an : successors() ) {
			an.sample( width );
		}
	}
	
	/**
	 * All this actually does is create GAN successors for each GSN in 'added'
	 * and mark this node as visited.
	 * 
	 * The name is fairly misleading.
	 * @param added
	 * @param width
	 */
	public void upSample( final ArrayList<FsssStateNode<S, A>> added, final int width )
	{
		visit();
		for( final FsssAbstractActionNode<S, A> aan : successors() ) {
			for( final FsssStateNode<S, A> s : added ) {
				final FsssActionNode<S, A> gan = s.createActionNode( aan.a() );
				aan.addGroundActionNode( gan );
			}
			
//			aan.sample( width );
		}
	}
	
	public void createActionNodes( final Iterable<A> actions )
	{
		for( final A a : actions ) {
			final FsssAbstractActionNode<S, A> an = new FsssAbstractActionNode<S, A>(
				this, model, abstraction, a, abstraction.createRepresenter() );
			final FsssAbstractActionNode<S, A> check = successors.put( a, an );
			
			// TODO: Debugging code
			if( check != null ) {
				System.out.println( "! " + this + ": child for " + a + " already exists " + check );
			}
			
			assert( check == null );
			
			for( final FsssStateNode<S, A> s : states ) {
				final FsssActionNode<S, A> gan = s.createActionNode( a );
				an.addGroundActionNode( gan );
			}
		}
	}
	
	public void buildSubtree2( final FsssStateNode<S, A> gsn, final FsssAbstractStateNode<S, A> old_asn )
	{
		System.out.println( "ASN.buildSubtree2( " + gsn + ", " + old_asn + " )" );
		assert( old_asn.states.contains( gsn ) );
		for( final FsssActionNode<S, A> gan : gsn.successors() ) {
			final FsssAbstractActionNode<S, A> old_succ = old_asn.successor( gan.a() );
			if( old_succ == null ) {
				System.out.println( "!\t" + old_asn + " has no successor for " + gan.a() );
			}
			final FsssAbstractActionNode<S, A> aan = requireSuccessor( gan, old_succ );
			aan.buildSubtree2( gan, old_succ );
		}
	}
	
	public FsssAbstractActionNode<S, A> requireSuccessor( final FsssActionNode<S, A> gan, final FsssAbstractActionNode<S, A> old_aan )
	{
		FsssAbstractActionNode<S, A> succ = successors.get( gan.a() );
		if( succ == null ) {
			final RefineablePartitionTreeRepresenter<S, A> repr = old_aan.repr.emptyInstance();
			succ = new FsssAbstractActionNode<S, A>( this, model, abstraction, gan.a(), repr );
			successors.put( gan.a(), succ );
		}
		succ.addGroundActionNode( gan );
		return succ;
	}
	
	public void buildSubtree( final FsssAbstractStateNode<S, A> union )
	{
		System.out.println( "ASN.buildSubtree( " + union + " )" );
		
		// TODO: Debugging code
		final ArrayList<A> actions = Fn.takeAll( model.actions( exemplar().s() ).iterator() );
		if( actions.isEmpty() ) {
			System.out.println( "! actions is empty" );
		}
		System.out.println( "ASN " + this + ": exemplar is " + exemplar() );
		
		// Create and populate successor abstract action nodes
		// FIXME: If actions is empty, then no successor will be added here.
		// That screws things up later when we do upSample().
		for( final A a : model.actions( exemplar().s() ) ) {
			final FsssAbstractActionNode<S, A> union_a = union.successor( a );
			
			// TODO: Debugging code
			if( union_a == null ) {
				System.out.println( "! ASN " + union + " has no successor for " + a );
			}
			
			assert( union_a != null );
			final RefineablePartitionTreeRepresenter<S, A> repr = union_a.repr.emptyInstance();
			final FsssAbstractActionNode<S, A> an = new FsssAbstractActionNode<S, A>(
				this, model, abstraction, a, repr );
			final FsssAbstractActionNode<S, A> check = successors.put( a, an );
			assert( check == null );
			
			
		}
		
		for( final FsssStateNode<S, A> gsn : states ) {
			for( final FsssActionNode<S, A> gan : gsn.successors() ) {
				final FsssAbstractActionNode<S, A> succ = successors.get( gan.a() );
				
				// TODO: Debugging code
				if( succ == null ) {
					System.out.println( "! No AAN successor for " + gan.a() );
				}
				
				assert( succ != null );
				succ.addGroundActionNode( gan );
			}
		}
		
		for( final FsssAbstractActionNode<S, A> an : successors() ) {
			final FsssAbstractActionNode<S, A> union_a = union.successor( an.a() );
			an.buildSubtree( union_a );
		}
		
//		backup();
	}
	
	public void leaf( final Iterable<A> actions )
	{
		visit();
		createActionNodes( actions );
		for( final FsssAbstractActionNode<S, A> an : successors() ) {
			for( final FsssActionNode<S, A> gan : an.actions ) {
				gan.leaf();
			}
			an.leaf();
		}
	}
	
	// FIXME: We're assuming that the abstraction does not identify
	// states that have different legal action sets. Document this
	// somewhere!
	public FsssStateNode<S, A> exemplar()
	{
		return states.get( 0 );
	}

	public boolean isTerminal()
	{
		return depth == 1 || exemplar().s().isTerminal();
	}
	
	public Representation<S> x()
	{
		return x;
	}

	public int nsuccessors()
	{
		return successors.size();
	}
}