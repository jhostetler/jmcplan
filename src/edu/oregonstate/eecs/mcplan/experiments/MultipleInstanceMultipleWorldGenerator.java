/**
 * 
 */
package edu.oregonstate.eecs.mcplan.experiments;

import java.io.File;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.math3.random.MersenneTwister;

import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * Executes an experiment multiple times, once for each World instance
 * provided during setup.
 * 
 * @author jhostetler
 */
public class MultipleInstanceMultipleWorldGenerator<P, W extends Copyable<? extends W>>
	extends Generator<ExperimentalSetup<P, W>>
{
	private final Environment master_;
	private final List<P> ps_;
	private ListIterator<P> pitr_ = null;
	private int pidx_ = 0;
	private P pval_ = null;
	private final List<W> ws_;
	private ListIterator<W> witr_ = null;
	private int widx_ = 0;
	
	private File pdir_ = null;
	private boolean first_ = true;
	
	public MultipleInstanceMultipleWorldGenerator( final Environment master, final List<P> ps, final List<W> ws )
	{
		master_ = master;
		ps_ = ps;
		pitr_ = ps_.listIterator();
		assert( pitr_.hasNext() );
		pval_ = pitr_.next();
		ws_ = ws;
		witr_ = ws_.listIterator();
		assert( witr_.hasNext() );
	}

	@Override
	public boolean hasNext()
	{
		return pitr_.hasNext() || witr_.hasNext();
	}
	
	private Environment makeEnvironment()
	{
		final Environment env = new Environment.Builder()
			.root_directory( new File( pdir_, "w" + widx_ ) )
			.rng( new MersenneTwister( master_.rng.nextInt() ) )
			.finish();
		return env;
	}

	@Override
	public ExperimentalSetup<P, W> next()
	{
		if( first_ ) {
			pdir_ = new File( master_.root_directory, "p" + pidx_ + "_" + pval_.toString() );
			pdir_.mkdir();
			first_ = false;
		}
		if( !witr_.hasNext() ) {
			if( pitr_.hasNext() ) {
				pval_ = pitr_.next();
				++pidx_;
				pdir_ = new File( master_.root_directory, "p" + pidx_ + "_" + pval_.toString() );
				pdir_.mkdir();
				witr_ = ws_.listIterator();
				widx_ = 0;
			}
			else {
				throw new AssertionError( "No such element" );
			}
		}
		final Environment env = makeEnvironment();
		env.root_directory.mkdir();
		++widx_;
		return new ExperimentalSetup<P, W>( env, pval_, witr_.next().copy() );
	}
}
