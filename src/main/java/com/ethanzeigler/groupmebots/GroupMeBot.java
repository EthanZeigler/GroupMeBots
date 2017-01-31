package com.ethanzeigler.groupmebots;

import com.ethanzeigler.groupmebots.weatherbot.WeatherBot;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by Ethan on 1/29/17.
 */
public class GroupMeBot {
    public static void main(String[] args) {
        DateTime dateTime = GroupMeBot.getDateTime();
        File file = new File(dateTime.getMonthOfYear() + "-" + dateTime.getDayOfMonth() + "-" + dateTime.getYearOfCentury() + "_" + dateTime.getHourOfDay() + "-" + dateTime.getMinuteOfHour() + ".txt");
        try {
            file.createNewFile();
            PrintStream printStream = new PrintStream(file);
            System.setOut(printStream);
            System.out.println("Created File named");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(getTimeStamp(GroupMeBot.getDateTime()) + "Creating bots...");
        Set<AbstractBot> bots = new HashSet<>();
        bots.add(new WeatherBot());

        System.out.println(getTimeStamp(GroupMeBot.getDateTime()) + "Enabling bots");
        for (AbstractBot bot : bots) {
            bot.init();
            bot.onStart(GroupMeBot.getDateTime());
        }

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable updater = ()-> {
            try {
                for (AbstractBot bot : bots) {
                    System.out.println(getTimeStamp(getDateTime()) + "Begin updating bots...");
                    bot.onRefresh(getDateTime());
                    System.out.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        final ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(updater, 5, 5, TimeUnit.MINUTES);
    }

    public static String getTimeStamp(DateTime dateTime) {
        return String.format("[%s/%s/%s %s:%s.%s] ", dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), dateTime.getYearOfCentury(), dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), dateTime.getSecondOfMinute());
    }

    public static DateTime getDateTime() {
        return new DateTime().withZone(DateTimeZone.forID("EST"));
    }
}
