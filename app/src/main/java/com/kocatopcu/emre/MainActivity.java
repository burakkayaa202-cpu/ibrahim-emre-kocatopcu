package com.kocatopcu.emre;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_NAME = "EmrePrefs";
    private static final String KEY_COUNT = "total_clicks";
    private static final String KEY_LAST = "last_access_time";
    private static final String KEY_THEME = "theme_index";

    private View rootLayout;
    private TextView txtAvatar, txtSelam, txtKategori, txtBaslik, txtMetin,
            txtKaynak, txtSayac, txtTemaAdi, txtSonErisim;
    private MaterialCardView cardIcerik, cardSayac, cardTema;
    private MaterialButton btnTikla, btnTema, btnSifirla;

    private SharedPreferences prefs;
    private Vibrator vibrator;
    private final Random random = new Random();
    private int sayac = 0;
    private int temaIndex = 0;
    private int sonIndex = -1;

    private static class Tema {
        final String ad; final String arka; final String kart; final String vurgu;
        Tema(String ad, String arka, String kart, String vurgu) {
            this.ad = ad; this.arka = arka; this.kart = kart; this.vurgu = vurgu;
        }
    }

    private static class Bilgi {
        final String kategori, baslik, metin, kaynak;
        Bilgi(String kategori, String baslik, String metin, String kaynak) {
            this.kategori = kategori; this.baslik = baslik; this.metin = metin; this.kaynak = kaynak;
        }
    }

    private final Tema[] temalar = {
            new Tema("Gece", "#0D1117", "#161B22", "#58A6FF"),
            new Tema("Okyanus", "#0B1620", "#12232F", "#2DD4BF"),
            new Tema("Mor", "#140B24", "#1E1236", "#A78BFA"),
            new Tema("Gün Batımı", "#1A0F0F", "#261717", "#FB923C"),
            new Tema("Orman", "#0C1710", "#132219", "#4ADE80")
    };

    private final ArrayList<Bilgi> havuz = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        sayac = prefs.getInt(KEY_COUNT, 0);
        temaIndex = prefs.getInt(KEY_THEME, 0) % temalar.length;

        bind();
        doldur();
        dinleyiciler();
        temaUygula();
        selamla();

        String son = prefs.getString(KEY_LAST, null);
        txtSonErisim.setText(son == null ? "İlk kez hoş geldin" : "Son ziyaret: " + son);
    }

    private void bind() {
        rootLayout = findViewById(R.id.rootLayout);
        txtAvatar = findViewById(R.id.txtAvatar);
        txtSelam = findViewById(R.id.txtSelam);
        txtKategori = findViewById(R.id.txtKategori);
        txtBaslik = findViewById(R.id.txtBaslik);
        txtMetin = findViewById(R.id.txtMetin);
        txtKaynak = findViewById(R.id.txtKaynak);
        txtSayac = findViewById(R.id.txtSayac);
        txtTemaAdi = findViewById(R.id.txtTemaAdi);
        txtSonErisim = findViewById(R.id.txtSonErisim);
        cardIcerik = findViewById(R.id.cardIcerik);
        cardSayac = findViewById(R.id.cardSayac);
        cardTema = findViewById(R.id.cardTema);
        btnTikla = findViewById(R.id.btnTikla);
        btnTema = findViewById(R.id.btnTemaDegistir);
        btnSifirla = findViewById(R.id.btnSifirla);
    }

    private void doldur() {
        havuz.add(new Bilgi("PROFİL", "İbrahim Emre Kocatopcu",
                "Ekibin lideri, kararlı bir geliştirici ve güvenilir bir dost. Fikirleri çalışan ürünlere dönüştürmeyi sever.", "Kişisel Profil"));
        havuz.add(new Bilgi("İSİM KÖKENİ", "Emre ne demek?",
                "Eski Türkçede \"emre\"; âşık, dost, gönül verilmiş kişi ve halk ozanı anlamlarında kullanılır.", "Türkçe Etimoloji"));
        havuz.add(new Bilgi("TARİH", "Yunus Emre",
                "1238 - 1320 yılları arasında yaşamış, Türkçenin sevgi ve tasavvuf dilini zirveye taşıyan büyük ozan.", "Edebiyat Tarihi"));
        havuz.add(new Bilgi("SÖZ", "Sevgi Üzerine",
                "\"Sevelim, sevilelim, dünya kimseye kalmaz.\"", "Yunus Emre"));
        havuz.add(new Bilgi("SÖZ", "Bilgi Üzerine",
                "\"İlim ilim bilmektir, ilim kendin bilmektir. Sen kendini bilmezsin, ya nice okumaktır.\"", "Yunus Emre"));
        havuz.add(new Bilgi("MÜZİK", "Emre Aydın",
                "Türk rock müziğine damga vurmuş, ödüllü söz yazarı ve besteci.", "Müzik Dünyası"));
        havuz.add(new Bilgi("YAZILIM", "Kod Felsefesi",
                "İyi kod, satır sayısıyla değil, okunabilirliği ve kurduğu mantıkla ölçülür.", "Yazılım Notu"));
        havuz.add(new Bilgi("YAZILIM", "Küçük Adımlar",
                "Büyük projeler, her gün atılan küçük ve düzenli commit'lerle büyür.", "Yazılım Notu"));
        havuz.add(new Bilgi("EKİP", "Anka Ruhu",
                "Asla pes etmeyen, küllerinden yeniden doğan ve projesini sonuna kadar savunan ekip ruhu.", "Takım Kültürü"));
        havuz.add(new Bilgi("LİDERLİK", "Sakin Kalmak",
                "Kriz anlarında sakin kalmak ve ekibi doğru yöne çevirmek liderliğin en zor ama en değerli yanıdır.", "Liderlik Notu"));
    }

    private void dinleyiciler() {
        btnTikla.setOnClickListener(v -> {
            titret();
            yeniBilgi();
        });
        btnTema.setOnClickListener(v -> {
            titret();
            temaIndex = (temaIndex + 1) % temalar.length;
            temaUygula();
            kaydet();
        });
        btnSifirla.setOnClickListener(v -> {
            titret();
            sayac = 0;
            sayacYaz();
            degistir(txtKategori, "HOŞ GELDİN");
            degistir(txtBaslik, "Baştan başlayalım");
            degistir(txtMetin, "Sayaç sıfırlandı. Yeni bir bilgi keşfetmeye hazırsın.");
            degistir(txtKaynak, "");
            kaydet();
        });
    }

    private void yeniBilgi() {
        int i;
        do {
            i = random.nextInt(havuz.size());
        } while (i == sonIndex && havuz.size() > 1);
        sonIndex = i;
        Bilgi b = havuz.get(i);

        degistir(txtKategori, b.kategori);
        degistir(txtBaslik, b.baslik);
        degistir(txtMetin, b.metin);
        degistir(txtKaynak, "— " + b.kaynak);

        sayac++;
        sayacYaz();
        kaydet();
    }

    private void degistir(TextView tv, String yeni) {
        tv.animate().alpha(0f).setDuration(120).withEndAction(() -> {
            tv.setText(yeni);
            tv.animate().alpha(1f).setDuration(200).start();
        }).start();
    }

    private void temaUygula() {
        Tema t = temalar[temaIndex];
        int vurgu = Color.parseColor(t.vurgu);
        int kart = Color.parseColor(t.kart);
        int cizgi = ColorUtils.setAlphaComponent(Color.WHITE, 28);

        rootLayout.setBackgroundColor(Color.parseColor(t.arka));
        for (MaterialCardView c : new MaterialCardView[]{cardIcerik, cardSayac, cardTema}) {
            c.setCardBackgroundColor(kart);
            c.setStrokeColor(cizgi);
        }

        GradientDrawable avatar = new GradientDrawable();
        avatar.setShape(GradientDrawable.OVAL);
        avatar.setColor(ColorUtils.setAlphaComponent(vurgu, 60));
        avatar.setStroke(dp(2), vurgu);
        txtAvatar.setBackground(avatar);

        GradientDrawable chip = new GradientDrawable();
        chip.setCornerRadius(dp(12));
        chip.setColor(ColorUtils.setAlphaComponent(vurgu, 45));
        txtKategori.setBackground(chip);
        txtKategori.setTextColor(vurgu);

        btnTikla.setBackgroundTintList(android.content.res.ColorStateList.valueOf(vurgu));
        btnTikla.setTextColor(ColorUtils.calculateLuminance(vurgu) > 0.5 ? Color.BLACK : Color.WHITE);

        txtTemaAdi.setText(t.ad);
        txtTemaAdi.setTextColor(vurgu);
        txtSayac.setTextColor(vurgu);
        sayacYaz();
    }

    private void sayacYaz() {
        txtSayac.setText(String.valueOf(sayac));
    }

    private void selamla() {
        int saat = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String s;
        if (saat < 6) s = "İyi geceler";
        else if (saat < 12) s = "Günaydın";
        else if (saat < 18) s = "Tünaydın";
        else s = "İyi akşamlar";
        txtSelam.setText(s);
    }

    private void titret() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(25);
                }
            }
        } catch (Exception ignored) { }
    }

    private void kaydet() {
        prefs.edit()
                .putInt(KEY_COUNT, sayac)
                .putInt(KEY_THEME, temaIndex)
                .putString(KEY_LAST, new SimpleDateFormat("d MMM, HH:mm", new Locale("tr")).format(new Date()))
                .apply();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onPause() {
        super.onPause();
        kaydet();
    }
}
