package com.vrexas.bitcointest5;
/*
 * Copyright 2013 Google Inc.
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import java.io.File;
import java.util.ArrayList;

/**
 * ForwardingService demonstrates basic usage of the library. It sits on the network and when it receives coins, simply
 * sends them onwards to an address given on the command line.
 */
public class BotMasterAndroid {

    NetworkParameters params;
    String filePrefix;
    private static Address botAddress1, botAddress2;
    private static WalletAppKit kit;

    private Context context;

    BotMasterAndroid(Context context) {
        this.context = context;
    }

    /*
     * Inizializza il portafoglio, la rete bitcoin, gli indirizzi dei bot e scarica la blockchain 
     */
    private void init() {

        params = TestNet3Params.get();
        filePrefix = "forwarding-service-testnet";

        //botAddress = Address.fromBase58(params, "mkXJLEzLrqkEHAU6XjnxcpBdAsvhfAj5ow");
        botAddress1 = new Address(params, "mgXaam8xQx1HiQnpKW5ana5jnsPEzc4uQZ");
        botAddress2 = new Address(params, "muMWvMjKBcbSorRNaMeQsWhg1oQ9S44LMz");

        File file = new File(Environment.getExternalStorageDirectory(), "/Master/");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Toast.makeText(context, "Errore nella creazione della directory", Toast.LENGTH_SHORT).show();
            }
        }

        Toast.makeText(context, "Directory creata correttamente", Toast.LENGTH_SHORT).show();


        kit = new WalletAppKit(params, file, filePrefix);


        //Log.d("Debug", "Sto scaricando la blockchain");


        //IDEALE ----> Almeno all'inizio utilizzare il task per scaricare la blockchain
        //new HttpAsyncTask().execute();

        //Meglio non metterli qua....
        kit.startAsync();
        kit.awaitRunning();

        //Log.d("Debug", "Ho scaricato la blockchain");
        //Toast.makeText(MainActivity.this, "Ho scaricato la blockchain", Toast.LENGTH_SHORT).show();


        kit.wallet().addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance) {
                Coin value = tx.getValueSentToMe(w);
                Toast.makeText(context, "Received tx for " + value.toFriendlyString() + ": " + tx, Toast.LENGTH_SHORT).show();

                Futures.addCallback(tx.getConfidence().getDepthFuture(1), new FutureCallback<TransactionConfidence>() {
                    @Override
                    public void onSuccess(TransactionConfidence result) {
                        Toast.makeText(context, "Transazione ricevuta con successo", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        // This kind of future can't fail, just rethrow in case something weird happens.
                        Toast.makeText(context, "Transazione non ricevuta", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        Address sendToAddress = kit.wallet().currentReceiveKey().toAddress(params);
        String string = "Indirizzo del Master: " + sendToAddress;
        String s2 = "Il Master ha: " +kit.wallet().getBalance().toFriendlyString();
        //Restituire Stringa??
    }


    /*
     * Invio di un comando a un bot
     */
    public static String sendCommand(String command, Address botAddress) throws Exception {

        byte[] hash = command.getBytes("UTF-8");

        Transaction transaction = new Transaction(kit.wallet().getParams());

        transaction.addOutput(Coin.MILLICOIN, botAddress);
        transaction.addOutput(Coin.ZERO, new ScriptBuilder().op(106).data(hash).build());

        SendRequest sendRequest = SendRequest.forTx(transaction);

        String string = new String(hash);
        System.out.println("Sending ... " +string);

        kit.wallet().completeTx(sendRequest);   // Could throw InsufficientMoneyException

        kit.peerGroup().setMaxConnections(1);
        kit.peerGroup().broadcastTransaction(sendRequest.tx);

        return transaction.getHashAsString();
    }


    /*
     * Invio di un comando a una lista di bot
     */
    public static String sendCommand(String command, ArrayList<Address> botAddressList) throws Exception {

        byte[] hash = command.getBytes("UTF-8");

        Transaction transaction = new Transaction(kit.wallet().getParams());

        for (Address address : botAddressList) {
            transaction.addOutput(Coin.MILLICOIN, address);
        }
        transaction.addOutput(Coin.ZERO, new ScriptBuilder().op(106).data(hash).build());

        SendRequest sendRequest = SendRequest.forTx(transaction);

        String string = new String(hash);
        System.out.println("Sending ... " +string);

        kit.wallet().completeTx(sendRequest);   // Could throw InsufficientMoneyException

        kit.peerGroup().setMaxConnections(botAddressList.size());
        kit.peerGroup().broadcastTransaction(sendRequest.tx);

        return transaction.getHashAsString();
    }


    /*
        L'ideale sarebbe usare questo Task ma occorre passare l'oggetto WalletAppKit alla prossima activity
     */
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //Toast.makeText(MainActivity.this, "Sto scaricando la blockchain", Toast.LENGTH_SHORT).show();
            kit.startAsync();
            kit.awaitRunning();
            return "Blockchain";
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(context, "Ho scaricato la blockchain" +s, Toast.LENGTH_SHORT).show();

            /*
                   RICHIAMARE LA PROSSIMA ACTIVITY (LISTA DEI BOT) QUI E PASSARLE L'OGGETTO WalletAppKit
                   Ma non so come fare...forse Content Provider???
             */

            Address sendToAddress = kit.wallet().currentReceiveKey().toAddress(params);
            String string = "Indirizzo del Master: " + sendToAddress;
            //textView.setText(string);

        }

    }

}