import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by denny on 6/25/15.
 */
public class Calc {
    public static void main(String[] args) {
        // Home:  (50.390627)  (30.622939
        // northern chimney (50.393486) (30.570415)
        // southern chimney (50.392711) (30.569885)

        // home -> northern chimney = 274.887
        // home -> southern chimney = 273.5


        LocationMock a = new LocationMock();
        a.setLongitude(30.622939);
        a.setLatitude(50.390627);
        LocationMock b = new LocationMock();
        b.setLongitude(30.569885);
        b.setLatitude(50.392711);

        System.out.println(a.bearingTo(b)+360);
    }
}
