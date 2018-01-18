package com.athira.callyourcontact;

import android.Manifest;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends ListActivity {
    ArrayList<String> contacts = new ArrayList<>();
    Cursor c;
    private Intent callIntent;
    ArrayAdapter<String> adapter;
    Utils utils = new Utils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FetchContactsAsync lca = new FetchContactsAsync();
        lca.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void callAlert(final int pos) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Call " + contacts.get(pos) + "?");
        alertDialog.setPositiveButton("CALL", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String[] split = contacts.get(pos).split("\n");
                callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + split[1]));
                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    utils.askPermission(MainActivity.this, "CALL_PHONE");
                } else
                    startActivity(callIntent);
            }
        });
        alertDialog.setNegativeButton("CANCEL", null);
        alertDialog.show();
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        callAlert(position);
    }

    private class FetchContactsAsync extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                utils.askPermission(MainActivity.this, "READ_CONTACTS");
            else {
                c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
                        null,
                        null,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            }
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {

            if (c != null) {
                while (c.moveToNext()) {
                    String contactName = c.getString(c
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phNumber = c.getString(c
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    contacts.add(contactName + "\n" + phNumber);
                }
                c.close();
                return contacts;
            } else
                return null;
        }


        @Override
        protected void onPostExecute(ArrayList<String> contacts) {
            // TODO Auto-generated method stub
            super.onPostExecute(contacts);
            if (contacts != null) {
                if (contacts.size() == 0)
                    Toast.makeText(getApplicationContext(), "No contacts found!", Toast.LENGTH_LONG).show();
                else {
                    ListView listview = getListView();
                    adapter = new ArrayAdapter<>(MainActivity.this,
                            android.R.layout.simple_list_item_1, contacts);
                    listview.setAdapter(adapter);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case 0:
                    startActivity(callIntent);
                    break;
                case 1:
                    new FetchContactsAsync().execute();
                    break;
            }
        } else {
            switch (requestCode) {
                case 0:
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.CALL_PHONE)) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.grant_permission), Toast.LENGTH_LONG).show();
                        utils.askPermission(MainActivity.this, "CALL_PHONE");
                    } else
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.denied_permission), Toast.LENGTH_LONG).show();
                    break;
                case 1:
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.READ_CONTACTS)) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.grant_permission), Toast.LENGTH_LONG).show();
                        utils.askPermission(MainActivity.this, "READ_CONTACTS");
                    } else
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.denied_permission), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}