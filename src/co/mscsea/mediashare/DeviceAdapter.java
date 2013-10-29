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
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.samsung.android.sdk.mediacontrol.SmcDevice;
import com.squareup.picasso.Picasso;

public class DeviceAdapter extends BaseAdapter {

	private List<SmcDevice> items = new ArrayList<SmcDevice>();
	private LayoutInflater inflater;
	private byte[] lock = new byte[0];
	private Context context;
	
	public DeviceAdapter(Context context) {
		this.context = context;
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		synchronized (lock) {
			return items.size();
		}
	}

	@Override
	public SmcDevice getItem(int position) {
		synchronized (lock) {
			if (position >= 0 || position < items.size()) {
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
		
		SmcDevice item = getItem(position);
		Uri icon = item.getIconUri();
		if (icon == null) {
			holder.icon.setImageResource(R.drawable.ic_device);
		} else {
			Picasso.with(context).
					load(item.getIconUri()).
					placeholder(R.drawable.ic_device).
					error(R.drawable.ic_device).
					into(holder.icon);
		}
		holder.info1.setText(item.getName());
		holder.info2.setText(item.getIpAddress());
		
		return convertView;
	}
	
	public void setItems(List<SmcDevice> devices) {
		synchronized (lock) {
			if (devices != null) {
				items.clear();
				items.addAll(devices);
				notifyDataSetChanged();
			}
		}
	}
	
	public void addItem(SmcDevice device) {
		synchronized (lock) {
			for (int n = 0; n < items.size(); n++) {
				SmcDevice d = items.get(n);
				if (d.getId().equals(device.getId())) {
					items.remove(n);
					items.add(n, device);
					notifyDataSetChanged();
					return;
				}
			}
			
			items.add(device);
			notifyDataSetChanged();
		}
	}
	
	public void removeItem(SmcDevice device) {
		synchronized (lock) {
			for (int n = 0; n < items.size(); n++) {
				SmcDevice d = items.get(n);
				if (d.getId().equals(device.getId())) {
					items.remove(n);
					notifyDataSetChanged();
					return;
				}
			}
		}
	}
	
	private class ViewHolder {
		public ImageView icon;
		public TextView info1;
		public TextView info2;
	}
}
