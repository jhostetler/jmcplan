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
 * FIXME: It seems silly to keep track of average reward here, when you're already
 * storing the GANs. You could just average over GAN L/U and not worry about
 * adding the rewards every time.
 * 
 * @author jhostetler
 *
 */
public class FsssAbstractActionNode<S extends State, A extends VirtualConstructor<A>>
{
	private static final ch.qos.logback.classic.Logger Log = LoggerManager.getLogger( "log.search" );
	
	public final int depth;
	public final FsssAbstractStateNode<S, A> predecessor;
	private final FsssModel<S, A> model;
	private final FsssAbstraction<S, A> abstraction;
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
	
//	private final Map<Representation<S>, FsssAbstractStateNode<S, A>> successors
//		= new HashMap<Representation<S>, FsssAbstractStateNode<S, A>>();
	
	/**
	 * This list is used to provide a deterministic iteration order for the
	 * successors
	 */
//	private final ArrayList<FsssAbstractStateNode<S, A>> ordered_successors
//		= new ArrayList<FsssAbstractStateNode<S, A>>();
	
	public FsssAbstractActionNode( final FsssAbstractStateNode<S, A> predecessor,
								   final FsssModel<S, A> model, final FsssAbstraction<S, A> abstraction,
								   final A a, final ClassifierRepresenter<S, A> repr )
	{
		this.depth = predecessor.depth;
		this.predecessor = predecessor;
		this.model = model;
		this.abstraction = abstraction;
		this.a = a;
		this.repr = repr;
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
//		return Ubar.variance();
		final MeanVarianceAccumulator acc = new MeanVarianceAccumulator();
		for( final FsssActionNode<S, A> gan : actions ) {
			acc.add( gan.U() );
		}
		return acc.variance();
	}
	
	public double Lvar()
	{
//		return Lbar.variance();
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
			}
			
			assert( b >= 0 );
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
		if( N > 0 ) {
			U += model.discount() * u / N;
			L += model.discount() * l / N;
		}
//		else {
//			U = R.mean() + model.discount()*model.Vmax();
//			L = R.mean() + model.discount()*model.Vmin();
//		}
		
		// Note: I originally considered it an error if any GANs had no
		// successors. However, that can happen if sampling is stopped early.
		for( final FsssActionNode<S, A> gan : actions ) {
			if( gan.nsuccessors() > 0 ) {
				gan.backup();
			}
			
//			// TODO: Debugging code
//			if( gan.nsuccessors() == 0 ) {
//				FsssTest.printTree( FsssTest.findRoot( this ), System.out, 1 );
//				System.out.println( "! " + this );
//				System.exit( 0 );
//			}
		}
	}
	
	public Iterable<FsssAbstractStateNode<S, A>> successors()
	{
//		return ordered_successors;
		
		return Fn.in( repr.classes() );
	}
	
