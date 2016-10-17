/**
 * 
 */
package edu.oregonstate.eecs.mcplan.rddl;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import rddl.EvalException;
import rddl.RDDL;
import rddl.RDDL.DOMAIN;
import rddl.RDDL.INSTANCE;
import rddl.RDDL.LCONST;
import rddl.RDDL.NONFLUENTS;
import rddl.RDDL.PVARIABLE_DEF;
import rddl.RDDL.PVAR_NAME;
import rddl.RDDL.TYPE_NAME;
import rddl.parser.parser;

/**
 * @author jhostetler
 *
 */
public class RddlSpec
{
	private static interface Converter
	{
		public abstract double convert( final Object o );
	}
	
	public TreeMap<String, UnorderedPair<PVAR_NAME, ArrayList<LCONST>>> _rddlVars;
	public final String _simName;
	public final int _numAgents;
	
	public final TreeSet<String> stateVars = new TreeSet<String>();
	public final TreeSet<String> actionVars = new TreeSet<String>();
	
	public final ArrayList<String> state_var_names = new ArrayList<String>();
	
	/**
	 * State predicate name -> (Assignment -> index in state vector)
	 */
	public Map<PVAR_NAME, TObjectIntMap<ArrayList<LCONST>>> state_var_indices
		= new HashMap<PVAR_NAME, TObjectIntMap<ArrayList<LCONST>>>();
	public int Nstate_vars = -1;
	
	public double[] state_defaults;
	
	public final Map<PVAR_NAME, Converter> type_converters = new HashMap<PVAR_NAME, Converter>();
	
	public final DOMAIN rddl_domain;
	public final INSTANCE rddl_instance;
	public final NONFLUENTS rddl_nonfluents;
	
