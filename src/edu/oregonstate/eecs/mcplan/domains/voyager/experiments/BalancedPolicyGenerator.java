package edu.oregonstate.eecs.mcplan.domains.voyager.experiments;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerInstance;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerParameters;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;
import edu.oregonstate.eecs.mcplan.domains.voyager.policies.BalancedPolicy;

public final class BalancedPolicyGenerator
	extends ActionGenerator<VoyagerState, AnytimePolicy<VoyagerState, UndoableAction<VoyagerState>>>
{
	private final VoyagerParameters params_;
	private final VoyagerInstance instance_;
	private final Player player_;
	
	private final List<AnytimePolicy<VoyagerState, UndoableAction<VoyagerState>>> actions_
		= new ArrayList<AnytimePolicy<VoyagerState, UndoableAction<VoyagerState>>>();
	private ListIterator<AnytimePolicy<VoyagerState, UndoableAction<VoyagerState>>> itr_ = null;
	
	public BalancedPolicyGenerator( final VoyagerParameters params, final VoyagerInstance instance, final Player player )
	{
		params_ = params;
		instance_ = instance;
		player_ = player;
	}
	
	@Override
	public boolean hasNext()
	{
		return itr_.hasNext();
	}

	@Override
	public AnytimePolicy<VoyagerState, UndoableAction<VoyagerState>> next()
	{
		return itr_.next();
	}

	@Override
	public ActionGenerator<VoyagerState, AnytimePolicy<VoyagerState, UndoableAction<VoyagerState>>> create()
	{
		return new BalancedPolicyGenerator( params_, instance_, player_ );
	}

	@Override
	public void setState( final VoyagerState s, final long t, final int turn )
	{
		actions_.clear();
		
		final List<AnytimePolicy<VoyagerState, UndoableAction<VoyagerState>>> policies
			= new ArrayList<AnytimePolicy<VoyagerState, UndoableAction<VoyagerState>>>();
		policies.add( new BalancedPolicy( player_, instance_.nextSeed(), 0.5, 1.0, 0.1 ) );
		policies.add( new BalancedPolicy( player_, instance_.nextSeed(), 0.75, 1.25, 0.2 ) );
		policies.add( new BalancedPolicy( player_, instance_.nextSeed(), 1.0, 1.5, 0.2 ) );
		
		actions_.addAll( policies );
		itr_ = actions_.listIterator();
	}

	@Override
	public int size()
	{
		return actions_.size();
	}
}