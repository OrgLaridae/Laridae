package ML;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String path = "/Users/vidu/Desktop/sample/";
        double[][] val = SataliteRead.dBZToZ(path + "ekxv0010.txt");
        SataliteRead.writeZ(path + "z.txt", val);

        List[] lists = KMeansClass.run(path, 20, 1);

        Imaging.drawClusters(lists[0], lists[1], 240, 240, path + "clusters.png");
        Imaging.drawPoints(lists[1], 240, 240, path + "points.png");
    }


}