package ginious.home.measure.util;

import java.text.MessageFormat;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

/**
 * Helper for writing log messages.
 */
public final class LogHelper {

	private enum LogLevel {
		INFO, DEBUG, WARN, ERROR;
	}

	private static void log(LogLevel inLevel, Object inSource, Throwable inError, String inMessage,
			Object... inParams) {

		String lLogMessage = MessageFormat.format(inMessage, inParams);

		Class<?> lLoggingType = inSource.getClass();
		if (inSource instanceof Class) {
			lLoggingType = (Class<?>) inSource;
		} // if
		Logger lLogger = Logger.getLogger(lLoggingType);

		if (LogLevel.DEBUG == inLevel) {
			lLogger.debug(lLogMessage);
		} else if (LogLevel.INFO == inLevel) {
			lLogger.info(lLogMessage);
		} else if (LogLevel.WARN == inLevel) {
			lLogger.warn(lLogMessage, inError);
		} else if (LogLevel.ERROR == inLevel) {
			lLogger.error(lLogMessage, inError);
		} else {
			throw new NotImplementedException("Log level not supported!");
		} // else
	}

	public static void logDebug(Object inSource, String inMessage, Object... inParams) {
		log(LogLevel.DEBUG, inSource, null, inMessage, inParams);
	}

	public static void logError(Object inSource, String inMessage, Object... inParams) {
		log(LogLevel.ERROR, inSource, null, inMessage, inParams);
	}

	public static void logError(Object inSource, Throwable inError, String inMessage, Object... inParams) {
		log(LogLevel.ERROR, inSource, inError, inMessage, inParams);
	}

	public static void logInfo(Object inSource, String inMessage, Object... inParams) {
		log(LogLevel.INFO, inSource, null, inMessage, inParams);
	}

	public static void logWarning(Object inSource, String inMessage, Object... inParams) {
		log(LogLevel.WARN, inSource, null, inMessage, inParams);
	}

	public static void logWarning(Object inSource, Throwable inError, String inMessage, Object... inParams) {
		log(LogLevel.WARN, inSource, inError, inMessage, inParams);
	}

	/**
	 * Utility constructor.
	 */
	private LogHelper() {
		super();
	}
}
