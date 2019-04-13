package com.example.udacityalmahmoud.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.udacityalmahmoud.popularmovies.R;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailActivity extends AppCompatActivity {

    Integer id;
    TextView title, userRating, releaseDate, plot;
    ImageView posterImage;

    String api_key = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = (TextView) findViewById(R.id.title);
        userRating = (TextView) findViewById(R.id.user_rating);
        releaseDate = (TextView) findViewById(R.id.release_date);
        plot = (TextView) findViewById(R.id.synopsis);
        posterImage = (ImageView) findViewById(R.id.poster_image);

        Intent intent = getIntent();
        id = intent.getIntExtra("Movie ID", 0);

        FetchMovieDetails fetchMovieDetails = new FetchMovieDetails();
        fetchMovieDetails.execute();
    }

    public class FetchMovieDetails extends AsyncTask<Void, Void, Void> {

        String LOG_TAG = "FetchMovieDetails";
        String originalTitle, releaseDate, plotSynopsis, posterPath;
        Double ratings;

        @Override
        protected Void doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;

            try {
                String base_url = "https://api.themoviedb.org/3/movie/";
                URL url = new URL(base_url + Integer.toString(id) + "?api_key=" + api_key);
                Log.d(LOG_TAG,"URL: " + url.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if(buffer.length() == 0) {
                    return null;
                }
                movieJsonStr = buffer.toString();
                Log.d(LOG_TAG, "JSON Parsed: " + movieJsonStr);

                JSONObject main = new JSONObject(movieJsonStr);
                originalTitle = main.getString("original_title");
                releaseDate = main.getString("release_date");
                ratings = main.getDouble("vote_average");
                plotSynopsis = main.getString("overview");
                posterPath = "https://image.tmdb.org/t/p/w600_and_h900_bestv2" + main.getString("poster_path");

            }catch(Exception e){
                Log.e(LOG_TAG, "Error", e);
            } finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            title.setText(originalTitle);
            userRating.setText("User Ratings: " + Double.toString(ratings));
            DetailActivity.this.releaseDate.setText("Release Date: " + releaseDate);
            plot.setText(plotSynopsis);
            posterImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            posterImage.setPadding(8, 8, 8, 8);
            Picasso.with(DetailActivity.this).load(posterPath).into(posterImage);
        }
    }
}
