package com.example.watplan;

import com.example.watplan.Models.Block;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// semestry w bazie
// grupy w bazie


// SCENARIO:
// aplikacja przechowuje potencjalnie nieaktualną wersję dancyh
// uzytkownik chce wyswietlic grupe

// sprawdz, czy wersja sie zgadza
// request-> tworzenie listy blokow -> wyswietlenie ich -> wrzucenie ich od bazy danych


public class UpdateHandler {

    private static final String baseAdress = "http://10.0.2.2:8000/Plan/";
    private static final OkHttpClient client = new OkHttpClient();

    public static ArrayList<String> getSemesterList() {
        String address = "get_semesters/";
        ArrayList<String> semesters = new ArrayList<>();

        String data = makeRequest(address, new HashMap<>());
        if (data != null) {
            data = data.replaceAll("[\\[\\]\"]", "");
            semesters.addAll(Arrays.asList(data.split(",")));
        } else {
            //handle failure here
            System.out.println("bad response");
        }
        return semesters;
    }

    public static ArrayList<String> getGroupList(String nameSemester) {
        String address = "get_groups/";
        Map<String, String> headers = new HashMap<>();
        headers.put("semester", nameSemester);
        ArrayList<String> groups = new ArrayList<>();

        String data = makeRequest(address, headers);
        if (data != null) {
            data = data.replaceAll("[\\[\\]\"]", "");
            groups.addAll(Arrays.asList(data.split(",")));
        } else {
            //handle failure here
            System.out.println("bad response");
        }
        return groups;
    }

    public ArrayList<Block> getGroupBlocks(String nameSemester, String nameGroup) {
        String address = "get_group/";
        Map<String, String> headers = new HashMap<>();
        headers.put("semester", nameSemester);
        String data = makeRequest(address, headers);
        ArrayList<Block> blockList = new ArrayList<>();

        if (data != null) {
            try {
                JSONObject jdata = new JSONObject(data);
                String version = jdata.get("version").toString();
                JSONArray arr = jdata.getJSONArray("data");

                for (int i = 0; i < arr.length(); i++) {
                    String date = arr.getJSONObject(i).getString("date");
                    System.out.println(date);
                    JSONArray blocks = arr.getJSONObject(i).getJSONArray("blocks");

                    for (int j = 0; j < blocks.length(); j++) {
                        JSONObject jblock = blocks.getJSONObject(j);
                        Block block = new Block();
                        for (int k = 0; k < jblock.length(); k++) {
                            String name = Objects.requireNonNull(jblock.names()).get(k).toString();
                            block.insert(name, jblock.getString(name));
                        }
                        blockList.add(block);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            //handle failure here
            System.out.println("bad response");
        }
        return blockList;
    }

    public void checkForUpdates() {
        String address = "get_versions/";
        String data = makeRequest(address, new HashMap<>());

    }

    public void updateGroup(String nameGroup) {

    }

    private static String makeRequest(String address, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder();
        headers.forEach(builder::addHeader);
        Request request = builder.url(baseAdress + address).build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
