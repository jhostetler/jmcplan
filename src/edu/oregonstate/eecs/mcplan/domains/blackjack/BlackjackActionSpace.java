/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.ActionSpace;
import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class BlackjackActionSpace extends ActionSpace<BlackjackMdpState, BlackjackAction>
{
	private final ArrayList<BlackjackAction> pass_hit_ = new ArrayList<BlackjackAction>();
	private final ArrayList<BlackjackAction> pass_only_ = new ArrayList<BlackjackAction>();
	
	private BlackjackMdpState s_ = null;
	private ArrayList<BlackjackAction> actions_ = null;
	
	public BlackjackActionSpace()
	{
		pass_hit_.add( new HitAction( 0 ) );
		pass_hit_.add( new PassAction( 0 ) );
		
		pass_only_.add( new PassAction( 0 ) );
	}
	
	@Override
	public void setState( final BlackjackMdpState s )
	{
		s_ = s;
		if( s_.dealer_value > 21 || s_.player_value > 21 || s_.player_passed
			|| s_ == BlackjackMdpState.TheAbsorbingState ) {
			actions_ = pass_only_;
		}
		else {
			actions_ = pass_hit_;
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
	public Generator<BlackjackAction> generator()
	{
		return Generator.fromIterator( actions_.iterator() );
	}

}
