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
    private EsferaCelesteView esferaCelesteView; // Componente de visualização da constelação de satélites
    private SignalQualityView signalQualityView; // Componente de visualização da qualidade do sinal
    private LocationManager locationManager; // Gerenciador de localização para acessar o GPS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gnss);

        // Inicializando as visualizações
        esferaCelesteView = findViewById(R.id.esferacelesteview_id);
        signalQualityView = findViewById(R.id.signal_quality_view);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); // Obtém o serviço de localização

        // Verificação de permissões de localização
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Caso as permissões não sejam concedidas, solicita ao usuário
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        try {
            // Solicita atualizações de localização a cada 1 segundo e 1 metro de distância
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            // Registra um callback para atualizações de status dos satélites GNSS
            locationManager.registerGnssStatusCallback(gnssStatusCallback);
        } catch (SecurityException e) {
            e.printStackTrace(); // Captura qualquer exceção de segurança
        }

        // Configuração dos botões de filtro
        Button buttonFilterAll = findViewById(R.id.button_filter_all); // Botão para filtrar todos os satélites
        Button buttonFilterGPS = findViewById(R.id.button_filter_gps); // Botão para filtrar satélites GPS
        Button buttonFilterGalileo = findViewById(R.id.button_filter_galileo); // Botão para filtrar satélites Galileo
        Button buttonFilterGlonass = findViewById(R.id.button_filter_glonass); // Botão para filtrar satélites Glonass
        Button buttonFilterUsed = findViewById(R.id.button_filter_used); // Botão para filtrar satélites utilizados

        // Definindo a ação para cada botão
        buttonFilterAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                esferaCelesteView.setFilter("ALL", false); // Filtra todos os satélites
                updateSignalQualityView(); // Atualiza a visualização da qualidade do sinal
            }
        });

        buttonFilterGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                esferaCelesteView.setFilter("GPS", false); // Filtra apenas os satélites GPS
                updateSignalQualityView(); // Atualiza a visualização da qualidade do sinal
            }
        });

        buttonFilterGalileo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                esferaCelesteView.setFilter("Galileo", false); // Filtra satélites Galileo
                updateSignalQualityView(); // Atualiza a visualização da qualidade do sinal
            }
        });

        buttonFilterGlonass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                esferaCelesteView.setFilter("Glonass", false); // Filtra satélites Glonass
                updateSignalQualityView(); // Atualiza a visualização da qualidade do sinal
            }
        });

        buttonFilterUsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                esferaCelesteView.setFilter(esferaCelesteView.getCurrentConstellationFilter(), true); // Filtra satélites usados
                updateSignalQualityView(); // Atualiza a visualização da qualidade do sinal
            }
        });
    }

    // Método para atualizar a visualização da qualidade do sinal
    private void updateSignalQualityView() {
        List<SignalQualityView.SatelliteInfo> satelliteInfoList = new ArrayList<>();
        // Converte as informações dos satélites filtrados para a visualização de qualidade de sinal
        for (EsferaCelesteView.SatelliteInfo satInfo : esferaCelesteView.getFilteredSatelliteInfoList()) {
            satelliteInfoList.add(new SignalQualityView.SatelliteInfo(satInfo.svid, satInfo.snr)); // Adiciona satélite na lista
        }
        signalQualityView.setSatelliteInfoList(satelliteInfoList); // Define a lista na visualização de qualidade de sinal
    }

    // Listener para atualizações de localização
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            // Atualiza a localização na visualização da esfera celeste
            esferaCelesteView.setNewLocation(location);
            updateSignalQualityView(); // Atualiza a visualização da qualidade do sinal
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(@NonNull String provider) {}

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            // Exibe um aviso solicitando que o usuário ative os serviços de localização
            Toast.makeText(GNSSActivity.this, "Por favor, ative os serviços de localização", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS); // Abre as configurações de localização
            startActivity(intent);
        }
    };

    // Callback para atualizações de status dos satélites GNSS
    private final GnssStatus.Callback gnssStatusCallback = new GnssStatus.Callback() {
        @Override
        public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
            // Atualiza o status dos satélites na visualização da esfera celeste
            esferaCelesteView.setNewStatus(status);
            updateSignalQualityView(); // Atualiza a visualização da qualidade do sinal
        }
    };
}
