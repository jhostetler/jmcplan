package edu.oregonstate.eecs.mcplan.rddl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rddl.EvalException;
import rddl.RDDL.LCONST;
import rddl.RDDL.PVAR_NAME;
import edu.oregonstate.eecs.mcplan.State;


public class RDDLState extends rddl.State implements State
{
	private int t = 0;
	private final int horizon;
	
	public RDDLState( final int horizon )
	{
		super();
		this.horizon = horizon;
	}
	
	@Override
	public void close()
	{ }
	
	public RDDLState( final RDDLState that )
	{
		t = that.t;
		horizon = that.horizon;
		
		_hmPVariables = that._hmPVariables;
		_hmTypes = that._hmTypes;
		_hmCPFs = that._hmCPFs;
		_hmObject2Consts = that._hmObject2Consts;
		_alStateNames = that._alStateNames;
		_alActionNames = that._alActionNames;
		_tmIntermNames = that._tmIntermNames;
		_alIntermNames = that._alIntermNames;
		_alObservNames = that._alObservNames;
		_alNonFluentNames = that._alNonFluentNames;
		_hmTypeMap = that._hmTypeMap;
		
		_state = new HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, Object>>( that._state.size() );
		_nextState = new HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, Object>>( that._state.size() );
		for( final PVAR_NAME p : that._state.keySet() ) {
			final HashMap<ArrayList<LCONST>, Object> this_s = new HashMap<ArrayList<LCONST>, Object>();
			final HashMap<ArrayList<LCONST>, Object> this_ns = new HashMap<ArrayList<LCONST>, Object>();
			final HashMap<ArrayList<LCONST>, Object> that_s = that._state.get( p );
			final HashMap<ArrayList<LCONST>, Object> that_ns = that._nextState.get( p );
			
			for( final Map.Entry<ArrayList<LCONST>, Object> e : that_s.entrySet() ) {
				this_s.put( e.getKey(), e.getValue() );
			}
			for( final Map.Entry<ArrayList<LCONST>, Object> e : that_ns.entrySet() ) {
				this_ns.put( e.getKey(), e.getValue() );
			}
			
			_state.put( p, this_s );
			_nextState.put( p, this_ns );
		}
		
		_nonfluents = that._nonfluents;
		
		// [jhostetler] We *think* there's no need to copy the elements of
		// these maps because we *think* there are none.
		
		_actions = new HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, Object>>( that._actions.size() );
		for( final PVAR_NAME p : that._actions.keySet() ) {
			_actions.put( p, new HashMap<ArrayList<LCONST>, Object>() );
//			if( !that._actions.get( p ).isEmpty() ) {
//				System.out.println( that._actions.get( p ) );
//			}
//			assert( that._actions.get( p ).isEmpty() );
		}
		
		_interm = new HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, Object>>( that._interm.size() );
		for( final PVAR_NAME p : that._interm.keySet() ) {
			_interm.put( p, new HashMap<ArrayList<LCONST>, Object>() );
			assert( that._interm.get( p ).isEmpty() );
		}
		
		_observ = new HashMap<PVAR_NAME, HashMap<ArrayList<LCONST>, Object>>( that._observ.size() );
		for( final PVAR_NAME p : that._observ.keySet() ) {
			_observ.put( p, new HashMap<ArrayList<LCONST>, Object>() );
			assert( that._observ.get( p ).isEmpty() );
		}
		
		_alConstraints = that._alConstraints;
		_reward = that._reward;
		_nMaxNondefActions = that._nMaxNondefActions;
	}
	
	@Override
	public void advanceNextState(final boolean clear_observations) throws EvalException
	{
		super.advanceNextState( clear_observations );
		t += 1;
	}
	
	@Override
	public boolean isTerminal()
	{
		return t >= horizon;
	}
	
	@Override
	public String toString()
	{
		return " - t = " + t + " / " + horizon + "\n" + super.toString();
	}

}