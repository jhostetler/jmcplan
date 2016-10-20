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

/**
 * @author jhostetler
 *
 */
public final class ExperimentalSetup<P, W>
{
	public final class Builder
	{
		private Environment environment_ = null;
		private P parameters_ = null;
		private W world_ = null;
		
		public Builder environment( final Environment e ) { environment_ = e; return this; }
		public Builder parameters( final P p ) { parameters_ = p; return this; }
		public Builder world( final W w ) { world_ = w; return this; }
		
		public ExperimentalSetup<P, W> finish()
		{
			return new ExperimentalSetup<P, W>( environment_, parameters_, world_ );
		}
	}
	
	// -----------------------------------------------------------------------
	
	public final Environment environment;
	public final P parameters;
	public final W world;
	
	public ExperimentalSetup( final Environment environment, final P parameters, final W world )
	{
		this.environment = environment;
		this.parameters = parameters;
		this.world = world;
	}
}
