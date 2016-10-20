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

import weka.classifiers.Classifier;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.search.MctsVisitor;
import edu.oregonstate.eecs.mcplan.search.MutableActionNode;
import edu.oregonstate.eecs.mcplan.search.MutableStateNode;

/**
 * @author jhostetler
 *
 */
public class ChiUctVisitor<S, A extends VirtualConstructor<A>>
	implements MctsVisitor<S, FactoredRepresentation<S>, A>
{
	private final Classifier classifier_;
	
	public ChiUctVisitor( final Classifier classifier )
	{
		classifier_ = classifier;
	}
	
	@Override
	public void visitNode( final MutableStateNode<S, FactoredRepresentation<S>, A> sn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void leaveNode( final MutableStateNode<S, FactoredRepresentation<S>, A> sn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitNode( final MutableActionNode<S, FactoredRepresentation<S>, A> an )
	{
		if( an.)
	}

	@Override
	public void leaveNode( final MutableActionNode<S, FactoredRepresentation<S>, A> an )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startEpisode( final S s, final int nagents, final int[] turn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean startRollout( final S s, final int[] turn )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void startTree( final S s, final int[] turn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void treeAction( final JointAction<A> a, final S sprime, final int[] next_turn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void treeDepthLimit( final S s, final int[] turn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startDefault( final S s, final int[] turn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void defaultAction( final JointAction<A> a, final S sprime, final int[] next_turn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void defaultDepthLimit( final S s, final int[] turn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void depthLimit( final S s, final int[] turn )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkpoint()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean halt()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
