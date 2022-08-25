package com.ldd.on_callpharmacy;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseFile;

import java.util.Comparator;

public class Pharmacy /*implements Comparable<Pharmacy>*/{
    private ParseFile phaImage;
    private String name;
    private double distance;
    private String open_close;
    private LatLng myLatLng;
    private LatLng pLatLng;


    public Pharmacy() {
    }

    public Pharmacy(ParseFile phaImage, String name, double distance, String open_close, LatLng myLatLng, LatLng pLatLng) {
        this.phaImage = phaImage;
        this.name = name;
        this.distance = distance;
        this.open_close = open_close;
        this.myLatLng = myLatLng;
        this.pLatLng = pLatLng;
    }

    public static Comparator<Pharmacy> sortbydistance = new Comparator<Pharmacy>() {
        @Override
        public int compare(Pharmacy p1, Pharmacy p2) {
//            return p1.getName().compareTo(p2.getName());
            return Double.compare(p1.getDistance(), p2.getDistance());
        }
    };

    public ParseFile getPhaImage() {
        return phaImage;
    }

    public void setPhaImage(ParseFile phaImage) {
        this.phaImage = phaImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getOpen_close() {
        return open_close;
    }

    public void setOpen_close(String open_close) {
        this.open_close = open_close;
    }

    public LatLng getMyLatLng() {
        return myLatLng;
    }

    public void setMyLatLng(LatLng myLatLng) {
        this.myLatLng = myLatLng;
    }

    public LatLng getpLatLng() {
        return pLatLng;
    }

    public void setpLatLng(LatLng pLatLng) {
        this.pLatLng = pLatLng;
    }
}
