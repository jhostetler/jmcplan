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
package edu.oregonstate.eecs.mcplan.domains.voyager.experiments;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.domains.voyager.PlanetId;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerInstance;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;
import edu.oregonstate.eecs.mcplan.domains.voyager.policies.AggressivePolicy;
import edu.oregonstate.eecs.mcplan.domains.voyager.policies.BalancedPolicy;
import edu.oregonstate.eecs.mcplan.domains.voyager.policies.ExpansionPolicy;
import edu.oregonstate.eecs.mcplan.domains.voyager.policies.FortifyPolicy;

/**
 * @author jhostetler
 *
 */
public class SpecialistPolicyGenerator
	extends ActionGenerator<VoyagerState, Policy<VoyagerState, VoyagerAction>>
{
	private final VoyagerInstance instance_;
	private final Player player_;
	
	private final List<Policy<VoyagerState, VoyagerAction>> actions_
		= new ArrayList<Policy<VoyagerState, VoyagerAction>>();
	private ListIterator<Policy<VoyagerState, VoyagerAction>> itr_ = null;
	
	public SpecialistPolicyGenerator( final VoyagerInstance instance, final Player player )
	{
		instance_ = instance;
		player_ = player;
	}
	
	@Override
	public ActionGenerator<VoyagerState, Policy<VoyagerState, VoyagerAction>> create()
	{
		return new SpecialistPolicyGenerator( instance_, player_ );
	}

	@Override
	public void setState( final VoyagerState s, final long t, final int[] turn )
	{
		actions_.clear();
		
		actions_.add( new ExpansionPolicy( player_,
			new int[] { PlanetId.Natural_2nd( player_ ) }, 2 ) );
		actions_.add( new ExpansionPolicy( player_,
			new int[] { PlanetId.Natural_2nd( player_ ), PlanetId.Natural_3rd( player_ ) }, 2 ) );
		actions_.add( new ExpansionPolicy( player_,
			new int[] { PlanetId.Natural_2nd( player_ ), PlanetId.Natural_3rd( player_ ), PlanetId.Wing( player_ ) }, 2 ) );
		actions_.add( new FortifyPolicy( player_, new int[] { PlanetId.Center }, 2, 2 ) );
		actions_.add( new FortifyPolicy( player_, new int[] { PlanetId.Wing( player_ ) }, 2, 2 ) );
		actions_.add( new FortifyPolicy( player_, new int[] { PlanetId.Wing( player_.enemy() ) }, 2, 2 ) );
		actions_.add( new AggressivePolicy( player_, 2 ) );
		actions_.add( new BalancedPolicy( player_, instance_.nextSeed(), 0.75, 1.25, 0.2 ) );
		
		itr_ = actions_.listIterator();
	}

	@Override
	public int size()
	{
		return actions_.size();
	}

	@Override
	public boolean hasNext()
	{
		return itr_.hasNext();
	}

	@Override
	public Policy<VoyagerState, VoyagerAction> next()
	{
		return itr_.next();
	}

}
