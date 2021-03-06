package com.example.dodododo;

import static android.widget.Toast.LENGTH_LONG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private static final int REQUEST_CODE_LOCATION = 2;
    double latitude;
    double longitude;

    private Socket mSocket;

    TestData data;
    Gson gson = new Gson();
    String  jsonData;
    Button button;
    Button button1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button=findViewById(R.id.button);
        button1=findViewById(R.id.button1);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this::onMapReady);
        data = new TestData();
        data.roomName="test";
        data.userName="test";

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jsonData = gson.toJson(data);
                mSocket.emit("test",jsonData);
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CoordinateData coordinateData=new CoordinateData();
                coordinateData.roomName="test";
                coordinateData.userName="test";
                coordinateData.latitude=latitude;
                coordinateData.longitude=longitude;

                jsonData = gson.toJson(coordinateData);
                mSocket.emit("UpdataCoordinate",jsonData);




            }
        });


        jsonData = gson.toJson(data);
        try {
            mSocket = IO.socket("https://testserver.iou040428.repl.co");
            mSocket.connect();

            mSocket.on(Socket.EVENT_CONNECT, onConnect);
            mSocket.on("test",test);
            mSocket.on("subscribe",subscribe);
            mSocket.on("UpdataCoordinate",UpdataCoordinate);
            mSocket.on("makeBox",makeBox);

            // To know if the new user entered the room.
            //mSocket.on("updateChat", onUpdateChat); // To update if someone send a message to chatroom
            //mSocket.on("userLeftChatRoom", onUserLeft); // To know if the user left the chatroom.            Log.d("asdf", "asdf");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.d("??????", "??????");
        }

    }



    Emitter.Listener onConnect=new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            // Gson changes data object to Json type.
            mSocket.emit("subscribe", jsonData);





        }
    };
    private  Emitter.Listener test=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("adsf","asdf");
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run()
                {
                    Toast.makeText(getApplicationContext(),args[0].toString(), Toast.LENGTH_SHORT).show();
                }
            }, 0);


        }
    };
    private  Emitter.Listener subscribe=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("?????????",args[0].toString());
        }
    };
    private  Emitter.Listener UpdataCoordinate=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run()
                {
                    Toast.makeText(getApplicationContext(),args[0].toString(), LENGTH_LONG).show();
                }
            }, 0);

        }
    };
    private  Emitter.Listener makeBox=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run()
                {
                    Toast.makeText(getApplicationContext(),args[0].toString(), Toast.LENGTH_SHORT).show();
                    CoordinateData coordinateData= gson.fromJson(args[0].toString(),CoordinateData.class);
                    Log.d("a",coordinateData.latitude+"\n"+coordinateData.longitude);
                    LatLng S = new LatLng(coordinateData.latitude+0.0009, coordinateData.longitude);

                    MarkerOptions markerOptions = new MarkerOptions();         // ?????? ??????
                    markerOptions.position(S);
                    markerOptions.title("??????");                         // ?????? ??????
                    markerOptions.snippet("?????????");         // ?????? ??????
                    mMap.addMarker(markerOptions);

                }
            }, 0);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });

        }
    };


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        Location userLocation = getMyLocation();
        if( userLocation != null ) {
             latitude = userLocation.getLatitude();
             longitude = userLocation.getLongitude();

            System.out.println("////////////?????? ??? ????????? : "+latitude+","+longitude);
        }
        LatLng SEOUL = new LatLng(latitude, longitude);

        MarkerOptions markerOptions = new MarkerOptions();         // ?????? ??????
        markerOptions.position(SEOUL);
        markerOptions.title("??????");                         // ?????? ??????
        markerOptions.snippet("????????? ??????");         // ?????? ??????
        Marker m= mMap.addMarker(markerOptions);
        m.remove();


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 20));                 // ?????? ??????
                               // ?????? ??????
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);                           // ?????? ?????? ??????

    }
    private Location getMyLocation() {
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("////////////??????????????? ????????? ???????????????");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, this.REQUEST_CODE_LOCATION);
            getMyLocation(); //?????? ???????????? ????????? ?????????, ??? ?????? ???????????? ?????? ????????? ??????????????? ????????????!
        }
        else {
            System.out.println("////////////???????????? ????????????");

            // ???????????? ?????? ?????????
            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = locationManager.getLastKnownLocation(locationProvider);
            if (currentLocation != null) {
                double lng = currentLocation.getLongitude();
                double lat = currentLocation.getLatitude();
            }
        }
        return currentLocation;
    }




}