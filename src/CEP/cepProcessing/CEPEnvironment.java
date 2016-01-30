package CEP.cepProcessing;

/**
 * Created by ruveni on 1/21/16.
 */
public class CEPEnvironment {
    public static final int TIME_GAP = 8; //in seconds
    public static final int THRESHOLD_LIFTED_INDEX=-1;
    public static final int THRESHOLD_HELICITY=150;
    public static final int THRESHOLD_INHIBITION=-50;
    public static final String LIFTED_INDEX_FILE_PATH="src/CEP/csvFiles/Lifted/best4layerliftedindexfile7.csv";
    public static final String HELICITY_INDEX_FILE_PATH="src/CEP/csvFiles/Helicity/stormrelativehelicity7.csv";
    public static final String CONVECTIVE_INHIBITION_FILE_PATH="src/CEP/csvFiles/Convective/convective7.csv";
    public static final double DISTANCE_THRESHOLD = 40;
}
