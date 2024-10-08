package edublt.com.locationapi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.GnssStatus;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EsferaCelesteView extends View {
    private GnssStatus newStatus;
    private Paint paint;
    private int r;
    private int height, width;
    private double latitude;
    private double longitude;
    private double altitude;
    private String filterConstellation = "ALL";
    private boolean filterUsedInFix = false;

    private List<SatelliteInfo> satelliteInfoList = new ArrayList<>();

    public EsferaCelesteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // coletando informações do tamanho tela de desenho
        width = getMeasuredWidth();
        height = getMeasuredHeight();

        // definindo o raio da esfera celeste
        if (width < height)
            r = (int) (width / 2 * 0.9);
        else
            r = (int) (height / 2 * 0.9);
        // configurando o pincel para desenhar a projeção da esfera celeste
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.BLUE);
        // desenha a projeção da esfera celeste
        // desenhando círculos concêntricos
        int radius = r;
        canvas.drawCircle(computeXc(0), computeYc(0), radius, paint);
        radius = (int) (radius * Math.cos(Math.toRadians(45)));
        canvas.drawCircle(computeXc(0), computeYc(0), radius, paint);
        radius = (int) (radius * Math.cos(Math.toRadians(60)));
        canvas.drawCircle(computeXc(0), computeYc(0), radius, paint);

        // desenhando os eixos
        canvas.drawLine(computeXc(0), computeYc(-r), computeXc(0), computeYc(r), paint);
        canvas.drawLine(computeXc(-r), computeYc(0), computeXc(r), computeYc(0), paint);

        // configurando o pincel para desenhar os satélites
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);

        // desenhando os satélites (caso exista um GnssStatus disponível)
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

        // configurando o pincel para desenhar a posição do usuário
        paint.setColor(Color.GREEN);
        paint.setTextAlign(Paint.Align.CENTER);

        String userPosition = "Lat: " + latitude + ", Lon: " + longitude + ", Alt: " + altitude + "m";
        adjustTextSize(paint, width, userPosition);
        canvas.drawText(userPosition, computeXc(0), computeYc(r + 50), paint);
    }

    private void adjustTextSize(Paint paint, int width, String text) {
        // Adjust the text size based on the width
        paint.setTextSize(40); // Starting text size
        float textWidth = paint.measureText(text);
        while (textWidth > width && paint.getTextSize() > 0) {
            paint.setTextSize(paint.getTextSize() - 1);
            textWidth = paint.measureText(text);
        }
    }

    private int computeXc(double x) {
        return (int) (x + width / 2);
    }

    private int computeYc(double y) {
        return (int) (-y + height / 2);
    }

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
            satelliteInfoList.add(new SatelliteInfo(newStatus.getSvid(i), constellation, usedInFix, x, y));
        }
        invalidate();
    }

    public void setNewLocation(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.altitude = location.getAltitude();
        invalidate();
    }

    public void setFilter(String constellation, boolean usedInFix) {
        this.filterConstellation = constellation;
        this.filterUsedInFix = usedInFix;
        invalidate();
    }

    public String getCurrentConstellationFilter() {
        return filterConstellation;
    }

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

    static class SatelliteInfo {
        int svid;
        String constellation;
        boolean usedInFix;
        float x;
        float y;

        SatelliteInfo(int svid, String constellation, boolean usedInFix, float x, float y) {
            this.svid = svid;
            this.constellation = constellation;
            this.usedInFix = usedInFix;
            this.x = x;
            this.y = y;
        }
    }
}
