package Util;

public class Lambert_LatLon {
    public static double[] latLonToLambert
            (double latitude, double longitude, double refLatitude, double standardParallel1,  double standardParallel2,
             double refLongitude){
        double F = F(standardParallel1, standardParallel2);
        double n = n(standardParallel1, standardParallel2);
        double p = p(latitude, n, F);
        double p0 = p0(refLatitude, n, F);

        double x = p * Math.sin(n*(longitude-refLongitude));
        double y = p0 - p*Math.cos(n*(longitude-refLongitude));
        return new double[]{x, y};
    }


    private static double n(double st1, double st2){
        return Math.log10(Math.cos(st1)/Math.cos(st2)) / Math.log10(Math.tan((Math.PI/4)+st2/2)/Math.tan((Math.PI/4)+st1/2));
    }

    private static double p(double latitude, double n, double F){
        return F/Math.pow(Math.tan(Math.PI/4 + latitude/2),n);
    }

    private static double p0(double refLatitude, double n, double F){
        return p(refLatitude, n, F);
    }

    private static double F(double st1, double n){
        return Math.cos(st1) * Math.pow(Math.tan(Math.PI/4 + st1/2),n) / n;
    }

    public static double[] lambertToLatLon
            (double x, double y, double refLatitude, double standardParallel1,  double standardParallel2,
              double refLongitude){
        double F = F(standardParallel1, standardParallel2);
        double n = n(standardParallel1, standardParallel2);
        double p0 = p0(refLatitude, n, F);

        double q = q(x, y, n, p0);
        double theta = theta(x, y, p0);

        double latitude = 2*Math.atan(Math.pow(F/q, 1/n)) - Math.PI/2;

        double longitude = refLongitude + theta/n;

        return new double[]{latitude, longitude};
    }

    private static double q(double x, double y, double n, double p0){
        return sign(n) * Math.sqrt(Math.pow(x, 2) + Math.pow(p0-y, 2));
    }

    private static double theta(double x, double y, double p0){
        return Math.atan(x/(p0-y));
    }
    private static int sign(double val){
        return val>0? 1:-1;
    }
}
