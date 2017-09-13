package com.ethanzeigler.groupmebots.weatherbot;

import com.ethanzeigler.groupmebots.AbstractBot;
import com.ethanzeigler.groupmebots.GroupMeBots;
import com.ethanzeigler.groupmebots.ProductionLevel;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Ethan on 1/29/17.
 */
public class WeatherBot extends AbstractBot {
    private int lastDayOfMorningAnnounce;
    private boolean rainedLastHour = false;
    private int lastHourOfUpdate = -1;
    private ProductionLevel productionLevel;

    private Set<SevereAlert> cachedAlerts = new HashSet<>();

    public WeatherBot(ProductionLevel productionLevel) {
        this.productionLevel = productionLevel;
    }

    /**
     * Called when the bot starts
     *
     * @param dateTime
     */
    @Override
    public void onStart(DateTime dateTime) {
//        try {
//            postMessage("Just restarting, don't worry about me.");
//        } catch (UnirestException e) {
//            GroupMeBots.log(e, dateTime);
//        }

        if (dateTime.getHourOfDay() >= 7) {
            lastDayOfMorningAnnounce = dateTime.getDayOfYear();
        } else {
            lastDayOfMorningAnnounce = dateTime.getDayOfYear() - 1;
        }
        lastHourOfUpdate = dateTime.getHourOfDay();
    }

    /**
     * Called on refresh
     *
     * @param dateTime
     */
    @Override
    public void onRefresh(DateTime dateTime) {
        if (lastDayOfMorningAnnounce != dateTime.getDayOfYear()) {
            if (dateTime.getHourOfDay() > 6) {
                GroupMeBots.log("Fetching announce data");
                announceMorningWeatherWunder(dateTime);
                lastDayOfMorningAnnounce = dateTime.getDayOfYear();
            }
        }

        if (dateTime.getHourOfDay() > 6 && dateTime.getHourOfDay() < 23) {
            if (lastHourOfUpdate != dateTime.getHourOfDay()) {
                lastHourOfUpdate = dateTime.getHourOfDay();
                try {
                    GroupMeBots.log("Fetching Weather data");
                    Forecast forecast = Forecast.fetchForecast();
                    GroupMeBots.log("[Weatherbot] Forecast input for rain: \n" +
                            "*******************************\n*******************************\n"
                            + forecast.toString() +
                            "\n*******************************\n*******************************\n");
                    alertCheck(forecast);
                    rainCheck(forecast, dateTime);
                } catch (UnirestException e) {
                    e.printStackTrace();
                }
            }
            // is within rain check section
        } else {
            rainedLastHour = false;
        }
    }

    private void rainCheck(Forecast forecast, DateTime dateTime) {
        double percipChance = forecast.getHourlyPercipChance(0);
        double percipIntensity = forecast.getHourlyPercipIntensity(0);
        GroupMeBots.log("[WeatherBot] Raincheck: C=" + percipChance + ", I=" + percipIntensity + ", L=" + rainedLastHour);
        if (percipChance >= 0.5 && percipIntensity > 0.01) {
            if (!rainedLastHour) {
                rainedLastHour = true;
                try {
                    postMessage("It looks like there might be rain this hour ☔️. " + (int) (percipChance * 100) + "% chance of rain at " + percipIntensity + " inches per hour.");
                } catch (UnirestException e) {
                    e.printStackTrace();
                }
            }
        } else {
            rainedLastHour = false;
        }
    }

    private void alertCheck(Forecast forecast) {
        for (SevereAlert currentAlert : forecast.getSevereAlerts()) {
            GroupMeBots.log("Alert detected: " + currentAlert.getTitle());
            if (cachedAlerts.stream().noneMatch(severeAlert -> currentAlert.isRepeatOf(severeAlert))) {
                GroupMeBots.log("[WeatherBot] Alert not previously found. Broadcasting.");
                cachedAlerts.add(currentAlert);
                broadcastAlert(currentAlert);
            }
        }

        cachedAlerts = forecast.getSevereAlerts();
    }

