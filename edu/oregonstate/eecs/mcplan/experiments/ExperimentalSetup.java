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
