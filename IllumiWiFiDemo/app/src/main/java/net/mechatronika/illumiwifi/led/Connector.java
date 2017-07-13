package net.mechatronika.illumiwifi.led;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;



class Connector {


    final int txPort=5401;

    byte[] rxBuf;
    byte[] txBuf;
    int txIdx;


    public class Color
    {
        int r;
        int g;
        int b;
        int w;

        public Color(int pr,int pg,int pb,int pw)
        {
            r=pr;
            g=pg;
            b=pb;
            w=pw;
        }

        public int getAndroidColor()
        {
            return android.graphics.Color.rgb(r,g,b);
        }
    }


    String adresIp=null;
    CillumiWiFiLEDSender sender;
    Handler handlerWiadomosciZIllumi;

    enum EkomendaIllumiWiFi{wyslijBiezacyKolor,wyslijBiezacyKolorZPrzejsciem,ustawDomyslnyKolor,pobierzKolory,pobierzInformacje};


    public Connector(Handler phandlerWiadomosciZIllumi)
    {
        txBuf=new byte[32];
        rxBuf=new byte[32];
        handlerWiadomosciZIllumi=phandlerWiadomosciZIllumi;
        txIdx=0;

    }


    int sprawdzWatekWysylajacy()
    {
        if (sender!=null)
        {
            if (sender.isAlive())
            {
                Log.e("","Connector: watek wysylajacy nadal aktywny.");
                return 1;
            }
        }

        return 0;
    }

    public boolean Connect(String padresIp)
    {
        adresIp=padresIp;
        return true;
    }

    public boolean Disconnect()
    {
        adresIp=null;
        return true;
    }

    public boolean SendColor(Connector.Color biezacyKolor) {

        if (adresIp == null) return false;

        if (sprawdzWatekWysylajacy() != 0) return false;

        sender = new CillumiWiFiLEDSender(handlerWiadomosciZIllumi, adresIp, EkomendaIllumiWiFi.wyslijBiezacyKolor, biezacyKolor, 0);
        sender.start();
        return true;


    }

    public boolean SendFadeToColor(Connector.Color biezacyKolor, int czasPrzejscia)
    {
        if (adresIp == null) return false;

        if (sprawdzWatekWysylajacy()!=0) return false;

        sender = new CillumiWiFiLEDSender(handlerWiadomosciZIllumi,adresIp,EkomendaIllumiWiFi.wyslijBiezacyKolorZPrzejsciem,biezacyKolor,czasPrzejscia);
        sender.start();

        return true;
    }

    public boolean SendDefaultColor(Connector.Color domyslnyKolor)
    {
        if (adresIp == null) return false;

        if (sprawdzWatekWysylajacy()!=0) return false;

        sender = new CillumiWiFiLEDSender(handlerWiadomosciZIllumi,adresIp,EkomendaIllumiWiFi.ustawDomyslnyKolor,domyslnyKolor,0);
        sender.start();

        return true;
    }

    public boolean GetColors()
    {
        if (adresIp == null) return false;

        if (sprawdzWatekWysylajacy()!=0) return false;

        sender = new CillumiWiFiLEDSender(handlerWiadomosciZIllumi,adresIp,EkomendaIllumiWiFi.pobierzKolory,null,0);
        sender.start();

        return true;
    }


    public boolean GetInfo()
    {
        if (adresIp == null) return false;

        if (sprawdzWatekWysylajacy()!=0) return false;

        sender = new CillumiWiFiLEDSender(handlerWiadomosciZIllumi,adresIp,EkomendaIllumiWiFi.pobierzInformacje,null,0);
        sender.start();

        return true;
    }

    class CillumiWiFiLEDSender extends Thread
    {
        Handler handlerWiadomosciZIllumi;

        EkomendaIllumiWiFi komenda;
        Connector.Color p1;
        int p2;

        String adresIp;
        DatagramSocket udp;
        DatagramPacket rxDp;
        DatagramPacket txDp;


        CillumiWiFiLEDSender(Handler phandlerWiadomosciZIllumi, String padresIp,EkomendaIllumiWiFi pkomenda,Connector.Color parametr1,int parametr2)
        {
            handlerWiadomosciZIllumi=phandlerWiadomosciZIllumi;
            adresIp=padresIp;
            komenda=pkomenda;

            p1=parametr1;
            p2=parametr2;
        }


