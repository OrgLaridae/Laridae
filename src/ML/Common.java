package ML;

import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;

import java.util.ArrayList;

/**
 * Created by vidu on 2/2/16.
 */
public class Common {

    public static Vector[] getBounds(ArrayList<Vector> list, int lon, int lat){
        double left = 0;
        double right = lon;
        double top = lat;
        double bottom = 0;

        for(Vector point:list){
            if(left<point.apply(0)){
                left = point.apply(0);
            }
            if(right>point.apply(0)){
                right = point.apply(0);
            }
            if(top>point.apply(1)){
                top = point.apply(1);
            }
            if(bottom<point.apply(1)){
                bottom = point.apply(1);
            }

        }

        Vector upper = Vectors.dense(top,left);
        Vector lower = Vectors.dense(bottom,right);

        return new Vector[]{upper, lower};
    }

}
