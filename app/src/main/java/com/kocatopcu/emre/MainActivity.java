package com.kocatopcu.emre;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * İbrahim Emre Kocatopcu - Mobil Uygulama
 * Ana Activity: veri havuzu, animasyon, titreşim, tema ve oturum kaydı.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "EmreAppCore";
    private static final String PREF_NAME = "EmrePrefs";
    private static final String KEY_CLICK_COUNT = "total_clicks";
    private static final String KEY_LAST_ACCESS = "last_access_time";
    private static final String KEY_THEME_INDEX = "theme_index";

    // UI bileşenleri
    private TextView txtBaslik;
    private TextView txtDetayBilgi;
    private TextView txtSayacGosterge;
    private TextView txtLogEkrani;
    private Button btnVeriGetir;
    private Button btnTemaDegistir;
    private Button btnSifirla;
    private LinearLayout rootLayout;

    // Veri ve sayaçlar
    private int tiklamaSayaci = 0;
    private int temaIndex = 0;
    private SharedPreferences sharedPreferences;
    private Vibrator vibrator;
    private Random randomGenerator;

    private final ArrayList<EmreModel> veriHavuzu = new ArrayList<>();

    private final String[] temaArkaPlanlari = {
            "#0D1117", // GitHub Dark
            "#111827", // Midnight
            "#1a0b2e", // Deep Purple
            "#0f2027", // Dark Neon Blue
            "#2c1810"  // Warm Dark Roast
    };

    private final String[] neonRenkler = {
            "#58A6FF", "#3FB950", "#D29922", "#F85149", "#BC8CFF", "#7EE787", "#00F2FE"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializationSubsystem();
        bindUserInterfaceComponents();
        populateMasterDataPool();
        setupEventListeners();
        applyTheme();
        updateCounterText();
        loadPersistentStateAndLog();

        Log.d(TAG, "MainActivity başarıyla başlatıldı.");
    }

    /** Sistem servislerini ve kayıtlı verileri hazırlar. */
    private void initializationSubsystem() {
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        randomGenerator = new Random();
        tiklamaSayaci = sharedPreferences.getInt(KEY_CLICK_COUNT, 0);
        temaIndex = sharedPreferences.getInt(KEY_THEME_INDEX, 0);
    }

    /** XML'deki elemanları Java nesnelerine bağlar. */
    private void bindUserInterfaceComponents() {
        rootLayout = findViewById(R.id.rootLayout);
        txtBaslik = findViewById(R.id.txtBaslik);
        txtDetayBilgi = findViewById(R.id.txtDetayBilgi);
        txtSayacGosterge = findViewById(R.id.txtSayacGosterge);
        txtLogEkrani = findViewById(R.id.txtLogEkrani);
        btnVeriGetir = findViewById(R.id.btnTikla);
        btnTemaDegistir = findViewById(R.id.btnTemaDegistir);
        btnSifirla = findViewById(R.id.btnSifirla);
    }

    /** İçerik havuzunu doldurur. */
    private void populateMasterDataPool() {
        veriHavuzu.add(new EmreModel("Kimlik Analizi", "İbrahim Emre Kocatopcu: Ekibin lideri, kararlı geliştirici ve güvenilir dost.", "Profil"));
        veriHavuzu.add(new EmreModel("İsim Kökeni", "Emre: Eski Türkçe'de aşık, dost, halk ozanı ve gönül verilmiş kimse demektir.", "Kültür"));
        veriHavuzu.add(new EmreModel("Tarihi Miras", "Yunus Emre (1238-1320): Türkçe'nin tasavvuf ve sevgi dilini zirveye taşıyan büyük Türk ozanı.", "Sanat"));
        veriHavuzu.add(new EmreModel("Müzik Dünyası", "Emre Aydın: Türk rock müziğine damga vurmuş, ödüllü söz yazarı ve besteci.", "Müzik"));
        veriHavuzu.add(new EmreModel("Sahne Sanatları", "Emre Altuğ ve Emre Kaya: Pop müziğin enerjik sesleri ve hit parça üreticileri.", "Müzik"));
        veriHavuzu.add(new EmreModel("Yazılım Felsefesi", "Kodlama sadece satır yazmak değil, mantık kurmak ve dijital eser yaratmaktır.", "Teknoloji"));
        veriHavuzu.add(new EmreModel("Anka Ruhu", "Asla pes etmeyen, küllerinden yeniden doğan ve projelerini sonuna kadar savunan ekip ruhu.", "Ekip"));
        veriHavuzu.add(new EmreModel("Liderlik", "İbrahim Emre: Kriz anlarında sakin kalabilen, ekibi yönlendiren stratejik akıl.", "Karakter"));
        veriHavuzu.add(new EmreModel("Felsefi Söz", "\"Sevelim, sevilelim, dünya kimseye kalmaz.\" — Yunus Emre", "Felsefe"));
        veriHavuzu.add(new EmreModel("Bilgi Notu", "\"İlim ilim bilmektir, ilim kendin bilmektir.\" — Yunus Emre", "Bilim"));
    }

    /** Buton dinleyicilerini atar. */
    private void setupEventListeners() {
        if (btnVeriGetir != null) {
            btnVeriGetir.setOnClickListener(v -> {
                tiklamaSayaci++;
                triggerHapticFeedback();
                playScaleAnimation(v);
                processAndDisplayRandomData();
                updateCounterText();
                savePersistentState();
            });
        }

        if (btnTemaDegistir != null) {
            btnTemaDegistir.setOnClickListener(v -> {
                temaIndex = (temaIndex + 1) % temaArkaPlanlari.length;
                triggerHapticFeedback();
                playScaleAnimation(v);
                applyTheme();
                savePersistentState();
                appendExecutionLog("Tema değişti: " + temaArkaPlanlari[temaIndex]);
            });
        }

        if (btnSifirla != null) {
            btnSifirla.setOnClickListener(v -> {
                tiklamaSayaci = 0;
                triggerHapticFeedback();
                playScaleAnimation(v);
                updateCounterText();
                savePersistentState();
                if (txtBaslik != null) {
                    txtBaslik.setText("İbrahim Emre Kocatopcu");
                }
                appendExecutionLog("Sayaç sıfırlandı.");
            });
        }
    }

    /** Rastgele veri seçer ve ekranda gösterir. */
    private void processAndDisplayRandomData() {
        if (veriHavuzu.isEmpty()) return;

        EmreModel secilenVeri = veriHavuzu.get(randomGenerator.nextInt(veriHavuzu.size()));

        String formatliMetin = String.format(Locale.getDefault(),
                "📌 [%s] %s\n\n💡 Detay: %s\n\n🔄 Toplam Etkileşim: %d",
                secilenVeri.kategori,
                secilenVeri.baslik,
                secilenVeri.aciklama,
                tiklamaSayaci
        );

        if (txtBaslik != null) {
            txtBaslik.setText(formatliMetin);
            int renkIndex = randomGenerator.nextInt(neonRenkler.length);
            txtBaslik.setTextColor(Color.parseColor(neonRenkler[renkIndex]));
        }

        appendExecutionLog("Veri yüklendi: " + secilenVeri.baslik);
    }

    /** Seçili arka plan temasını uygular. */
    private void applyTheme() {
        if (rootLayout != null) {
            rootLayout.setBackgroundColor(Color.parseColor(temaArkaPlanlari[temaIndex]));
        }
    }

    /** Sayaç göstergesini günceller (layout'ta görünürse). */
    private void updateCounterText() {
        if (txtSayacGosterge != null) {
            txtSayacGosterge.setText("Toplam tıklama: " + tiklamaSayaci);
        }
    }

    /** Kısa titreşim üretir. */
    private void triggerHapticFeedback() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(40);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Titreşim hatası: " + e.getMessage());
        }
    }

    /** Buton için küçülüp büyüyen animasyon. */
    private void playScaleAnimation(View view) {
        ScaleAnimation scaleAnim = new ScaleAnimation(
                1.0f, 0.92f, 1.0f, 0.92f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnim.setDuration(100);
        scaleAnim.setRepeatCount(1);
        scaleAnim.setRepeatMode(Animation.REVERSE);
        view.startAnimation(scaleAnim);
    }

    /** Zaman damgalı log yazar (konsol + ekran). */
    private void appendExecutionLog(String mesaj) {
        String zaman = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String logSatiri = "[" + zaman + "] " + mesaj;
        Log.i(TAG, logSatiri);

        if (txtLogEkrani != null) {
            String mevcutLoglar = txtLogEkrani.getText().toString();
            txtLogEkrani.setText(logSatiri + "\n" + mevcutLoglar);
        }
    }

    /** Durumu SharedPreferences'a kaydeder. */
    private void savePersistentState() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_CLICK_COUNT, tiklamaSayaci);
        editor.putInt(KEY_THEME_INDEX, temaIndex);
        editor.putString(KEY_LAST_ACCESS,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        editor.apply();
    }

    /** Kayıtlı durumu okuyup log'a yazar. */
    private void loadPersistentStateAndLog() {
        String sonErisim = sharedPreferences.getString(KEY_LAST_ACCESS, "İlk açılış");
        appendExecutionLog("Oturum başlatıldı. Kayıtlı tıklama: " + tiklamaSayaci);
        appendExecutionLog("Son erişim: " + sonErisim);

        Toast.makeText(this, "Hoş geldin İbrahim Emre Kocatopcu!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePersistentState();
    }

    /** Veri modeli. */
    public static class EmreModel {
        public final String baslik;
        public final String aciklama;
        public final String kategori;

        public EmreModel(String baslik, String aciklama, String kategori) {
            this.baslik = baslik;
            this.aciklama = aciklama;
            this.kategori = kategori;
        }
    }
}
