/**
 * 
 */
package edu.oregonstate.eecs.mcplan.rddl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import rddl.EvalException;
import rddl.RDDL.PVAR_INST_DEF;
import edu.oregonstate.eecs.mcplan.ActionGenerator;

/**
 * @author jhostetler
 *
 */
public class RddlActionGenerator extends ActionGenerator<RDDLState, RDDLAction>
{
	private final RddlSpec spec;
	
	public RddlActionGenerator( final RddlSpec spec )
	{
		this.spec = spec;
	}
	
	// -----------------------------------------------------------------------
	
	private TreeMap<String, ArrayList<PVAR_INST_DEF>> actions = null;
	private Iterator<Map.Entry<String, ArrayList<PVAR_INST_DEF>>> next = null;
	
	@Override
	public RddlActionGenerator create()
	{
		return new RddlActionGenerator( spec );
	}

	@Override
	public void setState( final RDDLState s, final long t )
	{
		try {
			actions = rddl.ActionGenerator.getLegalBoolActionMap( s );
			next = actions.entrySet().iterator();
		}
		catch( final EvalException ex ) {
			throw new RuntimeException( ex );
		}
	}

	@Override
	public int size()
	{
		return actions.size();
	}

	@Override
	public boolean hasNext()
	{
		return next.hasNext();
	}

	@Override
	public RDDLAction next()
	{
		return new RDDLAction( next.next().getValue() );
	}
}
