package com.liem.appthoitiet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.liem.appthoitiet.R;
import com.liem.appthoitiet.models.Weather;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {
    private List<Weather> weatherList;
    private Context context;

    public WeatherAdapter(List<Weather> weatherList, Context context) {
        this.weatherList = weatherList;
        this.context = context;
    }

    @NonNull
    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.ViewHolder holder, int position) {
        Weather weather = weatherList.get(position);
        holder.txtDate.setText(new SimpleDateFormat("EEEE dd-MM-yyyy HH:MM").format(weather.getDate()));
        holder.txtDescription.setText(weather.getDescription());
        holder.txtMinTemp.setText(String.valueOf(weather.getTempMin()) + "°C");
        holder.txtMaxTemp.setText(String.valueOf(weather.getTempMax()) + "°C");

        String imgUrl = "https://openweathermap.org/img/w/" + weather.getIcon() + ".png";
        Picasso.get().load(imgUrl).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return weatherList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtDate, txtDescription, txtMinTemp, txtMaxTemp;
        private ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = (TextView) itemView.findViewById(R.id.txtDate);
            txtDescription = (TextView) itemView.findViewById(R.id.txtDescription);
            txtMinTemp = (TextView) itemView.findViewById(R.id.txtMinTemp);
            txtMaxTemp = (TextView) itemView.findViewById(R.id.txtMaxTemp);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
        }
    }
}
