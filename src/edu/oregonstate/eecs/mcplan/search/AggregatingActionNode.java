/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.TrivialRepresentation;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class AggregatingActionNode<S extends State, A extends VirtualConstructor<A>>
	extends MutableActionNode<S, A>
{
	private final Representer<S, ? extends FactoredRepresentation<S>> factored_repr_;
	private final Representer<FactoredRepresentation<S>, Representation<S>> abstract_repr_;
	
	public <X extends FactoredRepresentation<S>>
	AggregatingActionNode( final JointAction<A> a, final int nagents,
						   final Representer<S, ? extends FactoredRepresentation<S>> base_repr,
						   final Representer<FactoredRepresentation<S>, Representation<S>> abstract_repr )
	{
		super( a, nagents, base_repr );
		factored_repr_ = base_repr;
		abstract_repr_ = abstract_repr;
	}
	
	@Override
	public AggregatingActionNode<S, A> create()
	{
		return new AggregatingActionNode<S, A>( a(), nagents, factored_repr_.create(), abstract_repr_.create() );
	}
	
	@Override
	public MutableStateNode<S, A> successor(
			final S s, final int nagents, final int[] turn, final ActionGenerator<S, JointAction<A>> action_gen )
	{
//		System.out.println( "successor( " + s + " )" );
		if( s.isTerminal() ) {
//			System.out.println( "\tTerminal" );
			
//			final IdentityRepresentation<S> x = new IdentityRepresentation<S>( s.toString() );
//			final MutableStateNode<S, A> leaf = new LeafStateNode<S, A>( x, nagents, turn );
//			attachSuccessor( x, turn, leaf );
			
			final TrivialRepresentation<S> x = new TrivialRepresentation<S>();
			MutableStateNode<S, A> leaf = getStateNode( x, turn );
			if( leaf == null ) {
				leaf = new LeafStateNode<S, A>( x, nagents, turn );
				attachSuccessor( x, turn, leaf );
			}
			
			return leaf;
		}
		
		final FactoredRepresentation<S> x = factored_repr_.encode( s );
		final Representation<S> ab = abstract_repr_.encode( x );
		
		final MutableStateNode<S, A> sn = getStateNode( ab, turn );
		if( sn != null ) {
//			System.out.println( "Hit " + x );
			return sn;
		}
		else {
//			System.out.println( "Miss " + x );
			final MutableStateNode<S, A> succ = createSuccessor( s, ab, nagents, turn, action_gen );
			attachSuccessor( ab, turn, succ );
			return succ;
		}
	}

	protected MutableStateNode<S, A> createSuccessor(
			final S s, final Representation<S> x, final int nagents, final int[] turn,
			final ActionGenerator<S, JointAction<A>> action_gen )
	{
		return new SimpleMutableStateNode<S, A>( x, nagents, turn, action_gen ) {
			@Override
			protected MutableActionNode<S, A> createSuccessor(
				final JointAction<A> a, final int nagents, final Representer<S, ? extends Representation<S>> repr )
			{
				return new AggregatingActionNode<S, A>( a, nagents, factored_repr_.create(), abstract_repr_.create() );
			}
		};
	}
}
