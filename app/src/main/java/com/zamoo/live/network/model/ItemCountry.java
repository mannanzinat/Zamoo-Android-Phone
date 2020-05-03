package com.zamoo.live.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ItemCountry {

    @SerializedName("movies")
    @Expose
    private List<CommonModel> movies = null;
    @SerializedName("tvseries")
    @Expose
    private List<CommonModel> tvseries = null;
    @SerializedName("tv")
    @Expose
    private List<TvModel> tv = null;
    @SerializedName("radio")
    @Expose
    private List<RadioModel> radio = null;

    public List<CommonModel> getMovies() {
        return movies;
    }

    public void setMovies(List<CommonModel> movies) {
        this.movies = movies;
    }

    public List<CommonModel> getTvseries() {
        return tvseries;
    }

    public void setTvseries(List<CommonModel> tvseries) {
        this.tvseries = tvseries;
    }

    public List<TvModel> getTv() {
        return tv;
    }

    public void setTv(List<TvModel> tv) {
        this.tv = tv;
    }

    public List<RadioModel> getRadio() {
        return radio;
    }

    public void setRadio(List<RadioModel> radio) {
        this.radio = radio;
    }
}
