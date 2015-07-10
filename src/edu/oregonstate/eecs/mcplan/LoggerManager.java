/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;

/**
 * @author jhostetler
 *
 */
public class LoggerManager
{
	private static final Map<String, Logger> loggers = new HashMap<String, Logger>();
	
	public static Logger getLogger( final String name )
	{
		Logger logger = loggers.get( name );
		if( logger == null ) {
			logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger( name );
			
//			%d{HH:mm:ss.SSS} %-4relative %-5level %logger{35} - %msg%n
			final LoggerContext loggerContext = logger.getLoggerContext();
		    // we are not interested in auto-configuration
		    loggerContext.reset();
		
		    final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		    encoder.setContext(loggerContext);
//		    encoder.setPattern( "%-5level %logger{35}: %message%n" );
		    encoder.setPattern( "%message%n" );
		    encoder.start();
		    
		    final ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<ILoggingEvent>();
		    appender.setContext(loggerContext);
		    appender.setEncoder(encoder);
		    appender.start();
		
		    logger.addAppender(appender);


			loggers.put( name, logger );
		}
		return logger;
	}
}