	public RddlSpec( final File RDDLDomainFile,
			final File RDDLInstanceFile,
			final int numAgents,
			final long rand_seed ,
			final String name ){
		_numAgents = numAgents;
		_simName = name;
//		_rand = new Random(rand_seed);
		
		final RDDL _rddltemp = new RDDL();

		final File domf = RDDLDomainFile;
		final File instf = RDDLInstanceFile;

		RDDL domain = null;
		RDDL instance = null;
		
		try
		{
			domain = parser.parse(domf);
		} catch (final Exception e)
		{
			System.err.println("domain file did not parse");
			e.printStackTrace();
			System.exit(1);
		}


		try
		{
			instance = parser.parse(instf);
		} catch (final Exception e)
		{
			System.err.println("instance file did not parse");
			e.printStackTrace();
			System.exit(1);
		}
		
		rddl_domain = domain._tmDomainNodes.entrySet().iterator().next().getValue();
		rddl_instance = instance._tmInstanceNodes.entrySet().iterator().next().getValue();
		rddl_nonfluents = instance._tmNonFluentNodes.entrySet().iterator().next().getValue();
		final rddl.State rddlState = createInitialState();
		
		try {
			initRDDLData(rddlState);
		} catch (final EvalException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}
	
	public static String CleanFluentName(String s) {
		s = s.replace("[", "__");
		s = s.replace("]", "");
		s = s.replace(", ", "_");
		s = s.replace(',','_');
		s = s.replace(' ','_');
		s = s.replace('-','_');
		s = s.replace("()","");
		s = s.replace("(", "__");
		s = s.replace(")", "");
		if (s.endsWith("__"))
			s = s.substring(0, s.length() - 2);
		return s;
	}
	
	private void initRDDLData(final rddl.State rddlState) throws EvalException {

		final TreeMap<PVAR_NAME, ArrayList<ArrayList<LCONST>>>
			state_vars = collectStateVars(rddlState);
		final TreeMap<PVAR_NAME, ArrayList<ArrayList<LCONST>>>
			_action_vars = collectActionVars(rddlState);
		final TreeMap<PVAR_NAME, ArrayList<ArrayList<LCONST>>>
			_observ_vars = collectObservationVars(rddlState);
		
		
//		final TreeSet<String> stateVars = new TreeSet<String>();
//		final TreeSet<String> actionVars = new TreeSet<String>();
		
		final TreeMap<String, UnorderedPair<PVAR_NAME, ArrayList<LCONST> > >
			tmStateVars = new TreeMap<String, UnorderedPair<PVAR_NAME,ArrayList<LCONST>>>();
		final TreeMap<String, UnorderedPair<PVAR_NAME, ArrayList<LCONST> > >
			tmNextStateVars = new TreeMap<String, UnorderedPair<PVAR_NAME,ArrayList<LCONST>>>();
		final TreeMap<String, UnorderedPair<PVAR_NAME, ArrayList<LCONST> > >
			tmActionVars = new TreeMap<String, UnorderedPair<PVAR_NAME,ArrayList<LCONST>>>();
		final TreeMap<String,UnorderedPair<PVAR_NAME, ArrayList<LCONST> > >
			tmObservVars = new TreeMap<String, UnorderedPair<PVAR_NAME,ArrayList<LCONST>>>();
		
		int state_idx = 0;
		int default_idx = 0;
		state_defaults = new double[state_vars.size()];
		for (final Map.Entry<PVAR_NAME,ArrayList<ArrayList<LCONST>>> e : state_vars.entrySet()) {
			final PVAR_NAME p = e.getKey();
			
			final TObjectIntMap<ArrayList<LCONST>> assign_map = new TObjectIntHashMap<ArrayList<LCONST>>();
			state_var_indices.put( p, assign_map );
			
			final PVARIABLE_DEF pvar_def = rddlState._hmPVariables.get( p );
			final Converter converter = createTypeConverter( pvar_def );
			type_converters.put( p, converter );
			
			state_defaults[default_idx++] = converter.convert( rddlState.getDefaultValue( p ) );
			
			final ArrayList<ArrayList<LCONST>> assignments = e.getValue();
			for (final ArrayList<LCONST> assign : assignments) {
				final String name = CleanFluentName(p.toString() + assign);
				tmStateVars.put(name, new UnorderedPair<PVAR_NAME, ArrayList<LCONST> >(p, assign));
				stateVars.add(name);
				
				state_var_names.add( name );
				
				assign_map.put( assign, state_idx++ );
			}
		}
		Nstate_vars = state_idx;

		for (final Map.Entry<PVAR_NAME,ArrayList<ArrayList<LCONST>>> e : _action_vars.entrySet()) {
			final PVAR_NAME p = e.getKey();
			final ArrayList<ArrayList<LCONST>> assignments = e.getValue();
			for (final ArrayList<LCONST> assign : assignments) {
				final String name = CleanFluentName(p.toString() + assign);
				tmActionVars.put(name, new UnorderedPair<PVAR_NAME, ArrayList<LCONST> >(p, assign));
				actionVars.add(name);
			}
		}

//		_rddlActionSpace = new RDDLFactoredActionSpace();
//		_rddlActionSpace.setActionVariables(actionVars);
		
//		_rddlStateSpace = new RDDLFactoredStateSpace();
//		_rddlStateSpace.setStateVariables(stateVars);
		
		_rddlVars = new TreeMap<String, UnorderedPair<PVAR_NAME,ArrayList<LCONST>>>();
		_rddlVars.putAll( tmStateVars );
		_rddlVars.putAll( tmActionVars );
		
//		_rddlTransition = new RDDLFactoredTransition( rddlState, _rddlStateSpace,
//				_rddlActionSpace, tmStateVars, _rand.nextLong(), tmActionVars);

//		_rddlReward = new RDDLFactoredReward( rddlState, tmStateVars,
//				_rand.nextLong(), tmActionVars);
		
//		cur_state = generateInitialState();
	}
	
	private Converter createTypeConverter( final PVARIABLE_DEF pvar_def )
	{
		final RDDL.TYPE_NAME type_name = pvar_def._sRange;
		if( TYPE_NAME.BOOL_TYPE.equals( type_name ) ) {
			return new Converter() {
				@Override
				public double convert( final Object o ) { return ((Boolean) o).booleanValue() ? 1.0 : 0.0; }
			};
		}
		else if( TYPE_NAME.INT_TYPE.equals( type_name ) ) {
			return new Converter() {
				@Override
				public double convert( final Object o ) { return ((Integer) o).doubleValue(); }
			};
		}
		else if( TYPE_NAME.REAL_TYPE.equals( type_name ) ) {
			return new Converter() {
				@Override
				public double convert( final Object o ) { return ((Double) o).doubleValue(); }
			};
		}
		else {
			throw new AssertionError( "Unknown range type '" + type_name + "'" );
		}
	}

	private TreeMap<PVAR_NAME,ArrayList<ArrayList<LCONST>>>
			collectActionVars(final rddl.State rddlState )
					throws EvalException {

		final TreeMap<PVAR_NAME,ArrayList<ArrayList<LCONST>>> action_vars =
				new TreeMap<PVAR_NAME,ArrayList<ArrayList<LCONST>>>();

		for (final PVAR_NAME p : rddlState._alActionNames) {
			final ArrayList<ArrayList<LCONST>> gfluents = rddlState.generateAtoms(p);
			action_vars.put(p, gfluents);
		}

		return action_vars;
	}

	private TreeMap<PVAR_NAME,ArrayList<ArrayList<LCONST>>>
		collectObservationVars(final rddl.State rddlState)
			throws EvalException {

		final TreeMap<PVAR_NAME,ArrayList<ArrayList<LCONST>>> observ_vars =
				new TreeMap<PVAR_NAME,ArrayList<ArrayList<LCONST>>>();

		for (final PVAR_NAME p : rddlState._alObservNames) {
			final ArrayList<ArrayList<LCONST>> gfluents = rddlState.generateAtoms(p);
			observ_vars.put(p, gfluents);
		}
		return observ_vars;
	}


	private TreeMap<PVAR_NAME,ArrayList<ArrayList<LCONST>>>
		collectStateVars(final rddl.State rddlState) throws EvalException {

		final TreeMap<PVAR_NAME,ArrayList<ArrayList<LCONST>>> state_vars =
				new TreeMap<PVAR_NAME,ArrayList<ArrayList<LCONST>>>();

		for (final PVAR_NAME p : rddlState._alStateNames) {
			final ArrayList<ArrayList<LCONST>> gfluents = rddlState.generateAtoms(p);
			state_vars.put(p, gfluents);
		}

		return state_vars;
	}

	/**
	 * @return
	 */
	public RDDLState createInitialState()
	{
		final RDDLState state = new RDDLState( rddl_instance._nHorizon );
		state.init( rddl_nonfluents != null ? rddl_nonfluents._hmObjects : null,
					rddl_instance._hmObjects, rddl_domain._hmTypes,
					rddl_domain._hmPVariables, rddl_domain._hmCPF,
					rddl_instance._alInitState,
					rddl_nonfluents == null ? null : rddl_nonfluents._alNonFluents,
					rddl_domain._alStateConstraints, rddl_domain._exprReward, rddl_instance._nNonDefActions );
		return state;
	}

	/**
	 * Looks up the type of 'name' and returns the appropriate double encoding
	 * of 'v'.
	 * @param name
	 * @param v
	 * @return
	 */
	public double valueToDouble( final PVAR_NAME name, final Object v )
	{
		return type_converters.get( name ).convert( v );
	}
}
