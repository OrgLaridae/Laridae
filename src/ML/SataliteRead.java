package ML;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

    public static void writeZ(String location, double[][] Z){
        Path path = Paths.get(location);
        File file = path.toFile();
        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for (int i = 0; i < Z.length; i++) {
                for (int j = 0; j < Z[i].length; j++) {
                    bw.write(Z[i][j]+",");
                }
                bw.newLine();
            }
            bw.close();
        }catch (IOException ex){

        }
    }


}