//	protected FsssAbstractStateNode<S, A> successor( final Representation<S> x )
//	{
//		return successors.get( x );
//	}
	
	protected FsssAbstractStateNode<S, A> successor( final S s )
	{
		return repr.classify( s ).aggregate;
	}
	
	public int nsuccessors()
	{
//		return successors.size();
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
	
	/**
	 * Adds a successor node mapping 'a => aan' to both the successor map
	 * and the ordered successor list. Returns the previous mapping. No change
	 * is made to 'ordered_successors' if 'aan' was already in 'successors'.
	 * @param a
	 * @param aan
	 * @return
	 */
//	private FsssAbstractStateNode<S, A> addSuccessor( final Representation<S> x, final FsssAbstractStateNode<S, A> asn )
//	{
//		final FsssAbstractStateNode<S, A> previous = successors.put( x, asn );
//		if( previous == null ) {
//			ordered_successors.add( asn );
//		}
//		else {
//			if( previous != asn ) {
//				System.out.println( "\t! Mapping exists for " + x + ": " );
//				System.out.println( "\t\t" + previous );
//				System.out.println( "\t\t" + asn );
//			}
//			assert( previous == asn );
//		}
//		return previous;
//	}
	
	public void sample( final int width, final Budget budget )
	{
		while( n < width ) {
//			if( model.sampleCount() >= max_samples ) {
			if( budget.isExceeded() ) {
//				System.out.println( "! AAN.upSample(): terminating " + model.sampleCount() + " / " + max_samples );
//
//				FsssTest.printTree( FsssTest.findRoot( this ), System.out, 1 );
//				System.out.println( "! " + this );
//				System.exit(  0  );
				
				break;
			}
			
			final int Na = (int) Math.ceil( width / (double) actions.size() );
			assert( Na > 0 );
			assert( Na <= width );
			int N = 0;
			for( final FsssActionNode<S, A> gan : actions ) {
				if( gan.nsuccessors() < Na ) {
					final FsssStateNode<S, A> gsn = gan.sample();
					
//					final FsssAbstractStateNode<S, A> encoded = repr.addTrainingSample( this, gsn.s() );
//					FsssAbstractStateNode<S, A> asn = successors.get( encoded.x() );
//					if( asn == null ) {
//						asn = encoded;
//						addSuccessor( encoded.x(), asn );
//					}
					
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
//				System.out.println( "! AAN.upSample(): terminating " + model.sampleCount() + " / " + max_samples );
				break;
			}
			
//			System.out.println( "\tAAN @" + hashCode() + ": n = " + n );
			final int Na = (int) Math.ceil( width / (double) actions.size() );
			assert( Na > 0 );
			assert( Na <= width );
			int N = 0;
			for( final FsssActionNode<S, A> gan : actions ) {
				if( gan.nsuccessors() < Na ) {
					final FsssStateNode<S, A> gsn = gan.sample();
					
//					final FsssAbstractStateNode<S, A> encoded = repr.addTrainingSample( this, gsn.s() );
//					FsssAbstractStateNode<S, A> asn = successors.get( encoded.x() );
//					if( asn == null ) {
//						asn = encoded;
//						addSuccessor( encoded.x(), asn );
//					}
					
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
	
	public void splitSuccessor( final FsssAbstractStateNode<S, A> sn, final ArrayList<FsssAbstractStateNode<S, A>> parts )
	{
//		final FsssAbstractStateNode<S, A> check = successors.remove( sn.x() );
//		assert( sn == check );
//		ordered_successors.remove( sn );
		
		for( final FsssAbstractStateNode<S, A> p : parts ) {
			
			// This has already been accomplished by the caller (RefineablePartitionTreeRepresenter.doSplit())
//			addSuccessor( p.x(), p );
			
			for( final FsssStateNode<S, A> gsn : p.states() ) {
				p.buildSubtree2( gsn, sn );
			}
		}
	}
	
	public void buildSubtree2( final FsssActionNode<S, A> gan, final FsssAbstractActionNode<S, A> old_aan )
	{
//		System.out.println( "AAN.buildSubtree2( " + gan + ", " + old_aan + " )" );
		assert( old_aan.actions.contains( gan ) );
//		System.out.println( "\tbuildSubtree2(): " + old_aan );
		for( final FsssStateNode<S, A> gsn : gan.successors() ) {
//			System.out.println( "\tbuildSubtree2(): gsn " + gsn );
			final FsssAbstractStateNode<S, A> asn = requireSuccessor( gsn );
			
//			final Representation<S> x = old_aan.repr.encode( gsn.s() );
//			final FsssAbstractStateNode<S, A> old_succ = old_aan.successor( x );
			
			final FsssAbstractStateNode<S, A> old_succ = old_aan.successor( gsn.s() );
			
			if( old_succ == null ) {
				System.out.println( "\t! old_aan = " + old_aan );
//				System.out.println( "\t! x = " + x );
				System.out.println( "\t! gsn = " + gsn );
				System.out.println( "\t\t old_succ = null" );
			}
			if( old_succ.nvisits() > 0 ) {
//				asn.visit();
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
		
//		final FsssAbstractStateNode<S, A> check = successors.put( asn.x(), asn );
//		if( check == null ) {
//			ordered_successors.add( asn );
//		}
		
		// TODO: Why is it OK if the successor already exists?
//		final FsssAbstractStateNode<S, A> check = addSuccessor( asn.x(), asn );
//		assert( check == asn || check == null );
		
		return asn;
	}
	
//	public void leaf()
//	{
////		L = R.mean();
////		U = L;
//
//		R = new MeanVarianceAccumulator();
//		for( final FsssActionNode<S, A> gan : actions ) {
//			gan.leaf();
//			R.add( gan.r );
//		}
//		L = U = R.mean();
//	}
}
