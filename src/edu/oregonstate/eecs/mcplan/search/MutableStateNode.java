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
package edu.oregonstate.eecs.mcplan.search;

import java.util.HashMap;
import java.util.Map;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.util.Generator;
import edu.oregonstate.eecs.mcplan.util.MeanVarianceAccumulator;

/**
 * @author jhostetler
 *
 */
public abstract class MutableStateNode<S, A extends VirtualConstructor<A>>
	extends StateNode<S, A>
{
	protected final Map<JointAction<A>, MutableActionNode<S, A>> a_
		= new HashMap<JointAction<A>, MutableActionNode<S, A>>();
	protected int n_ = 0;
	protected final MeanVarianceAccumulator[] rv_;
	
	public final ActionGenerator<S, JointAction<A>> action_gen_;

	public MutableStateNode( /*final Representation<S> x,*/ final int nagents, final int[] turn,
							 final ActionGenerator<S, JointAction<A>> action_gen )
	{
		super( /*x,*/ nagents, turn );
		rv_ = new MeanVarianceAccumulator[nagents];
		for( int i = 0; i < nagents; ++i ) {
			rv_[i] = new MeanVarianceAccumulator();
		}
		action_gen_ = action_gen;
	}
	
	public abstract MutableActionNode<S, A> successor(
			final JointAction<A> a, final int nagents, final Representer<S, ? extends Representation<S>> repr );
	
	public void visit()
	{ n_ += 1; }
	
	@Override
	public int n()
	{ return n_; }
	
	@Override
	public double[] r()
	{
		final double[] r = new double[rv_.length];
		for( int i = 0; i < r.length; ++i ) {
			r[i] = rv_[i].mean();
		}
		return r;
	}
	
	@Override
	public double r( final int i )
	{
		return rv_[i].mean();
	}
	
	@Override
	public double[] rvar()
	{
		final double[] r = new double[rv_.length];
		for( int i = 0; i < r.length; ++i ) {
			r[i] = rv_[i].variance();
		}
		return r;
	}
	
	@Override
	public double rvar( final int i )
	{
		return rv_[i].variance();
	}
	
	public void updateR( final double[] r )
	{
		assert( r.length == rv_.length );
		for( int i = 0; i < r.length; ++i ) {
			rv_[i].add( r[i] );
		}
	}
	
	public void attachSuccessor( final JointAction<A> a, final MutableActionNode<S, A> node )
	{
		a_.put( a, node );
	}
	
	public Map<JointAction<A>, MutableActionNode<S, A>> successor_map()
	{
		return a_;
	}
	
	@Override
	public Generator<MutableActionNode<S, A>> successors()
	{
		return Generator.fromIterator( a_.values().iterator() );
	}
	
	@Override
	public MutableActionNode<S, A> getActionNode( final JointAction<A> a )
	{
		final MutableActionNode<S, A> an = a_.get( a );
		return an;
	}
	
//	@Override
//	public String toString()
//	{
//		return "StateNode[" + x + "]";
//	}

}
