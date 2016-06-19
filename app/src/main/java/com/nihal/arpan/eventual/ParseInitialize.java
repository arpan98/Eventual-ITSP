package com.nihal.arpan.eventual;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Log;
import android.util.Patterns;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.SaveCallback;

import java.util.regex.Pattern;

/**
 * Created by arpan on 12/1/16.
 */
public class ParseInitialize extends android.app.Application {

    Boolean put;

    @Override
    public void onCreate() {
        super.onCreate();
        put=false;
        //This will only be called once in your app's entire lifecycle.
        Parse.initialize(this, "09X6fff2y4FsZgQwBLe3LaVOX3QVNNwZe0eOGbBb", "wc5raEbirQuUBLrAPAArVPnidPUPSMjTzX5Xe5Wg");
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        try {
            Account[] accounts = AccountManager.get(ParseInitialize.this).getAccounts();
            for (Account account : accounts) {
                if (emailPattern.matcher(account.name).matches() && !put) {
                    String possibleEmail = account.name;
                    String username = possibleEmail.split("@")[0];
                    Log.d("swag", username);
                    installation.put("username",username);
                    put = true;
                }
            }
        }
        catch(Exception e) {
            Log.d("swag" , "Error in getting username");
        }
        installation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.d("swag","INSTALL DONE",e);
            }
        });
    }
}
