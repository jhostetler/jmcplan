/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Countdown;

/**
 * @author jhostetler
 *
 */
public abstract class SearchPolicy<S, F extends Representer<S, F>, A extends VirtualConstructor<A>>
	implements AnytimePolicy<S, A>
{
	private static final Logger log = LoggerFactory.getLogger( SearchPolicy.class );
	
	private final GameTreeFactory<S, F, A> factory_;
	private final MctsVisitor<S, A> visitor_;
	private final PrintStream log_stream_;
	
	private S s_ = null;
	private long t_ = 0L;
	
	public SearchPolicy( final GameTreeFactory<S, F, A> factory,
						 final MctsVisitor<S, A> visitor,
						 final PrintStream log_stream )
	{
		factory_ = factory;
		visitor_ = visitor;
		log_stream_ = log_stream;
	}
	
	public SearchPolicy( final GameTreeFactory<S, F, A> factory,
						 final MctsVisitor<S, A> visitor )
	{
		this( factory, visitor, System.out );
	}
	
	protected abstract A selectAction( final StateNode<Representation<S, F>, A> root );
	
	@Override
	public void setState( final S s, final long t )
	{
		s_ = s;
		t_ = t;
	}

	@Override
	public A getAction()
	{
		return getAction( maxControl() );
	}

	@Override
	public void actionResult( final S sprime, final double[] r )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName()
	{
		return "SearchPolicy";
	}

	@Override
	public long minControl()
	{
		return 0;
	}

	@Override
	public long maxControl()
	{
		return Long.MAX_VALUE;
	}

	@Override
	public A getAction( final long control )
	{
		log.info( "getAction( {} )", control );
		final MctsVisitor<S, A> time_limit
			= new TimeLimitMctsVisitor<S, A>( visitor_, new Countdown( control ) );
		final GameTree<Representation<S, F>, A> search = factory_.create( time_limit );
		search.run();
		
		if( log_stream_ != null ) {
			log_stream_.println( "[t = " + t_ + "]" );
			search.root().accept( new TreePrinter<Representation<S, F>, A>() );
		}

		return selectAction( search.root() );
	}

}
