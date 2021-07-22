package com.liem.appthoitiet.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.liem.appthoitiet.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

public class CurrentWeatherActivity extends AppCompatActivity {

    private EditText edtCity;
    private Button btnGetCity, btnMoreDay;
    private TextView txtCity, txtNation, txtTemp, txtState, txtHuminity, txtCloud, txtWind, txtCurrentDay;
    private ImageView imgIconWeather;
    private String city, lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_weather);

        initializeUI();

        btnGetCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                city = edtCity.getText().toString().trim();
                getCurrentWeatherData(city);
            }
        });

        edtCity.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    city = edtCity.getText().toString().trim();
                    getCurrentWeatherData(city);
                }
                return false;
            }
        });

        btnMoreDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!city.isEmpty()) {
                    String url = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=43a4ba75dc3793776a30decf39830cf4&units=metric&lang=" + lang;

                    Intent intent = new Intent(CurrentWeatherActivity.this, SevenDaysWeatherActivity.class);
                    intent.putExtra("city", city);
                    intent.putExtra("url", url);
                    startActivity(intent);
                } else
                    Toast.makeText(CurrentWeatherActivity.this, getString(R.string.city_not_valid), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            city = sharedPreferences.getString("city", "");
//            lang = sharedPreferences.getString("lang", "en");
            getCurrentWeatherData(city);
        } else {
            lang = "Locale.getDefault().toString()";
        }
    }

    public void getCurrentWeatherData(String city) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=09b8659c8135d63a3ce586364e590ee2&units=metric&lang=" + lang;
        RequestQueue requestQueue = Volley.newRequestQueue(CurrentWeatherActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                edtCity.setText("");
                editDataCity(city);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Long day = jsonObject.getLong("dt");
                    String cityName = jsonObject.getString("name");
                    updateCityName(cityName);

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
                    Log.i("tag", "KQ" + cityName.contains("Thành phố"));
                    if (cityName.contains(charSequence))
                        txtCity.setText(cityName);
                    else
                        txtCity.setText(getString(R.string.city) + ": " + cityName);
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
                        Toast.makeText(CurrentWeatherActivity.this, getString(R.string.city_not_valid), Toast.LENGTH_SHORT).show();
                        edtCity.setText("");
                        setEmptyCity();
                    }
                }
        );

        requestQueue.add(stringRequest);
    }

    private void editDataCity(String city) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("city", city);
        editor.apply();
    }

    private void editDataLang(String lang){
        SharedPreferences sharedPreferences = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lang", lang);
        editor.apply();
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

        if(Locale.getDefault().toString().equals("vi_VN"))
            lang = "vi";
        else
            lang = "en";
        loadData();
    }

    protected void setEmptyCity() {
        city = "";
    }

    protected void updateCityName(String cityName) {
        city = cityName;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_exit:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}