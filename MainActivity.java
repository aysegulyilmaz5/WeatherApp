package com.aysegulyilmaz.homework4;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ImageView back,search,icon;
    private TextView cityNameTV,temperature,weatherCondition;
    private RecyclerView weatherRV;
    private RelativeLayout idHome;
    private ProgressBar loadingPB;
    private TextInputEditText edtcity;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSON_CODE=1;
    private String cityName;
    private TextView lastUpdateTV;
    private TextView fahrenheit;
    private TextView humidityTV,cloudTV,windsTV,feelcTV,feelfTV;
    Button showLocation;
    private static final int REQUEST_CODE_PERMISSION = 2;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    GPSTracker gps;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        idHome = findViewById(R.id.idHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperature = findViewById(R.id.idTVTemperature);
        weatherCondition =findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRVWeather);
        edtcity = findViewById(R.id.edtCity);
        search = findViewById(R.id.search);
        back = findViewById(R.id.idIVBack);
        search = findViewById(R.id.search);
        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this,weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);
        icon = findViewById(R.id.idTVIcon);
        lastUpdateTV = findViewById(R.id.lastUpdate);
        fahrenheit = findViewById(R.id.fahrenheitTV);
        humidityTV = findViewById(R.id.humidityTV);
        cloudTV = findViewById(R.id.cloudTV);
        windsTV = findViewById(R.id.windTV);
        feelcTV = findViewById(R.id.feelscTV);
        feelfTV = findViewById(R.id.feelsfTV);

        try{
            if(ActivityCompat.checkSelfPermission(this, mPermission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{mPermission}, REQUEST_CODE_PERMISSION);

            }

        }catch (Exception e){
            e.printStackTrace();
        }
        showLocation = findViewById(R.id.gpsButton);
        showLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                gps = new GPSTracker(MainActivity.this);
                if(gps.canGetLocation()){
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    Toast.makeText(getApplicationContext(), "your location is - \nLat: "
                            + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();


                }else{
                    gps.showSettingsAlert();
                   
                }
            }
        });


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSON_CODE);

        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null){cityName = getCityName(location.getLongitude(),location.getLatitude());
            getWeatherInfo(cityName);
        } else {
            cityName = "London";
            getWeatherInfo(cityName);
        }

        search.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                String city = edtcity.getText().toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
                }else{
                    cityNameTV.setText(cityName);
                    getWeatherInfo(city);

                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSON_CODE){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED ){
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Please provide the permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName= "Not found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try{
            List<Address> addresses = gcd.getFromLocation(latitude,longitude,10);

            for(Address adr : addresses){
                if(adr!=null){
                    String city = adr.getLocality();
                    if(city!=null && !city.equals("")){
                        cityName = city;

                    }
                    else{
                        Log.d("TAG","CITY NOT FOUND");
                        Toast.makeText(this, "User city not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return cityName;
    }
    private String getGPS(){
        String cityName= "Not found";
        gps = new GPSTracker(MainActivity.this);
        if(gps.canGetLocation()){
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            Toast.makeText(getApplicationContext(), "your location is - \nLat: "
                    + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();


        }else{
            gps.showSettingsAlert();
        }
        return cityName;
    }

    private void getWeatherInfo(String cityName){
        //String url = "http://api.weatherapi.com/v1/forecast.json?key=29f71013dd264ba9a9004459222711&q=" +cityName+"&days=1&aqi=yes&alerts=yes";
        String url = "http://api.weatherapi.com/v1/forecast.json?key=dcf6a3704a374620889212657230601&q="+cityName+"&days=1&aqi=yes&alerts=yes";
        cityNameTV.setText(cityName);
        System.out.print("Hello world");
        RequestQueue requestqueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                idHome.setVisibility(View.VISIBLE);
                weatherRVModalArrayList.clear();
                try{
                    String temp = response.getJSONObject("current").getString("temp_c");
                    temperature.setText(temp+"°C");
                    String weatherCond = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    String lastUpdated = response.getJSONObject("current").getString("last_updated");
                    lastUpdateTV.setText(lastUpdated);
                    String fahrenh= response.getJSONObject("current").getString("temp_f");
                    fahrenheit.setText(fahrenh+"°F");
                    String hum = response.getJSONObject("current").getString("humidity");
                    humidityTV.setText("Humidity:  "+hum);
                    String clouding = response.getJSONObject("current").getString("cloud");
                    cloudTV.setText("Cloud:  "+clouding);
                    String winds = response.getJSONObject("current").getString("wind_degree");
                    windsTV.setText("Wind-Degree:  " + winds);
                    String feelingc = response.getJSONObject("current").getString("feelslike_c");
                    feelcTV.setText("Feels Like:  " +feelingc+"°C");
                    String feelingf = response.getJSONObject("current").getString("feelslike_f");
                    feelfTV.setText("Feels like:  "+feelingf+"°F");

                    Picasso.get().load("http:".concat(conditionIcon)).into(icon);
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    weatherCondition.setText(weatherCond);
                    if(isDay==1){
                        //morning
                        Picasso.get().load("https://images.unsplash.com/photo-1606158207522-d9eb6de3ee87?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=2940&q=80").into(back);
                    }
                    else{
                        Picasso.get().load("https://unsplash.com/photos/ZPRgmVqgPj0").into(back);
                    }

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecastO.getJSONArray("hour");


                    for(int i = 0;i<hourArray.length();i++){
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");

                        weatherRVModalArrayList.add(new WeatherRVModal(time,temper,img,wind));
                    }
                    weatherRVAdapter.notifyDataSetChanged();

                }
                catch(JSONException e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Please enter valid city name..",Toast.LENGTH_SHORT).show();

            }
        });

        requestqueue.add(jsonObjectRequest);


    }




}