package com.test.moviesdb.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MoviesArrayAdapter extends ArrayAdapter<MovieInfo> {

    public MoviesArrayAdapter(Context context, int resource, int textViewResourceId, List<MovieInfo> movies) {
        super(context, resource, textViewResourceId, movies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item, parent, false);
        }

        MovieInfo movieAtPosition = getItem(position);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.movie_image);
        Picasso.with(getContext()).load(movieAtPosition.getPosterUrl()).into(imageView);
        return convertView;
    }
}
