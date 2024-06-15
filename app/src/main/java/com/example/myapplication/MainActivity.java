package com.example.myapplication;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private TextView nearestStationTextView;
    private Button refreshButton;
    private boolean locationPermissionGranted = false;
    private boolean locationPermissionDenied = false;

    public class Station {
        private int seq;
        private String 車站編號;
        private String 車站中文名稱;
        private String 車站英文名稱;
        private double 車站緯度;
        private double 車站經度;

        // Getters and Setters
        public int getSeq() {
            return seq;
        }

        public void setSeq(int seq) {
            this.seq = seq;
        }

        public String get車站編號() {
            return 車站編號;
        }

        public void set車站編號(String 車站編號) {
            this.車站編號 = 車站編號;
        }

        public String get車站中文名稱() {
            return 車站中文名稱;
        }

        public void set車站中文名稱(String 車站中文名稱) {
            this.車站中文名稱 = 車站中文名稱;
        }

        public String get車站英文名稱() {
            return 車站英文名稱;
        }

        public void set車站英文名稱(String 車站英文名稱) {
            this.車站英文名稱 = 車站英文名稱;
        }

        public double get車站緯度() {
            return 車站緯度;
        }

        public void set車站緯度(double 車站緯度) {
            this.車站緯度 = 車站緯度;
        }

        public double get車站經度() {
            return 車站經度;
        }

        public void set車站經度(double 車站經度) {
            this.車站經度 = 車站經度;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nearestStationTextView = findViewById(R.id.nearest_station);
        refreshButton = findViewById(R.id.refresh_button);
        Button viewAllStationsButton = findViewById(R.id.view_all_stations_button);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (!locationPermissionGranted) return;
                updateNearestStations(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationPermissionGranted) {
                    requestSingleLocationUpdate();
                } else {
                    showLocationPermissionDialog();
                }
            }
        });

        viewAllStationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StationListActivity.class);
                startActivity(intent);
            }
        });

        showLocationPermissionDialog();
    }

    private void showLocationPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("定位服務");
        builder.setMessage("是否開啟定位服務以獲取最近的車站？");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                locationPermissionGranted = true;
                locationPermissionDenied = false;
                refreshButton.setVisibility(View.GONE); // 隱藏刷新按鈕
                requestLocationPermission();
            }
        });
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                locationPermissionGranted = false;
                locationPermissionDenied = true;
                refreshButton.setVisibility(View.VISIBLE); // 顯示刷新按鈕
                nearestStationTextView.setText("請按刷新重新開啟定位");
                Toast.makeText(MainActivity.this, "請開啟定位", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    private void requestSingleLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    updateNearestStations(location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}
            }, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                locationPermissionDenied = true;
                refreshButton.setVisibility(View.VISIBLE); // 顯示刷新按鈕
                nearestStationTextView.setText("請開啟定位");
                Toast.makeText(this, "請開啟定位", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationPermissionDenied) {
            refreshButton.setVisibility(View.VISIBLE); // 顯示刷新按鈕
            nearestStationTextView.setText("請開啟定位");
        } else {
            refreshButton.setVisibility(View.GONE); // 隱藏刷新按鈕
            requestLocationPermission();
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

            // Parse the JSON object and get the "data" array
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("data");

            // Return the "data" array as a string
            return jsonArray.toString();
        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void updateNearestStations(Location location) {
        Log.d("MainActivity", "updateNearestStations called with location: " + location);

        String json = loadJSONFromAsset();
        Log.d("MainActivity", "Loaded JSON: " + json);

        Gson gson = new Gson();
        Type listType = new TypeToken<List<Station>>() {}.getType();
        List<Station> stations = gson.fromJson(json, listType);
        Log.d("MainActivity", "Parsed stations: " + stations);

        List<StationDistance> stationDistances = new ArrayList<>();
        for (Station station : stations) {
            Location stationLocation = new Location("");
            stationLocation.setLatitude(station.get車站緯度());
            stationLocation.setLongitude(station.get車站經度());

            float distance = location.distanceTo(stationLocation);
            stationDistances.add(new StationDistance(station, distance));
        }

        Collections.sort(stationDistances, new Comparator<StationDistance>() {
            @Override
            public int compare(StationDistance s1, StationDistance s2) {
                return Float.compare(s1.getDistance(), s2.getDistance());
            }
        });

        StringBuilder nearestStationsText = new StringBuilder("距離最近的5個車站：\n");
        for (int i = 0; i < Math.min(5, stationDistances.size()); i++) {
            Station nearestStation = stationDistances.get(i).getStation();
            String line = (nearestStation.getSeq() >= 1 && nearestStation.getSeq() <= 24) ? " (紅線)" : " (橘線)";
            nearestStationsText.append(nearestStation.get車站中文名稱()).append("\t").append(line).append("\n");
        }

        nearestStationTextView.setText(nearestStationsText.toString());
    }


    public class StationDistance {
        private Station station;
        private float distance;

        public StationDistance(Station station, float distance) {
            this.station = station;
            this.distance = distance;
        }

        public Station getStation() {
            return station;
        }

        public float getDistance() {
            return distance;
        }
    }
}
