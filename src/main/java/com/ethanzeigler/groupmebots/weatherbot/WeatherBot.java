package com.ethanzeigler.groupmebots.weatherbot;

import com.ethanzeigler.groupmebots.AbstractBot;
import com.ethanzeigler.groupmebots.GroupMeBot;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ethan on 1/29/17.
 */
public class WeatherBot extends AbstractBot {
    private int lastDayOfMorningAnnounce;


    /**
     * Called when the bot starts
     *
     * @param dateTime
     */
    @Override
    public void onStart(DateTime dateTime) {
        try {
            postMessage("Just restarting, don't worry about me... (I hope it isn't because I spammed you \uD83D\uDE30)");
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        if (dateTime.getHourOfDay() >= 7) {
            lastDayOfMorningAnnounce = dateTime.getDayOfYear();
        } else {
            lastDayOfMorningAnnounce = dateTime.getDayOfYear() - 1;
        }
    }

    /**
     * Called on refresh
     *
     * @param dateTime
     */
    @Override
    public void onRefresh(DateTime dateTime) {
        System.out.println(GroupMeBot.getTimeStamp(dateTime) + "[WeatherBot] LastDay: " + lastDayOfMorningAnnounce + ", Current Day: " + dateTime.getDayOfMonth() + ", Hour: " + dateTime.getHourOfDay() + " vs > 6");
        if (lastDayOfMorningAnnounce != dateTime.getDayOfYear()) {
            if (dateTime.getHourOfDay() > 6) {
                announceMorningWeather(dateTime);
                lastDayOfMorningAnnounce = dateTime.getDayOfYear();
            }
        }
    }

    private void announceMorningWeather(DateTime dateTime) {
        try {
            HttpResponse<JsonNode> response =
                    Unirest.get("http://api.wunderground.com/api/d9f925a1da9be828/forecast/q/NJ/ewing.json")
                    .asJson();

            JSONObject data = response.getBody().getObject().getJSONObject("forecast");
            // get forecast string
            String daySummary = data.getJSONObject("txt_forecast").getJSONArray("forecastday").getJSONObject(0).getString("fcttext");
            String nightSummary = data.getJSONObject("txt_forecast").getJSONArray("forecastday").getJSONObject(1).getString("fcttext");
            double dayRain = getSimpleForecastNumeral(data, "qpf_day", "in");
            double nightRain = getSimpleForecastNumeral(data,"qpf_night", "in");
            double daySnow = getSimpleForecastNumeral(data,"snow_day", "in");
            double nightSnow = getSimpleForecastNumeral(data,"snow_night", "in");
            double avgHumidity = getSimpleForecastNumeral(data,"qpf_day", "in");

            String message = String.format("Good morning \uD83D\uDE0A%nDay: %s%nNight: %s%n", daySummary, nightSummary);
            if (dayRain > 0) {
                message = message.concat("Bring an umbrella! Total daytime rain: " + dayRain + '\n');
            }
            if (nightRain > 0) {
                message = message.concat("Close your window tonight! Total nighttime rain: " + nightRain + '\n');
            }
            if (daySnow > 0) {
                message = message.concat("Woo! Snow Day! Daytime snow: " + daySnow + '\n');
            }
            if (nightSnow > 0) {
                message = message.concat("Put your PJs on backwards! Nighttime snow: " + nightSnow + '\n');
            }
            if (avgHumidity > 69) {
                message = message.concat("It's going to be a humid one... : " + avgHumidity + "% avg. humidity" + '\n');
            }

            postMessage(message);
            System.out.println(GroupMeBot.getTimeStamp(dateTime) + "Morning weather announcement JSON return:\n");
            System.out.println(response.getBody().toString());

        } catch (UnirestException e) {
            try {
                postMessage("Oops. Can't connect to our weather overlords (WUnderground). Get on it, Ethan!");
            } catch (UnirestException e1) {
                e1.printStackTrace();
            }
        } catch (JSONException e) {
            try {
                postMessage("The internet seems to be spewing random nonsense at me... Ethan will look into it.");
                e.printStackTrace();
            } catch (UnirestException e1) {
                e1.printStackTrace();
            }
        }
    }

    private double getSimpleForecastNumeral(JSONObject data, String... keys) {
        try {
            JSONObject category = data.getJSONObject("simpleforecast").getJSONArray("forecastday").getJSONObject(0);

            for (int i = 0; i < keys.length; i++) {
                if (i + 1 == keys.length) {
                    return category.getDouble(keys[i]);
                } else {
                    category = category.getJSONObject(keys[i]);
                }
            }
        } catch (JSONException e) {
            return -1;
        }
        return -1;
    }

    @Override
    public String getBotID() {
        return "f4c307d3fb408a8b3ef2d93b2c";
    }

    @Override
    public String getBotName() {
        return "WeatherBot☀️";
    }

    @Override
    public String getAvatarURL() {
        return "https://upload.wikimedia.org/wikipedia/commons/thumb/2/20/Weather_Rounded.svg/200px-Weather_Rounded.svg.png";
    }

    @Override
    public String getCallbackURL() {
        return null;
    }

    @Override
    public String getGroupID() {
        return "28437540";
    }
}
