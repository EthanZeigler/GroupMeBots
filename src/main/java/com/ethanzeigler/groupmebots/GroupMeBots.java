package com.ethanzeigler.groupmebots;

import com.ethanzeigler.groupmebots.bowlingreminder.BowlingReminder;
import com.ethanzeigler.groupmebots.weatherbot.WeatherBot;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by Ethan on 1/29/17.
 */
public class GroupMeBots {
    public static PrivateKeys privateKeys;
    //public static Logger logger;
    //public static LoggerHandle handle;


    public static void main(String[] args) {
        DateTime dateTime = GroupMeBots.getDateTime();
        System.out.println("****************************************");

//        logger = Logger.getLogger("WeatherBotLog");
//        handle = new LoggerHandle(dateTime);
//        logger.addHandler(handle);

        // load private keys
        try {
            privateKeys = new PrivateKeys((JSONObject) JSONValue.parseWithException(
                    new BufferedReader(
                            new InputStreamReader(
                                    GroupMeBots.class.getResourceAsStream("/bot-keys.json")))));
        } catch (IOException | ParseException e) {
            log(e, getDateTime());
        }

        // initialize bots
        System.out.println(getTimeStamp(GroupMeBots.getDateTime()) + "Creating bots...");
        Set<AbstractBot> bots = new HashSet<>();

        bots.add(new WeatherBot(ProductionLevel.PRODUCTION));
        bots.add(new BowlingReminder());

        // call bot enablers
        log("Enabling bots...");
        for (AbstractBot bot : bots) {
            bot.init();
            bot.onStart(GroupMeBots.getDateTime());
        }

        // register update process
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable updater = ()-> {
            //log("Refreshing...");
            try {
                for (AbstractBot bot : bots) {
                    //log("Begin updating bot...");
                    bot.onRefresh(getDateTime());
                    //handle.flush();
                }
            } catch (Exception e) {
                log(e, getDateTime());
            }
            //log("Success");
        };

        // begin update process. Runs every 5 minutes.
        final ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(updater, 0, 5, TimeUnit.MINUTES);
    }

    public static String getTimeStamp(DateTime dateTime) {
        return String.format("[%2s/%2s/%2s %2s:%2s.%2s] ", dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), dateTime.getYearOfCentury(), dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), dateTime.getSecondOfMinute());
    }

    public static DateTime getDateTime() {
        return new DateTime().withZone(DateTimeZone.forID("EST")).plusHours(1);
    }

    public static DateTime getDateTime(long unixTime) {
        return new DateTime(unixTime * 1000L, DateTimeZone.forID("EST")).plusHours(1);
    }

    public static PrivateKeys getPrivateKeys() {
        return privateKeys;
    }

    public static void log(String msg) {
        System.out.println(getTimeStamp(getDateTime()) + msg);
        //logger.log(Level.INFO, msg);
    }

    public static void log(Throwable throwable, DateTime dateTime) {
        System.out.println(getTimeStamp(dateTime) + "Logging exception");
        throwable.printStackTrace();
        //logger.log(Level.SEVERE, "", throwable);
    }
}
