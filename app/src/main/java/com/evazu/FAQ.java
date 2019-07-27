package com.evazu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ExpandableListView;
import android.widget.ImageView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FAQ extends AppCompatActivity {

    private ExpandableListViewAdapter adapter;
    private ExpandableListView listView;
    private List<String> _headerList;
    private HashMap<String, String> _listDataView = new HashMap<>();

    int lastGroup = -99;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        toolbar = findViewById(R.id.toolbar_faq);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        listView = findViewById(R.id.listView);

        prepareListData();

        adapter = new ExpandableListViewAdapter(this, _headerList, _listDataView);

        listView.setAdapter(adapter);

        listView.setOnGroupExpandListener(groupPosition -> {   //Fix when same group is clicked after collapsing, it is not expanding
            if(!(lastGroup == -99)) {
                listView.collapseGroup(lastGroup);
            }
            lastGroup = groupPosition;
        });
    }

    private void prepareListData() {
        _headerList = Arrays.asList(getResources().getStringArray(R.array.faq_questions));
        String[] ans = getResources().getStringArray(R.array.faq_answers);

        for(int i=0;i<_headerList.size();i++) {
            _listDataView.put(_headerList.get(i), ans[i]);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(this);
    }
}
