package com.test.moviesdb.popularmovies;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailActivityFragment extends Fragment {

    private static final String TAG = MovieDetailActivityFragment.class.getName();

    private TextView movieTitle;
    private TextView movieDescription;
    private TextView movieRelease;
    private TextView movieRatings;
    private ImageView moviePoster;

    public MovieDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        movieTitle = (TextView) rootView.findViewById(R.id.movie_name);
        movieDescription = (TextView) rootView.findViewById(R.id.movie_description);
        movieRelease = (TextView) rootView.findViewById(R.id.movie_release);
        movieRatings = (TextView) rootView.findViewById(R.id.movie_rating);
        moviePoster = (ImageView) rootView.findViewById(R.id.movie_poster);

        MovieInfo movieInfo = (MovieInfo) getActivity().getIntent().getParcelableExtra("MovieInfo");
        Log.v(TAG, "Fetched movie info");

        movieTitle.setText(movieInfo.getTitle());
        if(movieInfo.getDescription() != null && !movieInfo.getDescription().equals("null")) {
            movieDescription.setText(movieInfo.getDescription());
        } else {
            movieDescription.setText("No movie description available.");
        }
        if(movieInfo.getReleaseDate() != null) {
            movieRelease.setText(movieInfo.getReleaseDate().toString());
        }
        movieRatings.setText(String.format("%s/10", movieInfo.getVoteAverage()));
        Picasso.with(getContext()).load(movieInfo.getPosterUrl()).into(moviePoster);

        return rootView;
    }
}
