package edu.oregonstate.eecs.mcplan.experiments;

import java.io.PrintStream;

public interface CsvWriter
{
	public abstract void writeCsv( final PrintStream out );
}
