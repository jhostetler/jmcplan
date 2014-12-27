/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.spbj;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.ActionGenerator;

/**
 * @author jhostetler
 *
 */
public class SpBjActionGenerator extends ActionGenerator<SpBjState, SpBjAction>
{
	private final ArrayList<ArrayList<SpBjActionCategory>> cat = new ArrayList<ArrayList<SpBjActionCategory>>();
	private int[] index = null;
	private int size = 0;
	private int ai = 0;
	
	@Override
	public SpBjActionGenerator create()
	{
		return new SpBjActionGenerator();
	}

	@Override
	public void setState( final SpBjState s, final long t )
	{
		cat.clear();
		size = 1;
		for( int i = 0; i < s.player_hand.Nhands; ++i ) {
			final ArrayList<SpBjActionCategory> cat_i = new ArrayList<SpBjActionCategory>();
			cat_i.add( SpBjActionCategory.Pass );
			if( s.player_hand.passed[i] ) {
				continue;
			}
			
			cat_i.add( SpBjActionCategory.Hit );
			if( s.player_hand.canDouble( i ) ) {
				cat_i.add( SpBjActionCategory.Double );
			}
			if( s.player_hand.canSplit( i ) ) {
				cat_i.add( SpBjActionCategory.Split );
			}
			
			cat.add( cat_i );
			size *= cat_i.size();
		}
		
		index = new int[cat.size()];
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public boolean hasNext()
	{
		return ai < size;
	}

	@Override
	public SpBjAction next()
	{
		final SpBjActionCategory[] ac = new SpBjActionCategory[cat.size()];
		for( int i = 0; i < ac.length; ++i ) {
			ac[i] = cat.get( i ).get( index[i] );
		}
		final SpBjAction a = new SpBjAction( ac );
		
		for( int i = 0; i < index.length; ++i ) {
			index[i] += 1;
			if( index[i] == cat.get( i ).size() ) {
				index[i] = 0;
			}
			else {
				break;
			}
		}
		
		ai += 1;
		return a;
	}

}
