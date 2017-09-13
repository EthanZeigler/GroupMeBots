package com.ethanzeigler.groupmebots;


import org.json.simple.JSONObject;

/**
 * Created by Ethan on 2/2/17.
 */
public class PrivateKeys {
    private JSONObject object;

    public PrivateKeys(JSONObject object) {
        this.object = object;
    }

    public String getKey(String... path) {
        JSONObject temp = object;
        for (int i = 0; i < path.length; i++) {
            if (i + 1 == path.length) {
                return (String) temp.get(path[i]);
            } else {
                temp = (JSONObject) temp.get(path[i]);
            }
        }
        return null;
    }
}
