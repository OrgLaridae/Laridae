import ML.Imaging;
import ML.KMeansClass;
import ML.RadarData;
import org.apache.spark.mllib.linalg.Vector;

import java.util.ArrayList;

public class Main {
    static int width = 240;
    static int height = 240;

    public static void main(String[] args) {
        String path = "/Users/vidu/Desktop/sample/";
        double[][] val = RadarData.dBZToZ(path + "ekxv0000.txt");

        RadarData.writeZ(path + "z.txt", val);

        RadarData.setSensitivity(0.01);
        RadarData.calculateThreashold();

        ArrayList<Vector> vals = RadarData.readZ(path + "z.txt", 25, 62, 172, 194);

        ArrayList<Vector>[] clusters= KMeansClass.run(vals, 20, 1);

        Imaging.drawClusters(clusters, width, height, path + "clusters.png");
        Imaging.drawPoints(clusters, width, width, path + "points.png");

        Vector[][] bounds = new Vector[clusters.length][2];
        for (int i = 0; i < clusters.length; i++) {
            bounds[i] = KMeansClass.getBounds(clusters[i], width, height);
        }
        Imaging.drawBounds(clusters, bounds, width, height, path + "bounds.png");

    }
}

