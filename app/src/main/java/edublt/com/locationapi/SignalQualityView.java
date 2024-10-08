package edublt.com.locationapi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SignalQualityView extends View {
    private Paint paint;
    private List<SatelliteInfo> satelliteInfoList = new ArrayList<>();

    public SignalQualityView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Setting up the paint for drawing the bars
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLUE);

        int width = getWidth();
        int height = getHeight();
        int barWidth = width / (satelliteInfoList.size() == 0 ? 1 : satelliteInfoList.size());
        int barSpacing = barWidth / 4; // Adding some spacing between bars
        barWidth = barWidth - barSpacing;

        for (int i = 0; i < satelliteInfoList.size(); i++) {
            SatelliteInfo satInfo = satelliteInfoList.get(i);
            float snr = satInfo.snr;
            int barHeight = (int) (snr / 100.0 * height);

            // Drawing the bar
            canvas.drawRect(i * (barWidth + barSpacing), height - barHeight, (i * (barWidth + barSpacing)) + barWidth, height, paint);

            // Drawing the SVID inside the bar
            paint.setColor(Color.WHITE);
            paint.setTextSize(30);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(String.valueOf(satInfo.svid), (i * (barWidth + barSpacing)) + (barWidth / 2), height - barHeight / 2, paint);

            paint.setColor(Color.BLUE); // Resetting the color for the next bar
        }
    }

    public void setSatelliteInfoList(List<SatelliteInfo> satelliteInfoList) {
        this.satelliteInfoList = satelliteInfoList;
        invalidate();
    }

    static class SatelliteInfo {
        int svid;
        float snr;

        SatelliteInfo(int svid, float snr) {
            this.svid = svid;
            this.snr = snr;
        }
    }
}
