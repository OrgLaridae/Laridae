package ML;

import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;

import java.util.ArrayList;

public class Main {
    static int width = 240;
    static int height = 240;

    public static void main(String[] args) {
        String path = "/Users/vidu/Desktop/sample/";
        double[][] val = SataliteRead.dBZToZ(path + "ekxv0010.txt");
        SataliteRead.writeZ(path + "z.txt", val);

        ArrayList<Vector>[] clusters= KMeansClass.run(path, 20, 1, 0, 0, width, height);

        Imaging.drawClusters(clusters, width, height, path + "clusters.png");
        Imaging.drawPoints(clusters, width, width, path + "points.png");

        Vector[][] bounds = new Vector[clusters.length][2];
        for (int i = 0; i < clusters.length; i++) {
            bounds[i] = getBounds(clusters[i]);
        }
        Imaging.drawBounds(clusters, bounds, width, height, path + "bounds.png");

    }

    public static Vector[] getBounds(ArrayList<Vector> list){
        double left = width;
        double right = 0;
        double top = height;
        double bottom = 0;

        for(Vector point:list){
            if(left>point.apply(0)){
                left = point.apply(0);
            }
            if(right<point.apply(0)){
                right = point.apply(0);
            }
            if(top>point.apply(1)){
                top = point.apply(1);
            }
            if(bottom<point.apply(1)){
                bottom = point.apply(1);
            }

        }

        Vector upper = Vectors.dense(left,top);
        Vector lower = Vectors.dense(right,bottom);

        return new Vector[]{upper, lower};
    }

}

