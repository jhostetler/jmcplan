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
	
//	private RDDLAction[] actions = null;
	private final int idx = 0;
	
	public RddlActionGenerator( final RddlSpec spec )
	{
		this.spec = spec;
	}
	
//	private RDDLAction[] getLegalActions( final RDDLState state )
//	{
////		setStateAction(cur_state, null);
//		final Set<String> action_vars = spec.actionVars;
//		String[] action_vars_array = new String[1];
//		action_vars_array = action_vars.toArray(action_vars_array);
//		final ArrayList<PVAR_INST_DEF>[] legal_actions
//			= enumerateActions( state, action_vars_array, 0, new ArrayList<PVAR_INST_DEF>(), true );
//		final RDDLAction[] flat_actions = convertToFactoredActions( legal_actions );
//		return flat_actions;
//	}
//
//	private RDDLAction[] convertToFactoredActions( final ArrayList<PVAR_INST_DEF>[] actionsList )
//	{
//		final RDDLAction[] ret = new RDDLAction[ actionsList.length ];
//		int index = 0;
//		for( final ArrayList<PVAR_INST_DEF> action : actionsList ){
//			ret[index++] = new RDDLAction( action );
//		}
//		return ret;
//	}
//
//	private ArrayList<PVAR_INST_DEF>[] enumerateActions(
//			final rddl.State _state,
//			final String[] action_vars_array,
//			final int index, final ArrayList<PVAR_INST_DEF> this_action,
//			final boolean check_constraints ) {
//		//check
////		if( check_constraints ){
////			try{
////				_state.checkStateActionConstraints(this_action, true);
////			}catch( final EvalException e ){
////				return null;
////			}
////		}
//
//		if( index == action_vars_array.length ){
//			final ArrayList<PVAR_INST_DEF>[]
//					ret = new ArrayList[1];
//			ret[0] = new ArrayList<PVAR_INST_DEF>(this_action);
//			return ret;
//		}
//
//		final String this_action_var = action_vars_array[index];
//		final UnorderedPair<PVAR_NAME, ArrayList<LCONST>>
//				rddlAction = spec._rddlVars.get(this_action_var);
//		//set true
//		final PVAR_INST_DEF true_action
//			= new PVAR_INST_DEF(rddlAction._o1._sPVarName, true, rddlAction._o2 );
//		this_action.add(true_action);
//		final ArrayList<PVAR_INST_DEF>[]
//				under_true = enumerateActions(_state, action_vars_array, index+1, this_action, check_constraints);
//		this_action.remove(this_action.size()-1);
//
//		//set false
//		final PVAR_INST_DEF false_action
//		= new PVAR_INST_DEF(rddlAction._o1._sPVarName, false, rddlAction._o2 );
//		this_action.add(false_action);
//		final ArrayList<PVAR_INST_DEF>[]
//			under_false = enumerateActions(_state, action_vars_array, index+1, this_action, check_constraints);
//		this_action.remove(this_action.size()-1);
//
//		return org.apache.commons.lang3.ArrayUtils.addAll( under_true, under_false );
//	}
	
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
//		actions = getLegalActions( s );
//		idx = 0;
			
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
