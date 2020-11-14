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

public class DriverLiveChatActivity extends AppCompatActivity {

    ListView messageListView;
    ArrayList<String> messages = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    EditText driverMessageEditText;
    String riderName;
    String driverName;
    Handler handler = new Handler();
    public void getMessage(){
        messageListView = (ListView)findViewById(R.id.messageListView);
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,messages);
        messageListView.setAdapter(arrayAdapter);

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
                                messages.add(driverName+": "+message.getString("message"));
                            }
                            else{
                                messages.add(riderName+": "+message.getString("message"));
                            }
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                    else if(objects.size()==0){
                        Toast.makeText(DriverLiveChatActivity.this, "No messages to show!", Toast.LENGTH_SHORT).show();
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
        liveChats.put("sender", driverName);
        liveChats.put("recepient",riderName);
        liveChats.put("message", driverMessageEditText.getText().toString());
        liveChats.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    Toast.makeText(DriverLiveChatActivity.this, "Message sent!", Toast.LENGTH_SHORT).show();
                    messages.add(driverName+": "+driverMessageEditText.getText().toString());
                    arrayAdapter.notifyDataSetChanged();
                    messageListView.setAdapter(arrayAdapter);
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
        setContentView(R.layout.activity_driver_live_chat);
        setTitle("Live Chat with your Rider");
        driverMessageEditText = (EditText)findViewById(R.id.driverMessageEditText);

        Intent intent = getIntent();
        driverName = intent.getStringExtra("driver");

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
        query.whereEqualTo("driverName", driverName);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null && objects.size()>0){
                    for(ParseObject object:objects){
                        riderName = object.getString("username");
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
                getMessage();
            }
        },3000);

    }
}