package com.example.shashi.thedementorsnetwork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

//import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;

@IgnoreExtraProperties
class User {

    public String username;
    public double lati;
    public double longi;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, double lati, double longi) {
        this.username = username;
        this.lati = lati;
        this.longi = longi;
    }

}

public class MainActivity extends AppCompatActivity {

    private static final String TAG ="MainActivity" ;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    Integer userId, userNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        userId = pref.getInt("key_name", -1);

        mDatabase.child("userNo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userNo = dataSnapshot.getValue(Integer.class);
                if (userId>userNo){
                    userId=-1;
                    editor.putInt("key_name", userId);
                    editor.commit();
                }
                Log.d(TAG,"Your user Id: " + userId +"\n"+"No of users is: "+userNo);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.d(TAG, "Failed to read value.");
            }
        });

        if (userId!=-1){
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        }

    }

    public void registerUser(View view) {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();

        userId = pref.getInt("key_name", -1);
//        userNo++;
        EditText eText = (EditText) findViewById(R.id.userName);
        String userName = eText.getText().toString();

        if(userId==-1){
            userNo++;
            userId=userNo;
            editor.putInt("key_name", userId);
            editor.putString("user_name", userName);
            editor.commit();
            mDatabase.child("userNo").setValue(userNo);
        }

        User user = new User(userName, 0.0, 0.0);

        database.getReference().child("users").child(Integer.toString(userId)).setValue(user);

        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);

    }
}
