package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class StationListActivity extends AppCompatActivity {

    private ListView redLineListView;
    private ListView orangeLineListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_list);

        redLineListView = findViewById(R.id.red_line_list_view);
        orangeLineListView = findViewById(R.id.orange_line_list_view);
        Button backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        String json = loadJSONFromAsset();
        if (json != null) {
            parseAndDisplayStations(json);
        }
    }

    private void parseAndDisplayStations(String json) {
        Gson gson = new Gson();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            Type listType = new TypeToken<List<MainActivity.Station>>() {}.getType();
            List<MainActivity.Station> stations = gson.fromJson(jsonArray.toString(), listType);

            List<MainActivity.Station> redLineStations = new ArrayList<>();
            List<MainActivity.Station> orangeLineStations = new ArrayList<>();

            for (MainActivity.Station station : stations) {
                int seq = station.getSeq();
                if (seq >= 1 && seq <= 24) {
                    redLineStations.add(station);
                } else if (seq >= 25 && seq <= 38) {
                    orangeLineStations.add(station);
                }
            }

            ArrayAdapter<String> redLineAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1,
                    getStationNames(redLineStations));

            ArrayAdapter<String> orangeLineAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1,
                    getStationNames(orangeLineStations));

            redLineListView.setAdapter(redLineAdapter);
            orangeLineListView.setAdapter(orangeLineAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("stations.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private List<String> getStationNames(List<MainActivity.Station> stations) {
        List<String> stationNames = new ArrayList<>();
        for (MainActivity.Station station : stations) {
            stationNames.add(station.get車站中文名稱());
        }
        return stationNames;
    }
}
