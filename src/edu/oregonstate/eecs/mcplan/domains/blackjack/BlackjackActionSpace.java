/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.ActionSet;
import edu.oregonstate.eecs.mcplan.ActionSpace;

/**
 * @author jhostetler
 *
 */
public class BlackjackActionSpace extends ActionSpace<BlackjackMdpState, BlackjackAction>
{
	private final ArrayList<BlackjackAction> pass_hit_ = new ArrayList<BlackjackAction>();
	private final ArrayList<BlackjackAction> pass_only_ = new ArrayList<BlackjackAction>();
	
	private BlackjackMdpState s_ = null;
	private final ArrayList<BlackjackAction> actions_ = null;
	
	private final BlackjackParameters params_;
	
	public BlackjackActionSpace( final BlackjackParameters params )
	{
		pass_hit_.add( new HitAction( 0 ) );
		pass_hit_.add( new PassAction( 0 ) );
		
		pass_only_.add( new PassAction( 0 ) );
		
		params_ = params;
	}
	
	@Override
	public ActionSet<BlackjackMdpState, BlackjackAction> getActionSet( final BlackjackMdpState s )
	{
		s_ = s;
		if( s_.dealer_value > params_.max_score || s_.player_value > params_.max_score || s_.player_passed
			|| s_ == BlackjackMdpState.TheAbsorbingState ) {
//			actions_ = pass_only_;
			return ActionSet.wrap( pass_only_ );
		}
		else {
//			actions_ = pass_hit_;
			return ActionSet.wrap( pass_hit_ );
		}
	}
	
	@Override
	public int cardinality()
	{
		return actions_.size();
	}

	@Override
	public boolean isFinite()
	{
		return true;
	}

	@Override
	public boolean isCountable()
	{
		return true;
	}

	@Override
	public int index( final BlackjackAction a )
	{
		throw new UnsupportedOperationException();
	}

//	@Override
//	public Generator<BlackjackAction> generator()
//	{
//		return Generator.fromIterator( actions_.iterator() );
//	}

}
