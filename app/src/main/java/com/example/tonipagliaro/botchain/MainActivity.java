package com.example.tonipagliaro.botchain;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    /*   @Override
       protected void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           setContentView(R.layout.activity_main);


           Button b=(Button)findViewById(R.id.button);

           b.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent i=new Intent(MainActivity.this,BotListActivity.class);
                   startActivity(i);

               }
           });

       }
   */
    NetworkParameters params;
    String filePrefix;

    // Mappa per la memorizzazione dei blocchi della clockchain
    final TreeMap<Integer, StoredBlock> checkpoints = new TreeMap<Integer, StoredBlock>();

    SPVBlockStore chainStore;
    BlockChain chain;
    PeerGroup peerGroup;
    Wallet wallet;
    DownloadProgressTracker downloadTracker;

    ProgressBar progressBar;
    TextView textView;

    int progressBarStatus;

    private Handler progressBarHandler = new Handler();

    final static String PATH_DIR_MASTER = "MasterUltima";
    final static String FILE_NAME_SPV = "forwarding-service-testnet.spvchain";
    final static String FILE_NAME_WALLET = "forwarding-service-testnet.wallet";

    File fileWALLET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) this.findViewById(R.id.progressBar);
        textView = (TextView) this.findViewById(R.id.textView);

        progressBarStatus = 0;

        params = TestNet3Params.get();
        //params = MainNetParams.get();
        filePrefix = "forwarding-service-testnet";

        File mainDir = new File(Environment.getExternalStorageDirectory(), PATH_DIR_MASTER);
        if (!mainDir.exists())
            if (!mainDir.mkdir()) {
                Toast.makeText(this, "Errore nella crezione della cartella", Toast.LENGTH_SHORT);
            }
        File fileSPV = new File(Environment.getExternalStorageDirectory() + File.separator + PATH_DIR_MASTER, FILE_NAME_SPV);

        fileWALLET = new File(Environment.getExternalStorageDirectory() + File.separator + PATH_DIR_MASTER, FILE_NAME_WALLET);
        if (fileWALLET.exists())
            try {
                wallet = Wallet.loadFromFile(fileWALLET);
            } catch (UnreadableWalletException e) {
                e.printStackTrace();
                Log.d("Error", "Errore nel caricamento del Wallet dal file");
            }


        try {
            chainStore = new SPVBlockStore(params, fileSPV);
            chain = new BlockChain(params, chainStore);
            peerGroup = new PeerGroup(params, chain);
            peerGroup.addPeerDiscovery(new DnsDiscovery(params));
            wallet = new Wallet(params);

            chain.addWallet(wallet);
            peerGroup.addWallet(wallet);

            long now = new Date().getTime() / 1000;
            //final long oneMonthAgo = now - (86400 * 30);

            peerGroup.setFastCatchupTimeSecs(now);


            new Thread(new Runnable() {
                public void run() {
                    while (progressBarStatus < 100) {

                        downloadTracker = new DownloadProgressTracker() {

                            @Override
                            protected void progress(double pct, int blocksSoFar, Date date) {
                                progressBarStatus = (int) pct;
                            }

                            @Override
                            public void doneDownload() {
                                progressBarStatus = 100;
                            }

                        };

                        // your computer is too fast, sleep 1 second
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // Update the progress bar
                        progressBarHandler.post(new Runnable() {
                            public void run() {
                                if (progressBarStatus >= 100) {
                                    try {
                                        wallet.saveToFile(fileWALLET);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        Log.d("Error", "Errore nel salvataggio del Wallet nel file");
                                    }
                                    progressBar.setProgress(progressBarStatus);

                                    org.bitcoinj.core.Address sendToAddress = wallet.currentReceiveKey().toAddress(params);
                                    String message = sendToAddress.toString();

                                    //  textView.setText(message);

                                    //inserimento del bottono per lista bot al termine del caricamento della blockchain
                                    Button b=(Button)findViewById(R.id.button);
                                    textView.setVisibility(View.INVISIBLE);
                                    b.setVisibility(View.VISIBLE);

                                    b.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent i=new Intent(MainActivity.this,BotListActivity.class);
                                            startActivity(i);

                                        }
                                    });


                                } else {
                                    progressBar.setProgress(progressBarStatus);
                                    String perc = progressBarStatus + " %";
                                    textView.setText(perc);
                                }
                            }
                        });
                    }

                    // ok, file is downloaded,
                    if (progressBarStatus >= 100) {

                        // sleep 2 seconds, so that you can see the 100%
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // close the progress bar dialog
                        //progressBar.dismiss();
                        //RICHIAMARE LA PROSSIMA ACTIVITY
                    }
                }
            }).start();


            //new ProgressTask().execute();
            new DownloadBlockchain().execute();

        } catch (BlockStoreException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    private class DownloadBlockchain extends AsyncTask<Void, Integer, String> {

        @Override
        protected String doInBackground(Void... par) {
            peerGroup.startAsync();
            peerGroup.startBlockChainDownload(downloadTracker);

            try {
                downloadTracker.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            peerGroup.stopAsync();

            return null;
        }


        @Override
        protected void onPostExecute(String s) {

            /*
                   RICHIAMARE LA PROSSIMA ACTIVITY (LISTA DEI BOT) QUI E PASSARLE L'OGGETTO WalletAppKit
                   Ma non so come fare...forse Content Provider???
             */


        }


    }


}