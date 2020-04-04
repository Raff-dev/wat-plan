package com.example.watplan;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class UpdateHandler {


    public void checkForUpdates(){

    }
    public void updateGroup(){

    }
    private void testRequest() throws IOException {
        String adress = "http://127.0.0.1:8000//Plan/get_semester/";
        final OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .addHeader("group", "WCY18IY5S1")
                .addHeader("semester", "letni")
                .url("http://10.0.2.2:8000/Plan/get_semester/").build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                String data = response.body().string();
                JSONObject j = new JSONObject(data);
                JSONArray arr = j.getJSONArray("yes");
                System.out.println("names " + arr.getJSONObject(1).names());
                System.out.println("keys " + arr.getJSONObject(1).keys());
//                for (int i = 0; i < arr.length(); i++) {
//
//                    String post_id = arr.getJSONObject(i).getString("title");
//                    System.out.println(post_id);
//                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
