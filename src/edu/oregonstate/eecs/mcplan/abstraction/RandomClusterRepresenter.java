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
package edu.oregonstate.eecs.mcplan.abstraction;

import java.util.ArrayList;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;

/**
 * @author jhostetler
 *
 */
public class RandomClusterRepresenter<S extends State> implements Representer<S, ClusterAbstraction<S>>
{
	private final RandomGenerator rng_;
	private final int max_branching_;
	
	private final ArrayList<ClusterAbstraction<S>> clusters_ = new ArrayList<ClusterAbstraction<S>>();
	
	public RandomClusterRepresenter( final RandomGenerator rng, final int max_branching )
	{
		rng_ = rng;
		max_branching_ = max_branching;
	}
	
	@Override
	public Representer<S, ClusterAbstraction<S>> create()
	{
		return new RandomClusterRepresenter<S>( rng_, max_branching_ );
	}

	@Override
	public ClusterAbstraction<S> encode( final S s )
	{
		final int i = rng_.nextInt( max_branching_ );
		final ClusterAbstraction<S> c;
		if( i >= clusters_.size() ) {
			c = new ClusterAbstraction<S>( clusters_.size() );
		}
		else {
			c = clusters_.get( i );
		}
		return c;
	}
}
