import ML.Imaging;
import ML.KMeansClass;
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
            bounds[i] = KMeansClass.getBounds(clusters[i], width, height);
        }
        Imaging.drawBounds(clusters, bounds, width, height, path + "bounds.png");

    }
}

