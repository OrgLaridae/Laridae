package CEP.customEvents;

import java.util.EventObject;

public class AlertEvent  extends EventObject{
    private String location;
    public AlertEvent(Object source, String coordinates){
        super(source);
        location=coordinates;
    }

    public String getCoordinates(){
        return location;
    }
}
