/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import java.util.Iterator;

import edu.oregonstate.eecs.mcplan.ActionSet;
import edu.oregonstate.eecs.mcplan.ActionSpace;
import edu.oregonstate.eecs.mcplan.Representation;

/**
 * @author jhostetler
 *
 */
public class NothingActionSpace extends ActionSpace<CosmicState, CosmicAction>
{
	private static final class A extends ActionSet<CosmicState, CosmicAction>
	{
		private class Itr implements Iterator<CosmicAction>
		{
			int i = 0;
			
			@Override
			public boolean hasNext()
			{
				return i < size();
			}

			@Override
			public CosmicAction next()
			{
				assert( i == 0 );
				final CosmicAction a = new CosmicNothingAction();
				i += 1;
				return a;
			}

			@Override
			public void remove()
			{ throw new UnsupportedOperationException(); }
		}
		
		@Override
		public Iterator<CosmicAction> iterator()
		{
			return new Itr();
		}

		@Override
		public int size()
		{
			return 1;
		}

		@Override
		public Representation<CosmicState> copy()
		{
			return new A();
		}

		@Override
		public boolean equals( final Object obj )
		{
			return obj instanceof A;
		}

		@Override
		public int hashCode()
		{
			return getClass().hashCode();
		}
		
	}
	
	// -----------------------------------------------------------------------

	@Override
	public ActionSet<CosmicState, CosmicAction> getActionSet( final CosmicState s )
	{
		return new A();
	}
	
	@Override
	public String toString()
	{
		return "Nothing";
	}

	@Override
	public int cardinality()
	{
		throw new UnsupportedOperationException();
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
	public int index( final CosmicAction a )
	{
		throw new UnsupportedOperationException();
	}

}
