package com.evazu;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class NavigationDrawerExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _headerList;
    private HashMap<String, List<String>> _childList = new HashMap<>();
    private HashMap<String, Drawable> _icons = new HashMap<>();
    private LayoutInflater mLayoutInflater;

    public NavigationDrawerExpandableListAdapter(Context context, List<String> headerList, HashMap<String, List<String>> childList, HashMap<String, Drawable> icons) {
        this._context = context;
        this._headerList = headerList;
        this._childList = childList;
        this._icons = icons;
        mLayoutInflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getGroupCount() {
        return _headerList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if(groupPosition == (_headerList.size() - 1)) {
            return _childList.get(_headerList.get(groupPosition)).size();
        }
        else
            return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return _headerList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if(getChildrenCount(groupPosition) > 0) {
            return _childList.get(_headerList.get(groupPosition)).get(childPosition);
        }
        else
            return null;

    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.navigation_menu_group_list, null);
        }
        TextView listTitleTextView = convertView
                .findViewById(R.id.groupTxtView);
        listTitleTextView.setText(listTitle);
        listTitleTextView.setCompoundDrawablesWithIntrinsicBounds(_icons.get(_headerList.get(groupPosition)), null, null, null);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (groupPosition == (_headerList.size() - 1)) {
            final String expandedListText = (String) getChild(groupPosition, childPosition);
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.navigation_menu_child_list, null);
            }
            TextView expandedListTextView = convertView
                    .findViewById(R.id.navigationListTV);
            expandedListTextView.setText(expandedListText);
            return convertView;
        } else {
            return null;
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
