package com.evazu;


import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EvazuDatabase {

    private DatabaseReference mDatabaseRef;

    private final String TAG = "EvazuDatabase";

    public EvazuDatabase() {
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    public void getValue(String name, DatabaseTask task) {
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(name).getValue() != null) {

                    task.getResult(dataSnapshot.child(name).getValue().toString());
                }
                else {
                    task.getResult(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "Cancelled!");
            }
        });
    }

}
