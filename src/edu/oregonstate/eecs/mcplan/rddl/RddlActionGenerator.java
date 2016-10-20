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
package edu.oregonstate.eecs.mcplan.rddl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import rddl.EvalException;
import rddl.RDDL.PVAR_INST_DEF;
import edu.oregonstate.eecs.mcplan.ActionGenerator;

/**
 * @author jhostetler
 *
 */
public class RddlActionGenerator extends ActionGenerator<RDDLState, RDDLAction>
{
	private final RddlSpec spec;
	
	public RddlActionGenerator( final RddlSpec spec )
	{
		this.spec = spec;
	}
	
	// -----------------------------------------------------------------------
	
	private TreeMap<String, ArrayList<PVAR_INST_DEF>> actions = null;
	private Iterator<Map.Entry<String, ArrayList<PVAR_INST_DEF>>> next = null;
	
	@Override
	public RddlActionGenerator create()
	{
		return new RddlActionGenerator( spec );
	}

	@Override
	public void setState( final RDDLState s, final long t )
	{
		try {
			actions = rddl.ActionGenerator.getLegalBoolActionMap( s );
			next = actions.entrySet().iterator();
		}
		catch( final EvalException ex ) {
			throw new RuntimeException( ex );
		}
	}

	@Override
	public int size()
	{
		return actions.size();
	}

	@Override
	public boolean hasNext()
	{
		return next.hasNext();
	}

	@Override
	public RDDLAction next()
	{
		return new RDDLAction( next.next().getValue() );
	}
}
