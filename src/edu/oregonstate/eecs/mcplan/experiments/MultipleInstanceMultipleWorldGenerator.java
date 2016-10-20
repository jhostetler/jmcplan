/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
