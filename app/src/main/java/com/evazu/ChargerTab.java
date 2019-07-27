package com.evazu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

public class ChargerTab extends Fragment implements View.OnClickListener{

    CardView one, two, four, six;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_charger, container, false);
        one = rootView.findViewById(R.id.evazu_one);
        two = rootView.findViewById(R.id.evazu_two);
        four = rootView.findViewById(R.id.evazu_four);
        six = rootView.findViewById(R.id.evazu_six);

        one.setOnClickListener(this);
        two.setOnClickListener(this);
        four.setOnClickListener(this);
        six.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.evazu_one:
            case R.id.evazu_two:
            case R.id.evazu_four:
            case R.id.evazu_six:
                startActivity(new Intent(getActivity(), ThankYou.class));
                Animatoo.animateSlideLeft(getActivity());
                break;
            default:
                return;
        }

    }
}
