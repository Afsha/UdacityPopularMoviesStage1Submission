package com.test.moviesdb.popularmovies;


import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;


public class MovieInfo implements Parcelable {

    private String title;
    private String description;
    private String posterUrl;
    private String releaseDate;
    private double voteAverage;

    public MovieInfo(String title, String posterUrl, String description, String releaseDate, double voteAverage) {
        this.title = title;
        this.description = description;
        this.posterUrl = posterUrl;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
    }

    private MovieInfo(Parcel in) {
        this.title = in.readString();
        this.description = in.readString();
        this.posterUrl = in.readString();
        this.releaseDate = in.readString();
        this.voteAverage = in.readDouble();
    }

    public String getTitle() {
        return title;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<MovieInfo> CREATOR
            = new Parcelable.Creator<MovieInfo>() {
        public MovieInfo createFromParcel(Parcel inParcel) {
            return new MovieInfo(inParcel);
        }

        public MovieInfo[] newArray(int size) {
            return new MovieInfo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(posterUrl);
        parcel.writeString(releaseDate);
        parcel.writeDouble(voteAverage);
    }
}
