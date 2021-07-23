package com.liem.appthoitiet.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.liem.appthoitiet.R;
import com.liem.appthoitiet.adapter.WeatherAdapter;
import com.liem.appthoitiet.models.Weather;
import com.liem.appthoitiet.utils.AppLanguageHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SevenDaysWeatherActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView txtCity;
    private RecyclerView recyclerView;

    private List<Weather> weatherList;
    private WeatherAdapter weatherAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seven_days_weather);
        getSupportActionBar().hide();

        AppLanguageHelper.setLocale(SevenDaysWeatherActivity.this, getIntent().getStringExtra("lang"));
        initializeUI();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Scrolling down
                if (dy < 0) {
                    showHeader();
                } else {
                    hideHeader();
                }

            }
        });
    }

    private void initializeUI() {
        btnBack = (ImageButton) findViewById(R.id.btnBackCurrentWeather);
        txtCity = (TextView) findViewById(R.id.txtCity2);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        weatherList = new ArrayList<>();
        weatherAdapter = new WeatherAdapter(weatherList, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(weatherAdapter);

        String city = getIntent().getStringExtra("city");
        CharSequence charSequence = getString(R.string.city);
        if (city.contains(charSequence))
            txtCity.setText(city);
        else
            txtCity.setText(getString(R.string.city) + ": " + city);

        getWeatherListData();
    }

    private void getWeatherListData() {
        String url = getIntent().getStringExtra("url");
        Log.i("tag", "url: " + url);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArrayDaily = jsonObject.getJSONArray("list");
                    for (int i = 0; i < jsonArrayDaily.length(); i++) {
                        JSONObject jsonWeatherDay = (JSONObject) jsonArrayDaily.get(i);

                        Date date = new Date(jsonWeatherDay.getLong("dt") * 1000L);
                        int minTemp = jsonWeatherDay.getJSONObject("main").getInt("temp_min");
                        int maxTemp = jsonWeatherDay.getJSONObject("main").getInt("temp_max");

                        JSONObject jsonWeather = (JSONObject) jsonWeatherDay.getJSONArray("weather").get(0);
                        String description = jsonWeather.getString("description");
                        String icon = jsonWeather.getString("icon");

                        Weather weather = new Weather();
                        weather.setDate(date);
                        weather.setDescription(description.substring(0, 1).toUpperCase() + description.substring(1).toLowerCase());
                        weather.setIcon(icon);
                        weather.setTempMax(maxTemp);
                        weather.setTempMin(minTemp);

                        weatherList.add(weather);
                    }
                    weatherAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SevenDaysWeatherActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(stringRequest);
    }

    private void hideHeader() {
        btnBack.setVisibility(View.GONE);
        txtCity.setVisibility(View.GONE);
    }

    private void showHeader() {
        btnBack.setVisibility(View.VISIBLE);
        txtCity.setVisibility(View.VISIBLE);
    }
}