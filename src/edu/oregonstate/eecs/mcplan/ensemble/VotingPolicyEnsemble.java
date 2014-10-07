/**
 * 
 */
package edu.oregonstate.eecs.mcplan.ensemble;

import java.util.ArrayList;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.Policy;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * Voted ensemble of policies with random tie-breaking.
 */
public class VotingPolicyEnsemble<S, A> extends Policy<S, A>
{
	private final RandomGenerator rng;
	private final ArrayList<Policy<S, A>> Pi;
	
	public VotingPolicyEnsemble( final RandomGenerator rng, final ArrayList<Policy<S, A>> Pi )
	{
		this.rng = rng;
		this.Pi = Pi;
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		for( final Policy<S, A> pi : Pi ) {
			pi.setState( s, t );
		}
	}

	@Override
	public A getAction()
	{
		final TObjectIntMap<A> counts = new TObjectIntHashMap<A>();
		for( final Policy<S, A> pi : Pi ) {
			final A a = pi.getAction();
			counts.adjustOrPutValue( a, 1, 1 );
		}
		
		int max_count = 0;
		final ArrayList<A> candidates = new ArrayList<A>();
		for( final TObjectIntIterator<A> itr = counts.iterator(); itr.hasNext(); ) {
			itr.advance();
//			System.out.println( "\t" + itr.key() + " -> " + itr.value() );
			final int c = itr.value();
			if( c > max_count ) {
				candidates.clear();
				max_count = c;
				candidates.add( itr.key() );
			}
			else if( c == max_count ) {
				candidates.add( itr.key() );
			}
		}
		
		if( candidates.size() == 1 ) {
			return candidates.get( 0 );
		}
		else {
			return candidates.get( rng.nextInt( candidates.size() ) );
		}
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		for( final Policy<S, A> pi : Pi ) {
			pi.actionResult( sprime, r );
		}
	}

	@Override
	public String getName()
	{
		return "VotingPolicyEnsemble";
	}

	@Override
	public int hashCode()
	{
		final HashCodeBuilder hb = new HashCodeBuilder( 5, 11 );
		for( final Policy<S, A> pi : Pi ) {
			hb.append( pi );
		}
		return hb.toHashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof VotingPolicyEnsemble<?, ?>) ) {
			return false;
		}
		@SuppressWarnings( "unchecked" )
		final VotingPolicyEnsemble<S, A> that = (VotingPolicyEnsemble<S, A>) obj;
		if( Pi.size() != that.Pi.size() ) {
			return false;
		}
		for( int i = 0; i < Pi.size(); ++i ) {
			if( !Pi.get( i ).equals( that.Pi.get( i ) ) ) {
				return false;
			}
		}
		return true;
	}

}
