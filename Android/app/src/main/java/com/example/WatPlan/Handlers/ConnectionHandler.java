package com.example.WatPlan.Handlers;

import androidx.core.util.Pair;

import com.example.WatPlan.Models.Block;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConnectionHandler {
    private static final String baseUrl = "http://watplan.eba-ykh43jj5.eu-central-1.elasticbeanstalk.com/";
//    private static final String baseUrl = "http://10.0.2.2:8000/";
    private static final OkHttpClient client = new OkHttpClient();


    static String getAppVersion() {
        String address = "Plan/get_app_version/";
        try {
            String data = makeRequest(address, new HashMap<>());
            JSONObject jdate = new JSONObject(Objects.requireNonNull(data));
            return jdate.get("version").toString();
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
            System.out.println("bad response");
        }
        return null;
    }


    public static Map<String, Map<String, String>> getVersionMap() {
        String address = "Plan/get_versions/";
        Map<String, String> headers = new HashMap<>();
        headers.put("version", UpdateHandler.VERSION);

        Map<String, Map<String, String>> versions = new HashMap<>();
        try {
            String data = makeRequest(address, headers);
            if (data == null) return null;

            JSONObject jData = new JSONObject(data);
            JSONArray arr = jData.getJSONArray("versions");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject jSemester = arr.getJSONObject(i);
                JSONArray jGroups = jSemester.getJSONArray("groups");
                String semester = jSemester.get("semester").toString();

                Map<String, String> groups = new HashMap<>();
                for (int j = 0; j < jGroups.length(); j++) {
                    JSONObject jGroup = jGroups.getJSONObject(j);
                    String group = jGroup.get("group").toString();
                    String version = jGroup.get("version").toString();
                    groups.put(group, version);
                }
                versions.put(semester, groups);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("bad response");
        }
        return versions;
    }

    static Pair<String, String> getBorderDates(String semesterName, String groupName) throws NullPointerException {
        String address = "Plan/get_group/";
        Map<String, String> headers = new HashMap<>();
        headers.put("semester", semesterName);
        headers.put("group", groupName);
        headers.put("version", UpdateHandler.VERSION);

        try {
            String data = makeRequest(address, headers);
            if (data == null) return null;

            JSONObject jdata = new JSONObject(data);
            String firstDay = jdata.get("first_day").toString();
            String lastDay = jdata.get("last_day").toString();
            return new Pair<>(firstDay, lastDay);
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("bad response");
        }
        return null;
    }

    static Map<Pair<String, String>, Block> getGroupBlocks(String semesterName, String groupName) throws NullPointerException {
        String address = "Plan/get_group/";
        Map<String, String> headers = new HashMap<>();
        headers.put("semester", semesterName);
        headers.put("group", groupName);
        headers.put("version", UpdateHandler.VERSION);

        try {
            Map<Pair<String, String>, Block> blockMap = new HashMap<>();
            String data = makeRequest(address, headers);
            if (data == null) throw new NullPointerException("Null block list");

            JSONObject jdata = new JSONObject(data);
            JSONArray arr = jdata.getJSONArray("data");

            for (int i = 0; i < arr.length(); i++) {
                String date = arr.getJSONObject(i).getString("date");
                JSONArray blocks = arr.getJSONObject(i).getJSONArray("blocks");

                for (int j = 0; j < blocks.length(); j++) {
                    JSONObject jblock = blocks.getJSONObject(j);
                    Block block = new Block();
                    for (int k = 0; k < jblock.length(); k++) {
                        String name = Objects.requireNonNull(jblock.names()).get(k).toString();
                        block.insert(name, jblock.getString(name));
                        block.insert("date", date);
                    }
                    String index = jblock.getString("index");
                    blockMap.put(new Pair<>(date, index), block);
                }
            }
            return blockMap;
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("bad response");
        }
        return null;
    }

    private static String makeRequest(String address, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder();
        headers.forEach(builder::addHeader);
        Request request = builder.url(baseUrl + address).build();
        try {
            Response response = client.newCall(request).execute();
            String data = response.body().string();
            response.body().close();
            return data;
        } catch (IOException e) {
            System.out.println("Request IOException at " + address );
        }
        return null;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }
}
