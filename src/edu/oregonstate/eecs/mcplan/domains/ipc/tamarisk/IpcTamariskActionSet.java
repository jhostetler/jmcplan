/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.tamarisk;

/**
 * @author jhostetler
 *
 */
public enum IpcTamariskActionSet
{
	Nothing {
		@Override
		public IpcTamariskAction create( final int reach )
		{
			return new IpcTamariskAction( this, reach );
		}
	},
	
	Eradicate {
		@Override
		public IpcTamariskAction create( final int reach )
		{
			return new IpcTamariskAction( this, reach );
		}
	},
	
	Restore {
		@Override
		public IpcTamariskAction create( final int reach )
		{
			return new IpcTamariskAction( this, reach );
		}
	};
	
	public abstract IpcTamariskAction create( final int reach );
}
