package edublt.com.locationapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.locationapi.R;

import java.util.ArrayList;
import java.util.List;

public class GNSSActivity extends AppCompatActivity {
    private EsferaCelesteView esferaCelesteView;
    private SignalQualityView signalQualityView;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gnss);

        esferaCelesteView = findViewById(R.id.esferacelesteview_id);
        signalQualityView = findViewById(R.id.signal_quality_view);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Checking for location permissions
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            locationManager.registerGnssStatusCallback(gnssStatusCallback);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        // Setting up filter buttons
        Button buttonFilterAll = findViewById(R.id.button_filter_all);
        Button buttonFilterGPS = findViewById(R.id.button_filter_gps);
        Button buttonFilterGalileo = findViewById(R.id.button_filter_galileo);
        Button buttonFilterGlonass = findViewById(R.id.button_filter_glonass);
        Button buttonFilterUsed = findViewById(R.id.button_filter_used);

        buttonFilterAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                esferaCelesteView.setFilter("ALL", false);
                updateSignalQualityView();
            }
        });

        buttonFilterGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                esferaCelesteView.setFilter("GPS", false);
                updateSignalQualityView();
            }
        });

        buttonFilterGalileo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                esferaCelesteView.setFilter("Galileo", false);
                updateSignalQualityView();
            }
        });

        buttonFilterGlonass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                esferaCelesteView.setFilter("Glonass", false);
                updateSignalQualityView();
            }
        });

        buttonFilterUsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                esferaCelesteView.setFilter(esferaCelesteView.getCurrentConstellationFilter(), true);
                updateSignalQualityView();
            }
        });
    }

    private void updateSignalQualityView() {
        List<SignalQualityView.SatelliteInfo> satelliteInfoList = new ArrayList<>();
        for (EsferaCelesteView.SatelliteInfo satInfo : esferaCelesteView.getFilteredSatelliteInfoList()) {
            satelliteInfoList.add(new SignalQualityView.SatelliteInfo(satInfo.svid, satInfo.snr));
        }
        signalQualityView.setSatelliteInfoList(satelliteInfoList);
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            esferaCelesteView.setNewLocation(location);
            updateSignalQualityView();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(@NonNull String provider) {}

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            Toast.makeText(GNSSActivity.this, "Please enable location services", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    };

    private final GnssStatus.Callback gnssStatusCallback = new GnssStatus.Callback() {
        @Override
        public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
            esferaCelesteView.setNewStatus(status);
            updateSignalQualityView();
        }
    };
}
