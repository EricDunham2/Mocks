package com.projects.mocks.mocks;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.tech.NfcF;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class BeamActivity extends AppCompatActivity
{

    NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    ArrayList<String> slist;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        settings = getSharedPreferences("settings", CONTEXT_RESTRICTED);

        if(settings.getBoolean("theme", false)){
            setTheme(R.style.AppThemeDark);
        }
        else{
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beam);

        settings = getSharedPreferences("settings", CONTEXT_RESTRICTED);

        //get information from inner db, and store it in an array list
        slist = new ArrayList<>();

        slist.add(settings.getString("username", ""));

        MainActivity.db.open();
        Cursor c = MainActivity.db.getAllPortfolio();
        if(c!= null)
        {
            if(c.moveToFirst()){
                c.moveToFirst();
                while(!c.isAfterLast())
                {
                    slist.add(c.getString(c.getColumnIndex("Symbol")));
                    c.moveToNext();
                }
            }
        }
        MainActivity.db.close();

        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        }
        catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray = new IntentFilter[] {ndef, };

        techListsArray = new String[][] { new String[] { NfcF.class.getName() } };



        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        assert nfcAdapter != null;

        nfcAdapter.setNdefPushMessageCallback(
                new NfcAdapter.CreateNdefMessageCallback() {
                    public NdefMessage createNdefMessage(NfcEvent event) {
                        return createMessage();
                    }
                }, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        //See if app got called by AndroidBeam intent.
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            extractPayload(intent);
        }
    }

    private NdefMessage createMessage() {
        String mimeType = "text/plain";
        byte[] mimeBytes = mimeType.getBytes();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        for (String element : slist) {
            try
            {
                out.writeUTF(element);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        byte[] payLoad = baos.toByteArray();

        return new NdefMessage(
                new NdefRecord[]{
                        new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                                mimeBytes,
                                null,
                                payLoad),
                        NdefRecord.createApplicationRecord("text")
                });
    }

    private void extractPayload(Intent beamIntent) {
        Parcelable[] messages = beamIntent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage message = (NdefMessage) messages[0];
        NdefRecord record = message.getRecords()[0];

        ByteArrayInputStream bais = new ByteArrayInputStream(record.getPayload());
        DataInputStream in = new DataInputStream(bais);
        ArrayList<String> al2 = new ArrayList<>();
        try
        {
            while (in.available() > 0) {
                String element = in.readUTF();
                al2.add(element);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        if(al2.size() > 0)
        {
            String name = al2.get(0);

            TextView tvPortfolioSummary = (TextView)findViewById(R.id.tvPortfolioSummary);

            tvPortfolioSummary.setText(name + "'s Portfolio");

            al2.remove(0);

            ListView lv = (ListView) findViewById(R.id.lvSymbols);
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, al2);
            lv.setAdapter(adapter);
        }
    }
}
