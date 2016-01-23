package CEP.customEvents;


/**
 * Created by ruveni on 1/24/16.
 */
public class Location {
    private String latitude;
    private String longitude;

    public Location(){

    }

    public Location(String latitude,String longitude){
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }


    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
