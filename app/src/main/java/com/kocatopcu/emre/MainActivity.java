package com.kocatopcu.emre;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final String[] sozler = {
        "İbrahim Emre Kocatopcu: Dost, güvenilir arkadaş.",
        "Emre: Gönül adamı ve halk ozanı ruhu.",
        "Sevelim, sevilelim, dünya kimseye kalmaz. — Yunus Emre",
        "İlim ilim bilmektir, ilim kendin bilmektir."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView txt = findViewById(R.id.txtBaslik);
        Button btn = findViewById(R.id.btnTikla);

        btn.setOnClickListener(v -> {
            int r = new Random().nextInt(sozler.length);
            txt.setText(sozler[r]);
        });
    }
}
