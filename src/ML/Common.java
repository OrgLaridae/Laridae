package ML;

import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;

import java.util.ArrayList;

/**
 * Created by vidu on 2/2/16.
 */
public class Common {

    public static Vector[] getBounds(ArrayList<Vector> list){
        double left = -180;
        double right = 180;
        double top = 90;
        double bottom = -90;

        for(Vector point:list){
            double[] latLon = Util.Lambert_LatLon.lambertToLatLon(point.apply(0), point.apply(1), 0, 0, 0, 0);
            double latitude = Math.toDegrees(latLon[0]);
            double longitude = Math.toDegrees(latLon[1]);

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
