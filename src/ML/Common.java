package ML;

import Util.LatLonUtil;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;

import java.util.ArrayList;

/**
 * Created by vidu on 2/2/16.
 */
public class Common {

    public static Vector[] getBounds(ArrayList<Vector> list){
        double left = 0;
        double right = 360;
        double top = 180;
        double bottom = 0;

        for(Vector point:list){
            //double[] latLon = LatLonUtil.lambertToLatLon(point.apply(0), point.apply(1), 0, 0, 0, 0);
            double latitude = point.apply(0);
            double longitude = point.apply(1);


            System.out.println(latitude + " " + longitude);

            if(left<longitude){
                left = longitude;
            }
            if(right>longitude){
                right = longitude;
            }
            if(top>latitude){
                top = latitude;
            }
            if(bottom<latitude){
                bottom = latitude;
            }

        }

        Vector upper = Vectors.dense(top,left);
        Vector lower = Vectors.dense(bottom,right);

        return new Vector[]{upper, lower};
    }

}
