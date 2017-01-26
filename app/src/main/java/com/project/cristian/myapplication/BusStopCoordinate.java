package com.project.cristian.myapplication;



public class BusStopCoordinate {
    private String name;
    private double x;
    private double y;
    private String http;
    private long distanceFromOriginal;          // distance from user's current location

    public BusStopCoordinate(String name, double x, double y, String http,  long distanceFromOriginal){
        this.name = name;
        this.x =x;
        this.y =y;
        this.http = http;
        this.distanceFromOriginal = distanceFromOriginal;
    }

    public String getName(){
        return name;
    }

    public double getY() {
        return y;
    }

    public double getX() { return x; }

    public String getHttp(){
        return http;
    }

    public long getDistanceFromOriginal() { return  distanceFromOriginal; }
}
