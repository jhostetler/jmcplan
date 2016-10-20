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

/**
 * Parameters of the FSSS algorithm.
 */
public class FsssParameters
{
	/**
	 * Sampling width (usually denoted C).
	 */
	public final int width;
	
	/**
	 * Maximum tree depth.
	 * <p>
	 * NOTE: A tree consisting of only the root has depth *1*. A tree with
	 * one layer of action nodes has depth *2*, etc. Depth 0 is reserved for
	 * future use.
	 */
	public final int depth;
	
	/**
	 * The search control budget.
	 */
	public final Budget budget;
	
	/**
	 * If true, use AbstractFsssNode.close() to free unneeded state nodes and
	 * conserve memory. Setting use_close = false can be useful for
	 * debugging because it allows FsssTest.validateTree() to check more things.
	 */
	public final boolean use_close;
	
	public FsssParameters( final int width, final int depth, final Budget budget, final boolean use_close )
	{
		this.width = width;
		this.depth = depth;
		this.budget = budget;
		this.use_close = use_close;
	}
}
