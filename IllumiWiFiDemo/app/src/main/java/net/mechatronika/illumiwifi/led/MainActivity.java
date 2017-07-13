package net.mechatronika.illumiwifi.led;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //Kontrolki (Views)

    EditText editTextAdresIP;
    View viewKolorBiezacy;
    View viewKolorDomyslny;
    SeekBar seekBarCzerwony;
    boolean odswiezKolorCzerwony;

    SeekBar seekBarZielony;
    boolean odswiezKolorZielony;

    SeekBar seekBarNiebieski;
    boolean odswiezKolorNiebieski;


    Button buttonUstawBiezacyKolorJakoDomyslny;
    Button buttonPobierzKolory;
    Button buttonWyslijBiezacyKolor;
    Button buttonWyslijBiezacyKolorZPrzejsciemBarw;
    Button buttonPobierzInformacje;

    //Kolory

    Connector.Color kolorBiezacy;
    Connector.Color kolorDomyslny;


    final int czasPrzejsciaBarw = 100;

    //Klasa sterownika illumiWiFi

    Connector illumi;

    //Handler przejmujacy informacje z watku komunikacyjnego ze sterownikiem

    Handler handlerKomunikacjaIllumi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        odswiezKolorCzerwony=true;
        odswiezKolorZielony=true;
        odswiezKolorNiebieski=true;

        //Znajdz kontrolki

        editTextAdresIP=(EditText)findViewById(R.id.editTextAdresIP);
        viewKolorBiezacy=findViewById(R.id.viewKolorBiezacy);
        viewKolorDomyslny=findViewById(R.id.viewKolorDomyslny);
        seekBarCzerwony=(SeekBar)findViewById(R.id.seekBarCzerwony);
        seekBarZielony=(SeekBar)findViewById(R.id.seekBarZielony);
        seekBarNiebieski=(SeekBar)findViewById(R.id.seekBarNiebieski);
        buttonUstawBiezacyKolorJakoDomyslny=(Button)findViewById(R.id.buttonUstawBiezacyKolorJakoDomyslny);
        buttonPobierzKolory=(Button)findViewById(R.id.buttonPobierzKolory);
        buttonWyslijBiezacyKolor=(Button)findViewById(R.id.buttonWyslijBiezacyKolor);
        buttonWyslijBiezacyKolorZPrzejsciemBarw=(Button)findViewById(R.id.buttonWyslijBiezacyKolorZPrzejsciemBarw);
        buttonPobierzInformacje=(Button)findViewById(R.id.buttonPobierzInformacje);

        //Obsluga zdarzen


        //Seekbary zmiany kolorow
        seekBarCzerwony.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Mechanizm blokowania odswiezenia biezaego koloru dla pojedynczego zdarzenia
                //uzyteczny w chwili gdy kolor zadany jest z aplikacji a nie przez seekbary.

                //Zobacz handler wiadomosci z illumi.

                if (odswiezKolorCzerwony) {
                    odswiezWidokiKolorow();
                }else {
                    odswiezKolorCzerwony=true;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarZielony.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (odswiezKolorZielony) {
                    odswiezWidokiKolorow();
                }else
                {
                    odswiezKolorZielony=true;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarNiebieski.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (odswiezKolorNiebieski) {
                    odswiezWidokiKolorow();
                }else {
                    odswiezKolorNiebieski=true;
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Buttony

        buttonUstawBiezacyKolorJakoDomyslny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ustawBiezacyKolorJakoDomyslny();
            }
        });

        buttonPobierzKolory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pobierzKolory();
            }
        });

        buttonWyslijBiezacyKolor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wyslijBiezacyKolor();
            }
        });

        buttonWyslijBiezacyKolorZPrzejsciemBarw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wyslijBiezacyKolorZPrzejsciemBarw();
            }
        });


        buttonPobierzInformacje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pobierzInformacje();
            }
        });

        //Informacje zwrotne z watku komunikacji ze sterownikiem
        handlerKomunikacjaIllumi = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                obsluzWiadomosciZIllumi(msg);
            }
        };

        //Utworz klase polaczenia
        illumi = new Connector(handlerKomunikacjaIllumi);

        kolorDomyslny=illumi.new Color(0,0,0,0);
        kolorBiezacy=illumi.new Color(0,0,0,0);

    }



    void odswiezWidokiKolorow()
    {
        if (viewKolorBiezacy==null) return;
        if (seekBarCzerwony==null) return;
        if (seekBarZielony==null) return;
        if (seekBarNiebieski==null) return;

        kolorBiezacy.r=seekBarCzerwony.getProgress();
        kolorBiezacy.g=seekBarZielony.getProgress();
        kolorBiezacy.b=seekBarNiebieski.getProgress();

        viewKolorBiezacy.setBackgroundColor(kolorBiezacy.getAndroidColor());
    }

    void ustawBiezacyKolorJakoDomyslny()
    {
        if (illumi==null) return;
        if (editTextAdresIP==null) return;
        if (viewKolorDomyslny==null) return;

        kolorDomyslny.r=kolorBiezacy.r;
        kolorDomyslny.g=kolorBiezacy.g;
        kolorDomyslny.b=kolorBiezacy.b;


        viewKolorDomyslny.setBackgroundColor(kolorDomyslny.getAndroidColor());

        illumi.Connect(editTextAdresIP.getText().toString());
        illumi.SendDefaultColor(kolorDomyslny);
        illumi.Disconnect();

    }

    void pobierzKolory()
    {
        if (illumi==null) return;
        if (editTextAdresIP==null) return;

        //Informacja zwrotna podawana jest przez handler
        illumi.Connect(editTextAdresIP.getText().toString());
        illumi.GetColors();
        illumi.Disconnect();
    }

    void wyslijBiezacyKolor()
    {
        if (illumi==null) return;
        if (editTextAdresIP==null) return;;

        illumi.Connect(editTextAdresIP.getText().toString());
        illumi.SendColor(kolorBiezacy);
        illumi.Disconnect();

    }

    void wyslijBiezacyKolorZPrzejsciemBarw()
    {
        if (illumi==null) return;
        if (editTextAdresIP==null) return;

        illumi.Connect(editTextAdresIP.getText().toString());
        illumi.SendFadeToColor(kolorBiezacy,czasPrzejsciaBarw);
        illumi.Disconnect();

    }

    void pobierzInformacje()
    {
        if (illumi==null) return;
        if (editTextAdresIP==null) return;

        illumi.Connect(editTextAdresIP.getText().toString());
        illumi.GetInfo();
        illumi.Disconnect();

    }

    void wyswietlPowiadomienie(String tekst)
    {
        Toast.makeText(getApplicationContext(),tekst,Toast.LENGTH_LONG).show();
    }

    void obsluzWiadomosciZIllumi(Message wiadomosc)
    {
        Connector.Color wiadomoscColor;

        switch (wiadomosc.arg1)
        {
            case 0:     //status

                switch (wiadomosc.arg2)
                {
                    case 0:
                        //sukces - nie wyswietlaj powiadomien

                        break;


                    case 1:
                        wyswietlPowiadomienie("Błąd sieciowy (prawdopodobnie zły adres IP).");
                        break;

                    case 2:
                        wyswietlPowiadomienie("Nieznana komenda");
                        break;

                }

                break;

            case 1:     //kolor biezacy

                if (viewKolorBiezacy==null) break;
                if (seekBarCzerwony==null) break;
                if (seekBarZielony==null) break;
                if (seekBarNiebieski==null) break;


                wiadomoscColor=(Connector.Color)wiadomosc.obj;


                kolorBiezacy.r=wiadomoscColor.r;
                kolorBiezacy.g=wiadomoscColor.g;
                kolorBiezacy.b=wiadomoscColor.b;


                //Nie odswiezaj kolorow przy nastepnym zdarzeniu dla seekbarow
                odswiezKolorCzerwony=false;
                odswiezKolorZielony=false;
                odswiezKolorNiebieski=false;

                seekBarCzerwony.setProgress(kolorBiezacy.r);
                seekBarZielony.setProgress(kolorBiezacy.g);
                seekBarNiebieski.setProgress(kolorBiezacy.b);

                viewKolorBiezacy.setBackgroundColor(kolorBiezacy.getAndroidColor());

                break;

            case 2:     //kolor domyslny

                wiadomoscColor=(Connector.Color)wiadomosc.obj;

                kolorDomyslny.r = wiadomoscColor.r;
                kolorDomyslny.g = wiadomoscColor.g;
                kolorDomyslny.b = wiadomoscColor.b;

                if (viewKolorDomyslny==null) break;
                viewKolorDomyslny.setBackgroundColor(kolorDomyslny.getAndroidColor());

                break;

            case 3:     //informacje o sterowniku

                wyswietlPowiadomienie("Wersja oprogramowania oraz MAC:"+(String)wiadomosc.obj);
                break;
        }
    }
}
