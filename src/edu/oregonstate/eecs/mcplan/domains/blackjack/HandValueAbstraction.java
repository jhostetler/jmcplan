/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import java.util.ArrayList;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;

/**
 * @author jhostetler
 *
 */
public class HandValueAbstraction extends FactoredRepresentation<BlackjackState>
{
	public final int dealer_value;
	public final int[] player_values;
	public final boolean[] player_aces;
	
	private final float[] phi_;
	
	private final BlackjackParameters params_;
	
	private final int hash_code_;
	private final String repr_;
	
	public HandValueAbstraction( final BlackjackState s )
	{
		assert( s.nplayers() == 1 );
		dealer_value = s.dealerUpcard().BlackjackValue();
		player_values = new int[s.nplayers()];
		player_aces = new boolean[s.nplayers()];
		params_ = s.parameters();
		for( int i = 0; i < s.nplayers(); ++i ) {
			final int[] v = params_.handValue( s.hand( i ) );
			player_values[i] = v[0];
			player_aces[i] = v[1] > 0;
		}
		
		phi_ = makePhi( params_, player_values[0], player_aces[0] ? 1 : 0, dealer_value );
		
		final HashCodeBuilder hb = newHashCodeBuilder();
		final StringBuilder sb = new StringBuilder();
		makeRepr( hb, sb );
		hash_code_ = hb.toHashCode();
		
		// TODO: Debugging
//		sb.append( "\n" ).append( new BlackjackStateToken( s ).toString() );
		repr_ = sb.toString();
	}
	
	public HandValueAbstraction( final int dealer_value, final int[] player_values,
								 final boolean[] player_aces, final BlackjackParameters params )
	{
		assert( player_values.length == 1 );
		this.dealer_value = dealer_value;
		this.player_values = player_values;
		this.player_aces = player_aces;
		phi_ = makePhi( params, player_values[0], player_aces[0] ? 1 : 0, dealer_value );
		params_ = params;
		final HashCodeBuilder hb = newHashCodeBuilder();
		final StringBuilder sb = new StringBuilder();
		makeRepr( hb, sb );
		hash_code_ = hb.toHashCode();
		repr_ = sb.toString();
	}
	
	public static float[] makePhi( final BlackjackParameters params,
									final int player_value, final int player_aces, final int dealer_value )
	{
		final float[] phi = new float[3];
		phi[0] = (player_value <= params.max_score ? player_value : params.busted_score);
		phi[1] = player_aces;
		phi[2] = (dealer_value <= params.max_score ? dealer_value : params.busted_score);
		return phi;
	}
	
	public static ArrayList<Attribute> makeAttributes( final BlackjackParameters params )
	{
		final ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add( new Attribute( "pv" ) );
		attributes.add( new Attribute( "pa" ) );
		attributes.add( new Attribute( "dv" ) );
		return attributes;
	}
	
	private HashCodeBuilder newHashCodeBuilder()
	{
		return new HashCodeBuilder( 37, 41 );
	}
	
	@Override
	public float[] phi()
	{
		return phi_;
	}
	
	private void makeRepr( final HashCodeBuilder hb, final StringBuilder sb )
	{
		hb.append( (dealer_value > params_.max_score ? params_.busted_score : dealer_value) );
		sb.append( "d:" ).append( dealer_value );
		for( int i = 0; i < player_values.length; ++i ) {
			final int v = player_values[i];
			if( v > params_.max_score ) {
				hb.append( params_.busted_score );
				hb.append( false );
			}
			else {
				hb.append( v );
				hb.append( player_aces[i] );
			}
			sb.append( ", " ).append( i ).append( ":" ).append( player_values[i] )
			  .append( " " ).append( player_aces[i] );
		}
	}
	
	private HandValueAbstraction( final HandValueAbstraction that )
	{
		dealer_value = that.dealer_value;
		player_values = that.player_values;
		player_aces = that.player_aces;
		params_ = that.params_;
		hash_code_ = that.hash_code_;
		repr_ = that.repr_;
		phi_ = that.phi_;
	}

	@Override
	public HandValueAbstraction copy()
	{
		return new HandValueAbstraction( this );
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof HandValueAbstraction) ) {
			return false;
		}
		final HandValueAbstraction that = (HandValueAbstraction) obj;
		if( dealer_value != that.dealer_value
				&& dealer_value <= params_.max_score
				&& that.dealer_value <= params_.max_score ) {
			return false;
		}
		for( int i = 0; i < player_values.length; ++i ) {
			if( player_values[i] > params_.max_score ) {
				if( that.player_values[i] <= params_.max_score ) {
					return false;
				}
			}
			else if( that.player_values[i] > params_.max_score ) {
				if( player_values[i] <= params_.max_score ) {
					return false;
				}
			}
			else {
				if( player_values[i] != that.player_values[i]
					|| player_aces[i] != that.player_aces[i] ) {
					return false;
				}
			}
			
		}
		return true;
	}

	@Override
	public int hashCode()
	{
		return hash_code_;
	}
	
	@Override
	public String toString()
	{
		return repr_;
	}
}
