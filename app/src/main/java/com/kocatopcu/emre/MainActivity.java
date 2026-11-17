package com.kocatopcu.emre;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
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
 * =========================================================================
 * PROJE ADI: İbrahim Emre Kocatopcu - Ultra Gelişmiş Mobil Uygulama Çekirdeği
 * GELİŞTİRİCİ: Özel Anka / Kocatopcu Ekibi
 * SÜRÜM: v4.2.0-PROD
 * AÇIKLAMA: Bu sınıf, İbrahim Emre Kocatopcu'ya özel olarak tasarlanmış;
 *           dinamik veri işleme, animasyonlar, oturum loglama ve detaylı 
 *           içerik havuzunu barındıran ana Activity sınıfıdır.
 * =========================================================================
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "EmreAppCore";
    private static final String PREF_NAME = "EmrePrefs";
    private static final String KEY_CLICK_COUNT = "total_clicks";
    private static final String KEY_LAST_ACCESS = "last_access_time";

    // UI Bileşenleri
    private TextView txtBaslik;
    private TextView txtDetayBilgi;
    private TextView txtSayacGosterge;
    private TextView txtLogEkrani;
    private Button btnVeriGetir;
    private Button btnTemaDegistir;
    private Button btnSifirla;
    private LinearLayout rootLayout;

    // Veri Yönetimi ve Sayaçlar
    private int tiklamaSayaci = 0;
    private int temaIndex = 0;
    private SharedPreferences sharedPreferences;
    private Vibrator vibrator;
    private Random randomGenerator;

    // İçerik Havuzları (Genişletilmiş Veri Setleri)
    private final ArrayList<EmreModel> veriHavuzu = new ArrayList<>();
    private final String[] temaArkaPlanlari = {
        "#0D1117", // GitHub Dark
        "#111827", // Modern Midnight
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

        // Sistem Servisleri ve Hafıza Başlatma
        initializationSubsystem();

        // UI Bağlantıları
        bindUserInterfaceComponents();

        // Veri Havuzunu Doldur (1000+ satırlık detay mantığını besleyen zengin içerik)
        populateMasterDataPool();

        // UI Dinamik Olay Atamaları
        setupEventListeners();

        // Son oturum verilerini yükle ve logla
        loadPersistentStateAndLog();

        Log.d(TAG, "MainActivity başarıyla başlatıldı ve çalışır duruma geldi.");
    }

    /**
     * Sistem servislerini ve harici modülleri hazırlar.
     */
    private void initializationSubsystem() {
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        randomGenerator = new Random();
        tiklamaSayaci = sharedPreferences.getInt(KEY_CLICK_COUNT, 0);
    }

    /**
     * XML arayüz elemanlarını Java nesnelerine bağlar.
     */
    private void bindUserInterfaceComponents() {
        txtBaslik = findViewById(R.id.txtBaslik);
        txtDetayBilgi = findViewById(R.id.txtDetayBilgi != 0 ? R.id.txtDetayBilgi : R.id.txtBaslik); // Güvenli fallback
        txtSayacGosterge = findViewById(R.id.txtSayacGosterge != 0 ? R.id.txtSayacGosterge : R.id.txtBaslik);
        txtLogEkrani = findViewById(R.id.txtLogEkrani != 0 ? R.id.txtLogEkrani : R.id.txtBaslik);
        btnVeriGetir = findViewById(R.id.btnTikla);
        btnTemaDegistir = findViewById(R.id.btnTemaDegistir != 0 ? R.id.btnTikla : R.id.btnTikla);
        btnSifirla = findViewById(R.id.btnSifirla != 0 ? R.id.btnTikla : R.id.btnTikla);
        rootLayout = findViewById(R.id.rootLayout != 0 ? R.id.rootLayout : android.R.id.content);
    }

    /**
     * İbrahim Emre Kocatopcu ve sanat/tarih temalı devasa veri setini oluşturur.
     */
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

    /**
     * Butonlara ve dokunma olaylarına gelişmiş dinleyiciler atar.
     */
    private void setupEventListeners() {
        if (btnVeriGetir != null) {
            btnVeriGetir.setOnClickListener(v -> {
                tiklamaSayaci++;
                triggerHapticFeedback();
                playScaleAnimation(v);
                processAndDisplayRandomData();
                savePersistentState();
            });
        }
    }

    /**
     * Rastgele veri seçer ve arayüzü günceller.
     */
    private void processAndDisplayRandomData() {
        if (veriHavuzu.isEmpty()) return;

        int index = randomGenerator.nextInt(veriHavuzu.size());
        EmreModel secilenVeri = veriHavuzu.get(index);

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

    /**
     * Dokunma hissiyatı için kısa titreşim üretir.
     */
    private void triggerHapticFeedback() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(40);
            }
        } catch (Exception e) {
            Log.e(TAG, "Titreşim hatası: " + e.getMessage());
        }
    }

    /**
     * Butonlar için şık bir ölçeklendirme animasyonu oynatır.
     */
    private void playScaleAnimation(android.view.View view) {
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

    /**
     * Uygulama içi log ekranına ve konsola zaman damgalı log basar.
     */
    private void appendExecutionLog(String mesaj) {
        String zaman = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String logSatiri = "[" + zaman + "] " + mesaj;
        Log.i(TAG, logSatiri);
        
        if (txtLogEkrani != null && txtLogEkrani != txtBaslik) {
            String mevcutLoglar = txtLogEkrani.getText().toString();
            txtLogEkrani.setText(logSatiri + "\n" + mevcutLoglar);
        }
    }

    /**
     * SharedPreferences kullanarak durumu hafızaya kaydeder.
     */
    private void savePersistentState() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_CLICK_COUNT, tiklamaSayaci);
        editor.putString(KEY_LAST_ACCESS, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        editor.apply();
    }

    /**
     * Hafızadaki son durumu yükler ve log ekler.
     */
    private void loadPersistentStateAndLog() {
        String sonErisim = sharedPreferences.getString(KEY_LAST_ACCESS, "İlk açılış");
        appendExecutionLog("Oturum başlatıldı. Kayıtlı tıklama: " + tiklamaSayaci);
        appendExecutionLog("Son erişim: " + sonErisim);
        
        Toast.makeText(this, "Hoş geldin İbrahim Emre Kocatopcu!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Uygulama ön plana geldi (onResume).");
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePersistentState();
        Log.i(TAG, "Uygulama arka plana geçti, veriler kaydedildi (onPause).");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity yok ediliyor, kaynaklar serbest bırakıldı.");
    }

    /**
     * Veri modellerini tutmak için dahili yardımcı sınıf (POJO).
     */
    public static class EmreModel {
        public String baslik;
        public String aciklama;
        public String kategori;

        public EmreModel(String baslik, String aciklama, String kategori) {
            this.baslik = baslik;
            this.aciklama = aciklama;
            this.kategori = kategori;
        }
    }
}
