/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.IdentityRepresentation;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class SimpleMutableActionNode<S extends State, A extends VirtualConstructor<A>>
	extends MutableActionNode<S, A>
{
	public SimpleMutableActionNode( final JointAction<A> a, final int nagents,
									final Representer<S, ? extends Representation<S>> repr )
	{
		super( a, nagents, repr );
	}
	
	@Override
	public SimpleMutableActionNode<S, A> create()
	{
		return new SimpleMutableActionNode<S, A>( a(), nagents, repr_.create() );
	}
	
	@Override
	public MutableStateNode<S, A> successor(
			final S s, final int nagents, final int[] turn, final ActionGenerator<S, JointAction<A>> action_gen )
	{
//		System.out.println( "successor( " + s + " )" );
		if( s.isTerminal() ) {
//			System.out.println( "\tTerminal" );
			
			final IdentityRepresentation<S> x = new IdentityRepresentation<S>( s.toString() );
			final MutableStateNode<S, A> leaf = new LeafStateNode<S, A>( x, nagents, turn );
			attachSuccessor( x, turn, leaf );
			
//			final TrivialRepresentation<S> x = new TrivialRepresentation<S>();
//			MutableStateNode<S, A> leaf = getStateNode( x, turn );
//			if( leaf == null ) {
//				leaf = new LeafStateNode<S, A>( x, nagents, turn );
//				attachSuccessor( x, turn, leaf );
//			}
			
			return leaf;
		}
		
		final Representation<S> x = repr_.encode( s );
		
		final MutableStateNode<S, A> sn = getStateNode( x, turn );
		if( sn != null ) {
//			System.out.println( "\tHit " + x );
			return sn;
		}
		else {
//			System.out.println( "\tMiss " + x );
			final MutableStateNode<S, A> succ = createSuccessor( s, x, nagents, turn, action_gen );
			attachSuccessor( x, turn, succ );
			return succ;
		}
	}

	protected MutableStateNode<S, A> createSuccessor(
			final S s, final Representation<S> x, final int nagents, final int[] turn,
			final ActionGenerator<S, JointAction<A>> action_gen )
	{
		return new SimpleMutableStateNode<S, A>( x, nagents, turn, action_gen );
	}
}
