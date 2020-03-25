package satyam.myappcompany.memorableplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static satyam.myappcompany.memorableplaces.MainActivity.location;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    Marker myMarker;


    public void centreMapOnLocation(Location location, String title) {

        if(location != null){

        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14));

    }}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnowLocation =  locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centreMapOnLocation(lastKnowLocation, "Your Location");

        }
    }}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);


        Intent intent = getIntent();

        if (intent.getIntExtra("putNumber", 0) == 0) {
            // Zoom on user Initial Location

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    centreMapOnLocation(location, "Your Location");

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location lastKnowLocation =  locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centreMapOnLocation(lastKnowLocation, "Your Location");

            }
        }else{

            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(location.get(intent.getIntExtra("putNumber",0)).latitude);
            placeLocation.setLongitude(location.get(intent.getIntExtra("putNumber",0)).longitude);

            centreMapOnLocation(placeLocation, MainActivity.places.get(intent.getIntExtra("putNumber",0)));


        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {


        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        String address = " ";

        try
        {
            List<Address> listAddress = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);

            if(listAddress != null && listAddress.size() >0 ){
                if(listAddress.get(0).getThoroughfare()!=null){
                    if(listAddress.get(0).getSubThoroughfare() != null){
                        address +=  listAddress.get(0).getSubThoroughfare() + " ";
                    }
                    address += listAddress.get(0).getThoroughfare() ;
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }


        if(address.equals(" ") || address.equals("Unnamed Road"))
        {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm yyyy-MM-dd");
            address += simpleDateFormat.format(new Date());



        }

         mMap.addMarker(new MarkerOptions().position(latLng).title(address));

        MainActivity.places.add(address);
        location.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged();

        SharedPreferences sharedPreferences = this.getSharedPreferences("satyam.myappcompany.memorableplaces", Context.MODE_PRIVATE);

        ArrayList<String> latitude = new ArrayList<>();
        ArrayList<String> longitude = new ArrayList<>();

        for(LatLng coord : MainActivity.location){
           latitude.add(Double.toString(coord.latitude));
           longitude.add(Double.toString(coord.longitude));
        }

        try{
            sharedPreferences.edit().putString("places", ObjectSerializer.serialize(MainActivity.places)).apply();
            sharedPreferences.edit().putString("latitude", ObjectSerializer.serialize(latitude)).apply();
            sharedPreferences.edit().putString("longitude", ObjectSerializer.serialize(longitude)).apply();

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

      /*  ArrayList<String> loc =new ArrayList<>();

        try{
            loc =(ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("location", ObjectSerializer.serialize(new ArrayList<String>())));
        }
        catch(Exception e){
            e.printStackTrace();
        } */
        Toast.makeText(this, "Location Saved", Toast.LENGTH_SHORT).show();
    }
}
