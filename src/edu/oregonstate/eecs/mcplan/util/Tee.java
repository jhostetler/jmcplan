/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Turns two OutputStreams into one OutputStream.
 * 
 * @author jhostetler
 */
public final class Tee extends OutputStream
{
	private final OutputStream a_;
	private final OutputStream b_;
	
	/**
	 * 
	 */
	public Tee( final OutputStream a, final OutputStream b )
	{
		super();
		a_ = a;
		b_ = b;
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write( final int b ) throws IOException
	{
		a_.write( b );
		b_.write( b );
	}
	
	@Override
	public void close() throws IOException
	{
		super.close();
		a_.close();
		b_.close();
	}
	
	@Override
	public void flush() throws IOException
	{
		super.flush();
		a_.flush();
		b_.flush();
	}

}
