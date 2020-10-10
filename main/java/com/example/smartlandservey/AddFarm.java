package com.example.smartlandservey;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.SphericalUtil;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class AddFarm extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    Button save, clear;
    List<LatLng> latLngs;
    List<Double> latitude1;
    List<Double> longitude2;
    LatLng MyLocation;
    LocationRequest mLocationRequest;
    double latitude, longitude;
    GoogleApiClient mGoogleApiClient;
    double area;
    PolygonOptions rectOptions;
    GoogleMap map;
    int state=0;
    TextView landArea;
    private int mYear, mMonth, mDay;
    FirebaseDatabase database;
    DatabaseReference myRef;
    AlertDialog a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_farm);
        database = FirebaseDatabase.getInstance();

        landArea = findViewById(R.id.area);
        clear = findViewById(R.id.clear);
        save = findViewById(R.id.save);
        latitude1=new ArrayList<>();
        longitude2=new ArrayList<>();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(AddFarm.this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        createLocationRequest();
        latLngs = new ArrayList<>();
        rectOptions = new PolygonOptions();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFarm);
        mapFragment.getMapAsync(this);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map.clear();
                latLngs.clear();
                rectOptions.getPoints().clear();
                landArea.setText("");
                landArea.setVisibility(View.INVISIBLE);

            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder b = new AlertDialog.Builder(AddFarm.this);
                LayoutInflater l = getLayoutInflater();
                View v = l.inflate(R.layout.farm_details, null);
                final EditText farmName = v.findViewById(R.id.farmName);
                final EditText mobile = v.findViewById(R.id.MobileNumber);
                final EditText survey = v.findViewById(R.id.surveyno);
                final EditText Area = v.findViewById(R.id.area);
                final EditText sd = v.findViewById(R.id.date);
                sd.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View view) {
                        final Calendar c = Calendar.getInstance();
                        mYear = c.get(Calendar.YEAR);
                        mMonth = c.get(Calendar.MONTH);
                        mDay = c.get(Calendar.DAY_OF_MONTH);


                        final DatePickerDialog datePickerDialog = new DatePickerDialog(AddFarm.this,
                                new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker view, int year,
                                                          int monthOfYear, int dayOfMonth) {
                                        sd.setText(dayOfMonth+"-"+(monthOfYear+1)+"-"+year);



                                    }
                                }, mYear, mMonth, mDay);
                        datePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                datePickerDialog.dismiss();
                            }
                        });
                        datePickerDialog.show();
                    }
                });

                Button save=v.findViewById(R.id.saveData);

                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        myRef = database.getReference(farmName.getText().toString());
                        myRef.child("FarmName").setValue(farmName.getText().toString());
                        myRef.child("Mobile").setValue(mobile.getText().toString());
                        myRef.child("Survey-NO").setValue(survey.getText().toString());
                        myRef.child("Area").setValue(area);
                        myRef.child("Date").setValue(sd.getText().toString());
                        for (int i = 0; i < latLngs.size(); i++){

                            myRef.child("DataLatLng"+i).setValue(latitude1.get(i)+","+longitude2.get(i));
                        }


                        a.dismiss();
                    }
                });

                b.setView(v);
                a = b.create();
                a.show();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(AddFarm.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AddFarm.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this
        );
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        MyLocation = new LatLng(latitude, longitude);
        if (latitude != 0 && longitude != 0 && state==0 ) {
            //Log.i("Location", "" + latitude + "," + longitude);
            CameraPosition camPos = new CameraPosition.Builder().target(MyLocation)
                    .zoom(15)
                    .build();

            CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            map.setMyLocationEnabled(true);
            map.animateCamera(camUpd3);
            state=1;

        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map=googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                latLngs.add(latLng);
                latitude1.add(latLng.latitude);
                longitude2.add(latLng.longitude);
                landArea.setVisibility(View.VISIBLE);
                rectOptions.add(latLng);
                Log.i("Data", "computeArea " + SphericalUtil.computeArea(latLngs));//Area in sqaure meters
                Polygon polygon = map.addPolygon(rectOptions);
                polygon.setStrokeColor(Color.GREEN);
                polygon.setFillColor(getResources().getColor(R.color.light));
                Log.i("data","polyline");
                map.addMarker(new MarkerOptions()
                        .draggable(true)
                        .alpha(0.7f)
                        .position(latLng));
                area=SphericalUtil.computeArea(latLngs)*0.00024711;
                landArea.setText("Land Area: "+area+ " Acre");
            }
        });

    }
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000*10);
        mLocationRequest.setFastestInterval(1000*5);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

}
