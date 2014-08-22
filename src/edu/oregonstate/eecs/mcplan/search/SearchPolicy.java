/**
 * 
 */
package edu.oregonstate.eecs.mcplan.search;

import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.oregonstate.eecs.mcplan.AnytimePolicy;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Countdown;

/**
 * A Policy that uses a GameTreeSearch to choose an action.
 * 
 * TODO: Maybe the TimeLimitMctsVisitor shouldn't be baked in to this class.
 * 
 * @author jhostetler
 */
public abstract class SearchPolicy<S, A extends VirtualConstructor<A>>
	extends AnytimePolicy<S, JointAction<A>>
{
	private static final Logger log = LoggerFactory.getLogger( SearchPolicy.class );
	
	private final GameTreeFactory<S, A> factory_;
	private final MctsVisitor<S, A> visitor_;
	private final PrintStream log_stream_;
	
	private S s_ = null;
	private long t_ = 0L;
	
	public SearchPolicy( final GameTreeFactory<S, A> factory,
						 final MctsVisitor<S, A> visitor,
						 final PrintStream log_stream )
	{
		factory_ = factory;
		visitor_ = visitor;
		log_stream_ = log_stream;
	}
	
	public SearchPolicy( final GameTreeFactory<S, A> factory,
						 final MctsVisitor<S, A> visitor )
	{
		this( factory, visitor, System.out );
	}
	
	protected abstract JointAction<A> selectAction( final GameTree<S, A> tree );
	
	@Override
	public void setState( final S s, final long t )
	{
		s_ = s;
		t_ = t;
		
//		System.out.println( "SearchPolicy.setState( " + s + " )" );
	}

	@Override
	public JointAction<A> getAction()
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
	public JointAction<A> getAction( final long control )
	{
//		log.info( "getAction( {} )", control );
		final MctsVisitor<S, A> time_limit
			= new TimeLimitMctsVisitor<S, A>( visitor_, new Countdown( control ) );
		final GameTree<S, A> search = factory_.create( time_limit );
//		final long start = System.currentTimeMillis();
		search.run();
//		final long stop = System.currentTimeMillis();
//		System.out.println( "*** Tree search finished in " + (stop - start) + " ms" );
		
		if( log_stream_ != null ) {
			log_stream_.println( "[t = " + t_ + "]" );
			search.root().accept( new TreePrinter<S, A>( log_stream_ ) );
		}

		return selectAction( search );
	}

}
