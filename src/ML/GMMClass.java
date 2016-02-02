package ML;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.clustering.GaussianMixture;
import org.apache.spark.mllib.clustering.GaussianMixtureModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.stat.distribution.MultivariateGaussian;
import org.apache.spark.rdd.RDD;
import org.codehaus.janino.Java;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vidu on 1/30/16.
 */
public class GMMClass {

    public static ArrayList<Vector>[] run(ArrayList<Vector> vals) {
        SparkConf sparkConf = new SparkConf().setAppName("JavaGMM").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        JavaRDD<Vector> points = sc.parallelize(vals);
        int find_f_iterations = 10;
        int find_f_runs = 1;
        //System.out.println(BIC(points, 10));
        int k = findK(points);//KMeansClass.findK(points, find_f_iterations, find_f_runs);;//findK(points);

        //int k =10;
        GaussianMixtureModel gmm = new GaussianMixture().setK(k).run(points.rdd());
        JavaRDD<Integer> values = gmm.predict(points);//.toJavaRDD().map(o -> (Integer)o);

        ArrayList<Vector>[] clusters = new ArrayList[k];
        for (int i = 0; i < k; i++) {
            clusters[i] = new ArrayList<>();
        }


        List<Vector> pointList = points.collect();
        List<Integer> valueList = values.collect();

        for (int i = 0; i < pointList.size(); i++) {
            clusters[valueList.get(i)].add(pointList.get(i));
        }


        //printRelations(points,gmm);
        sc.stop();


        return clusters;
    }

    public static int findK(JavaRDD<Vector> points){
        int bestk = 0;
        double bestBIC = Double.MAX_VALUE;
        int kMax = 15;//(int)Math.sqrt(points.count());
        ArrayList<Double> BICs = new ArrayList<>();
        for (int k = 1;  k < kMax; k++) {
            double bic = BIC(points, k);
            BICs.add(bic);
            if(bic<bestBIC){
                bestBIC = bic;
                bestk = k;
            }
        }
        System.out.println(BICs);
        return bestk;
    }

    public static double BIC(JavaRDD<Vector> points, int k){
        GaussianMixtureModel gmm = new GaussianMixture().setK(k).run(points.rdd());

        //double sumLogWeight = 0;

        JavaRDD<double[]> probabilities = gmm.predictSoft(points.rdd()).toJavaRDD();


        JavaRDD<Double> maxProbabilities = probabilities.map(new Function<double[], Double>() {
            Double max;
            @Override
            public Double call(double[] probabilities) throws Exception {
                max = 0.0;
                for (double probability :
                        probabilities) {
                    //System.out.print(probability+" ");
                    if (max < probability) {
                        max = probability;
                    }
                }
                return max;
            }
        });

        //JavaRDD<Double> LogMaxProbabilities = maxProbabilities.map((Function<Double, Double>) (D) -> Math.log(D));
        //LogMaxProbabilities.foreach(d -> System.out.println(d));
        Double sum = maxProbabilities.reduce((Function2<Double, Double, Double>) (a, b) -> a+b);
        //System.out.println(LogSum);
//        for (int i = 0; i < gmm.k(); i++) {
//            sumLogWeight += Math.log(gmm.weights()[i]);
//        }

        return sum;// + (3*k-1)*Math.log(points.count());
    }

    public static void printRelations(JavaRDD<Vector> points, GaussianMixtureModel model){
        List<Vector> pointVectors = points.collect();

        
        for (Vector point :
                pointVectors) {
            double[] membership = model.predictSoft(point);
            for (int i = 0; i < membership.length; i++) {
                System.out.print(membership[i]+", ");
            }
            System.out.println();
        }

        for(int j=0; j<model.k(); j++) {
            System.out.printf("weight=%f\nmu=%s\nsigma=\n%s\n",
                    model.weights()[j], model.gaussians()[j].mu(), model.gaussians()[j].sigma());
        }
    }

//    public static int findK(JavaRDD<Vector> points){
//        int kMax = (int)Math.sqrt(points.count());
//        double Fk = 0;
//        double ak = 0;
//        double ak_1 = 0; //a(k - 1)
//        double Sk = 0;
//        double Sk_1 = 0; //S(k - 1)
//
//        int dimensions = points.first().size();
//
//
//        for (int k = 1;  k < kMax; k++) {
//            GaussianMixtureModel gmm = new GaussianMixture().setK(k).run(points.rdd());
////            KMeans kmeans = new KMeans();
////            kmeans = kmeans.setEpsilon(epsilon).setK(k).setMaxIterations(iterations).setRuns(runs).setInitializationMode(KMeans.K_MEANS_PARALLEL());
////
////
////            model = kmeans.run(points.rdd());
//            //model = KMeans.train(points.rdd(), k, iterations, runs, KMeans.K_MEANS_PARALLEL());
//            JavaRDD<Integer> values = gmm.predict(points);//.toJavaRDD().map(o -> (Integer)o);
//            Sk = findSk(k, gmm, values, points);
//            ak = findak(k, dimensions, ak_1);
//            Fk = findFk(k, Sk, Sk_1, ak);
//
//            if(Fk < 0.89){
//                return k;
//            }
//            Sk_1 = Sk;
//            ak_1 = ak;
//        }
//
//        return 1;
//    }
//
//
//    public static double findSk(int k, GaussianMixtureModel model, JavaRDD<Integer> values, JavaRDD<Vector> points){
//        if(k==1){
//            return 0.0;
//        }else {
//
//
//            Vector[] centers = model.clusterCenters();
//            JavaPairRDD<Integer, Vector> pointCenters = values.mapToPair(new PairFunction<Integer, Integer, Vector>() {
//                int x = 0;
//
//                @Override
//                public Tuple2<Integer, Vector> call(Integer i) throws Exception {
//                    return new Tuple2<>(x++, centers[i]);
//
//                }
//            });
//
//            c
//
//            JavaPairRDD<Integer, Vector> union = pointCenters.union(indexedPoints);
//
//            JavaPairRDD<Integer, Vector> indexedDistances = union.reduceByKey(new Function2<Vector, Vector, Vector>() {
//                @Override
//                public Vector call(Vector vector, Vector vector2) throws Exception {
//                    return Vectors.dense(Vectors.sqdist(vector, vector2));
//                }
//            });
//
//            JavaRDD<Double> distances = indexedDistances.map(new Function<Tuple2<Integer, Vector>, Double>() {
//                @Override
//                public Double call(Tuple2<Integer, Vector> tuple) throws Exception {
//                    return tuple._2().apply(0);
//                }
//            });
//
//            return distances.reduce(new Function2<Double, Double, Double>() {
//                @Override
//                public Double call(Double a, Double b) throws Exception {
//                    return a + b;
//                }
//            });
//        }
//    }
//
//    public static double findak(int k, int Nd, double ak_1/*a(k-1)*/){
//        if(k==1){
//            return 1.0;
//        }else if(k==2){
//            return  1.0-3.0/(4.0*Nd);
//        }else{
//            return ak_1 + (1.0-ak_1)/6.0;
//        }
//    }
//
//    public static double findFk(int k, double Sk, double Sk_1 /*S(k-1)*/, double ak){
//        if(k==1){
//            return 1;
//        }else if(Sk_1==0){
//            return 1;
//        }else{
//            return Sk/(ak*Sk_1);
//        }
//    }
}
