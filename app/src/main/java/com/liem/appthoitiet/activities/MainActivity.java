package com.liem.appthoitiet.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.liem.appthoitiet.R;
import com.liem.appthoitiet.utils.AppLanguageHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText edtCity;
    private Button btnGetCity, btnMoreDay;
    private TextView txtCity, txtNation, txtTemp, txtState, txtHuminity, txtCloud, txtWind, txtCurrentDay;
    private ImageView imgIconWeather;
    private String city, langCode, lat, lon;

    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadData();
        AppLanguageHelper.setLocale(MainActivity.this, langCode.substring(0, 2));
        initializeUI();

        btnGetCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                city = edtCity.getText().toString().trim();
                getCurrentWeatherData(city, null, null);
            }
        });

        edtCity.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    city = edtCity.getText().toString().trim();
                    getCurrentWeatherData(city, null, null);
                    return true;
                }
                return false;
            }
        });

        btnMoreDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=43a4ba75dc3793776a30decf39830cf4&units=metric&lang=" + langCode.substring(0, 2);
                Intent intent = new Intent(MainActivity.this, SevenDaysWeatherActivity.class);
                intent.putExtra("city", city);
                intent.putExtra("url", url);
                intent.putExtra("lang", langCode);
                startActivity(intent);
            }
        });
    }

    private void loadData() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            langCode = sharedPreferences.getString("lang", "en_US");
            lat = sharedPreferences.getString("lat", "55.5");
            lon = sharedPreferences.getString("longitude", "37");
        } else {
            lat = "55.5";
            lon = "37";
            langCode = "en_US";
        }
    }

    public void getCurrentWeatherData(String city, String lat, String lon) {
        String url;
        if (city == null)
            url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=09b8659c8135d63a3ce586364e590ee2&units=metric&lang=" + langCode.substring(0, 2);
        else
            url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=09b8659c8135d63a3ce586364e590ee2&units=metric&lang=" + langCode.substring(0, 2);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                edtCity.setText("");
                editDataCity(city);
                hideSearchBar();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Long day = jsonObject.getLong("dt");
                    JSONObject jsonObjectCoord = jsonObject.getJSONObject("coord");

                    String city = jsonObject.getString("name");
                    String lat = jsonObjectCoord.getString("lat");
                    String lon = jsonObjectCoord.getString("lon");
                    updateDataLocate(city, lat, lon);

                    JSONObject jsonObjectSys = jsonObject.getJSONObject("sys");
                    String nation = jsonObjectSys.getString("country");

                    JSONArray jsonArrayWeather = jsonObject.getJSONArray("weather");
                    JSONObject jsonObjectWeather = (JSONObject) jsonArrayWeather.get(0);
                    String icon = jsonObjectWeather.getString("icon");
                    String state = jsonObjectWeather.getString("description");

                    JSONObject jsonObjectMain = jsonObject.getJSONObject("main");
                    String temp = jsonObjectMain.getString("temp");
                    String humidity = jsonObjectMain.getString("humidity");

                    JSONObject jsonObjectWind = jsonObject.getJSONObject("wind");
                    String wind = jsonObjectWind.getString("speed");

                    JSONObject jsonObjectClouds = jsonObject.getJSONObject("clouds");
                    String clouds = jsonObjectClouds.getString("all");

                    String imgUrl = "https://openweathermap.org/img/w/" + icon + ".png";
                    Picasso.get().load(imgUrl).into(imgIconWeather);

                    //Convert milisecond to second
                    Date date = new Date(day * 1000L);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE dd-MM-yyyy HH:mm");
                    txtCurrentDay.setText(getString(R.string.last_update) + " " + simpleDateFormat.format(date));

                    CharSequence charSequence = getString(R.string.city);
                    if (city.contains(charSequence))
                        txtCity.setText(city);
                    else
                        txtCity.setText(getString(R.string.city) + ": " + city);
                    txtNation.setText(getString(R.string.nation) + ": " + new Locale("", nation).getDisplayCountry());
                    txtTemp.setText(temp + "°C ");
                    txtState.setText(state.substring(0, 1).toUpperCase() + state.substring(1).toLowerCase());
                    txtHuminity.setText(humidity + "%");
                    txtCloud.setText(clouds + "%");
                    txtWind.setText(wind + " m/s");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, getString(R.string.city_not_valid), Toast.LENGTH_SHORT).show();
                        edtCity.setText("");
                    }
                }
        );

        requestQueue.add(stringRequest);
    }

    private void updateDataLocate(String city, String lat, String lon) {
        this.city = city;
        this.lat = lat;
        this.lon = lon;
    }

    private void editDataCity(String city) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("city", city);
        editor.apply();
    }

    private void editDataLang(String langCode) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lang", langCode);
        editor.apply();

        recreate();
    }

    private void restartApp() {
        Intent mStartActivity = new Intent(this, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    private void initializeUI() {
        edtCity = (EditText) findViewById(R.id.edtCity);
        btnGetCity = (Button) findViewById(R.id.btnGetCity);
        btnMoreDay = (Button) findViewById(R.id.btnMoreDay);
        txtCity = (TextView) findViewById(R.id.txtCity);
        txtNation = (TextView) findViewById(R.id.txtNation);
        txtTemp = (TextView) findViewById(R.id.txtTemp);
        txtState = (TextView) findViewById(R.id.txtState);
        txtCloud = (TextView) findViewById(R.id.txtClouds);
        txtHuminity = (TextView) findViewById(R.id.txtHuminity);
        txtWind = (TextView) findViewById(R.id.txtWindmill);
        txtCurrentDay = (TextView) findViewById(R.id.txtCurrenDay);
        imgIconWeather = (ImageView) findViewById(R.id.iconWeather);
        btnMoreDay.setText(getString(R.string.more_day));

        hideSearchBar();
        getCurrentWeatherData(city, lat, lon);
    }

    private void hideSearchBar() {
        edtCity.setVisibility(View.GONE);
        btnGetCity.setVisibility(View.GONE);
    }

    private void showSearchBar() {
        edtCity.setVisibility(View.VISIBLE);
        btnGetCity.setVisibility(View.VISIBLE);
    }

    private void getLocationWeather() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //Check permission
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            //when permission granted
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    //Initialize location
                    Location location = task.getResult();
                    if (location != null) {
                        //Initialize geoCder
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        //Initialize address list
                        try {
                            List<Address> addresses = geocoder.getFromLocation(
                                    location.getLatitude(), location.getLongitude(), 1
                            );

                            double latitude = addresses.get(0).getLatitude();
                            double longitude = addresses.get(0).getLongitude();

                            getCurrentWeatherData(null, String.valueOf(latitude), String.valueOf(longitude));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        else
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
    }

    private void confirmExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.drawable.ic_app);
        builder.setMessage(getString(R.string.are_you_sure))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void controlVisibleSearchBar() {
        if (edtCity.getVisibility() == View.VISIBLE)
            hideSearchBar();
        else
            showSearchBar();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_exit:
                confirmExit();
                return true;
            case R.id.action_search:
                controlVisibleSearchBar();
                return true;
            case R.id.language_vi:
                editDataLang("vi_VN");
                return true;
            case R.id.language_en:
                editDataLang("en_US");
                return true;
            case R.id.menu_get_location:
                getLocationWeather();
                return true;
            case R.id.menu_background:
            case R.id.menu_share:
                Toast.makeText(this, getString(R.string.comming_soon), Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        confirmExit();
    }
}