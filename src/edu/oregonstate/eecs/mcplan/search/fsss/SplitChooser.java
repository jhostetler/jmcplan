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

package edu.oregonstate.eecs.mcplan.search.fsss;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

public interface SplitChooser<S extends State, A extends VirtualConstructor<A>>
{
	public static interface Factory<S extends State, A extends VirtualConstructor<A>>
	{
		public abstract SplitChooser<S, A> createSplitChooser(
			final FsssParameters parameters, final FsssModel<S, A> model );
	}
	
	/**
	 * Chooses an attribute and value to split on. Must return 'null' if no
	 * more refinements should be attempted on 'aan'. Must return a non-null
	 * SplitChoice with non-null .dn and null .split member to indicate that no
	 * more refinements should be attempted for the .dn member.
	 * 
	 * @param aan
	 * @return
	 */
	public abstract SplitChoice<S, A> chooseSplit( final FsssAbstractActionNode<S, A> aan );
	
	/**
	 * Returns an attribute and value to split on. Returns 'null' if no
	 * attribute-value pair partitions the state into two non-empty sets.
	 * @param asn
	 * @return
	 */
	public abstract Split chooseSplit( final DataNode<S, A> dn );
}