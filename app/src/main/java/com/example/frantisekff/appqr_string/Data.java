package com.example.frantisekff.appqr_string;

import java.util.Map;

/**
 * Created by frantisek.ff on 27. 2. 2018.
 */

public class Data {
    private Point[] points = new Point[4];
    private int num_of_chars;
    private int url;
    Map<String, Object> response;


    public Data(Point[] points, int num_of_chars,Map<String, Object> response) {
        this.points = points;
        this.num_of_chars = num_of_chars;
        this.response = response;
    }

    public Data() {

    }

    public int getNum_of_chars() {
        return num_of_chars;
    }

    public void setNum_of_chars(int num_of_chars) {
        this.num_of_chars = num_of_chars;
    }

    public Map<String, Object> getResponse() {
        return response;
    }

    public void setResponse(Map<String, Object> response) {
        this.response = response;
    }

    public Point[] getPoints() {
        return points;
    }

    public void setPoints(Point[] points) {
        this.points = points;
    }

}
