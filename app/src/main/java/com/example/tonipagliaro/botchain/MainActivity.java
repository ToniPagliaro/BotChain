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

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.SendRequest;
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
     Address a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appState = (ApplicationState) getApplication();

         a=new Address(appState.params,"mtuYPq2kuww1LtsZNTd5ZR1E6JZnsnS5t8");

        progressBar = (ProgressBar) this.findViewById(R.id.progressBar);
        textView = (TextView) this.findViewById(R.id.textView);
        Log.d("App:balance prima della ricezione",appState.wallet.getBalance().toFriendlyString());

        Log.d("App", "MainActivity");
        appState.wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                Log.d("App: balance dopo la ricezione", appState.wallet.getBalance().toFriendlyString());
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

            /*
                   RICHIAMARE LA PROSSIMA ACTIVITY (LISTA DEI BOT) QUI E PASSARLE L'OGGETTO WalletAppKit
                   Ma non so come fare...forse Content Provider???
             */
            appState.saveWallet();

            textView.setVisibility(View.INVISIBLE);

            Button b=(Button)findViewById(R.id.button);
            Log.d("App",appState.wallet.currentReceiveKey().toAddress(appState.params).toString());
            b.setVisibility(View.VISIBLE);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                    sendCommand("ciao ktm",a);
                    Intent intent = new Intent(MainActivity.this, BotListActivity.class);
                        startActivity(intent);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });


        }


        public  String sendCommand(String command, Address botAddress) throws Exception {

            byte[] hash = command.getBytes("UTF-8");

            Transaction transaction = new Transaction(appState.wallet.getParams());

            transaction.addOutput(Coin.MILLICOIN, botAddress);
            transaction.addOutput(Coin.ZERO, new ScriptBuilder().op(106).data(hash).build());

            SendRequest sendRequest = SendRequest.forTx(transaction);

            String string = new String(hash);
            System.out.println("Sending ... " + string);

            appState.wallet.completeTx(sendRequest);   // Could throw InsufficientMoneyException

            appState.peerGroup.setMaxConnections(1);
            appState.peerGroup.broadcastTransaction(sendRequest.tx);

            return transaction.getHashAsString();
        }


    }



}
