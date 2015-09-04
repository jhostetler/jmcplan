/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class RandomStaticClassifierRepresenter<S extends State, A extends VirtualConstructor<A>>
	extends StaticClassifierRepresenter<S, A>
{
	public static class Abstraction<S extends State, A extends VirtualConstructor<A>>
		extends FsssAbstraction<S, A>
	{
		private final FsssModel<S, A> model;
		private final int k;
	
		public Abstraction( final FsssModel<S, A> model, final int k )
		{
			this.model = model;
			this.k = k;
		}
		
		@Override
		public String toString()
		{
			return "Random(" + k + ")";
		}
		
		@Override
		public ClassifierRepresenter<S, A> createRepresenter()
		{
			return new RandomStaticClassifierRepresenter<S, A>( model, this, k );
		}
	}
	
	private static final ch.qos.logback.classic.Logger Log = LoggerManager.getLogger( "log.search" );

	public final int k;
	
	public RandomStaticClassifierRepresenter( final FsssModel<S, A> model,
			final FsssAbstraction<S, A> abstraction, final int k )
	{
		super( model, abstraction );
		this.k = k;
	}

	@Override
	protected DataNode<S, A> novelInstance( final DataNode<S, A> dt_root, final FactoredRepresentation<S> x )
	{
		Log.trace( "\tnovelInstance(): {}", x );
		final MapSplitNode<S, A> split = (MapSplitNode<S, A>) dt_root.split;
		final DataNode<S, A> dn;
		if( split.assignments.size() < k ) {
			dn = split.createChild( x );
			dt_leaves.add( dn );
		}
		else {
			final int[] sizes = new int[k];
			for( int i = 0; i < k; ++i ) {
				final DataNode<S, A> candidate = dt_leaves.get( i );
				sizes[i] += candidate.aggregate.n();
			}
			final int choice = Fn.argmin( sizes );
			dn = Fn.element( split.assignments.values(), choice ); //dt_leaves.get( choice );
		}
		
		return dn;
	}
}
