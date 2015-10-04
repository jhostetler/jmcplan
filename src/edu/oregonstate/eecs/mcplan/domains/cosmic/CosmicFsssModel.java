/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.cosmic;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.search.fsss.FsssModel;

/**
 * @author jhostetler
 *
 */
public class CosmicFsssModel extends FsssModel<CosmicState, CosmicAction>
{
	private final RandomGenerator rng;
	
	public CosmicFsssModel( final RandomGenerator rng )
	{
		this.rng = rng;
	}
	
	@Override
	public FsssModel<CosmicState, CosmicAction> create( final RandomGenerator rng )
	{
		return new CosmicFsssModel( rng );
	}

	@Override
	public double Vmin( final CosmicState s )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double Vmax( final CosmicState s )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double Vmin( final CosmicState s, final CosmicAction a )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double Vmax( final CosmicState s, final CosmicAction a )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double discount()
	{
		return 1.0;
	}

	@Override
	public double heuristic( final CosmicState s )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RandomGenerator rng()
	{
		return rng;
	}

	@Override
	public FactoredRepresenter<CosmicState, ? extends FactoredRepresentation<CosmicState>> base_repr()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Representer<CosmicState, ? extends Representation<CosmicState>> action_repr()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CosmicState initialState()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<CosmicAction> actions( final CosmicState s )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CosmicState sampleTransition( final CosmicState s, final CosmicAction a )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double reward( final CosmicState s )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double reward( final CosmicState s, final CosmicAction a )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sampleCount()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void resetSampleCount()
	{
		// TODO Auto-generated method stub
		
	}

}
