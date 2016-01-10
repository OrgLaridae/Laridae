package CEP.cepProcessing;

import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by chamil on 9/3/15.
 */
public class RadarAlert {
    private SiddhiManager siddhiManager;
    private InputHandler inputHandler;

    public RadarAlert(SiddhiManager siddhiManager){
        this.siddhiManager=siddhiManager;
        String queryReference = siddhiManager.addQuery("from reflectStream select file:getPath(reflexMatrix) as filePath, radar:boundary(reflexMatrix) as boundary insert into boundaryStream ;");

        siddhiManager.addCallback(queryReference, new QueryCallback() {
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                System.out.print("Radar : ");
                EventPrinter.print(timeStamp, inEvents, removeEvents);
            }
        });

        inputHandler = siddhiManager.getInputHandler("reflectStream");
    }

    public void SendDataToCEP(String matrix) {
        try {
            inputHandler.send(new Object[]{matrix});
        }catch (Exception e){

        }
    }
}