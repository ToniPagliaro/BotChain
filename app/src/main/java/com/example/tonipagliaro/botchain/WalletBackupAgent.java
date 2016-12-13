package com.example.tonipagliaro.botchain;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import org.bitcoinj.core.Coin;

import java.io.IOException;

/**
 * Created by giuse on 09/12/2016.
 */
public class WalletBackupAgent extends BackupAgentHelper {
    static final String FILES_BACKUP_KEY = "wallet_files";

    @Override
    public void onCreate() {
        FileBackupHelper helper = new FileBackupHelper(this,
                ApplicationState.current.walletFile.getName());
        addHelper(FILES_BACKUP_KEY, helper);
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
                         ParcelFileDescriptor newState) throws IOException {
        // Blocca fino a quando il BackupManager non ha finito il backup
        synchronized (ApplicationState.walletFileLock) {
            // Ora portiamo un file di backup nel cloud. Il file principale è uno solo
            // nel telefono. Così non dobbiamo vederlo quando lo rimpiazziamo nel cloud.
            Log.d("App", "Eseguo il backup del file nel cloud");
            super.onBackup(oldState, data, newState);
        }
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode,
                          ParcelFileDescriptor newState) throws IOException {
        // Blocca fino a quando il BackupManager non ha finito di leggere i files.
        synchronized (ApplicationState.walletFileLock) {
            // Assicurati che il wallet nel cellulare ha zero balance.
            if (ApplicationState.current.wallet.getBalance().compareTo(Coin.ZERO) > 0) {
                Log.d("App", "Il Wallet nel cellulare presenta dei bitcoin. Non eseguo il ripristino.");
                return;
            }
            Log.d("App", "Ripristino del wallet da backup.");
            super.onRestore(data, appVersionCode, newState);
        }
    }
}
