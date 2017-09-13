package com.ethanzeigler.groupmebots.weatherbot;

import com.ethanzeigler.groupmebots.GroupMeBots;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Ethan on 1/30/17.
 */
public class Forecast {
    private JSONObject json;
    private Forecast() {

    }

    public static Forecast fetchForecast() throws UnirestException {
        Forecast forecast = new Forecast();
        String latitude = GroupMeBots.getPrivateKeys().getKey("forecast.io", "latitude");
        String longitude = GroupMeBots.getPrivateKeys().getKey("forecast.io", "longitude");
        String key = GroupMeBots.getPrivateKeys().getKey("forecast.io", "api_key");

        HttpResponse<JsonNode> response =
                Unirest.get(String.format("https://api.darksky.net/forecast/%s/%s,%s", key, latitude, longitude))
                        .asJson();

        forecast.json = response.getBody().getObject();

        return forecast;
    }

    public String getDaySummary(int i) {
        return getDaily(i).getString("summary");
    }

    public double getDayLow(int i) {
        return getDaily(i).getDouble("temperatureMin");
    }

    public double getDayHigh(int i) {
        return getDaily(i).getDouble("temperatureMax");
    }

    public DateTime getDayLowTime(int i) {
        return GroupMeBots.getDateTime(getDaily(i).getLong("temperatureMinTime"));
    }

    public DateTime getDayHighTime(int i) {
        return GroupMeBots.getDateTime(getDaily(i).getLong("temperatureMaxTime"));
    }

    public double getHourlyPercipIntensity(int hour) {
        try {
            Double intensity = getHourly(hour).getDouble("precipIntensity");
            return intensity;
        } catch (JSONException e) {
            return 0;
        }
    }

    public double getHourlyPercipChance(int hour) {
        return getHourly(hour).getDouble("precipProbability");
    }

    /**
     * Gets humidity
     * @param i day today being 0
     * @return humidity as a decimal from 0 to 1
     */
    public double getDayHumidity(int i) {
        return getDaily(i).getDouble("humidity");
    }

    public JSONObject getDaily(int i) {
        return json.getJSONObject("daily").getJSONArray("data").getJSONObject(i);
    }

    public JSONObject getHourly(int i) {
        return json.getJSONObject("hourly").getJSONArray("data").getJSONObject(i);
    }

    public Set<SevereAlert> getSevereAlerts() {
        if (json.has("alerts")) {
            JSONArray alerts = json.getJSONArray("alerts");
            Set<SevereAlert> alertsSet = new HashSet<>();
            for (int i = 0; i < alerts.length(); i++) {
                alertsSet.add(new SevereAlert(alerts.getJSONObject(i)));
            }
            return alertsSet;
        } else {
            return new HashSet<>();
        }
    }

    public String toString() {
        return json.toString();
    }
}
