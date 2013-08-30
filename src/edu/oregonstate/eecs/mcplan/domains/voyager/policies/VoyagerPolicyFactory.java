/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager.policies;

import edu.oregonstate.eecs.mcplan.Policy;
import edu.oregonstate.eecs.mcplan.UndoableAction;
import edu.oregonstate.eecs.mcplan.domains.voyager.Player;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerInstance;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerParameters;
import edu.oregonstate.eecs.mcplan.domains.voyager.VoyagerState;
import edu.oregonstate.eecs.mcplan.experiments.Environment;

/**
 * @author jhostetler
 *
 */
public interface VoyagerPolicyFactory
{
	public abstract Policy<VoyagerState, UndoableAction<VoyagerState>>
	create( final Environment env, final VoyagerParameters params, final VoyagerInstance instance, final Player player );
}
