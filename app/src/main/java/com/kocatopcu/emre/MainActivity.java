package com.kocatopcu.emre;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // Güncelleme kontrolü bu repodaki son sürüme bakar (repo herkese açık olmalı)
    private static final String RELEASE_API =
            "https://api.github.com/repos/burakkayaa202-cpu/ibrahim-emre-kocatopcu/releases/latest";

    private static final String PREF_NAME = "EmrePrefs";
    private static final String KEY_PUAN = "puan";
    private static final String KEY_SERI = "seri";
    private static final String KEY_SON_ODUL = "son_odul";
    private static final String KEY_FAVS = "favoriler";
    private static final String KEY_THEME = "theme_index";
    private static final String KEY_LAST = "last_access_time";

    private View rootLayout;
    private TextView txtAvatar, txtSelam, txtOdulBaslik, txtOdulAciklama, txtPuan, txtSeri,
            txtFavori, txtSeviye, txtKategori, txtBaslik, txtMetin, txtKaynak, txtSurum, btnSifirla;
    private MaterialCardView cardOdul, cardStat, cardIcerik;
    private MaterialButton btnOdul, btnFavori, btnPaylas, btnTikla, btnTema, btnFavoriler;
    private LinearProgressIndicator progress;

    private SharedPreferences prefs;
    private Vibrator vibrator;
    private final Random random = new Random();

    private int puan, seri, temaIndex, sonIndex = -1, suankiIndex = -1;
    private String sonOdul = "";
    private final Set<String> favoriler = new HashSet<>();

    private static class Tema {
        final String ad, arka, kart, vurgu;
        Tema(String ad, String arka, String kart, String vurgu) {
            this.ad = ad; this.arka = arka; this.kart = kart; this.vurgu = vurgu;
        }
    }

    private static class Bilgi {
        final String kategori, baslik, metin, kaynak;
        Bilgi(String k, String b, String m, String ka) {
            kategori = k; baslik = b; metin = m; kaynak = ka;
        }
    }

    private final Tema[] temalar = {
            new Tema("Gece", "#0D1117", "#161B22", "#58A6FF"),
            new Tema("Okyanus", "#0B1620", "#12232F", "#2DD4BF"),
            new Tema("Mor", "#140B24", "#1E1236", "#A78BFA"),
            new Tema("Gün Batımı", "#1A0F0F", "#261717", "#FB923C"),
            new Tema("Orman", "#0C1710", "#132219", "#4ADE80")
    };

    private final String[] seviyeAdlari = {"Çaylak", "Yolcu", "Usta", "Bilge", "Efsane"};
    private final ArrayList<Bilgi> havuz = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        yukle();
        bind();
        doldur();
        dinleyiciler();
        temaUygula();

        txtSurum.setText("Sürüm " + surumAdi() + "  •  Güncellemeleri denetle");
        guncellemeDenetle(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        selamla();
        odulGuncelle();
        istatistikGuncelle();
    }

    @Override
    protected void onPause() {
        super.onPause();
        kaydet();
    }

    // ---------- Kurulum ----------

    private void bind() {
        rootLayout = findViewById(R.id.rootLayout);
        txtAvatar = findViewById(R.id.txtAvatar);
        txtSelam = findViewById(R.id.txtSelam);
        txtOdulBaslik = findViewById(R.id.txtOdulBaslik);
        txtOdulAciklama = findViewById(R.id.txtOdulAciklama);
        txtPuan = findViewById(R.id.txtPuan);
        txtSeri = findViewById(R.id.txtSeri);
        txtFavori = findViewById(R.id.txtFavori);
        txtSeviye = findViewById(R.id.txtSeviye);
        txtKategori = findViewById(R.id.txtKategori);
        txtBaslik = findViewById(R.id.txtBaslik);
        txtMetin = findViewById(R.id.txtMetin);
        txtKaynak = findViewById(R.id.txtKaynak);
        txtSurum = findViewById(R.id.txtSurum);
        btnSifirla = findViewById(R.id.btnSifirla);
        cardOdul = findViewById(R.id.cardOdul);
        cardStat = findViewById(R.id.cardStat);
        cardIcerik = findViewById(R.id.cardIcerik);
        btnOdul = findViewById(R.id.btnOdul);
        btnFavori = findViewById(R.id.btnFavori);
        btnPaylas = findViewById(R.id.btnPaylas);
        btnTikla = findViewById(R.id.btnTikla);
        btnTema = findViewById(R.id.btnTemaDegistir);
        btnFavoriler = findViewById(R.id.btnFavoriler);
        progress = findViewById(R.id.progressSeviye);
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
        havuz.add(new Bilgi("ALIŞKANLIK", "Her Gün Bir Adım",
                "Alışkanlıklar motivasyondan değil, tekrardan doğar. Bugün küçük de olsa bir adım at.", "Gelişim Notu"));
        havuz.add(new Bilgi("SÖZ", "Gönül Üzerine",
                "\"Gönül Çalab'ın tahtı, Çalab gönüle bakmıştır.\"", "Yunus Emre"));
    }

    private void dinleyiciler() {
        btnOdul.setOnClickListener(v -> odulAl());

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

        btnFavori.setOnClickListener(v -> {
            if (suankiIndex < 0) {
                Toast.makeText(this, "Önce bir bilgi keşfet", Toast.LENGTH_SHORT).show();
                return;
            }
            titret();
            String id = String.valueOf(suankiIndex);
            if (favoriler.contains(id)) favoriler.remove(id);
            else favoriler.add(id);
            favoriButonuGuncelle();
            istatistikGuncelle();
            kaydet();
        });

        btnPaylas.setOnClickListener(v -> {
            if (suankiIndex < 0) {
                Toast.makeText(this, "Önce bir bilgi keşfet", Toast.LENGTH_SHORT).show();
                return;
            }
            Bilgi b = havuz.get(suankiIndex);
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, b.baslik + "\n" + b.metin + "\n— " + b.kaynak);
            startActivity(Intent.createChooser(i, "Paylaş"));
        });

        btnFavoriler.setOnClickListener(v -> favorileriGoster());

        txtSurum.setOnClickListener(v -> guncellemeDenetle(true));

        btnSifirla.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Verileri sıfırla")
                .setMessage("Puanın, serin ve favorilerin silinecek. Emin misin?")
                .setPositiveButton("Sıfırla", (d, w) -> {
                    puan = 0; seri = 0; sonOdul = "";
                    favoriler.clear();
                    suankiIndex = -1;
                    kaydet();
                    degistir(txtKategori, "HOŞ GELDİN");
                    degistir(txtBaslik, "Baştan başlayalım");
                    degistir(txtMetin, "Verilerin sıfırlandı. Yeni bir yolculuğa hazırsın.");
                    degistir(txtKaynak, "");
                    favoriButonuGuncelle();
                    odulGuncelle();
                    istatistikGuncelle();
                })
                .setNegativeButton("Vazgeç", null)
                .show());
    }

    // ---------- Günlük ödül ----------

    private String fmt(Calendar c) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.getTime());
    }

    private String bugun() { return fmt(Calendar.getInstance()); }

    private String dun() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -1);
        return fmt(c);
    }

    /** Seri, dün veya bugün ödül alındıysa geçerlidir; yoksa kopmuştur. */
    private int etkinSeri() {
        return (sonOdul.equals(bugun()) || sonOdul.equals(dun())) ? seri : 0;
    }

    private int odulMiktari(int gun) {
        int m = 10 + 5 * ((gun - 1) % 7);
        if (gun % 7 == 0) m += 50; // haftalık bonus
        return m;
    }

    private void odulGuncelle() {
        boolean alindi = sonOdul.equals(bugun());
        int s = etkinSeri();

        if (alindi) {
            btnOdul.setText("Bugünkü ödül alındı ✓");
            btnOdul.setEnabled(false);
            btnOdul.setAlpha(0.5f);
            txtOdulAciklama.setText(s + " günlük serin var. Yarın gel: " + (s + 1)
                    + ". gün ödülü +" + odulMiktari(s + 1) + " puan.");
        } else {
            btnOdul.setText("Ödülü Al  (+" + odulMiktari(s + 1) + " puan)");
            btnOdul.setEnabled(true);
            btnOdul.setAlpha(1f);
            if (s == 0) {
                txtOdulAciklama.setText("Seriyi bugün başlat. Her gün girdikçe ödül büyür, 7. günde bonus var.");
            } else {
                txtOdulAciklama.setText(s + " günlük serin devam ediyor. Bugün girmezsen seri sıfırlanır.");
            }
        }
    }

    private void odulAl() {
        if (sonOdul.equals(bugun())) return;
        titret();
        int yeniSeri = etkinSeri() + 1;
        int miktar = odulMiktari(yeniSeri);
        seri = yeniSeri;
        puan += miktar;
        sonOdul = bugun();
        kaydet();
        odulGuncelle();
        istatistikGuncelle();

        String mesaj = yeniSeri + ". gün serisi! +" + miktar + " puan kazandın.";
        if (yeniSeri % 7 == 0) mesaj += "\nHaftalık bonus dahil 🎉";
        new AlertDialog.Builder(this)
                .setTitle("🎁 Günlük ödül")
                .setMessage(mesaj)
                .setPositiveButton("Harika", null)
                .show();
    }

    // ---------- İçerik ----------

    private void yeniBilgi() {
        int i;
        do {
            i = random.nextInt(havuz.size());
        } while (i == sonIndex && havuz.size() > 1);
        sonIndex = i;
        bilgiGoster(i);
        puan += 2;
        istatistikGuncelle();
        kaydet();
    }

    private void bilgiGoster(int i) {
        suankiIndex = i;
        Bilgi b = havuz.get(i);
        degistir(txtKategori, b.kategori);
        degistir(txtBaslik, b.baslik);
        degistir(txtMetin, b.metin);
        degistir(txtKaynak, "— " + b.kaynak);
        favoriButonuGuncelle();
    }

    private void favoriButonuGuncelle() {
        boolean fav = suankiIndex >= 0 && favoriler.contains(String.valueOf(suankiIndex));
        btnFavori.setText(fav ? "♥ Favoride" : "♡ Favori");
    }

    private void favorileriGoster() {
        if (favoriler.isEmpty()) {
            Toast.makeText(this, "Henüz favorin yok", Toast.LENGTH_SHORT).show();
            return;
        }
        final ArrayList<Integer> idler = new ArrayList<>();
        for (String s : favoriler) idler.add(Integer.parseInt(s));
        String[] basliklar = new String[idler.size()];
        for (int k = 0; k < idler.size(); k++) basliklar[k] = havuz.get(idler.get(k)).baslik;

        new AlertDialog.Builder(this)
                .setTitle("♥ Favorilerim")
                .setItems(basliklar, (d, which) -> bilgiGoster(idler.get(which)))
                .setNegativeButton("Kapat", null)
                .show();
    }

    private void degistir(TextView tv, String yeni) {
        tv.animate().alpha(0f).setDuration(120).withEndAction(() -> {
            tv.setText(yeni);
            tv.animate().alpha(1f).setDuration(200).start();
        }).start();
    }

    // ---------- Selam / istatistik / tema ----------

    private void selamla() {
        int saat = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String s;
        if (saat < 6) s = "İyi geceler 🌙";
        else if (saat < 12) s = "Günaydın ☀️";
        else if (saat < 18) s = "Tünaydın 🌤️";
        else s = "İyi akşamlar 🌆";
        txtSelam.setText(s + ", Emre");
    }

    private void istatistikGuncelle() {
        txtPuan.setText(String.valueOf(puan));
        txtSeri.setText(String.valueOf(etkinSeri()));
        txtFavori.setText(String.valueOf(favoriler.size()));

        int seviye = puan / 100 + 1;
        String ad = seviyeAdlari[Math.min(seviye - 1, seviyeAdlari.length - 1)];
        txtSeviye.setText("Seviye " + seviye + " • " + ad + "  (" + (puan % 100) + "/100)");
        progress.setProgressCompat(puan % 100, true);
    }

    private void temaUygula() {
        Tema t = temalar[temaIndex];
        int vurgu = Color.parseColor(t.vurgu);
        int kart = Color.parseColor(t.kart);
        int cizgi = ColorUtils.setAlphaComponent(Color.WHITE, 28);
        int yaziRengi = ColorUtils.calculateLuminance(vurgu) > 0.5 ? Color.BLACK : Color.WHITE;

        rootLayout.setBackgroundColor(Color.parseColor(t.arka));
        for (MaterialCardView c : new MaterialCardView[]{cardOdul, cardStat, cardIcerik}) {
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

        for (MaterialButton b : new MaterialButton[]{btnTikla, btnOdul}) {
            b.setBackgroundTintList(ColorStateList.valueOf(vurgu));
            b.setTextColor(yaziRengi);
        }
        btnFavori.setTextColor(vurgu);
        btnPaylas.setTextColor(vurgu);
        txtPuan.setTextColor(vurgu);
        txtSeri.setTextColor(vurgu);
        txtFavori.setTextColor(vurgu);
        progress.setIndicatorColor(vurgu);
        btnTema.setText("Tema: " + t.ad);
    }

    // ---------- Güncelleme denetimi ----------

    private int surumKodu() {
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            return (int) PackageInfoCompat.getLongVersionCode(pi);
        } catch (Exception e) {
            return 0;
        }
    }

    private String surumAdi() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "?";
        }
    }

    private void guncellemeDenetle(final boolean elle) {
        if (elle) Toast.makeText(this, "Güncellemeler denetleniyor...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(RELEASE_API).openConnection();
                c.setConnectTimeout(8000);
                c.setReadTimeout(8000);
                c.setRequestProperty("Accept", "application/vnd.github+json");
                if (c.getResponseCode() != 200) throw new Exception("HTTP " + c.getResponseCode());

                BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject j = new JSONObject(sb.toString());
                final int uzak = Integer.parseInt(j.getString("tag_name").replaceAll("[^0-9]", ""));
                final String ad = j.optString("name", "Yeni sürüm");
                final String notlar = j.optString("body", "");
                String link = j.getString("html_url");
                JSONArray assets = j.optJSONArray("assets");
                if (assets != null && assets.length() > 0) {
                    link = assets.getJSONObject(0).getString("browser_download_url");
                }
                final String indirme = link;

                runOnUiThread(() -> {
                    if (isFinishing()) return;
                    if (uzak > surumKodu()) {
                        guncellemeDialogu(ad, notlar, indirme);
                    } else if (elle) {
                        Toast.makeText(this, "Uygulaman güncel ✓", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                if (elle) runOnUiThread(() ->
                        Toast.makeText(this, "Güncelleme denetlenemedi", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void guncellemeDialogu(String ad, String notlar, final String link) {
        String mesaj = "Yeni bir sürüm hazır.\n\n" + (notlar.isEmpty() ? ad : notlar)
                + "\n\nİndirdikten sonra APK'ya dokunarak mevcut uygulamanın üstüne kurabilirsin. Verilerin korunur.";
        new AlertDialog.Builder(this)
                .setTitle("🚀 Güncelleme var")
                .setMessage(mesaj)
                .setPositiveButton("Güncelle", (d, w) ->
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link))))
                .setNegativeButton("Sonra", null)
                .show();
    }

    // ---------- Yardımcılar ----------

    private void yukle() {
        puan = prefs.getInt(KEY_PUAN, 0);
        seri = prefs.getInt(KEY_SERI, 0);
        sonOdul = prefs.getString(KEY_SON_ODUL, "");
        temaIndex = prefs.getInt(KEY_THEME, 0) % temalar.length;
        favoriler.clear();
        favoriler.addAll(prefs.getStringSet(KEY_FAVS, new HashSet<String>()));
    }

    private void kaydet() {
        prefs.edit()
                .putInt(KEY_PUAN, puan)
                .putInt(KEY_SERI, seri)
                .putString(KEY_SON_ODUL, sonOdul)
                .putInt(KEY_THEME, temaIndex)
                .putStringSet(KEY_FAVS, new HashSet<>(favoriler))
                .putString(KEY_LAST, new SimpleDateFormat("d MMM, HH:mm", new Locale("tr")).format(new Date()))
                .apply();
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

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