    private void broadcastAlert(SevereAlert alert) {
        String msg = "A severe alert has been issued:\n";
        msg += alert.getTitle() + "\n";
        msg += alert.getUrl();
        try {
            postMessage(msg);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }


    public void announceMorningWeather(DateTime dateTime) {
        try {
            Forecast forecast = Forecast.fetchForecast();
            String msg = "";

            switch (GroupMeBots.getDateTime().getDayOfWeek()) {
                case DateTimeConstants.MONDAY:
                    msg = "Garfield isn't the only one who hates mondays... College students! \uD83D\uDE09\n";
                    break;
                case DateTimeConstants.TUESDAY:
                    msg = "One day down, 4 to go! \uD83D\uDE0A\n";
                    break;
                case DateTimeConstants.WEDNESDAY:
                    msg = "HUMP DAY! Half way there, everyone! \uD83D\uDC2A\n";
                    break;
                case DateTimeConstants.THURSDAY:
                    msg = "Getting close... Friday night is in sight! \uD83D\uDE2E\n";
                    break;
                case DateTimeConstants.FRIDAY:
                    msg = "Just a couple more classes. You can do it! \uD83D\uDE03\n";
                    break;
                case DateTimeConstants.SATURDAY:
                    msg = "SATURDAY VICTORY SCREECH! (insert SpongeBob pun here)\n";
                    break;
                case DateTimeConstants.SUNDAY:
                    msg = "Enjoy one more day of freedom (taps plays in the distance)\n";
                    break;
            }

            msg += String.format("%s High of %s degrees.", forecast.getDaySummary(0), forecast.getDayHigh(0));

            postMessage(msg);

        } catch (UnirestException e) {
            GroupMeBots.log(e, dateTime);
        }
    }

    @Deprecated
    private void announceMorningWeatherWunder(DateTime dateTime) {
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

            String msg;

            switch (GroupMeBots.getDateTime().getDayOfWeek()) {
                case DateTimeConstants.MONDAY:
                    msg = "Garfield isn't the only one who hates mondays... College students! \uD83D\uDE09\n";
                    break;
                case DateTimeConstants.TUESDAY:
                    msg = "One day down, 4 to go! \uD83D\uDE0A\n";
                    break;
                case DateTimeConstants.WEDNESDAY:
                    msg = "HUMP DAY! Half way there, everyone! \uD83D\uDC2A\n";
                    break;
                case DateTimeConstants.THURSDAY:
                    msg = "Getting close... Friday night is in sight! \uD83D\uDE2E\n";
                    break;
                case DateTimeConstants.FRIDAY:
                    msg = "Just a couple more classes. You can do it! \uD83D\uDE03\n";
                    break;
                case DateTimeConstants.SATURDAY:
                    msg = "SATURDAY VICTORY SCREECH! (insert SpongeBob pun here)\n";
                    break;
                case DateTimeConstants.SUNDAY:
                    msg = "Enjoy one more day of freedom (taps plays in the distance)\n";
                    break;
                default:
                    msg = "";
            }

            msg += String.format("%nDay: %s%nNight: %s%n", daySummary, nightSummary);
            if (dayRain > 0) {
                msg = msg.concat("Bring an umbrella! Total daytime rain: " + dayRain + '\n');
            }
            if (nightRain > 0) {
                msg = msg.concat("Close your window tonight! Total nighttime rain: " + nightRain + '\n');
            }
            if (daySnow > 0) {
                msg = msg.concat("Woo! Snow Day! Daytime snow: " + daySnow + '\n');
            }
            if (nightSnow > 0) {
                msg = msg.concat("Put your PJs on backwards! Nighttime snow: " + nightSnow + '\n');
            }
            if (avgHumidity > 69) {
                msg = msg.concat("It's going to be a humid one... : " + avgHumidity + "% avg. humidity" + '\n');
            }

            postMessage(msg);
            GroupMeBots.log("Morning weather announcement JSON return:\n");
            GroupMeBots.log(response.getBody().toString());

        } catch (UnirestException e) {
            try {
                postMessage("Oops. Can't connect to our weather overlords. Get on it, Ethan!");
            } catch (UnirestException e1) {
                GroupMeBots.log(e1, dateTime);
            }
        } catch (JSONException e) {
            try {
                postMessage("The internet seems to be spewing random nonsense at me... Ethan will look into it.");
                GroupMeBots.log(e, dateTime);
            } catch (UnirestException e1) {
                GroupMeBots.log(e1, dateTime);
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
        switch (productionLevel) {
            case DEVELOPMENT: return GroupMeBots.getPrivateKeys().getKey("weatherbot:development", "bot_id");
            case PRODUCTION: return GroupMeBots.getPrivateKeys().getKey("weatherbot:production", "bot_id");
            default: return null;
        }
    }

    @Override
    public String getBotName() {
        return "WeatherBot ☀️";
    }

    @Override
    public String getAvatarURL() {
        // this doesn't appear to be working correctly programmatically, so I'm nullifying the return, which is ignored.
        //return "https://upload.wikimedia.org/wikipedia/commons/thumb/2/20/Weather_Rounded.svg/200px-Weather_Rounded.svg.png";
        return null;
    }

    @Override
    public String getCallbackURL() {
        return null;
    }

    @Override
    public String getGroupID() {
        switch (productionLevel) {
            case DEVELOPMENT: return GroupMeBots.getPrivateKeys().getKey("weatherbot:development", "group_id");
            case PRODUCTION: return GroupMeBots.getPrivateKeys().getKey("weatherbot:production", "group_id");
            default: return null;
        }
    }
}
