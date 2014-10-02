package robert.pointerclue;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Rober_000 on 2014-09-30.
 */
public class LocationChecker {

    private Context context;
    private GoogleApiClient gac;
    private FusedLocationProviderApi fl = LocationServices.FusedLocationApi;
    private LocationRequest locationRequest;
    private Date lastUpdated;
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            lastUpdated = new Date();
        }
    };
    private GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(Bundle bundle) {
            fl.requestLocationUpdates(gac, locationRequest, locationListener);
        }

        @Override
        public void onConnectionSuspended(int i) {
        }
    };
    private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            start();
        }
    };

    public LocationChecker(Context context) {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        gac = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(onConnectionFailedListener)
                .build();
    }

    public void start() {
        gac.connect();
    }

    public void stop() {
        fl.removeLocationUpdates(gac, locationListener);
        gac.disconnect();
    }

    public Location getLocation() {
        return fl.getLastLocation(gac);
    }

    public boolean isStale() {
        Calendar staleCalendar = GregorianCalendar.getInstance();
        staleCalendar.add(Calendar.SECOND, -4);
        return (lastUpdated == null) || (lastUpdated.before(staleCalendar.getTime()));
    }
}
