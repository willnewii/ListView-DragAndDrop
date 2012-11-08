package com.will.draganddrop.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class DragaDropAdapter<T> extends ArrayAdapter<T> {
	
	private LayoutInflater inflater ;
	private List<T> mList ;
	
	//是否是单选
	private boolean isRadio  ;
	//存储对象是否被选中
	private HashMap<T, Boolean> hashMap ;
	
	private ActionMode actionMode ;
	private final String TAG = "DragaDropAdapter";
	
	public DragaDropAdapter(Context context, List<T> objects) {
		super(context, 0, 0, objects);
		inflater = LayoutInflater.from(context);
		mList = objects ;
		SelectedInit();
	}
	
	public DragaDropAdapter(Context context, List<T> objects , ActionMode actionMode) {
		super(context, 0, 0, objects);
		inflater = LayoutInflater.from(context);
		mList = objects ;
		this.actionMode = actionMode ;
		SelectedInit();
	}
	
	public boolean isRadio() {
		return isRadio;
	}
	public void setRadio(boolean isRadio) {
		this.isRadio = isRadio;
		SelectedInit();
	}

	/**
	 * 初始化 默认为False
	 */
	public void SelectedInit(){
		if(hashMap == null)
			hashMap = new HashMap<T, Boolean>();
		for(T t : mList){
			hashMap.put(t, false);
		}
		updateActionBar();
		notifyDataSetChanged();
	}
	
	/**
	 * 全选
	 */
	public void SelectedAll(){
		for(T t : mList){
			hashMap.put(t, true);
		}
		updateActionBar();
		notifyDataSetChanged();
	}
	
	/**
	 * 单个反选
	 */
	public void SelectedToogle(T t){
		if(isRadio)
			SelectedInit();
		
		hashMap.put(t, !hashMap.get(t));
		updateActionBar();
		notifyDataSetChanged();
	}
	
	/**
	 * 全部反选
	 */
	public void SelectedToogleAll(){
		for(T t : mList){
			hashMap.put(t, !hashMap.get(t));
		}
		updateActionBar();
		notifyDataSetChanged();
	}
	
	/**
	 * 清楚选中状态.
	 * @param position
	 */
	public void remove(int position){
		hashMap.remove(getItem(position));
    	remove(getItem(position));
    	updateActionBar();
	}
	
	/**
	 * 获取选中的队列
	 */
	public ArrayList<T> getSelectedList(){
		ArrayList<T> mSList = new ArrayList<T>();
		Iterator<Entry<T, Boolean>> iter = hashMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<T, Boolean> entry = (Map.Entry<T, Boolean>) iter.next(); 
			if(entry.getValue()){
				//被选中的对象.
				mSList.add(entry.getKey());
			}
		}
		return mSList ;
	}
	
	/**
	 * 获取选择的个数
	 */
	public int getSelectedCount(){
		int i = 0 ;
		Iterator<Entry<T, Boolean>> iter = hashMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<T, Boolean> entry = (Map.Entry<T, Boolean>) iter.next(); 
			if(entry.getValue()){
				i++ ;
			}
		}
		return i ;
	}
	
	/**
	 * 勾选后更新数值
	 */
	private void updateActionBar(){
		if(actionMode != null)
			actionMode.setTitle("已选" + getSelectedCount() + "个");
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder;
		
		if (row == null) {													
			row = inflater.inflate(R.layout.dragrow, parent, false);
			
			holder = new ViewHolder();
			holder.label = (TextView) row.findViewById(R.id.textView);
			holder.mCheckBox = (CheckBox) row.findViewById(R.id.item_cb);
			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}
		
		holder.label.setText((String)getItem(position));
		holder.mCheckBox.setChecked(hashMap.get(getItem(position)));
		
		return(row);
	}

    private static class ViewHolder {
        TextView label;
        CheckBox mCheckBox ;
    }
	
}
