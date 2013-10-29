/*
 * Copyright 2013 Samsung Developer Relations Team (MSCSEA)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.mscsea.mediashare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PhoneAdapter extends BaseAdapter {

	private String[] items;
	private LayoutInflater inflater;
	private Context context;
	
	public PhoneAdapter(Context context, String[] items) {
		this.context = context;
		this.items = items;
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		if (items != null) {
			return items.length;
		}
		
		return 0;
	}

	@Override
	public String getItem(int position) {
		if (items != null) {
			return items[position];
		}
		
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_device, parent, false);
			holder = new ViewHolder();
		    holder.icon = (ImageView) convertView.findViewById(R.id.icon);
		    holder.info1 = (TextView) convertView.findViewById(R.id.info1);
		    holder.info2 = (TextView) convertView.findViewById(R.id.info2);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.icon.setImageResource(R.drawable.ic_folder);
		holder.info1.setText(getItem(position));
		holder.info2.setText(context.getText(R.string.folder));
		
		return convertView;
	}
	
	private class ViewHolder {
		public ImageView icon;
		public TextView info1;
		public TextView info2;
	}
}
