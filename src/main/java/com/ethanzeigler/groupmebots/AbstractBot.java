package com.ethanzeigler.groupmebots;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.MultipartBody;
import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by Ethan on 1/29/17.
 */
public abstract class AbstractBot {
    public void init() {
        MultipartBody field = Unirest.post("https://api.groupme.com/v3/bots?token=5wE5f2uhl4IzlBm9G3yAW0De1EEPgg41SQdszYun")
                .field("name", getBotName())
                .field("group_id", getGroupID());

        if (getAvatarURL() != null) {
            field.field("avatar_url", getAvatarURL());
        }

        try {
            field.asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the bot starts
     */
    public abstract void onStart(DateTime dateTime);

    /**
     * Called on refresh
     * @param dateTime
     */
    public abstract void onRefresh(DateTime dateTime);

    public abstract String getBotID();

    public abstract String getBotName();

    public abstract String getAvatarURL();

    public abstract String getCallbackURL();

    public abstract String getGroupID();

    public void postMessage(String message) throws UnirestException {
        Unirest.post("https://api.groupme.com/v3/bots/post")
                .field("bot_id", getBotID())
                .field("text", message)
                .asJson();

        System.out.println(GroupMeBot.getTimeStamp(GroupMeBot.getDateTime()) + "Posted Message: \"" + message + "\"");
    }
}
