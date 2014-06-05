/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.frogger;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class FroggerActionGenerator extends ActionGenerator<FroggerState, FroggerAction>
{
	private final boolean[] valid_ = new boolean[5];
	private static MoveAction[] actions_ = new MoveAction[] {
		new MoveAction( 0, 1 ),
		new MoveAction( -1, 0 ),
		new MoveAction( 0, 0 ),
		new MoveAction( 1, 0 ),
		new MoveAction( 0, -1 )
	};
	private int idx_ = 0;
	private int size_ = 0;
	private int n_ = 0;
	
	@Override
	public ActionGenerator<FroggerState, FroggerAction> create()
	{
		return new FroggerActionGenerator();
	}
	
	public void print()
	{
		for( final MoveAction a : actions_ ) {
			System.out.print( a.create() + " -> " );
			System.out.print( a.dx + " " + a.dy + "\n" );
		}
	}

	@Override
	public void setState( final FroggerState s, final long t, final int[] turn )
	{
		// Up, left, stay, right, down
		valid_[0] = s.frog_y < s.params.lanes + 1;
		valid_[1] = s.frog_x > 0;
		valid_[2] = true;
		valid_[3] = s.frog_x < s.params.road_length - 1;
		valid_[4] = s.frog_y > 0;
		size_ = Fn.sum( valid_ );
		n_ = 0;
		idx_ = 0;
	}

	@Override
	public int size()
	{
		return size_;
	}

	@Override
	public boolean hasNext()
	{
		return n_ < size_;
	}

	@Override
	public FroggerAction next()
	{
		while( !valid_[idx_] ) {
			++idx_;
		}
		n_ += 1;
		return actions_[idx_++].create();
	}
}
