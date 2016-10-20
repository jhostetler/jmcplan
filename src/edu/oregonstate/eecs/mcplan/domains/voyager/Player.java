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

/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

/**
 * @author jhostetler
 *
 */
public enum Player
{
	Min, Max, Neutral;
	
	// -----------------------------------------------------------------------
	
	public static final int Ncompetitors;
	public static final Player[] competitors;
	
	static {
		Ncompetitors = values().length - 1;
		competitors = new Player[Ncompetitors];
		for( int i = 0; i < Ncompetitors; ++i ) {
			final Player p = values()[i];
			assert( p != Neutral );
			competitors[i] = p;
		}
	}
	
	public final int id;
	
	private final String repr_;
	
	private Player()
	{
		this.id = this.ordinal();
		repr_ = "Player" + id;
	}
	
	public Player enemy()
	{
		switch( this ) {
			case Min: return Max;
			case Max: return Min;
			case Neutral: return null;
			default: throw new AssertionError();
		}
	}
	
	@Override
	public String toString()
	{
		return repr_;
	}
}
