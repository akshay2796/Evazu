package com.evazu;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GetRentalStatus {

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    int verification = 0;
    String TAG = "GetRentalStatus";

    public GetRentalStatus() {
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid());
        }
    }

    public void getResult(RentalInterface rentalInterface) {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("verification").getValue() != null)
                    verification = Integer.parseInt(dataSnapshot.child("verification").getValue().toString());

                rentalInterface.onCallBack(verification == 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });
    }
}
