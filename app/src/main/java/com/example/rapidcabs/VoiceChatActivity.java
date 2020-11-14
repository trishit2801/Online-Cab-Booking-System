package com.example.rapidcabs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class VoiceChatActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 100;
    private TextView outputTextView;
    ListView voiceChatListView;
    ArrayList<String> voiceChats = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    String riderName,driverName;
    Handler handler = new Handler();
    public void getChat(){
        voiceChatListView = (ListView)findViewById(R.id.voiceChatListView);
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,voiceChats);
        voiceChatListView.setAdapter(arrayAdapter);

        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("liveChats");
        query1.whereEqualTo("sender", riderName);
        query1.whereEqualTo("recepient", driverName);

        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("liveChats");
        query2.whereEqualTo("sender", driverName);
        query2.whereEqualTo("recepient", riderName);

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery<ParseObject> query3 =  ParseQuery.or(queries);
        query3.orderByAscending("createdAt");
        query3.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    if(objects.size()>0){
                        for(ParseObject message:objects){
                            String sender = message.getString("sender");
                            if(sender.equals(ParseUser.getCurrentUser().getUsername())){
                                voiceChats.add(riderName+": "+message.getString("message"));
                            }
                            else{
                                voiceChats.add(driverName+": "+message.getString("message"));
                            }
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                    else if(objects.size()==0){
                        Toast.makeText(VoiceChatActivity.this, "No messages to show!", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    e.printStackTrace();
                }
            }
        });
    }
    public void sendVoice(View v)
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        try {
            startActivityForResult(intent, REQUEST_CODE);
        }
        catch (ActivityNotFoundException a) {
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    outputTextView.setText(result.get(0));
                    ParseObject liveChats = new ParseObject("liveChats");
                    liveChats.put("sender", riderName);
                    liveChats.put("recepient",driverName);
                    liveChats.put("message", outputTextView.getText().toString());
                    liveChats.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                Toast.makeText(VoiceChatActivity.this, "Message sent!", Toast.LENGTH_SHORT).show();
                                voiceChats.add(riderName+": "+outputTextView.getText().toString());
                                arrayAdapter.notifyDataSetChanged();
                                voiceChatListView.setAdapter(arrayAdapter);
                            }
                            else{
                                e.printStackTrace();
                            }
                        }
                    });

                }
                break;
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_chat);
        setTitle("Voice Chat with your Driver");
        outputTextView= (TextView) findViewById(R.id.outputTextView);

        Intent intent = getIntent();
        riderName = intent.getStringExtra("rider");

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
        query.whereEqualTo("username", riderName);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null && objects.size()>0){
                    for(ParseObject object:objects){
                        driverName = object.getString("driverName");
                    }
                }
                else{
                    e.printStackTrace();
                }
            }
        });

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getChat();
            }
        },3000);


    }
}