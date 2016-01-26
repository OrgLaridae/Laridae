package WRF;

import CEP.customEvents.Boundary;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by ruveni on 1/25/16.
 */
// Calculating e_ns, e_we, ref_lat, ref_lon


public class NamelistCalc{

    static double Meters_per_lat = 110.57;
    static double Meters_per_lon = 111.32;

    public static double get_refLat(Boundary boundary){
        //Assuming that boundryString is in the form lat_upper,lat_lower,lon_east, lon_west
        double result=(boundary.getMinLatitude()+boundary.getMaxLatitude())/2;
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        return Double.parseDouble(df.format(result));

    }

    public static double get_refLon(Boundary boundary){
        //Assuming that boundryString is in the form lat_upper,lat_lower,lon_east, lon_west
        double result=(boundary.getMinLongitude()+boundary.getMaxLongitude())/2;
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        return Double.parseDouble(df.format(result));
    }

    public static int get_e_we(Boundary boundary, int resolution_km){
        //Calculating lattitude gap
        double gap = Math.abs(boundary.getMinLongitude()-boundary.getMaxLongitude());

        //converting to meters
        double distance = gap*Meters_per_lat;

        //intermediate value
        double grid_units = distance/resolution_km;

        //Calculating relative grid units
        double units = 75*(grid_units/383.94);  //383.4 is the no of grid units for 75 units
        units = Math.floor(units);

        return (int)units;
    }

    public static int get_e_ns(Boundary boundary, int resolution_km){
        //Calculating lattitude gap
        double gap = Math.abs(boundary.getMinLatitude()-boundary.getMaxLatitude());

        //converting to meters
        double distance = gap*Meters_per_lon;

        //intermediate value
        double grid_units = distance/resolution_km;

        //Calculating relative grid units
        double units = 70*(grid_units/180.92);  //180.92 is the no of grid units for 70 units
        units = Math.floor(units);

        return (int)units;
    }

}