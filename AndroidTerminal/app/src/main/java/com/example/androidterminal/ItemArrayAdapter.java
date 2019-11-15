package com.example.androidterminal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class ItemArrayAdapter extends ArrayAdapter<String[]> {
	private List<String[]> dataList = new ArrayList<String[]>();

    static class ItemViewHolder {
        TextView header;
        TextView datapoint;
    }

    public ItemArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

	@Override
	public void add(String[] object) {
		dataList.add(object);
		super.add(object);
	}

    @Override
	public int getCount() {
		return this.dataList.size();
	}

    @Override
	public String[] getItem(int index) {
		return this.dataList.get(index);
	}

    @Override
    public void clear() {
        dataList.clear();
        super.clear();
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
        ItemViewHolder viewHolder;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.item_layout, parent, false);
            viewHolder = new ItemViewHolder();
            viewHolder.header = row.findViewById(R.id.header);
            viewHolder.datapoint = row.findViewById(R.id.datapoint);
            row.setTag(viewHolder);
		} else {
            viewHolder = (ItemViewHolder)row.getTag();
        }
        String[] stat = getItem(position);
        viewHolder.header.setText(stat[0]);
        viewHolder.datapoint.setText(stat[1]);
		return row;
	}
}
