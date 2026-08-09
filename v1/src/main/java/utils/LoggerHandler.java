package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class LoggerHandler {

    public static final Logger logger = Logger.getLogger(LoggerHandler.class);

    static {
        try {
            String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            String logFileName = "logs/logfile" + timestamp + ".log";
            FileAppender fileAppender = new FileAppender(
                    new PatternLayout("%d{ISO8601} %-5p %c - %m%n"), logFileName, true);
            logger.addAppender(fileAppender);
        } catch (Exception e) {
            logger.error("Failed to initiate logger file appender");
        }
    }

    /** Logs an informational message — use for normal step-by-step flow updates.
     *  Appears at INFO level in the log file with an ISO8601 timestamp prefix.
     *  Usage: logging.info("Clicked the Submit button"); */
    public void info(String s) {
        logger.info(s);
    }

    /** Logs a warning — use when something unexpected happened but the test can continue.
     *  Appears at WARN level; won't fail the test but should be investigated.
     *  Usage: logging.warn("Element was stale, retrying..."); */
    public void warn(String s) {
        logger.warn(s);
    }

    /** Logs a debug message — fine-grained detail, useful when diagnosing issues locally.
     *  Appears at DEBUG level; typically filtered out in CI log configs.
     *  Usage: logging.debug("Raw cell value before parsing: " + rawValue); */
    public void debug(String s) {
        logger.debug(s);
    }

    /** Logs a fatal message — use for unrecoverable errors that will halt the suite.
     *  Appears at FATAL level; signals the highest-severity failure in the log.
     *  Usage: logging.fatal("WebDriver could not be initialised"); */
    public void fatal(String s) {
        logger.fatal(s);
    }

    /** Logs a trace message — lowest level, use for very granular execution tracing.
     *  Appears at TRACE level; typically only enabled during deep debugging sessions.
     *  Usage: logging.trace("Entering readAllRows, sheet index 0"); */
    public void trace(String s) {
        logger.trace(s);
    }

    /** Logs an error message — use inside catch blocks when an action fails.
     *  Appears at ERROR level; pairs with a captureFailure screenshot in WebDriverHelper.
     *  Usage: logging.error("Failed to click [Login button]: " + e.getMessage()); */
    public void error(String s) {
        logger.error(s);
    }
}
