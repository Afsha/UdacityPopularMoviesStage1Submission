package com.test.moviesdb.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MovieListActivityFragment extends Fragment {

    private static final String TAG = MovieListActivityFragment.class.getName();
    private ArrayAdapter<MovieInfo> moviesArrayAdapter = null;
    private String movieOrderSetting = null;

    private final String apiKeyParam = "api_key";
    private final String apiKeyValue = "";
    private final String MOVIE_LIST_KEY = "MovieInfoList";
    private final String ORDER_SETTINGS = "MovieSortOrder";


    public MovieListActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if(savedInstanceState != null && savedInstanceState.containsKey(ORDER_SETTINGS)) {
            Log.i(TAG, "Retreiving sort order saved state");
            movieOrderSetting = savedInstanceState.getString(ORDER_SETTINGS);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<MovieInfo> movieList = new ArrayList<MovieInfo>();
        for(int index = 0; index < moviesArrayAdapter.getCount(); index++) {
            movieList.add(moviesArrayAdapter.getItem(index));
        }
        outState.putParcelableArrayList(MOVIE_LIST_KEY, movieList);
        outState.putString(ORDER_SETTINGS, movieOrderSetting);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ArrayList<MovieInfo> movieList = new ArrayList<MovieInfo>();
        if(savedInstanceState != null && savedInstanceState.containsKey(MOVIE_LIST_KEY)) {
            Log.i(TAG, "Retreiving movie list saved state");
            movieList = savedInstanceState.getParcelableArrayList(MOVIE_LIST_KEY);
        }
        moviesArrayAdapter = new MoviesArrayAdapter(getActivity(), R.layout.grid_item, R.id.movie_image, movieList);

        View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);
        GridView gridView = (GridView)rootView.findViewById(R.id.grid_view);
        gridView.setAdapter(moviesArrayAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieInfo movieInfoSelected = moviesArrayAdapter.getItem(position);
                Intent detailActivityIntent = new Intent(getActivity(), MovieDetailActivity.class);
                detailActivityIntent.putExtra("MovieInfo", movieInfoSelected);
                startActivity(detailActivityIntent);
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        boolean orderChanged = false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String orderDefault = getString(R.string.pref_order_default);
        String order = prefs.getString(getString(R.string.pref_order_key), orderDefault);
        if(order != movieOrderSetting) {
            orderChanged = true;
            movieOrderSetting = order;
        }
        if(moviesArrayAdapter == null || moviesArrayAdapter.isEmpty() || orderChanged) {

            Log.v(TAG, "Sort order : " + movieOrderSetting);

            if(movieOrderSetting.equals(orderDefault)) {
                new MovieListDownloadTask(true).execute();
            } else {
                new MovieListDownloadTask(false).execute();
            }
        }
    }

    private class MovieListDownloadTask extends AsyncTask<Void, Void, List<MovieInfo>> {

        private final String TAG = MovieListDownloadTask.class.getName();

        private final String baseUrl = "http://api.themoviedb.org/3/discover/movie?";
        private final String sortParam = "sort_by";
        private final String popularitySort = "popularity.desc";
        private final String mostRatedSort = "vote_average.desc";

        private boolean sortByMostPopular = true;

        public MovieListDownloadTask(boolean sortByMostPopular) {
            this.sortByMostPopular = sortByMostPopular;
        }

        @Override
        protected List<MovieInfo> doInBackground(Void... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;
            List<MovieInfo> movieInfoList = null;

            String sortParamValue = null;
            if(sortByMostPopular) {
                sortParamValue = popularitySort;
            } else {
                sortParamValue = mostRatedSort;
            }
            try {
                Uri popularMoviesUri = Uri.parse(baseUrl).buildUpon()
                        .appendQueryParameter(sortParam, sortParamValue)
                        .appendQueryParameter(apiKeyParam, apiKeyValue).build();
                URL httpUri = new URL(popularMoviesUri.toString());
                connection = (HttpURLConnection) httpUri.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                // Read the input stream into a String
                InputStream inputStream = connection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                String moviesListJSON = buffer.toString();
                Log.i(TAG, "Downloaded movies: " + moviesListJSON);
                movieInfoList = getMovieDetailListFromJson(moviesListJSON);
            } catch (MalformedURLException exception) {
                Log.e(TAG, "Invalid url", exception);
                return null;
            } catch (IOException exception) {
                Log.e(TAG, "Error downloading movie data", exception);
                return null;
            } catch (ParseException exception) {
                Log.e(TAG, "Error parsing movie data", exception);
                return null;
            } catch (JSONException exception) {
                Log.e(TAG, "Error parsing JSON movie data", exception);
                return null;
            } finally {
                if(connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            return movieInfoList;
        }

        @Override
        protected void onPostExecute(List<MovieInfo> moviesList) {
            moviesArrayAdapter.clear();
            moviesArrayAdapter.addAll(moviesList);
        }
    }

    private Bitmap downloadMoviePoster(String posterUrl) {

        final String baseUrl = "http://image.tmdb.org/t/p";
        final String imageSizeStr = "w185";
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        Bitmap bitmap;

        try {
            Uri popularMoviesUri = Uri.parse(baseUrl).buildUpon()
                    .appendPath(imageSizeStr)
                    .appendPath(posterUrl).build();
            Log.v(TAG, "Downloading poster: " + popularMoviesUri.toString());
            URL httpUri = new URL(popularMoviesUri.toString());
            connection = (HttpURLConnection) httpUri.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            inputStream = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);

        } catch (MalformedURLException exception) {
            Log.e(TAG, "Invalid url", exception);
            return null;
        } catch (IOException exception) {
            Log.e(TAG, "Error downloading movie data", exception);
            return null;
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }
        return bitmap;
    }

    private List<MovieInfo> getMovieDetailListFromJson(String movieListJsonStr)
        throws JSONException, ParseException {

        // These are the names of the JSON objects that need to be extracted.
        final String RESULT_LIST = "results";
        final String VOTEAVG = "vote_average";
        final String TITLE = "title";
        final String POSTER = "poster_path";
        final String RELEASE = "release_date";
        final String DESCRIPTION = "overview";

        final String imageBaseUrl = "http://image.tmdb.org/t/p";
        final String imageSizeStr = "w185";

        JSONObject movieListJson = new JSONObject(movieListJsonStr);
        JSONArray resultsArray = movieListJson.getJSONArray(RESULT_LIST);

        List<MovieInfo> downloadedMovies = new ArrayList<MovieInfo>();
        for (int i = 0; i < resultsArray.length(); i++) {

            // Get the JSON object representing the movie
            JSONObject movieJSon = resultsArray.getJSONObject(i);
            String movieTitle = movieJSon.getString(TITLE);
            // There is an extra '/' in the poster link.
            String moviePoster = movieJSon.getString(POSTER).substring(1);
            moviePoster = Uri.parse(imageBaseUrl).buildUpon()
                    .appendPath(imageSizeStr)
                    .appendPath(moviePoster).build().toString();
            String movieDescription = movieJSon.getString(DESCRIPTION);
            String movieRelease =  movieJSon.getString(RELEASE);
            double movieVoteAvg = movieJSon.getDouble(VOTEAVG);

            downloadedMovies.add(new MovieInfo(movieTitle, moviePoster, movieDescription, movieRelease, movieVoteAvg));
        }
        return downloadedMovies;
    }
}
