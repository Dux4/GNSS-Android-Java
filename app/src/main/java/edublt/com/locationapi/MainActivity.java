package edublt.com.locationapi;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.example.locationapi.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        setContentView(R.layout.activity_main); // Define o layout da atividade principal

        // Encontra o botão pelo ID e o associa à variável 'botaoGnss'
        Button botaoGnss = findViewById(R.id.button_gnss);

        // Define um listener para o botão, para responder a cliques
        botaoGnss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cria uma nova intenção para abrir a atividade GNSSActivity
                Intent intencao = new Intent(MainActivity.this, GNSSActivity.class);
                startActivity(intencao); // Inicia a nova atividade
            }
        });
    }
}
