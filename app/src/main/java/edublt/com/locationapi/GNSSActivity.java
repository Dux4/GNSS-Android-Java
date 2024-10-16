package edublt.com.locationapi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;

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

        // Verifica permissões de localização
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            locationManager.registerGnssStatusCallback(gnssStatusCallback);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        // Botão único para abrir o diálogo de filtro
        Button buttonFilter = findViewById(R.id.button_filter);
        buttonFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
    }

    // Método para exibir o diálogo de filtro com opções de satélite
    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione o tipo de satélite");

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_filter_options, null);
        builder.setView(customLayout);

        RadioGroup radioGroup = customLayout.findViewById(R.id.radio_group);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                RadioButton selectedButton = customLayout.findViewById(selectedId);
                String filter = "ALL";
                boolean usedOnly = false;

                if (selectedButton != null) {
                    switch (selectedButton.getText().toString()) {
                        case "Todos":
                            filter = "ALL";
                            break;
                        case "GPS":
                            filter = "GPS";
                            break;
                        case "Galileo":
                            filter = "Galileo";
                            break;
                        case "Glonass":
                            filter = "Glonass";
                            break;
                        case "Usados":
                            filter = esferaCelesteView.getCurrentConstellationFilter();
                            usedOnly = true;
                            break;
                    }

                    esferaCelesteView.setFilter(filter, usedOnly);
                    updateSignalQualityView();
                }
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
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
            Toast.makeText(GNSSActivity.this, "Por favor, ative os serviços de localização", Toast.LENGTH_SHORT).show();
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
