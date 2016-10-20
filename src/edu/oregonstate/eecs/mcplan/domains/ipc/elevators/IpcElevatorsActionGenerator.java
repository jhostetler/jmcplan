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
package edu.oregonstate.eecs.mcplan.domains.ipc.elevators;

import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public final class IpcElevatorsActionGenerator extends Generator<IpcElevatorsAction>
{
	private final int[] idx;
	private boolean has_next = true;
	
	public IpcElevatorsActionGenerator( final IpcElevatorsParameters params )
	{
		idx = new int[params.Nelevators];
	}
	
	@Override
	public boolean hasNext()
	{
		return has_next;
	}

	@Override
	public IpcElevatorsAction next()
	{
		final IpcElevatorsAction.Type[] sig = new IpcElevatorsAction.Type[idx.length];
		for( int t = 0; t < idx.length; ++t ) {
			sig[t] = IpcElevatorsAction.Type.values()[idx[t]];
		}
		final IpcElevatorsAction a = new IpcElevatorsAction( sig );
		int i = 0;
		while( i < idx.length && idx[i] == IpcElevatorsAction.Type.values().length - 1 ) {
			idx[i] = 0;
			++i;
		}
		if( i == idx.length ) {
			has_next = false;
		}
		else {
			idx[i] += 1;
		}
		return a;
	}

}
