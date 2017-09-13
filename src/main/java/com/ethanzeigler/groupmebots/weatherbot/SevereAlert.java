package com.ethanzeigler.groupmebots.weatherbot;

import org.json.JSONObject;

/**
 * Created by Ethan on 2/20/17.
 */
public class SevereAlert {
    private long issueTime;
    private String title;
    private String text;
    private String url;

    public SevereAlert(JSONObject alertJson) {
        issueTime = alertJson.getLong("time");
        title = alertJson.getString("title");
        text = alertJson.getString("description");
        url = alertJson.getString("uri");
    }

    public long getIssueTime() {
        return issueTime;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SevereAlert that = (SevereAlert) o;

        if (title != null ? !title.equalsIgnoreCase(that.title) : that.title != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (issueTime ^ (issueTime >>> 32));
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

    public boolean isRepeatOf(SevereAlert alert) {
        return title.equalsIgnoreCase(alert.getTitle().trim());
    }

    @Override
    public String toString() {
        return "SevereAlert{" +
                "issueTime=" + issueTime +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
