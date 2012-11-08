
package com.will.draganddrop.demo;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.will.draganddrop.TouchInterceptorListView;
import com.will.draganddrop.TouchInterceptorListView.SwipeListener;

public class DragAndDropDemo extends ListActivity implements OnItemClickListener {
	private TouchInterceptorListView mListView;
	
	private DragaDropAdapter<String> mDragaDropAdapter ;
	private ActionMode mActionMode ;
	ArrayList<String> mArrayListOptions = new ArrayList<String>(Arrays.asList(new String[] {
			"1. One", "2. Two", "3. Three", "4. Four", "5. Five" ,
			"6. One", "7. Two", "8. Three", "9. Four", "10. Five", "BElephant" ,
			"11. One", "12. Two", "13. Three", "14. Four", "15. Five"}
	));
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mListView = (TouchInterceptorListView)getListView();
        mListView.setOnCreateContextMenuListener(this);
        
        //((TouchInterceptorListView) mListView).setDropListener(mDropListener);
       
        mActionMode = startActionMode(new AnActionModeOfEpicProportions());
        mDragaDropAdapter = new DragaDropAdapter<String>(this, mArrayListOptions , mActionMode);
        
        mListView.setAdapter(mDragaDropAdapter);
        mListView.setCacheColorHint(0);
        ((TouchInterceptorListView) mListView).setDragDropListener(mDragDropListener);
        mListView.setDivider(null);
        mListView.setSelector(R.drawable.list_selector_background);
        mListView.setSwipeListener(mSwipeListener);
        
        
//        mListView.setOnItemClickListener(this);
        
    }
    
    private final class AnActionModeOfEpicProportions implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.add("单/多选")
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        	
            menu.add("全选")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            
            menu.add("反选")
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            
            menu.add("全不选")
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        	if("单/多选".equals(item.toString())){
        		if(mDragaDropAdapter.isRadio()){
        			Toast.makeText(DragAndDropDemo.this, "Got click: 多选", Toast.LENGTH_SHORT).show();
        		}else{
        			Toast.makeText(DragAndDropDemo.this, "Got click: 单选", Toast.LENGTH_SHORT).show();
        		}
        		mDragaDropAdapter.setRadio(!mDragaDropAdapter.isRadio());
        	}else if("全选".equals(item.toString())){
        		mDragaDropAdapter.SelectedAll();
        	}else if("反选".equals(item.toString())){
        		mDragaDropAdapter.SelectedToogleAll();
        	}else if("全不选".equals(item.toString())){
        		mDragaDropAdapter.SelectedInit();
        	}
            Toast.makeText(DragAndDropDemo.this, "Got click: " + item, Toast.LENGTH_SHORT).show();
            mActionMode.setTitle("已选" + mDragaDropAdapter.getSelectedCount() + "个");
           // mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
        // clear the listeners so we won't get any more callbacks
    	TouchInterceptorListView lv = (TouchInterceptorListView) getListView();
        lv.setDragDropListener(null);
    }
    
    private TouchInterceptorListView.DragDropListener mDragDropListener =
        new TouchInterceptorListView.DragDropListener() {
        public void drop(int from, int to) {
        	String object = mDragaDropAdapter.getItem(from);
        	mDragaDropAdapter.remove(object);
        	mDragaDropAdapter.insert(object, to);
        }

		@Override
		public void drag(int from, int to) {
		}
    };
    
    /**
     * 点击/滑动事件
     */
    private SwipeListener mSwipeListener = new SwipeListener() {
		@Override
		public void onSwipeItem(boolean isRight, int pos) {
			Toast.makeText(DragAndDropDemo.this,"Swipe to " + (isRight ? "right" : "left") + " direction",
					Toast.LENGTH_SHORT).show();
        	mDragaDropAdapter.remove(pos);
		}
		@Override
		public void onClickItem(int pos) {
			// TODO Auto-generated method stub
			mDragaDropAdapter.SelectedToogle(mArrayListOptions.get(pos));
		}
	};

	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
	}
}