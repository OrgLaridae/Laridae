package CEP.sidhdhiExtention;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;


/**
 * Created by ruveni on 16/11/15.
 */

@SiddhiExtension(namespace = "radar", function = "boundary")
public class RadarBoundary extends FunctionExecutor{
    Logger log = Logger.getLogger(RadarBoundary.class);
    private static final int MATRIX_SIZE = 240;
    double[][] matrix;
    int minRow = 240, maxRow = 0, minCol = 240, maxCol = 0;
    private static final int THRESHOLD = 40; //in dBZ
    Attribute.Type returnType;
    private static final double ALPHA = 0.5;
    private static final double BETA = -32;

    @Override
    public void init(Attribute.Type[] types, SiddhiContext siddhiContext) {
        matrix = new double[MATRIX_SIZE][MATRIX_SIZE];
        for (Attribute.Type attributeType : types) {
            if (attributeType == Attribute.Type.STRING) {
                returnType = attributeType;
                break;
            } else {
                throw new QueryCreationException("file cannot have parameters with types String or Bool");
            }
        }
    }

    @Override
    protected Object process(Object o) {
        if (o instanceof Object) {
            String data = o.toString();
            String[] inputValues = data.split(",");
            int k = 1;

            for (int i = 0; i < MATRIX_SIZE; i++) {
                for (int j = 0; j < MATRIX_SIZE; j++) {
                    //in dbZ
                    double radarData = Double.parseDouble(inputValues[k]);
                    radarData = (radarData == 255.0) ? 0 : radarData;
                    //converts to z values
//                    radarData = (ALPHA * radarData) + BETA;
//                    radarData = Math.pow(10, (radarData / 10));
                    //calculates the boundary
                    if (radarData > THRESHOLD) {
                        if (i > maxRow) {
                            maxRow = i;
                        }
                        if (i < minRow) {
                            minRow = i;
                        }
                        if (j > maxCol) {
                            maxCol = j;
                        }
                        if (j < minCol) {
                            minCol = j;
                        }
                    }
                    k++;
                }
            }
        }
        //returns the boundary
        return minRow + " " + maxRow + " " + minCol + " " + maxCol;
    }

    public void destroy() {
    }

    public Attribute.Type getReturnType() {
        return returnType;
    }

}
