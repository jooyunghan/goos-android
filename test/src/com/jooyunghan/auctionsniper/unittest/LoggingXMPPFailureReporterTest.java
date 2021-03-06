package com.jooyunghan.auctionsniper.unittest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.jooyunghan.auctionsniper.xmpp.LoggingXMPPFailureReporter;

public class LoggingXMPPFailureReporterTest extends TestCase {
	private final Logger logger = mock(Logger.class);
	private final LoggingXMPPFailureReporter reporter = new LoggingXMPPFailureReporter(
			logger);

	public void testWritesMessageTranslationFailureToLog() throws Exception {
		final String auctionId = "auction id";
		final String failedMessage = "bad message";
		final Exception exception = new Exception("bad");
		reporter.cannotTranslateMessage(auctionId, failedMessage, exception);
		verify(logger).error(
				"<auction id> Could not translate message \"bad message\" "
						+ "because \"java.lang.Exception: bad\"");
	}
}
