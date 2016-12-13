package com.example.tonipagliaro.botchain;

import android.app.Application;
import android.app.backup.BackupManager;
import android.os.Environment;
import android.util.Log;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.net.discovery.PeerDiscovery;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class ApplicationState extends Application {

    static final Object[] walletFileLock = new Object[0];

    private String filePrefix = "testnet";

    File walletFile;
    Wallet wallet;
    boolean walletShouldBeRebuilt = false;

    File keychainFile;

    NetworkParameters params = TestNet3Params.get();

    private PeerDiscovery peerDiscovery;
    private ArrayList<InetSocketAddress> isas = new ArrayList<InetSocketAddress>();

    SPVBlockStore chainStore;
    BlockChain chain;
    //Aggiunte
    PeerGroup peerGroup;

    private BackupManager backupManager;

    public static ApplicationState current;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("App", "Start app state");
        ApplicationState.current = this;
        backupManager = new BackupManager(this);

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

    /*
    public ArrayList<InetSocketAddress> discoverPeers() {
        if (isas.size() == 0) {
            try {
                long now = new Date().getTime() / 1000;
                isas.addAll(Arrays.asList(peerDiscovery.getPeers(VersionMessage.NODE_NETWORK, now, TimeUnit.SECONDS)));
                Collections.shuffle(isas); // try different order each time
            } catch (PeerDiscoveryException e) {
                Log.d("Wallet", "Couldn't discover peers.");
            }
        }
        Log.d("Wallet", "discoverPeers returning "+isas.size()+" peers");
        // shallow clone to prevent concurrent modification exceptions
        return (ArrayList<InetSocketAddress>) isas.clone();
    }
    */

}
