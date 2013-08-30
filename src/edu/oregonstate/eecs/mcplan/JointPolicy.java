/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;

import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.ActionGen;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.IdentityRepresenter;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.Simulator;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.State;
import edu.oregonstate.eecs.mcplan.domains.toy.Irrelevance.Visitor;
import edu.oregonstate.eecs.mcplan.search.ActionNode;
import edu.oregonstate.eecs.mcplan.search.SparseSampleTree;
import edu.oregonstate.eecs.mcplan.search.StateNode;
import edu.oregonstate.eecs.mcplan.search.TimeLimitMctsVisitor;
import edu.oregonstate.eecs.mcplan.search.TreePrinter;
import edu.oregonstate.eecs.mcplan.sim.SequentialJointSimulator;
import edu.oregonstate.eecs.mcplan.util.Countdown;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class JointPolicy<S, A extends VirtualConstructor<A>> implements Policy<S, JointAction<A>>
{
	public static final class Builder<S, A extends VirtualConstructor<A>>
	{
		private final ArrayList<Policy<S, A>> Pi_ = new ArrayList<Policy<S, A>>();
		
		public Builder<S, A> pi( final Policy<S, A> pi )
		{
			Pi_.add( pi );
			return this;
		}
		
		public JointPolicy<S, A> finish()
		{
			return new JointPolicy<S, A>( Pi_ );
		}
	}
	
	private final List<Policy<S, A>> Pi_;
	
	private JointPolicy( final List<Policy<S, A>> Pi )
	{
		Pi_ = Pi;
	}
	
	@Override
	public void setState( final S s, final long t )
	{
		for( final Policy<S, A> pi : Pi_ ) {
			pi.setState( s, t );
		}
	}

	@Override
	public JointAction<A> getAction()
	{
		final JointAction.Builder<A> j = new JointAction.Builder<A>( Pi_.size() );
		for( int i = 0; i < Pi_.size(); ++i ) {
			final Policy<S, A> pi = Pi_.get( i );
			j.a( i, pi.getAction() );
		}
		return j.finish();
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		for( int i = 0; i < Pi_.size(); ++i ) {
			Pi_.get( i ).actionResult( sprime, r );
		}
	}

	@Override
	public String getName()
	{
		// TODO:
		return "JointPolicy";
	}
	
	public static void main( final String[] args )
	{
		final MersenneTwister rng = new MersenneTwister( 42 );
		final Simulator sim = new Simulator();
		final int width = 40;
		final int depth = 4;
		
		final ActionGen action_gen = new ActionGen( rng );
		final ArrayList<ActionGen> gen_list = new ArrayList<ActionGen>();
		gen_list.add( action_gen );
		final ProductActionGenerator<State, UndoableAction<State>> pgen
			= new ProductActionGenerator<State, UndoableAction<State>>( gen_list );
		final SequentialJointSimulator<State, UndoableAction<State>> joint_sim
			= new SequentialJointSimulator<State, UndoableAction<State>>( 1, sim );
		final SparseSampleTree<State, IdentityRepresenter, JointAction<UndoableAction<State>>> tree
			= new SparseSampleTree<State, IdentityRepresenter, JointAction<UndoableAction<State>>>(
				joint_sim, new IdentityRepresenter(), pgen, width, depth,
				TimeLimitMctsVisitor.create( new Visitor<JointAction<UndoableAction<State>>>(), new Countdown( 1000 ) ) )
			{
				@Override
				public double[] backup( final StateNode<Representation<State, IdentityRepresenter>,
														JointAction<UndoableAction<State>>> s )
				{
					double max_q = -Double.MAX_VALUE;
					for( final ActionNode<Representation<State, IdentityRepresenter>, JointAction<UndoableAction<State>>> an
							: Fn.in( s.successors() ) ) {
						if( an.q( 0 ) > max_q ) {
							max_q = an.q( 0 );
						}
					}
					return new double[] { max_q };
				}
			};
		tree.run();
		tree.root().accept( new TreePrinter<Representation<State, IdentityRepresenter>, JointAction<UndoableAction<State>>>() );
	}

}
