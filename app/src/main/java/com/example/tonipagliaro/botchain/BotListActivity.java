package com.example.tonipagliaro.botchain;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import java.util.ArrayList;
import java.util.List;

public class BotListActivity extends AppCompatActivity {
    ArrayList<String> indirizzi=new ArrayList<String>();
    static ArrayList<String> indirizziAttivi=new ArrayList<String>();


    static ApplicationState appState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        appState = (ApplicationState) getApplication();

        indirizzi=appState.indirizzi;

        appState.wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                Coin value=tx.getValueSentToMe(wallet);
                try {
                   String message= readOpReturn(tx);


                    for(String s : indirizziAttivi)
                        Log.d("App","indirizzo attivo   "+s);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });



        setContentView(R.layout.activity_bot_list);
    }



    public static String readOpReturn(Transaction tx) throws Exception {
        List<TransactionOutput> ti = tx.getOutputs();
        String mess="";
        Address from = null;
        for (TransactionOutput t : ti) {
            Script script = t.getScriptPubKey();

            //prelievo dell'indirizzo
            if (t.getAddressFromP2PKHScript(appState.wallet.getParams()) != null) {
                from = t.getAddressFromP2PKHScript(appState.wallet.getParams());
            }


            if (script.isOpReturn()) {
                byte[] message = new byte[script.getProgram().length-2];

                System.arraycopy(script.getProgram(), 2, message, 0, script.getProgram().length - 2);

                 mess = new String(message);
                Log.d("App","Op_return   "+mess);

            }
            switch(mess) {
                case "ping_ok":
                    indirizziAttivi.add(from.toString());

            }

        }
        return mess;
    }

    /*la gestione della risposta dal bot deve essere fatta in readOpReturn o in OnCoinsReceived ???
    in onCOinsReceived non è visibile l'indirizzo del bot che ha inviato la risposta a meno che non viene restituita da readOpReturn insieme al valore del messaggio
     */

public ArrayList<String> getIndirizzi(){
    return indirizzi;
}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
