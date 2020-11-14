package com.example.rapidcabs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class LiveChatActivity extends AppCompatActivity {

    ListView chatListView;
    ArrayList<String> chats = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    EditText messageEditText;
    String riderName;
    String driverName;
    Handler handler = new Handler();
    public void getChat(){
        chatListView = (ListView)findViewById(R.id.messageListView);
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,chats);
        chatListView.setAdapter(arrayAdapter);

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
                                chats.add(riderName+": "+message.getString("message"));
                            }
                            else{
                                chats.add(driverName+": "+message.getString("message"));
                            }
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                    else if(objects.size()==0){
                        Toast.makeText(LiveChatActivity.this, "No messages to show!", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    e.printStackTrace();
                }
            }
        });
    }
    public void sendMessage(View view){

            ParseObject liveChats = new ParseObject("liveChats");
            liveChats.put("sender", riderName);
            liveChats.put("recepient",driverName);
            liveChats.put("message", messageEditText.getText().toString());
            liveChats.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        Toast.makeText(LiveChatActivity.this, "Message sent!", Toast.LENGTH_SHORT).show();
                        chats.add(riderName+": "+messageEditText.getText().toString());
                        arrayAdapter.notifyDataSetChanged();
                        chatListView.setAdapter(arrayAdapter);
                    }
                    else{
                        e.printStackTrace();
                    }
                }
            });

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_chat);
        setTitle("Live Chat with your Driver");
        messageEditText = (EditText)findViewById(R.id.driverMessageEditText);

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