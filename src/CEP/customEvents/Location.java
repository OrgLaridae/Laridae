package CEP.customEvents;


/**
 * Created by ruveni on 1/24/16.
 */
public class Location {
    private String latitude;
    private String longitude;
    private String lambertX;
    private String lambertY;

    public Location(){

    }

    public Location(String latitude,String longitude){
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }

    public Location(String latitude,String longitude, String lambertX, String lambertY){
        this.setLatitude(latitude);
        this.setLongitude(longitude);
        this.setLambertX(lambertX);
        this.setLambertY(lambertY);
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

    public String getLambertX() {
        return lambertX;
    }

    public void setLambertX(String lambertX) {
        this.lambertX = lambertX;
    }

    public String getLambertY() {
        return lambertY;
    }

    public void setLambertY(String lambertY) {
        this.lambertY = lambertY;
    }
}
