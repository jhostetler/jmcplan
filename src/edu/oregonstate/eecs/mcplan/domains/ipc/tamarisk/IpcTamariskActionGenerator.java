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
package edu.oregonstate.eecs.mcplan.domains.ipc.tamarisk;

import edu.oregonstate.eecs.mcplan.util.Generator;

/**
 * @author jhostetler
 *
 */
public class IpcTamariskActionGenerator extends Generator<IpcTamariskAction>
{
	private final int Nreaches;
	private int reach = 0;
	private int i = 0;
	
	public IpcTamariskActionGenerator( final IpcTamariskParameters params )
	{
		Nreaches = params.Nreaches;
	}

	@Override
	public boolean hasNext()
	{
		// +1 to include Nothing action
		return i < (2*Nreaches + 1);
	}

	@Override
	public IpcTamariskAction next()
	{
		final IpcTamariskAction a;
		if( i == 0 ) {
			a = IpcTamariskActionSet.Nothing.create( 0 );
		}
		else if( (i & 1) == 1 ) { // Start with == 1 because i > 0
			a = IpcTamariskActionSet.Eradicate.create( reach );
		}
		else {
			a = IpcTamariskActionSet.Restore.create( reach );
			reach += 1;
		}
		i += 1;
		return a;
	}
}
