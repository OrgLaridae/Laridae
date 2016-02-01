package Util;

public class Lambert_LatLon {
    public static double[] latLonToLambert(double centralMeridian, double latitude, double longitude){
        double x = longitude - centralMeridian;
        double y = Math.sin(latitude);
        return new double[]{x, y};
    }

    public static double[] lambertToLatLon(double centralMeridian, double x, double y){
        double longitude = x +centralMeridian;
        double latitude = Math.asin(y);

        return new double[]{latitude, longitude};
    }

}
