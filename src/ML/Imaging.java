package ML;

import org.apache.spark.mllib.linalg.Vector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Imaging {

    public static void drawClusters(ArrayList<Vector>[] clusters, int width, int height, String location) {
        HashMap<Integer, Integer> colors = new HashMap<>();
        Random rand = new Random();

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int rc = 0; rc < height; rc++) {
            for (int cc = 0; cc < width; cc++) {
                img.setRGB(rc, cc, Color.white.getRGB());
            }
        }

        for (int i = 0; i < clusters.length; i++) {
            ArrayList<Vector> points = clusters[i];
            for (Vector point: points){
                int x = ((int) point.apply(0));
                int y = ((int) point.apply(1));
                int colorVal = i;

                if(!colors.containsKey(colorVal)){
                    Integer color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()).getRGB();
                    colors.put(colorVal, color);
                    img.setRGB(x, y, color);
                }else {
                    img.setRGB(x, y, colors.get(colorVal));
                }

            }
        }
//        for (int i = 0; i < points.size(); i++) {
//            double[] vectorArray = points.get(i).toArray();
//            int x = (int) vectorArray[0];
//            int y = (int) vectorArray[1];
//            Integer colorVal = values.get(i);
//            if(!colors.containsKey(colorVal)){
//                Integer color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()).getRGB();
//                colors.put(colorVal, color);
//                img.setRGB(x, y, color);
//            }else {
//                img.setRGB(x, y, colors.get(colorVal));
//            }
//        }

        writeImage(location, img);
    }

    public static void drawPoints(ArrayList<Vector>[] clusters, int width, int height, String location) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ColorHelper ch = new ColorHelper();

        for (int i = 0; i < clusters.length; i++) {
            ArrayList<Vector> points = clusters[i];
            for (Vector point: points){
                int x = ((int) point.apply(0));
                int y = ((int) point.apply(1));
                double col = point.apply(2);

                Color color = ch.numberToColor(col);
                if (color != null) {
                    img.setRGB(x, y, ch.numberToColor(col).getRGB());
                } else {
                    img.setRGB(x, y, Color.white.getRGB());
                }
            }
        }

//        for (int i = 0; i < points.size(); i++) {
//            double[] vectorArray = points.get(i).toArray();
//            int x = (int) vectorArray[0];
//            int y = (int) vectorArray[1];
//            double col = vectorArray[2];
//
//            Color color = ch.numberToColor(col);
//            if (color != null) {
//                img.setRGB(x, y, ch.numberToColor(col).getRGB());
//            } else {
//                img.setRGB(x, y, Color.white.getRGB());
//            }
//        }

        writeImage(location, img);
    }

    public static void drawBounds(ArrayList<Vector>[] clusters, Vector[][] bounds, int width, int height, String location){
        HashMap<Integer, Integer> colors = new HashMap<>();
        Random rand = new Random();

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int rc = 0; rc < height; rc++) {
            for (int cc = 0; cc < width; cc++) {
                img.setRGB(rc, cc, Color.white.getRGB());
            }
        }

        for (int i = 0; i < clusters.length; i++) {
            ArrayList<Vector> points = clusters[i];
            for (Vector point: points){
                int x = ((int) point.apply(0));
                int y = ((int) point.apply(1));
                int colorVal = i;

                if(!colors.containsKey(colorVal)){
                    Integer color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()).getRGB();
                    colors.put(colorVal, color);
                    img.setRGB(x, y, color);
                }else {
                    img.setRGB(x, y, colors.get(colorVal));
                }

            }
        }
        Graphics2D graph = img.createGraphics();

        for (Vector[] bound_2: bounds){
            int x1 = ((int) bound_2[0].apply(0));
            int y1 = ((int) bound_2[0].apply(1));
            int x2 = ((int) bound_2[1].apply(0));
            int y2 = ((int) bound_2[1].apply(1));

            graph.setColor(Color.GREEN);
            graph.drawRect(x1, y1, x2 - x1, y2 - y1);
        }

        graph.dispose();

        writeImage(location, img);

    }


    public static void writeImage(String location, BufferedImage img){
        try {
            File outputfile = new File(location);
            ImageIO.write(img, "png", outputfile);
        } catch (IOException ex) {

        }
    }
    public static class ColorHelper {

        private final static int LOW = 0;
        private final static int HIGH = 255;
        private final static int HALF = (HIGH + 1) / 2;

        private final static Map<Integer, Color> map = initNumberToColorMap();
        private static int factor;

        /**
         *
         * @param value
         *            should be from 0 unti 100
         */
        public static Color numberToColor(final double value) {
            if (value < 0 || value > 100) {
                return null;
            }
            return numberToColorPercentage(value / 100);
        }

        /**
         * @param value
         *            should be from 0 unti 1
         * @return
         */
        public static Color numberToColorPercentage(final double value) {
            if (value < 0 || value > 1) {
                return null;
            }
            Double d = value * factor;
            int index = d.intValue();
            if (index == factor) {
                index--;
            }
            return map.get(index);
        }

        /**
         * @return
         */
        private static Map<Integer, Color> initNumberToColorMap() {
            HashMap<Integer, Color> localMap = new HashMap<Integer, Color>();
            int r = LOW;
            int g = LOW;
            int b = HALF;

            // factor (increment or decrement)
            int rF = 0;
            int gF = 0;
            int bF = 1;

            int count = 0;
            // 1276 steps
            while (true) {
                localMap.put(count++, new Color(r, g, b));
                if (b == HIGH) {
                    gF = 1; // increment green
                }
                if (g == HIGH) {
                    bF = -1; // decrement blue
                    // rF = +1; // increment red
                }
                if (b == LOW) {
                    rF = +1; // increment red
                }
                if (r == HIGH) {
                    gF = -1; // decrement green
                }
                if (g == LOW && b == LOW) {
                    rF = -1; // decrement red
                }
                if (r < HALF && g == LOW && b == LOW) {
                    break; // finish
                }
                r += rF;
                g += gF;
                b += bF;
                r = rangeCheck(r);
                g = rangeCheck(g);
                b = rangeCheck(b);
            }
            initList(localMap);
            return localMap;
        }

        private static void initList(final HashMap<Integer, Color> localMap) {
            ArrayList<Integer> list = new ArrayList<Integer>(localMap.keySet());
            Collections.sort(list);
            Integer min = list.get(0);
            Integer max = list.get(list.size() - 1);
            factor = max + 1;
            //System.out.println(factor);
        }

        private static int rangeCheck(final int value) {
            if (value > HIGH) {
                return HIGH;
            } else if (value < LOW) {
                return LOW;
            }
            return value;
        }

    }
}
