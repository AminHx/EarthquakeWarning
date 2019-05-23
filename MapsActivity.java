package com.aminhx.earthquakeapp;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import co.ronash.pushe.Pushe;



public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int PERMISSION_REQ_CODE = 1234;
    private TextView txt_distance, txt_speed, txt_traveltime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //start service and play song
        startService(new Intent(MapsActivity.this, SoundService.class));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        checkPermissions();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        txt_distance = (TextView) findViewById(R.id.txt_distance);
        txt_speed = (TextView) findViewById(R.id.txt_speed);
        txt_traveltime = (TextView) findViewById(R.id.txt_traveltime);

        Pushe.initialize(this,true);


        }



    @Override
    protected void onPause() {
        stopService(new Intent(MapsActivity.this, SoundService.class));
        super.onPause();
    }

    @Override
    protected void onResume() {
        startService(new Intent(MapsActivity.this, SoundService.class));
        super.onResume();
    }

    private void checkPermissions() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQ_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_REQ_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied to access location", Toast.LENGTH_SHORT).show();
            }
        }
    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Styilng map using a json file.
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));



        GpsTracker gpstracker =  new GpsTracker(this);
        double lat = gpstracker.getLatitude();
        double lon = gpstracker.getLongitude();

        // Add a marker in Kermanshah
        final LatLng location = new LatLng(lat, lon);


        if(gpstracker.canGetLocation()){
            Toast.makeText(this,
                    "Your Location \n lat : " + lat + "\n lon : " + lon,
                    Toast.LENGTH_SHORT).show();
        } else {
            gpstracker.showGpsAlertDialog();
        }


        double lat2 = MyPushListener.lat;
        double lon2 = MyPushListener.lon;
        final LatLng location2 = new LatLng(lat2, lon2);


        mMap.addMarker(new MarkerOptions().position(location).title("You!"))
                .setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        mMap.addMarker(new MarkerOptions().position(location2).title("Earthquake Center"))
                .setIcon(BitmapDescriptorFactory.fromResource(R.drawable.earthquake));



        // Set the camera between 2 markers when app starts
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new MarkerOptions().position(location).getPosition());
        builder.include(new MarkerOptions().position(location2).getPosition());
        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.20); // offset from edges of the map 20% of screen
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        mMap.animateCamera(cu);
        //draw a line between User and the Earthquake locations
        mMap.addPolygon(new PolygonOptions()
                        .add(location, location2)
                        .strokeColor(0x80800000)
                        .fillColor(Color.BLUE)
        );




        double d = CalculationByDistance(location, location2);
        final float speed = 2f;
        float travel_time = (float) (d / speed);

        Math.round(d *100.0);

        txt_distance.setText("Distance: " + Double.parseDouble(String.format("%.1f",d)) + " km");
        txt_speed.setText("Speed: " + speed + " km/s");
        txt_traveltime.setText("Remaining Time: " + String.valueOf(travel_time) + " Seconds");




        final long timer = (long) travel_time * 1000;
        final int[] counter = {39};
        new CountDownTimer(timer ,100) {
            @Override
            public void onTick(long millisUntilFinished) {
                txt_traveltime.setText("Remaining Time: "+ "\n" + millisUntilFinished / 1000);

                float r = (speed * 100) *  counter[0];
                mMap.addCircle(new CircleOptions()
                        .center(location2)
                        .radius(r)
                        .strokeWidth(0)
                        .strokeColor(Color.BLACK)
                        .fillColor(0x05ff0000)
                );
                counter[0]++;
            }

            @Override
            public void onFinish() {
                txt_traveltime.setText("0");
                txt_traveltime.setTextColor(Color.RED);
            }
        }.start();

        class serv extends Service {

            MediaPlayer mp;
            @Override
            public IBinder onBind(Intent intent) {
                // TODO Auto-generated method stub
                return null;
            }
            public void onCreate()
            {
                mp = MediaPlayer.create(this, R.raw.warning);
                mp.setLooping(true);
            }
            public void onDestroy()
            {
                mp.stop();
            }
            public void onStart(Intent intent,int startid){
                mp.start();
            }
        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem itemOk = menu.add("Show Information");
        itemOk.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        itemOk.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String getdistance = txt_distance.getText().toString().trim();
                String getspeed = txt_speed.getText().toString().trim();
                Intent intent = new Intent(MapsActivity.this, InformationActivity.class);
                intent.putExtra("distance", getdistance);
                intent.putExtra("speed", getspeed);
                startActivity(intent);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public LatLng getRandomLocation(LatLng point, int radius) {

        List<LatLng> randomPoints = new ArrayList<>();
        List<Float> randomDistances = new ArrayList<>();
        Location myLocation = new Location("");
        myLocation.setLatitude(point.latitude);
        myLocation.setLongitude(point.longitude);

        //This is to generate 10 random points
        for(int i = 0; i<10; i++) {
            double x0 = point.latitude;
            double y0 = point.longitude;


            // Convert radius from meters to degrees
            double radiusInDegrees = radius / 111000f;
            Random random = new Random();

            double u = random.nextDouble();
            double v = random.nextDouble();
            double w = radiusInDegrees * Math.sqrt(u);
            double t = 2 * Math.PI * v;
            double x = w * Math.cos(t);
            double y = w * Math.sin(t);

            // Adjust the x-coordinate for the shrinking of the east-west distances
            double new_x = x / Math.cos(y0);

            double foundLatitude = new_x + x0;
            double foundLongitude = y + y0;
            LatLng randomLatLng = new LatLng(foundLatitude, foundLongitude);
            randomPoints.add(randomLatLng);
            Location l1 = new Location("");
            l1.setLatitude(randomLatLng.latitude);
            l1.setLongitude(randomLatLng.longitude);
            randomDistances.add(l1.distanceTo(myLocation));
        }
        //Get nearest point to the centre
        int indexOfNearestPointToCentre = randomDistances.indexOf(Collections.min(randomDistances));
        return randomPoints.get(indexOfNearestPointToCentre);
    }

    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

}

//Coordinate of kermanshah: 34.325798, 47.068387