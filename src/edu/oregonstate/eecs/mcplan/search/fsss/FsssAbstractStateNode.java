package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

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
	
	private final MeanVarianceAccumulator R = new MeanVarianceAccumulator();
	
	private double U = Double.NaN;
	private double L = Double.NaN;
	
	private final MeanVarianceAccumulator Ubar = new MeanVarianceAccumulator();
	private final MeanVarianceAccumulator Lbar = new MeanVarianceAccumulator();
	
	private boolean backed_up = false;
	
	private final Map<A, FsssAbstractActionNode<S, A>> successors
		= new HashMap<A, FsssAbstractActionNode<S, A>>();
	
	/**
	 * This list is used to provide a deterministic iteration order for the
	 * successors.
	 */
	private final ArrayList<FsssAbstractActionNode<S, A>> ordered_successors
		= new ArrayList<FsssAbstractActionNode<S, A>>();
	
	public FsssAbstractStateNode( final FsssAbstractActionNode<S, A> predecessor,
								  final FsssModel<S, A> model, final FsssAbstraction<S, A> abstraction,
								  final Representation<S> x )
	{

		this.depth = predecessor.depth - 1;
		this.predecessor = predecessor;
		this.model = model;
		this.abstraction = abstraction;
		this.x = x;
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
		for( final FsssStateNode<S, A> s : states ) {
			addGroundStateNode( s );
		}
	}
	
	/**
	 * Adds a successor node mapping 'a => aan' to both the successor map
	 * and the ordered successor list. Returns the previous mapping. No change
	 * is made to 'ordered_successors' if 'aan' was already in 'successors'.
	 * @param a
	 * @param aan
	 * @return
	 */
	private FsssAbstractActionNode<S, A> addSuccessor( final A a, final FsssAbstractActionNode<S, A> aan )
	{
		if( isTerminal() ) {
			System.out.println( "! " + this );
		}
		assert( !isTerminal() );
		final FsssAbstractActionNode<S, A> previous = successors.put( a, aan );
		if( previous == null ) {
			ordered_successors.add( aan );
		}
		return previous;
	}
	
