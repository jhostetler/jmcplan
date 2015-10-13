package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * A state node in an Abstract FSSS tree.
 * @param <S>
 * @param <A>
 */
public final class FsssAbstractStateNode<S extends State, A extends VirtualConstructor<A>> implements AutoCloseable
{
	private static final ch.qos.logback.classic.Logger Log = LoggerManager.getLogger( "log.search" );
	
	public final int depth;
	public final FsssAbstractActionNode<S, A> predecessor;
	private final FsssModel<S, A> model;
	private final FsssAbstraction<S, A> abstraction;
	private final Representation<S> x;
	private final ArrayList<FsssStateNode<S, A>> states = new ArrayList<FsssStateNode<S, A>>();
	private int n = 0;
	private int nvisits = 0;
	
	private final MeanVarianceAccumulator R = new MeanVarianceAccumulator();
	
	private double U = Double.NaN;
	private double L = Double.NaN;
	
	private final MeanVarianceAccumulator Ubar = new MeanVarianceAccumulator();
	private final MeanVarianceAccumulator Lbar = new MeanVarianceAccumulator();
	
	private boolean backed_up = false;
	private boolean pure = true;
	private boolean freed = false;
	private boolean terminal = false;
	
	private final Map<A, FsssAbstractActionNode<S, A>> successors
		= new HashMap<A, FsssAbstractActionNode<S, A>>();
	
	/**
	 * This list is used to provide a deterministic iteration order for the
	 * successors.
	 * FIXME: You've rolled your own LinkedHashMap
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
	
	@Override
	public void close()
	{
		Log.debug( "close(): {}", this );
		
		if( freed ) {
			throw new IllegalStateException( "already closed" );
		}
		
		for( final FsssStateNode<S, A> sn : states ) {
			sn.close();
		}
		states.clear();
		states.trimToSize();
		for( final FsssAbstractActionNode<S, A> aan : ordered_successors ) {
			aan.close();
		}
		
		freed = true;
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
			Log.error( "! {}", this );
			throw new IllegalStateException( "Adding successor to terminal ASN" );
		}
		final FsssAbstractActionNode<S, A> previous = successors.put( a, aan );
		if( previous == null ) {
			ordered_successors.add( aan );
		}
		return previous;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "[@" ).append( Integer.toHexString( System.identityHashCode( this ) ) )
		  .append( ": " ).append( x );
		if( isClosed() ) {
			sb.append( " (CLOSED)" );
		}
		if( isExpanded() ) {
			sb.append( " (" ).append( isTerminal() ? "terminal" : "non-terminal" ).append( ")" );
		}
		else {
			sb.append( " (not expanded)" );
		}
		sb.append( "; nvisits: " ).append( nvisits() )
		  .append( "; n: " ).append( n() )
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
		assert( !isClosed() );
		
		// Can't mix terminal and non-terminal GSNs.
		if( terminal && !gsn.isTerminal() ) {
			FsssTest.printTree( FsssTest.findRoot( this ), System.out, 1 );
			
			Log.error( "! Adding {} gsn to {} asn", (gsn.isTerminal() ? "terminal" : "non-terminal"),
						(isTerminal() ? "terminal" : "non-terminal") );
			Log.error( "!\tGSN: {}", gsn );
			Log.error( "!\tASN: {}", this );
			
			throw new IllegalArgumentException(
				"Adding " + (gsn.isTerminal() ? "terminal" : "non-terminal")
				+ " gsn to " + (isTerminal() ? "terminal" : "non-terminal") + " asn" );
		}
		
		states.add( gsn );
		R.add( gsn.r );
		Ubar.add( gsn.U() );
		Lbar.add( gsn.L() );
		U = Ubar.mean();
		L = Lbar.mean();
		n = states.size();
		
		if( gsn.isTerminal() ) {
			terminal = true;
		}
		
		if( pure && !gsn.x().equals( states.get( 0 ).x() ) ) {
			assert( states.size() > 1 ); // We didn't just compare the state to itself
			pure = false;
		}
	}
	
	public boolean isExpanded()
	{
		return nvisits() > 0;
	}
	
	public boolean isPure()
	{
		return pure;
	}
	
	public boolean isReadyToClose()
	{
		return isExpanded() && (isTerminal() || isPure());
	}
	
	public boolean isActive()
	{
		return isExpanded() && !isTerminal() && !isPure();
	}
	
	public boolean isClosed()
	{
		return freed;
	}
	
	/**
	 * Number of constituent ground states.
	 * @return
	 */
	public int n()
	{
		return n;
	}
	
	public int nvisits()
	{
		return nvisits;
	}
	
	public void visit()
	{
		nvisits += 1;
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
	
	/**
	 * Returns all actions that achieve the maximum value of L(s, a).
	 * @return
	 */
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
		return ordered_successors;
	}
	
	public FsssAbstractActionNode<S, A> successor( final A a )
	{
		return successors.get( a );
	}
	
	public void expand( final Iterable<A> actions, final int width, final Budget budget )
	{
		createActionNodes( actions );
		sample( width, budget );
		visit();
	}
	
	public void sample( final int width, final Budget budget )
	{
		assert( !isClosed() );
		for( final FsssAbstractActionNode<S, A> an : successors() ) {
			an.sample( width, budget );
		}
	}
	
	/**
	 * Creates GAN successors for each non-terminal GSN in 'added', and ensures
	 * that corresponding AANs exist.
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
						aan = new FsssAbstractActionNode<S, A>(
							this, model, abstraction, gan.a(), abstraction.createRepresenter() );
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
			final FsssAbstractActionNode<S, A> check = addSuccessor( a, an );
			
			if( check != null ) {
				Log.error( "! {}: child for {} already exists {}", this, a, check );
				FsssTest.printTree( this, System.out, 1 );
				throw new IllegalStateException( "Duplicate action node successor" );
			}
			
			for( final FsssStateNode<S, A> s : states ) {
				final FsssActionNode<S, A> gan = s.createActionNode( a );
				an.addGroundActionNode( gan );
			}
		}
	}
	
	public void buildSubtree2( final FsssStateNode<S, A> gsn, final FsssAbstractStateNode<S, A> old_asn )
	{
		Log.trace( "ASN.buildSubtree2( {}, {} )", gsn, old_asn );
		
		// TODO: Debugging code
		if( !old_asn.states.contains( gsn ) ) {
			Log.error( "! {}", old_asn );
			Log.error( "! Does not contain {}", gsn );
			
			FsssTest.printAncestorChain( old_asn );
			FsssTest.printTree( FsssTest.findRoot( old_asn.predecessor ), System.out, 1 );
			
			throw new IllegalStateException( "gsn not in old_asn" );
		}
		
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
			// Create empty AAN successor with duplicated repr from 'old_aan'
			final ClassifierRepresenter<S, A> repr = old_aan.repr.emptyInstance();
			succ = new FsssAbstractActionNode<S, A>( this, model, abstraction, gan.a(), repr );
			addSuccessor( gan.a(), succ );
		}
		succ.addGroundActionNode( gan );
		return succ;
	}
	
	public void leaf()
	{
		assert( !isClosed() );
		visit();
		final MeanVarianceAccumulator Ubar = new MeanVarianceAccumulator();
		for( final FsssStateNode<S, A> gsn : states ) {
			gsn.leaf();
			assert( gsn.U() == gsn.L() );
			Ubar.add( gsn.U() );
		}
		U = L = Ubar.mean();
	}

	public boolean isTerminal()
	{
		return depth == 1 || terminal;
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
