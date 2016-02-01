package Util;

public class Lambert_LatLon {
    public static double[] latLonToLambert
            (double latitude, double refLatitude, double standardParallel1,  double standardParallel2,
             double longitude, double refLongitude){
        double F = F(standardParallel1, standardParallel2);
        double n = n(standardParallel1, standardParallel2);
        double p = p(latitude, n, F);
        double p0 = p0(refLatitude, n, F);

        double x = p * Math.sin(n*(longitude-refLongitude));
        double y = p0 - p*Math.cos(n*(longitude-refLongitude));
        return new double[]{x, y};
    }


    public static double n(double st1, double st2){
        return Math.log10(Math.cos(st1)/Math.cos(st2)) / Math.log10(Math.tan((Math.PI/4)+st2/2)/Math.tan((Math.PI/4)+st1/2));
    }

    public static double p(double latitude, double n, double F){
        return F/Math.pow(Math.tan(Math.PI/4 + latitude/2),n);
    }

    public static double p0(double refLatitude, double n, double F){
        return p(refLatitude, n, F);
    }

    public static double F(double st1, double n){
        return Math.cos(st1) * Math.pow(Math.tan(Math.PI/4 + st1/2),n) / n;
    }
}
