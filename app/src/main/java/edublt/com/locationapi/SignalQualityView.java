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
    private Paint paint; // Objeto Paint utilizado para desenhar na tela
    private List<SatelliteInfo> satelliteInfoList = new ArrayList<>(); // Lista para armazenar informações dos satélites

    // Construtor da classe, que inicializa o Paint
    public SignalQualityView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(); // Inicializa o objeto Paint
    }

    // Método responsável por desenhar na tela
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Configuração do Paint para desenhar as barras
        paint.setStyle(Paint.Style.FILL); // Define o estilo do Paint como preenchido
        paint.setColor(Color.BLUE); // Define a cor azul para as barras

        // Obtém a largura e altura da tela
        int width = getWidth();
        int height = getHeight();

        // Calcula a largura de cada barra com base no número de satélites
        int barWidth = width / (satelliteInfoList.size() == 0 ? 1 : satelliteInfoList.size());
        int barSpacing = barWidth / 4; // Adiciona espaçamento entre as barras
        barWidth = barWidth - barSpacing; // Ajusta a largura das barras após aplicar o espaçamento

        // Loop para desenhar as barras para cada satélite
        for (int i = 0; i < satelliteInfoList.size(); i++) {
            SatelliteInfo satInfo = satelliteInfoList.get(i); // Obtém as informações de cada satélite
            float snr = satInfo.snr; // Relação sinal-ruído (Signal-to-Noise Ratio)
            int barHeight = (int) (snr / 100.0 * height); // Calcula a altura da barra com base no SNR

            // Desenha a barra na posição correta
            canvas.drawRect(i * (barWidth + barSpacing), height - barHeight, (i * (barWidth + barSpacing)) + barWidth, height, paint);

            // Desenha o SVID (identificador do satélite) dentro da barra
            paint.setColor(Color.WHITE); // Define a cor branca para o texto
            paint.setTextSize(30); // Define o tamanho do texto
            paint.setTextAlign(Paint.Align.CENTER); // Alinha o texto no centro
            canvas.drawText(String.valueOf(satInfo.svid), (i * (barWidth + barSpacing)) + (barWidth / 2), height - barHeight / 2, paint);

            paint.setColor(Color.BLUE); // Restaura a cor azul para a próxima barra
        }
    }

    // Método para definir a lista de informações dos satélites e redesenhar a tela
    public void setSatelliteInfoList(List<SatelliteInfo> satelliteInfoList) {
        this.satelliteInfoList = satelliteInfoList;
        invalidate(); // Solicita a atualização da tela
    }

    // Classe interna para armazenar as informações de cada satélite (SVID e SNR)
    static class SatelliteInfo {
        int svid; // Identificador do satélite
        float snr; // Relação sinal-ruído (Signal-to-Noise Ratio)

        SatelliteInfo(int svid, float snr) {
            this.svid = svid;
            this.snr = snr;
        }
    }
}
