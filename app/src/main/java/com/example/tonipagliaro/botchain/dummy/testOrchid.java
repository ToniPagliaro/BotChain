package com.example.tonipagliaro.botchain.dummy;

import com.subgraph.orchid.TorClient;
import com.subgraph.orchid.TorInitializationListener;

import org.apache.commons.io.IOUtils;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Toni Pagliaro on 28/02/2017.
 */
public class testOrchid {
    private static TorClient client;


    public static void main(String[] args) {
        startOrchid();
    }

    private static void startOrchid() {
        //listen on 127.0.0.1:9150 (default)
        client = new TorClient();
        client.addInitializationListener(createInitalizationListner());
        client.start();
        client.enableSocksListener();//or client.enableSocksListener(yourPortNum);

    }

    private static void stopOrchid() {
        client.stop();
    }

    public static TorInitializationListener createInitalizationListner() {
        return new TorInitializationListener() {
            @Override
            public void initializationProgress(String message, int percent) {
                System.out.println(">>> [ " + percent + "% ]: " + message);
            }

            @Override
            public void initializationCompleted() {
                System.out.println("Tor is ready to go!");
                doTests();
            }
        };
    }

    private static void doTests() {
        testOrchidUsingProxyObject();
    }



    private static void testOrchidUsingProxyObject() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    //Caution: Native Java DNS lookup will occur outside of the tor network.
                    //Monitor traffic on port 53 using tcpdump or equivalent.
                    URL url = new URL("http://sucza4jos42jrxcv.onion/ServerBotChain/FileServlet");
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8080));

                    HttpURLConnection uc = (HttpURLConnection) url.openConnection(proxy);
                    uc.setConnectTimeout(10000);
                    //     Document document = Jsoup.parse(IOUtils.toString(uc.getInputStream()));
                    //    String result = document.select("div[id=tor").text();

                    String result= IOUtils.toString(uc.getInputStream());
                    System.out.println("testOrchidUsingProxyObject: " + result);
                } catch (Exception ex) {
                    Logger.getLogger(testOrchid.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        thread.start();
    }


}


