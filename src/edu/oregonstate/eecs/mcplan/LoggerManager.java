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
	static {
		// Erase default configuration
		((LoggerContext) LoggerFactory.getILoggerFactory()).reset();
	}
	
	private static final Map<String, Logger> loggers = new HashMap<String, Logger>();
	
	public static Logger getLogger( final String name )
	{
		Logger logger = loggers.get( name );
		if( logger == null ) {
			logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger( name );
			
//			%d{HH:mm:ss.SSS} %-4relative %-5level %logger{35} - %msg%n
			final LoggerContext loggerContext = logger.getLoggerContext();
		
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
