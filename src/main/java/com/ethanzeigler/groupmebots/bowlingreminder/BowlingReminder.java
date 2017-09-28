package com.ethanzeigler.groupmebots.bowlingreminder;

import com.ethanzeigler.groupmebots.AbstractBot;
import com.ethanzeigler.groupmebots.GroupMeBots;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.time.DayOfWeek;

/**
 * Created by Ethan on 2/16/17.
 */
public class BowlingReminder extends AbstractBot {
    private int lastDay = -1;

    /**
     * Called when the bot starts
     *
     * @param dateTime
     */
    @Override
    public void onStart(DateTime dateTime) {
        lastDay = dateTime.getDayOfYear() - 1;
        if (dateTime.getHourOfDay() >= 19 &&
                (dateTime.getDayOfWeek() == DateTimeConstants.TUESDAY ||
                dateTime.getDayOfWeek() == DateTimeConstants.THURSDAY)) {
            lastDay += 1;
        }/* else if (dateTime.getDayOfWeek() == DateTimeConstants.SATURDAY
                && dateTime.getHourOfDay() >= 16) {
           lastDay += 1;
        }*/
    }

    /**
     * Called on refresh
     *
     * @param dateTime
     */
    @Override
    public void onRefresh(DateTime dateTime) {
        try {
            if (lastDay != dateTime.getDayOfYear()) {
                // has not announced today
                if (dateTime.getHourOfDay() >= 19) {
                    // it is 7. Check
                    if (dateTime.getDayOfWeek() == DateTimeConstants.TUESDAY) {
                        // it's tuesday. Post a normal reminder
                        postReminder();
                    } else if (dateTime.getDayOfWeek() == DateTimeConstants.THURSDAY) {
                        // it's thursday. There may or may not be practice
                        /*if (dateTime.getDayOfMonth() < 7) {
                            // it's the first of the month. No practice
                            postNoPracticeReminder();
                        } else {*/
                            // it's not the first of the month. Practice
                            postReminder();
                        //}
                    }
                }/* else if (dateTime.getDayOfWeek() == DateTimeConstants.SATURDAY && dateTime.getHourOfDay() >= 16) {
                    // it's saturday and it's after 4
                    postReminder();
                }*/
            }
        } catch (UnirestException e) {
            GroupMeBots.log("[BowlingReminder] Error: could not post reminder");
            GroupMeBots.log(e, dateTime);
        }
    }

    private void postReminder() throws UnirestException {
        lastDay = GroupMeBots.getDateTime().getDayOfYear();
        postMessage("Reminder: Practice is in 24 hours. All practice forms must be completed now!");
    }

    private void postNoPracticeReminder() throws UnirestException {
        lastDay = GroupMeBots.getDateTime().getDayOfYear();
        postMessage("Reminder: It's the first week of the month. No practice tomorrow.");
    }

    @Override
    public String getBotID() {
        return GroupMeBots.getPrivateKeys().getKey("bowling_reminders:production", "bot_id");
    }

    @Override
    public String getBotName() {
        return "Reminders";
    }

    @Override
    public String getAvatarURL() {
        return null;
    }

    @Override
    public String getCallbackURL() {
        return null;
    }

    @Override
    public String getGroupID() {
        return GroupMeBots.getPrivateKeys().getKey("bowling_reminders:production", "group_id");
    }
}