        void wyslijWiadomosc(int parametr1,int parametr2,Object object)
        {
            if (handlerWiadomosciZIllumi!=null)
            {
                Message wiadomosc = handlerWiadomosciZIllumi.obtainMessage();

                if (wiadomosc!=null)
                {
                    wiadomosc.arg1=parametr1;
                    wiadomosc.arg2=parametr2;
                    wiadomosc.obj=object;

                    handlerWiadomosciZIllumi.sendMessage(wiadomosc);

                }
            }
        }


        @Override
        public void run()
        {

            try
            {

                udp = new DatagramSocket();
                udp.setSoTimeout(10);
                rxDp = new DatagramPacket(rxBuf,rxBuf.length);

            }catch (Exception ex)
            {
                Log.e("","CillumiWiFiLEDSender:run Wyjatek:"+ex.toString());

                wyslijWiadomosc(0,1,null);       //blad sieciowy

                return;
            }


            txIdx=0;

            switch (komenda)
            {
                case wyslijBiezacyKolor:

                    //komenda
                    txBuf[txIdx++] = 0x01;
                    //kolor
                    txBuf[txIdx++] = (byte)((p1.r)&0xff);
                    txBuf[txIdx++] = (byte)((p1.g)&0xff);
                    txBuf[txIdx++] = (byte)(p1.b);

                    break;

                case ustawDomyslnyKolor:

                    //komenda
                    txBuf[txIdx++] = 0x02;
                    //kolor
                    txBuf[txIdx++] = (byte)((p1.r)&0xff);
                    txBuf[txIdx++] = (byte)((p1.g)&0xff);
                    txBuf[txIdx++] = (byte)(p1.b);

                    break;

                case wyslijBiezacyKolorZPrzejsciem:

                    //komenda
                    txBuf[txIdx++] = 0x03;
                    //kolor
                    txBuf[txIdx++] = (byte)((p1.r)&0xff);
                    txBuf[txIdx++] = (byte)((p1.g)&0xff);
                    txBuf[txIdx++] = (byte)(p1.b);
                    //czas przejscia
                    txBuf[txIdx++] = (byte)((p2>>8)&0xff);
                    txBuf[txIdx++] = (byte)(p2&0xff);

                    break;

                case pobierzInformacje:
                    //komenda
                    txBuf[txIdx++] = (byte)(0x81&0xff);
                    break;

                case pobierzKolory:

                    //komenda
                    txBuf[txIdx++] = (byte)(0x82&0xff);
                    break;


                default:

                    Log.e("","CillumiWiFiLEDSender:run nieznana komenda:"+komenda.toString());
                    wyslijWiadomosc(0,2,null);       //nieznana komenda
                    return;
            }

            try {

                txDp = new DatagramPacket(txBuf, txIdx, InetAddress.getByName(adresIp),txPort);
                udp.send(txDp);

                sleep(250,0);

                rxDp.setLength(rxBuf.length);
                udp.receive(rxDp);

                udp.close();

            }catch (Exception ex)
            {
                Log.e("","CillumiWiFiLEDSender:run Wyjatek:"+ex.toString());
                wyslijWiadomosc(0,1,null);       //blad sieciowy

                return;
            }

            switch (komenda)
            {
                case wyslijBiezacyKolor:
                case wyslijBiezacyKolorZPrzejsciem:
                case ustawDomyslnyKolor:
                    if (rxDp.getLength()>0)
                    {
                        wyslijWiadomosc(0,0,null);   //ok
                    }

                    break;

                case pobierzKolory:

                    if (rxDp.getLength()==6)
                    {
                        wyslijWiadomosc(1, 0, new Color(rxBuf[0]&0xff,rxBuf[1]&0xff,rxBuf[2]&0xff,0));  //kolor biezacy
                        wyslijWiadomosc(2, 0, new Color(rxBuf[3]&0xff,rxBuf[4]&0xff,rxBuf[5]&0xff,0));  //kolor domyslny
                    }else{
                        wyslijWiadomosc(0,1,null);       //blad sieciowy

                    }

                    break;

                case pobierzInformacje:

                    String informacje = new String();

                    if (rxDp.getLength()==18) {
                        for (int i = 0; i < 18; i++) {
                            informacje += String.format("%c", rxBuf[i]);
                        }
                        wyslijWiadomosc(3, 0, informacje);
                    }else{
                        wyslijWiadomosc(0,1,null);  //blad sieciowy
                    }
                    break;
            }
        }
    }

}
