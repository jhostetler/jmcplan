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

import java.util.Arrays;

import edu.oregonstate.eecs.mcplan.ActionGenerator;
import edu.oregonstate.eecs.mcplan.JointAction;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class DelegateStateNode<S, X extends Representation<S>, A extends VirtualConstructor<A>>
	extends MutableStateNode<S, X, A>
{
	private final BackupRule<X, A> backup_;
	private final double[] default_value_;
	
	// FIXME: Is the default_value mechanism a good way of handling this?
	public DelegateStateNode( final BackupRule<X, A> backup, final double[] default_value,
							  final X token, final int nagents, final int[] turn,
							  final ActionGenerator<S, JointAction<A>> action_gen )
	{
		super( token, nagents, turn, action_gen );
		backup_ = backup;
		default_value_ = default_value;
	}

//	@Override
	public double[] v()
	{
		// FIXME: This is a hack to avoid NP exception when backing up
		// un-expanded state nodes.
		if( successors().hasNext() ) {
			return backup_.apply( this );
		}
		else {
//			return Arrays.copyOf( default_value_, default_value_.length );
			return Arrays.copyOf( vhat_, nagents );
		}
	}
}
