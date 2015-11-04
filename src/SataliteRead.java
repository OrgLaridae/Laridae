import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SataliteRead {
    public static double[][] dBZToZ(String location){
        double[][] Z = new double[240][240];
        double alpha = 0.5;
        double beta = -32;
        Path path = Paths.get(location);
        try (Stream<String> lines = Files.lines(path)) {
            String[] lineArray = lines.collect(Collectors.toList()).toArray(new String[0]);
            for (int i = 0; i < lineArray.length; i++) {
                String[] stringData = lineArray[i].split(",");
                for (int j = 0; j < 240; j++) {
                    double data = Double.parseDouble(stringData[j]);
                    data=(data==255.0)? 0.0:data;
                    data = (alpha*data)+beta;
                    data = Math.pow(10, (data/10));
                    Z[i][j] = data;

                }
            }
        } catch (IOException ex) {

        }
        return Z;
    }


    public static void main(String[] args) {
        double[][] val = dBZToZ("/Users/vidu/Desktop/sample/ekxv0000.txt");
        for (int i = 0; i <240; i++) {
            for (int j = 0; j < 240; j++) {
                System.out.print(val[i][j]+",");
            }
            System.out.println();
        }
    }

}
