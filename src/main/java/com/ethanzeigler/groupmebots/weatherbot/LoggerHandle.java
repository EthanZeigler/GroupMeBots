package com.ethanzeigler.groupmebots.weatherbot;

import com.ethanzeigler.groupmebots.GroupMeBots;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by Ethan on 2/6/17.
 */
public class LoggerHandle extends Handler {
    private PrintStream printStream;

    public LoggerHandle(DateTime dateTime) {
        this.printStream = printStream;
        // create log file
        File file = new File(dateTime.getMonthOfYear() + "-" + dateTime.getDayOfMonth() + "-" + dateTime.getYearOfCentury() + "_" + dateTime.getHourOfDay() + "-" + dateTime.getMinuteOfHour() + ".txt");
        try {
            file.createNewFile();
            printStream = new PrintStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setFormatter(new Formatter() {
            /**
             * Format the given log record and return the formatted string.
             * <p>
             * The resulting formatted String will normally include a
             * localized and formatted version of the LogRecord's message field.
             * It is recommended to use the {@link Formatter#formatMessage}
             * convenience method to localize and format the message field.
             *
             * @param record the log record to be formatted.
             * @return the formatted log record
             */
            @Override
            public String format(LogRecord record) {
                return "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS [%4$-6s] %5$s%6$s%n";
            }
        });
    }

    @Override
    public void publish(LogRecord record) {
        printStream.print(record.getMessage());
        flush();
    }

    @Override
    public void flush() {
        printStream.flush();
    }

    @Override
    public void close() throws SecurityException {
        printStream.close();
    }
}
