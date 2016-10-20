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
