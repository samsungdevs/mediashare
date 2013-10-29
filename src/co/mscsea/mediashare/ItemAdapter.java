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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.samsung.android.sdk.mediacontrol.SmcItem;
import com.squareup.picasso.Picasso;

public class ItemAdapter extends BaseAdapter {

	private List<SmcItem> items = new ArrayList<SmcItem>();
	private LayoutInflater inflater;
	private Context context;
	private byte[] lock = new byte[0];
	
	public ItemAdapter(Context context) {
		this.context = context;
		inflater = LayoutInflater.from(context);
	}
	
	public void setItems(List<SmcItem> items) {
		synchronized (lock) {
			this.items.clear();
			this.items.addAll(items);
			notifyDataSetChanged();
		}
	}
	
	public void clear() {
		synchronized (lock) {
			this.items.clear();
			notifyDataSetChanged();
		}
	}
	
	@Override
	public int getCount() {
		synchronized (lock) {
			return items.size();
		}
	}

	@Override
	public SmcItem getItem(int position) {
		synchronized (lock) {
			if (position >= 0 && position < items.size()) {
				return items.get(position);
			}
			
			return null;
		}
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
		
		SmcItem item = getItem(position);
		holder.info1.setText(item.getTitle());
		if (item.getMediaType() == SmcItem.MEDIA_TYPE_ITEM_FOLDER) {
			holder.icon.setImageResource(R.drawable.ic_folder);
			holder.info2.setText("Folder");
		} else {
			Picasso.with(context).
					load(item.getThumbnail()).
					placeholder(R.drawable.ic_device).
					error(R.drawable.ic_device).
					into(holder.icon);
			holder.info2.setText(item.getMimeType());
		}
		
		return convertView;
	}
	
	private class ViewHolder {
		public ImageView icon;
		public TextView info1;
		public TextView info2;
	}
}
