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
