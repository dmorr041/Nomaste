package com.example.dmorr.nomaste;

import android.support.v7.widget.RecyclerView;

public class DashboardData {

    private String imageName;
    private String imageURL;

    public DashboardData(){

    }

    public DashboardData(String name, String url){
        this.imageName = name;
        this.imageURL = url;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageURL() {
        return imageURL;
    }
}
