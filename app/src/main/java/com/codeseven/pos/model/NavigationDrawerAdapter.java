package com.codeseven.pos.model;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codeseven.pos.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class NavigationDrawerAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<NavMenuItem> listDataHeader;
    private HashMap<NavMenuItem, List<NavMenuItem>> listDataChild;

    public NavigationDrawerAdapter(Context context, List<NavMenuItem> listDataHeader, HashMap<NavMenuItem, List<NavMenuItem>> listDataChild) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listDataChild;
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {

        if (this.listDataChild.get(this.listDataHeader.get(groupPosition)) == null)
            return 0;
        else
            return this.listDataChild.get(this.listDataHeader.get(groupPosition))
                    .size();    }

    @Override
    public NavMenuItem getGroup(int i) {
        return this.listDataHeader.get(i);
    }

    @Override
    public NavMenuItem getChild(int i, int i1) {
        return this.listDataChild.get(this.listDataHeader.get(i)).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean b, View convertView, ViewGroup viewGroup) {
        String headerTitle = getGroup(groupPosition).menuName;
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group_header, null);
        }

        TextView lblListHeader = convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        ImageView ivItem = convertView.findViewById(R.id.iv_item_image);

        if((getGroup(groupPosition).imageurl).length()>0)
        {
            Picasso.get().load(getGroup(groupPosition).imageurl).into(ivItem);
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {

        final String childText = getChild(groupPosition, childPosition).menuName;

        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.list_group_child, null);
        }
        TextView txtListChild = view.findViewById(R.id.lblListItem);

        txtListChild.setText(childText);
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
