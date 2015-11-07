package ML;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class KMeansClass {

    private static class ParsePoint implements FlatMapFunction<String, Vector> {
        private static final Pattern COMMA = Pattern.compile(",");
        int x = 0;
        @Override
        public Iterable<Vector> call(String line) {
            String[] tok = COMMA.split(line);
            int points = tok.length;
            ArrayList<Vector> values = new ArrayList<>();
            for (int i = 0; i < points; ++i) {
                Double value = Double.parseDouble(tok[i]);
                if(value>0.007)
                    values.add(Vectors.dense(x,i,0));//value));
            }
            x++;
            return values;
        }
    }


    public static List[] run(String parentPath, int iterations, int runs) {

        String inputFile = parentPath + "z.txt";

        SparkConf sparkConf = new SparkConf().setAppName("JavaKMeans");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        JavaRDD<String> lines = sc.textFile(inputFile);

        JavaRDD<Vector> points = lines.flatMap(new ParsePoint());
        int k = findK(points, iterations, runs);
        KMeansModel model = KMeans.train(points.rdd(), k, iterations, runs, KMeans.K_MEANS_PARALLEL());


        JavaRDD<Integer> values = model.predict(points);


//    System.out.println("Cluster centers:");
//    for (Vector center : model.clusterCenters()) {
//      System.out.println(" " + center);
//    }
//    double cost = model.computeCost(points.rdd());
//    System.out.println("Cost: " + cost);


        List<Integer> valueList = values.collect();
        List<Vector> pointList = points.collect();

        sc.stop();

        System.out.println(k);
        return new List[]{valueList, pointList};


    }

    public static int findK(JavaRDD<Vector> points, int iterations, int runs){
        int kMax = (int)Math.sqrt(points.count());
        KMeansModel model = null;
        double Fk = 0;
        double ak = 0;
        double ak_1 = 0; //ak - 1
        double Sk = 0;
        double Sk_1 = 0; //Sk - 1

        int dimensions = points.first().size();


        for (int k = 1;  k < kMax; k++) {
            model = KMeans.train(points.rdd(), k, iterations, runs, KMeans.K_MEANS_PARALLEL());
            JavaRDD<Integer> values = model.predict(points);
            Sk = findSk(k, model, values, points);
            ak = findak(k, dimensions, ak_1);
            Fk = findFk(k, Sk, Sk_1, ak);

            if(Fk < 0.9){
                return k;
            }
            Sk_1 = Sk;
            ak_1 = ak;
        }

        return 1;
    }


    public static double findSk(int k, KMeansModel model, JavaRDD<Integer> values, JavaRDD<Vector> points){
        if(k==1){
            return 0.0;
        }else {
            Vector[] centers = model.clusterCenters();
            JavaPairRDD<Integer, Vector> pointCenters = values.mapToPair(new PairFunction<Integer, Integer, Vector>() {
                int x = 0;

                @Override
                public Tuple2<Integer, Vector> call(Integer i) throws Exception {
                    return new Tuple2<>(x++, centers[i]);

                }
            });

            JavaPairRDD<Integer, Vector> indexedPoints = points.mapToPair(new PairFunction<Vector, Integer, Vector>() {
                int x = 0;

                @Override
                public Tuple2<Integer, Vector> call(Vector vector) throws Exception {
                    return new Tuple2<Integer, Vector>(x++, vector);
                }
            });

            JavaPairRDD<Integer, Vector> union = pointCenters.union(indexedPoints);

            JavaPairRDD<Integer, Vector> indexedDistances = union.reduceByKey(new Function2<Vector, Vector, Vector>() {
                @Override
                public Vector call(Vector vector, Vector vector2) throws Exception {
                    return Vectors.dense(Vectors.sqdist(vector, vector2));
                }
            });

            JavaRDD<Double> distances = indexedDistances.map(new Function<Tuple2<Integer, Vector>, Double>() {
                @Override
                public Double call(Tuple2<Integer, Vector> tuple) throws Exception {
                    return tuple._2().apply(0);
                }
            });

            return distances.reduce(new Function2<Double, Double, Double>() {
                @Override
                public Double call(Double a, Double b) throws Exception {
                    return a + b;
                }
            });
        }
    }

    public static double findak(int k, int Nd, double ak_1){
        if(k==1){
            return 0.0;
        }else if(k==2){
            return  1.0-3.0/(4.0*Nd);
        }else{
            return ak_1 + (1.0-ak_1)/6.0;
        }
    }

    public static double findFk(int k, double Sk, double Sk_1, double ak){
        if(k==1){
            return 1;
        }else if(Sk_1==0){
            return 1;
        }else{
            return Sk/(ak*Sk_1);
        }
    }
}