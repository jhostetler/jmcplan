/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.ArrayList;

/**
 * @author jhostetler
 *
 */
public class JointPolicy<S, A extends VirtualConstructor<A>> extends Policy<S, JointAction<A>>
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
	
	public static <S, A extends VirtualConstructor<A>> JointPolicy<S, A> create( final Policy<S, A>... Pi )
	{
		return new JointPolicy<S, A>( Pi );
	}
	
	private final ArrayList<Policy<S, A>> Pi_;
	
	public JointPolicy( final Policy<S, A>... Pi )
	{
		Pi_ = new ArrayList<Policy<S, A>>( Pi.length );
		for( final Policy<S, A> pi : Pi ) {
			Pi_.add( pi );
		}
	}
	
	public JointPolicy( final ArrayList<? extends Policy<S, A>> Pi )
	{
		Pi_ = new ArrayList<Policy<S, A>>( Pi.size() );
		Pi_.addAll( Pi );
	}
	
	@Override
	public int hashCode()
	{
		return Pi_.hashCode();
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof JointPolicy<?, ?>) ) {
			return false;
		}
		@SuppressWarnings( "unchecked" )
		final JointPolicy<S, A> that = (JointPolicy<S, A>) obj;
		return Pi_.equals( that.Pi_ );
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
	
	/*
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
	*/

}
