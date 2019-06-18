package com.example.myapplication;



import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    GoogleMap map;
    GoogleApiClient apiClient;
    FusedLocationProviderApi providerApi;

    Location location;
    LatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        providerApi = LocationServices.FusedLocationApi;

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume","onResume...");
        apiClient.connect();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map=googleMap;
        CameraPosition position = new CameraPosition.Builder().target(setCurrrentPosition()).zoom(16f).build();
        map.moveCamera(CameraUpdateFactory.newCameraPosition(position));

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start));
        markerOptions.position(latLng);

        map.addMarker(markerOptions);

        map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                int zoom=(int)map.getCameraPosition().zoom;
                /*String center=map.getCameraPosition().target.latitude+":"+
                        map.getCameraPosition().target.longitude;*/
                LatLng center = map.getCameraPosition().target;

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start));
                markerOptions.position(center);

            }
        });

        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                map.clear();
                LatLng center = map.getCameraPosition().target;

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start));
                markerOptions.position(center);

                map.addMarker(markerOptions);
            }
        });

        MyGeocodingThread thread = new MyGeocodingThread(latLng);
        thread.start();
    }


    class MyGeocodingThread extends  Thread {
        LatLng latLng;

        public MyGeocodingThread(LatLng latLng){
            this.latLng = latLng;
        }

        @Override
        public void run() {
            Geocoder geocoder = new Geocoder(MainActivity.this);

            List<Address> addresses = null;
            String addressText ="";
            try {
                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude,2);
                Thread.sleep(500);

                if(addresses != null && addresses.size()>0) {
                    Address address = addresses.get(0);
                    addressText = address.getAdminArea() + "" + (address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : address.getLocality()) + " ";

                    String txt = address.getSubLocality();

                    if (txt != null)
                        addressText += txt + "";

                    addressText += address.getThoroughfare() + " " + address.getSubThoroughfare();

                    Message msg = new Message();
                    msg.what = 100;
                    msg.obj = addressText;
                    handler.sendMessage(msg);
                }
            } catch(IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 100: {
                    Toast toast = Toast.makeText(MainActivity.this, (String)msg.obj, Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                }
            }
        };
    };

    public LatLng setCurrrentPosition() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = providerApi.getLastLocation(apiClient);
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
        }
        return latLng;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("OnConnected","OnConnect...");

        ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.lab1_map)).getMapAsync(this);

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = providerApi.getLastLocation(apiClient);
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            providerApi.requestLocationUpdates(apiClient, locationRequest, listener);
        }
    }

    LocationListener listener = new LocationListener(){
        @Override
        public void onLocationChanged(Location location) {

        }
    };

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}
