package edublt.com.locationapi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.GnssStatus;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EsferaCelesteView extends View {
    private GnssStatus newStatus;
    private Paint paint;
    private int r; // Raio da esfera
    private int height, width; // Altura e largura da tela
    private double latitude;
    private double longitude;
    private double altitude;
    private String filterConstellation = "ALL"; // Filtro para constelações de satélite
    private boolean filterUsedInFix = false; // Filtro para satélites usados na localização

    private List<SatelliteInfo> satelliteInfoList = new ArrayList<>(); // Lista de satélites

    // Variável para armazenar o texto formatado
    private String formattedText;

    private static final String PREFS_KEY_FORMAT = "location_format";

    public EsferaCelesteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(); // Inicializando a ferramenta de pintura
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Coletando informações do tamanho da tela de desenho
        width = getMeasuredWidth();
        height = getMeasuredHeight();

        // Definindo o raio da esfera celeste
        if (width < height)
            r = (int) (width / 2 * 0.9);
        else
            r = (int) (height / 2 * 0.9);

        // Configurando o pincel para desenhar a projeção da esfera celeste
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.BLUE);

        // Desenhando a projeção da esfera celeste com círculos concêntricos
        int radius = r;
        canvas.drawCircle(computeXc(0), computeYc(0), radius, paint);
        radius = (int) (radius * Math.cos(Math.toRadians(45)));
        canvas.drawCircle(computeXc(0), computeYc(0), radius, paint);
        radius = (int) (radius * Math.cos(Math.toRadians(60)));
        canvas.drawCircle(computeXc(0), computeYc(0), radius, paint);

        // Desenhando os eixos
        canvas.drawLine(computeXc(0), computeYc(-r), computeXc(0), computeYc(r), paint);
        canvas.drawLine(computeXc(-r), computeYc(0), computeXc(r), computeYc(0), paint);

        // Configurando o pincel para desenhar os satélites
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);

        // Desenhando os satélites se o status do GNSS estiver disponível
        if (newStatus != null) {
            for (SatelliteInfo satInfo : satelliteInfoList) {
                if (filterConstellation.equals("ALL") || satInfo.constellation.equals(filterConstellation)) {
                    if (!filterUsedInFix || satInfo.usedInFix) {
                        canvas.drawCircle(computeXc(satInfo.x), computeYc(satInfo.y), 10, paint);
                        paint.setTextAlign(Paint.Align.LEFT);
                        paint.setTextSize(30);
                        String satDetails = satInfo.svid + " (" + satInfo.constellation + ") " + (satInfo.usedInFix ? "Used" : "Not Used");
                        canvas.drawText(satDetails, computeXc(satInfo.x) + 10, computeYc(satInfo.y) + 10, paint);
                    }
                }
            }
        }

        // Configurando o pincel para desenhar a posição do usuário
        paint.setColor(Color.GREEN);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(30);

        // Use o texto formatado armazenado
        if (formattedText != null) {
            adjustTextSize(paint, width, formattedText); // Ajustando o tamanho do texto
            canvas.drawText(formattedText, computeXc(0), computeYc(-r - 50), paint); // Desenhando a posição do usuário
        }

        // Adiciona um Listener para o texto de localização
        setOnClickListener(v -> showFormatSelectionDialog());
    }

    // Ajusta o tamanho do texto de acordo com a largura disponível
    private void adjustTextSize(Paint paint, int width, String text) {
        paint.setTextSize(40); // Tamanho inicial do texto
        float textWidth = paint.measureText(text);
        while (textWidth > width && paint.getTextSize() > 0) {
            paint.setTextSize(paint.getTextSize() - 1);
            textWidth = paint.measureText(text);
        }
    }

    // Calcula a posição X do centro da tela
    private int computeXc(double x) {
        return (int) (x + width / 2);
    }

    // Calcula a posição Y do centro da tela
    private int computeYc(double y) {
        return (int) (-y + height / 2);
    }

    // Define o novo status GNSS e atualiza a lista de satélites
    public void setNewStatus(GnssStatus newStatus) {
        this.newStatus = newStatus;
        satelliteInfoList.clear();
        for (int i = 0; i < newStatus.getSatelliteCount(); i++) {
            float az = newStatus.getAzimuthDegrees(i);
            float el = newStatus.getElevationDegrees(i);
            float x = (float) (r * Math.cos(Math.toRadians(el)) * Math.sin(Math.toRadians(az)));
            float y = (float) (r * Math.cos(Math.toRadians(el)) * Math.cos(Math.toRadians(az)));
            String constellation = getConstellation(newStatus.getConstellationType(i));
            boolean usedInFix = newStatus.usedInFix(i);
            float snr = newStatus.getCn0DbHz(i);
            satelliteInfoList.add(new SatelliteInfo(newStatus.getSvid(i), constellation, usedInFix, x, y, snr));
        }
        invalidate(); // Solicita que a tela seja redesenhada
    }

    // Define a nova localização do usuário e atualiza a tela
    public void setNewLocation(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.altitude = location.getAltitude();

        // Carrega o formato atual das SharedPreferences antes de atualizar o texto
        updateLocationTextFormat(LocationFormat.valueOf(getSavedLocationFormat())); // Define o formato com base no salvo
        invalidate(); // Solicita que a tela seja redesenhada
    }

    // Método para obter o formato salvo das SharedPreferences
    private String getSavedLocationFormat() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return prefs.getString(PREFS_KEY_FORMAT, LocationFormat.DEGREES.name());
    }

    // Define os filtros para exibição de satélites e atualiza a tela
    public void setFilter(String constellation, boolean usedInFix) {
        this.filterConstellation = constellation;
        this.filterUsedInFix = usedInFix;
        invalidate(); // Solicita que a tela seja redesenhada
    }

    // Retorna a lista de satélites filtrada de acordo com os critérios de exibição
    public List<SatelliteInfo> getFilteredSatelliteInfoList() {
        List<SatelliteInfo> filteredList = new ArrayList<>();
        for (SatelliteInfo satInfo : satelliteInfoList) {
            if ((filterConstellation.equals("ALL") || satInfo.constellation.equals(filterConstellation)) &&
                    (!filterUsedInFix || satInfo.usedInFix)) {
                filteredList.add(satInfo);
            }
        }
        return filteredList;
    }

    // Retorna o filtro de constelação atual
    public String getCurrentConstellationFilter() {
        return filterConstellation;
    }

    // Retorna o nome da constelação com base no tipo de constelação GNSS
    private String getConstellation(int constellationType) {
        switch (constellationType) {
            case GnssStatus.CONSTELLATION_GPS:
                return "GPS";
            case GnssStatus.CONSTELLATION_GLONASS:
                return "Glonass";
            case GnssStatus.CONSTELLATION_GALILEO:
                return "Galileo";
            default:
                return "Other";
        }
    }

    // Método para exibir o diálogo de seleção de formato
    private void showFormatSelectionDialog() {
        String[] formats = {
                "Graus [+/-DDD.DDDDD]",
                "Graus-Minutos [+/-DDD:MM.MMMMM]",
                "Graus-Minutos-Segundos [+/-DDD:MM:SS.SSSSS]"
        };

        new AlertDialog.Builder(getContext())
                .setTitle("Selecione o formato de exibição")
                .setItems(formats, (dialog, which) -> {
                    LocationFormat selectedFormat;
                    switch (which) {
                        case 0:
                            selectedFormat = LocationFormat.DEGREES;
                            break;
                        case 1:
                            selectedFormat = LocationFormat.DEGREES_MINUTES;
                            break;
                        case 2:
                            selectedFormat = LocationFormat.DEGREES_MINUTES_SECONDS;
                            break;
                        default:
                            return;
                    }
                    // Salvar o formato selecionado nas SharedPreferences
                    saveLocationFormat(selectedFormat);
                    updateLocationTextFormat(selectedFormat); // Atualizar a exibição com o formato selecionado
                    invalidate(); // Solicita que a tela seja redesenhada
                })
                .show();
    }

    // Salva o formato selecionado nas SharedPreferences
    private void saveLocationFormat(LocationFormat format) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFS_KEY_FORMAT, format.name());
        editor.apply();
    }

    // Atualiza o texto formatado de acordo com o formato selecionado
    private void updateLocationTextFormat(LocationFormat format) {
        switch (format) {
            case DEGREES:
                formattedText = String.format("Lat: %.5f, Long: %.5f, Alt: %.2f", latitude, longitude, altitude);
                break;
            case DEGREES_MINUTES:
                formattedText = String.format("Lat: %s, Long: %s, Alt: %.2f",
                        convertToDegreesMinutes(latitude), convertToDegreesMinutes(longitude), altitude);
                break;
            case DEGREES_MINUTES_SECONDS:
                formattedText = String.format("Lat: %s, Long: %s, Alt: %.2f",
                        convertToDegreesMinutesSeconds(latitude), convertToDegreesMinutesSeconds(longitude), altitude);
                break;
        }
    }

    // Método para converter graus para graus-minutos
    private String convertToDegreesMinutes(double value) {
        int degrees = (int) value;
        double minutes = Math.abs((value - degrees) * 60);
        return String.format("%d°%.5f'", degrees, minutes);
    }

    // Método para converter graus para graus-minutos-segundos
    private String convertToDegreesMinutesSeconds(double value) {
        int degrees = (int) value;
        double minutes = Math.abs((value - degrees) * 60);
        int intMinutes = (int) minutes;
        double seconds = (minutes - intMinutes) * 60;
        return String.format("%d°%d'%.2f\"", degrees, intMinutes, seconds);
    }

    // Classe interna para informações do satélite
    public static class SatelliteInfo {
        int svid; // ID do satélite
        String constellation; // Constelação do satélite
        boolean usedInFix; // Indica se o satélite é usado na localização
        float x, y; // Posições do satélite
        float snr; // Qualidade do sinal

        SatelliteInfo(int svid, String constellation, boolean usedInFix, float x, float y, float snr) {
            this.svid = svid;
            this.constellation = constellation;
            this.usedInFix = usedInFix;
            this.x = x;
            this.y = y;
            this.snr = snr;
        }
    }

    // Enum para os formatos de localização
    private enum LocationFormat {
        DEGREES,
        DEGREES_MINUTES,
        DEGREES_MINUTES_SECONDS
    }
}
