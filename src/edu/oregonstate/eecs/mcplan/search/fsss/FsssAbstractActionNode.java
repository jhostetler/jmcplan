/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public class FsssAbstractActionNode<S extends State, A extends VirtualConstructor<A>>
{
	public final int depth;
	public final FsssAbstractStateNode<S, A> predecessor;
	private final FsssModel<S, A> model;
	private final FsssAbstraction<S, A> abstraction;
	private final A a;
	public final RefineablePartitionTreeRepresenter<S, A> repr;
//	public final Representer<S, ? extends Representation<S>> repr;
	
	public final ArrayList<FsssActionNode<S, A>> actions = new ArrayList<FsssActionNode<S, A>>();
	private final MeanVarianceAccumulator R = new MeanVarianceAccumulator();
	
	private double U;
	private double L;
	
	private int n = 0;
	
	private final Map<Representation<S>, FsssAbstractStateNode<S, A>> successors
		= new HashMap<Representation<S>, FsssAbstractStateNode<S, A>>();
	
	public FsssAbstractActionNode( final FsssAbstractStateNode<S, A> predecessor,
								   final FsssModel<S, A> model, final FsssAbstraction<S, A> abstraction,
								   final A a, final Representer<S, ? extends Representation<S>> repr )
	{
		this.depth = predecessor.depth;
		this.predecessor = predecessor;
		this.model = model;
		this.abstraction = abstraction;
		this.a = a;
		this.repr = (RefineablePartitionTreeRepresenter<S, A>) repr;
		this.U = model.Vmax();
		this.L = model.Vmin();
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
		
		assert( sstar != null );
		return sstar;
	}
	
	public void backup()
	{
		assert( nsuccessors() > 0 );
		double u = 0;
		double l = 0;
		int N = 0;
		for( final FsssAbstractStateNode<S, A> sn : successors() ) {
			final int n = sn.n();
			N += n;
			u += n * sn.U();
			l += n * sn.L();
		}
		U = R.mean() + (N > 0 ? model.discount() * u / N : 0);
		L = R.mean() + (N > 0 ? model.discount() * l / N : 0);
		
		for( final FsssActionNode<S, A> gan : actions ) {
			gan.backup();
		}
	}
	
	public Iterable<FsssAbstractStateNode<S, A>> successors()
	{
		return successors.values();
	}
	
	public FsssAbstractStateNode<S, A> successor( final Representation<S> x )
	{
		return successors.get( x );
	}
	
	public int nsuccessors()
	{
		return successors.size();
	}
	
	public void addGroundActionNode( final FsssActionNode<S, A> gan )
	{
		assert( gan.a().equals( this.a() ) );
		actions.add( gan );
		R.add( gan.r );
	}
	
	public void sample( final int width )
	{
		while( n < width ) {
			int N = 0;
			for( final FsssActionNode<S, A> gan : actions ) {
				final FsssStateNode<S, A> gsn = gan.sample();
				final RefineablePartitionTreeRepresenter<S, A>.DataNode dn
					= repr.addTrainingSample( this, gsn.s(), model.base_repr().encode( gsn.s() ) );
				FsssAbstractStateNode<S, A> sn = successors.get( dn.aggregate.x() );
				if( sn == null ) {
					sn = dn.aggregate;
					successors.put( dn.aggregate.x(), sn );
				}
				sn.addGroundStateNode( gsn );
				N += gan.nsuccessors();
			}
			n = N;
		}
	}
	
	public Map<FsssAbstractStateNode<S, A>, ArrayList<FsssStateNode<S, A>>> upSample( final int width )
	{
		final Map<FsssAbstractStateNode<S, A>, ArrayList<FsssStateNode<S, A>>> added
			= new HashMap<FsssAbstractStateNode<S, A>, ArrayList<FsssStateNode<S, A>>>();
		while( n < width ) {
//			System.out.println( "\tAAN @" + hashCode() + ": n = " + n );
			int N = 0;
			for( final FsssActionNode<S, A> gan : actions ) {
				// Sample ground successor
				final FsssStateNode<S, A> gsn = gan.sample();
				// Get abstract state
				final RefineablePartitionTreeRepresenter<S, A>.DataNode dn
					= repr.addTrainingSample( this, gsn.s(), model.base_repr().encode( gsn.s() ) );
				// Ensure ASN successor
				FsssAbstractStateNode<S, A> sn = successors.get( dn.aggregate.x() );
				if( sn == null ) {
					sn = dn.aggregate;
					successors.put( dn.aggregate.x(), sn );
				}
				// Add ground state to ASN successor
				sn.addGroundStateNode( gsn );
				
				ArrayList<FsssStateNode<S, A>> sn_added = added.get( sn );
				if( sn_added == null ) {
					sn_added = new ArrayList<FsssStateNode<S, A>>();
					added.put( sn, sn_added );
				}
				sn_added.add( gsn );
				
				N += gan.nsuccessors();
			}
			n = N;
		}
		return added;
	}
	
	public void splitSuccessor( final FsssAbstractStateNode<S, A> sn, final ArrayList<FsssAbstractStateNode<S, A>> parts )
	{
		final FsssAbstractStateNode<S, A> check = successors.remove( sn.x() );
		assert( sn == check );
		
		for( final FsssAbstractStateNode<S, A> p : parts ) {
			successors.put( p.x(), p );
			for( final FsssStateNode<S, A> gsn : p.states() ) {
				assert( sn.states().contains( gsn ) );
				p.buildSubtree2( gsn, sn );
			}
		}
	}
	
	public void buildSubtree2( final FsssActionNode<S, A> gan, final FsssAbstractActionNode<S, A> old_aan )
	{
//		System.out.println( "AAN.buildSubtree2( " + gan + ", " + old_aan + " )" );
		assert( old_aan.actions.contains( gan ) );
		for( final FsssStateNode<S, A> gsn : gan.successors() ) {
			final FsssAbstractStateNode<S, A> asn = requireSuccessor( gsn );
			final FsssAbstractStateNode<S, A> old_succ = old_aan.successor( old_aan.repr.encode( gsn.s() ) );
			if( old_succ.nvisits() > 0 ) {
				asn.visit();
				asn.buildSubtree2( gsn, old_succ );
			}
		}
		
		// TODO: Debugging code
//		boolean null_leaf = false;
//		for( final RefineablePartitionTreeRepresenter<S, A>.DataNode dn : repr.dt_leaves ) {
//			if( dn.aggregate == null ) {
//				null_leaf = true;
//				break;
//			}
//		}
//		if( null_leaf ) {
//			System.out.println( "!\trepr leaf is null:" );
//			for( final RefineablePartitionTreeRepresenter<S, A>.DataNode dn : repr.dt_leaves ) {
//				System.out.println( "\t\t" + dn.aggregate );
//			}
//		}
	}
	
	public void buildSubtree( final FsssAbstractActionNode<S, A> union )
	{
		System.out.println( "AAN.buildSubtree( " + union + " )" );
		for( final FsssActionNode<S, A> gan : actions ) {
			for( final FsssStateNode<S, A> gsn : gan.successors() ) {
				requireSuccessor( gsn );
			}
		}
		
		for( final FsssAbstractStateNode<S, A> sn : successors() ) {
//			sn.visit();
			
			// FIXME: sn.x() is an IndexRepresentation created by the repr for
			// 'this'. This representation is not necessarily compatible with
			// 'union', but it will accidentally work most of the time.
			final FsssAbstractStateNode<S, A> usucc = union.successor( sn.x() );
			sn.buildSubtree( union.successor( sn.x() ) );
		}
		
//		backup();
	}
	
	public FsssAbstractStateNode<S, A> requireSuccessor( final FsssStateNode<S, A> gsn )
	{
		final RefineablePartitionTreeRepresenter<S, A>.DataNode dn = repr.addTrainingSampleAsExistingNode( this, gsn );
		dn.aggregate.addGroundStateNode( gsn );
		
		final FsssAbstractStateNode<S, A> check = successors.put( dn.aggregate.x(), dn.aggregate );
		assert( check == dn.aggregate || check == null );
		return dn.aggregate;
	}
	
	public void leaf()
	{
		L = R.mean();
		U = L;
		
		for( final FsssActionNode<S, A> gan : actions ) {
			gan.leaf();
		}
	}
}
