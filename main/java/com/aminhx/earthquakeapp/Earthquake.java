package com.aminhx.earthquakeapp;

public class Earthquake {
    private String cord;
    private String types;
    private String speed;
    private String ma;


    public String getCordinate() {
        return cord;
    }
    public void setCordinate(String cord) {
        this.cord = cord;
    }

    public String getTypes() {
        return types;
    }
    public void setTypes(String types) {
        this.types = types;
    }

    public String getSpeed() {
        return speed;
    }
    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getMagnitude() {
        return ma;
    }
    public void setMagnitude(String ma) {
        this.ma = ma;
    }

    public String toString() {
        return String.format("Cordinate:%s, Speed:%s, Magnitude:%s \n", cord, speed, ma);
    }

}
