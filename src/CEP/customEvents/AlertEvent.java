package CEP.customEvents;

import java.util.ArrayList;
import java.util.EventObject;

public class AlertEvent  extends EventObject{
    private ArrayList<Location> location;
    public AlertEvent(Object source, ArrayList<Location> coordinates){
        super(source);
        location=coordinates;
    }

    public ArrayList<Location> getCoordinates(){
        return location;
    }
}
