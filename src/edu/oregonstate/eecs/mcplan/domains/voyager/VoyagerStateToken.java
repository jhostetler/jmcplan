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

package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;

public class VoyagerStateToken extends FactoredRepresentation<VoyagerState>
{
	private static final DecimalFormat id_format = new DecimalFormat( "00" );
	private static final DecimalFormat eta_format = new DecimalFormat( "00" );
	private static final DecimalFormat pop_format = new DecimalFormat( "000" );
	
	private static ArrayList<Attribute> attributes_ = null;
	
	public static ArrayList<Attribute> attributes( final int Nplanets, final int max_eta )
	{
		if( attributes_ == null ) {
			attributes_ = new ArrayList<Attribute>();
			final List<String> boolean_ = new ArrayList<String>();
			boolean_.add( "0" );
			boolean_.add( "1" );
			for( int i = 0; i < Nplanets; ++i ) {
				// Owner indicators -- using numeric features, but we could
				// use nominal values.
				for( int j = 0; j < Player.values().length; ++j ) {
//					attributes_.add( new Attribute( "p" + i + "o" + j, boolean_ ) );
					attributes_.add( new Attribute( "p" + i + "o" + j ) );
				}
				// Population
				for( int j = 0; j < Unit.values().length; ++j ) {
					attributes_.add( new Attribute( "p" + i + "pop" + j ) );
				}
				// Stored production
				for( int j = 0; j < Unit.values().length; ++j ) {
					attributes_.add( new Attribute( "p" + i + "prod" + j ) );
				}
			}
			
			// Incoming spaceships
			for( int p = 0; p < Nplanets; ++p ) {
				for( int y = 0; y < Player.Ncompetitors; ++y ) {
					for( int t = 0; t <= max_eta; ++t ) {
						for( int u = 0; u < Unit.values().length; ++u ) {
							attributes_.add( new Attribute( "p" + p + "y" + y + "t" + t + "u" + u ) );
						}
					}
				}
			}
			
			attributes_.add( new Attribute( "similar", boolean_ ) );
		}
		
		return attributes_;
	}
	
	public final long zobrist_hash;
	public final String repr;
	private final int hash_code_;
	
	private final double[] phi_;
	
	public VoyagerStateToken( final VoyagerState s )
	{
		long zhash = 0L;
		final StringBuilder sb = new StringBuilder();
		for( final Planet p : s.planets ) {
			zhash ^= p.zobristHash();
			sb.append( p.repr() );
			sb.append( "|" ); // TODO: debugging
		}
		for( final Spaceship ship : s.spaceships ) {
			zhash ^= ship.zobristHash();
			sb.append( ship.repr() );
			sb.append( "|" ); // TODO: debugging
		}
		zobrist_hash = zhash;
		hash_code_ = new Long( zobrist_hash ).hashCode();
		repr = sb.toString();
		
		phi_ = s.featureVector();
	}
	
	protected VoyagerStateToken( final VoyagerStateToken that )
	{
		zobrist_hash = that.zobrist_hash;
		repr = that.repr;
		hash_code_ = that.hash_code_;
		phi_ = that.phi_;
	}
	
	@Override
	public VoyagerStateToken copy()
	{
		return new VoyagerStateToken( this );
	}
	
//	private void foo( final VoyagerState s, final VoyagerHash h )
//	{
//		assert( Player.values().length < 10 ); // Assuming 1 digit for player IDs
//		assert( EntityType.values().length < 10 ); // Assuming 1 digit for type IDs
//		long zhash = 0L;
//		final StringBuilder sb = new StringBuilder();
//		for( final Planet p : s.planets ) {
//			zhash ^= h.hashOwner( p, p.owner() );
//			sb.append( p.owner().id );
//			zhash ^= h.hashProduction( p, p.nextProduced() );
//			// nextProduced() will be 'null' if owner == Neutral
//			if( p.owner() == Player.Neutral ) {
//				sb.append( EntityType.values().length );
//			}
//			else {
//				sb.append( p.nextProduced().ordinal() );
//			}
//			for( final EntityType type : EntityType.values() ) {
//				final int type_pop = p.population( type );
//				zhash ^= h.hashPopulation( p, type, type_pop );
//				sb.append( pop_format.format( type_pop ) );
//				final int type_stored = p.storedProduction( type );
//				zhash ^= h.hashStoredProduction( p, type, type_stored );
//				sb.append( pop_format.format( type_stored ) );
//			}
//			sb.append( "|" ); // TODO: debugging
//		}
//		for( final Spaceship ship : s.spaceships ) {
//			sb.append( eta_format.format( ship.arrival_time ) )
//			  .append( id_format.format( ship.src.id ) )
//			  .append( id_format.format( ship.dest.id ) )
//			  .append( ship.owner.id );
//			for( final EntityType type : EntityType.values() ) {
//				zhash ^= h.hashSpaceship( ship.src, ship.dest, ship.arrival_time,
//										  ship.owner, type, ship.population[type.ordinal()] );
//				sb.append( pop_format.format( ship.population[type.ordinal()] ) );
//			}
//			sb.append( "|" ); // TODO: debugging
//		}
//	}
	
	@Override
	public double[] phi()
	{
		return phi_;
	}
	
	@Override
	public int hashCode()
	{
		return hash_code_;
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof VoyagerStateToken) ) {
			return false;
		}
		final VoyagerStateToken that = (VoyagerStateToken) obj;
		if( zobrist_hash != that.zobrist_hash ) {
			return false;
		}
		else {
			return repr.equals( that.repr );
		}
	}
	
	@Override
	public String toString()
	{
		return repr;
	}
}
