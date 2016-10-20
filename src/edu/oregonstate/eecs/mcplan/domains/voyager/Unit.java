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

public enum Unit
{
	Worker( 20, 1, 6 ),
	Soldier( 20, 2, 6 );
	// Add types here. Don't forget to update 'max_hp' below!
	
	// TODO: Must keep this synchronized! No way to derive it due to Java
	// rules about static members in enums.
	public static final int max_hp = 6;
	
	// -----------------------------------------------------------------------
	
	public static Unit defaultProduction()
	{
		return Worker;
	}
	
	// -----------------------------------------------------------------------
	
	private final int cost_;
	private final int attack_;
	private final int hp_;
	
	private Unit( final int cost, final int attack, final int hp )
	{
		cost_ = cost;
		attack_ = attack;
		hp_ = hp;
	}
	
	public int cost()
	{
		return cost_;
	}
	
	public int attack( final Unit u )
	{
		// NOTE: No differential matchups currently
		return attack_;
	}
	
	public int hp()
	{
		return hp_;
	}
}
