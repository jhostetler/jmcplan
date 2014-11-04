package edu.oregonstate.eecs.mcplan.rddl;

import java.util.ArrayList;

import rddl.RDDL.PVAR_INST_DEF;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

public class RDDLAction implements VirtualConstructor<RDDLAction>
{
	public final ArrayList<PVAR_INST_DEF> pvar_list;
	
	public RDDLAction( final ArrayList<PVAR_INST_DEF> action )
	{
		this.pvar_list = action;
	}

	@Override
	public int hashCode()
	{
		return pvar_list.hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		return pvar_list.equals( ((RDDLAction)obj).pvar_list );
	}

	@Override
	public String toString()
	{
		return pvar_list.toString();
	}

	@Override
	public RDDLAction create()
	{
		return new RDDLAction( pvar_list );
	}
}