//	@Override
//	public int hashCode()
//	{ return id; }
//
//	@Override
//	public boolean equals( final Object obj )
//	{ return obj == this; }
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "[@" ).append( Integer.toHexString( System.identityHashCode( this ) ) )
		  .append( ": " ).append( x )
		  .append( " (" ).append( isTerminal() ? "terminal" : "non-terminal" ).append( ")" )
		  .append( "; nvisits: " ).append( nvisits() )
		  .append( "; states.size(): " ).append( states.size() )
		  .append( "; R: " ).append( R.mean() )
		  .append( "; U: " ).append( U )
		  .append( "; L: " ).append( L )
		  .append( "]" );
		
		// TODO: Debugging code
		for( final FsssStateNode<S, A> gsn : states ) {
			sb.append( "; " ).append( gsn );
		}
		
		return sb.toString();
	}
	
	public ArrayList<FsssStateNode<S, A>> states()
	{
		return states;
	}
	
	public void addGroundStateNode( final FsssStateNode<S, A> gsn )
	{
		assert( !backed_up );
		
//		System.out.println( "ASN: addGroundStateNode(): states.size() = " + states.size() );
		states.add( gsn );
		R.add( gsn.r );
		Ubar.add( gsn.U() );
		Lbar.add( gsn.L() );
		U = Ubar.mean();
		L = Lbar.mean();
		
		// TODO: Debugging code
//		final ArrayList<A> exemplar_actions = Fn.takeAll( model.actions( exemplar().s() ) );
//		final ArrayList<A> new_actions = Fn.takeAll( model.actions( gsn.s() ) );
//		if( exemplar_actions.size() != new_actions.size() ) {
//			System.out.println( "! In " + this + ": Unequal action sets" );
//			System.out.println( "\t " + exemplar() + " -> " + exemplar_actions );
//			System.out.println( "\t " + gsn + " -> " + new_actions );
//			assert( false );
//		}
//		else {
//			for( int i = 0; i < exemplar_actions.size(); ++i ) {
//				if( !exemplar_actions.get( i ).equals( new_actions.get( i ) ) ) {
//					System.out.println( "! In " + this + ": Unequal action sets" );
//					System.out.println( "\t " + exemplar() + " -> " + exemplar_actions );
//					System.out.println( "\t " + gsn + " -> " + new_actions );
//					assert( false );
//					break;
//				}
//			}
//		}
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
		assert( nsuccessors() > 0 );
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
		U = R.mean() + max_u;
		L = R.mean() + max_l;
		
		for( final FsssStateNode<S, A> gsn : states ) {
			gsn.backup();
		}
		
		backed_up = true;
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
		
		return astar;
	}
	
	public FsssAbstractActionNode<S, A> astar_random()
	{
		final ArrayList<FsssAbstractActionNode<S, A>> astar = new ArrayList<FsssAbstractActionNode<S, A>>();
		double ustar = -Double.MAX_VALUE;
		for( final FsssAbstractActionNode<S, A> an : successors() ) {
			final double u = an.U();
			if( u > ustar ) {
				ustar = u;
				astar.clear();
				astar.add( an );
			}
			else if( u >= ustar ) {
				astar.add( an );
			}
		}
		
		if( astar.isEmpty() ) {
			return null;
		}
		else {
			return astar.get( model.rng().nextInt( astar.size() ) );
		}
	}
	
	public ArrayList<FsssAbstractActionNode<S, A>> greatestLowerBound()
	{
		final ArrayList<FsssAbstractActionNode<S, A>> best = new ArrayList<FsssAbstractActionNode<S, A>>();
		double Lstar = -Double.MAX_VALUE;
		for( final FsssAbstractActionNode<S, A> an : successors() ) {
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
	
	public Iterable<FsssAbstractActionNode<S, A>> successors()
	{
//		return successors.values();
		return ordered_successors;
	}
	
	public FsssAbstractActionNode<S, A> successor( final A a )
	{
		return successors.get( a );
	}
	
	public void expand( final Iterable<A> actions, final int width, final int max_samples )
	{
		createActionNodes( actions );
		sample( width, max_samples );
	}
	
	public void sample( final int width, final int max_samples )
	{
		for( final FsssAbstractActionNode<S, A> an : successors() ) {
			an.sample( width, max_samples );
		}
	}
	
	/**
	 * All this actually does is create GAN successors for each non-terminal
	 * GSN in 'added'.
	 * 
	 * The name is fairly misleading.
	 * @param added
	 * @param width
	 */
	public void addActionNodes( final ArrayList<FsssStateNode<S, A>> added )
	{
		for( final FsssStateNode<S, A> gsn : added ) {
			assert( gsn.nsuccessors() == 0 );
			if( !gsn.isTerminal() ) {
				gsn.createActionNodes( model.actions( gsn.s() ) );
				for( final FsssActionNode<S, A> gan : gsn.successors() ) {
					FsssAbstractActionNode<S, A> aan = successors.get( gan.a() );
					if( aan == null ) {
						aan = new FsssAbstractActionNode<S, A>( this, model, abstraction, gan.a(), abstraction.createRepresenter() );
	//					successors.put( gan.a(), aan );
	//					ordered_successors.add( aan );
						addSuccessor( gan.a(), aan );
					}
					aan.addGroundActionNode( gan );
				}
			}
		}
	}
	
	public void createActionNodes( final Iterable<A> actions )
	{
		for( final A a : actions ) {
			final FsssAbstractActionNode<S, A> an = new FsssAbstractActionNode<S, A>(
				this, model, abstraction, a, abstraction.createRepresenter() );
//			final FsssAbstractActionNode<S, A> check = successors.put( a, an );
//			ordered_successors.add( an );
			final FsssAbstractActionNode<S, A> check = addSuccessor( a, an );
			
			// TODO: Debugging code
//			if( check != null ) {
//				System.out.println( "! " + this + ": child for " + a + " already exists " + check );
//				FsssTest.printTree( this, System.out, 1 );
//			}
			
			assert( check == null );
			
			for( final FsssStateNode<S, A> s : states ) {
				final FsssActionNode<S, A> gan = s.createActionNode( a );
				an.addGroundActionNode( gan );
			}
		}
	}
	
	public void buildSubtree2( final FsssStateNode<S, A> gsn, final FsssAbstractStateNode<S, A> old_asn )
	{
//		if( old_asn.nvisits() == 0 ) {
//			System.out.println( "!\tbuildSubtree2() on unvisited ASN " + old_asn );
//			System.out.println( "\t\t asn: " + old_asn.nsuccessors() + " successors" );
//			System.out.println( "\t\t gsn: " + gsn.nsuccessors() + " successors" );
////			assert( false );
//		}
//		System.out.println( "ASN.buildSubtree2( " + gsn + ", " + old_asn + " )" );
		
		// TODO: Debugging code
//		if( !old_asn.states.contains( gsn ) ) {
//			System.out.println( "! " + old_asn );
//			System.out.println( "! Does not contain " + gsn );
//		}
		
		assert( old_asn.states.contains( gsn ) );
		
		if( isTerminal() ) {
			leaf();
		}
		else {
			visit();
			for( final FsssActionNode<S, A> gan : gsn.successors() ) {
				final FsssAbstractActionNode<S, A> old_succ = old_asn.successor( gan.a() );
				assert( old_succ != null );
				final FsssAbstractActionNode<S, A> aan = requireSuccessor( gan, old_succ );
				aan.buildSubtree2( gan, old_succ );
			}
		}
	}
	
	public FsssAbstractActionNode<S, A> requireSuccessor( final FsssActionNode<S, A> gan, final FsssAbstractActionNode<S, A> old_aan )
	{
		FsssAbstractActionNode<S, A> succ = successors.get( gan.a() );
		if( succ == null ) {
			// FIXME: The 'aggregate' members of the new decision tree should
			// be set here (or somewhere else?)
			final ClassifierRepresenter<S, A> repr = old_aan.repr.emptyInstance();
			succ = new FsssAbstractActionNode<S, A>( this, model, abstraction, gan.a(), repr );
//			successors.put( gan.a(), succ );
//			ordered_successors.add( succ );
			addSuccessor( gan.a(), succ );
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
			final ClassifierRepresenter<S, A> repr = union_a.repr.emptyInstance();
			final FsssAbstractActionNode<S, A> an = new FsssAbstractActionNode<S, A>(
				this, model, abstraction, a, repr );
//			final FsssAbstractActionNode<S, A> check = successors.put( a, an );
//			ordered_successors.add( an );
			final FsssAbstractActionNode<S, A> check = addSuccessor( a, an );
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
	}
	
	public void leaf()
	{
		visit();
		final MeanVarianceAccumulator Ubar = new MeanVarianceAccumulator();
		for( final FsssStateNode<S, A> gsn : states ) {
			gsn.leaf();
			assert( gsn.U() == gsn.L() );
			Ubar.add( gsn.U() );
		}
//		U = L = R.mean();
		U = L = Ubar.mean();
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
