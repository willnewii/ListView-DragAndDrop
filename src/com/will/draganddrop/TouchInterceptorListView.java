/*
 * Copyright (C) 2011 Kevin Barry, TeslaCoil Software
 * Based on work Copyright (C) 2008, 2009, 2010, 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.will.draganddrop;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.will.draganddrop.demo.R;

public class TouchInterceptorListView extends ListView {
    
    private ImageView mDragView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    /**
     * At which position is the item currently being dragged. Note that this
     * takes in to account header items.
     */
    private int mDragPos;
    /**
     * At which position was the item being dragged originally
     */
    private int mSrcDragPos;
    private int mDragPointX;    // at what x offset inside the item did the user grab it
    private int mDragPointY;    // at what y offset inside the item did the user grab it
    private int mXOffset;  // the difference between screen coordinates and coordinates in this view
    private int mYOffset;  // the difference between screen coordinates and coordinates in this view
    private DragDropListener mDragDropListener;
    private int mUpperBound;
    private int mLowerBound;
    private int mHeight;
    
    /* REMOVEMODE
     * Google's TouchInterceptor has this code for removing items.
     * It seems interesting and useful, however it doesn't seem to fully work right now.
     * 
     * Commented out for now, hope to look at it again some day
     */
//    private RemoveListener mRemoveListener;
//    private GestureDetector mGestureDetector;
//    private static final int REMOVE_MODE_FLING = 0;
//    private static final int REMOVE_MODE_SLIDE = 1;
//    private static final int REMOVE_MODE_TRASH = 2;
//    private int mRemoveMode = -1;
    
    private Rect mTempRect = new Rect();
    private Bitmap mDragBitmap;
    private final int mTouchSlop;
    private int mItemHeightNormal;
    private int mItemHeightExpanded;
    private int mItemHeightHalf;
    private Drawable mTrashcan;
    
	private int REL_SWIPE_MIN_DISTANCE;
	private int REL_SWIPE_MAX_OFF_PATH;
	private int REL_SWIPE_THRESHOLD_VELOCITY;
	private SwipeListener mSwipeListener ;

    public TouchInterceptorListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        Resources res = getResources();
        mItemHeightNormal = res.getDimensionPixelSize(R.dimen.normal_height);
        mItemHeightHalf = mItemHeightNormal / 2;
        mItemHeightExpanded = res.getDimensionPixelSize(R.dimen.expanded_height);

        //手势滑动部分.
		DisplayMetrics dm = getResources().getDisplayMetrics();
		REL_SWIPE_MIN_DISTANCE = (int) (25.0f * dm.densityDpi / 160.0f + 0.5);
		REL_SWIPE_MAX_OFF_PATH = (int) (25.0f * dm.densityDpi / 160.0f + 0.5);
		REL_SWIPE_THRESHOLD_VELOCITY = (int) (25.0f * dm.densityDpi / 160.0f + 0.5);
        
		final GestureDetector gestureDetector = new GestureDetector(context ,new MyGestureDetector());

		this.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//    	 REMOVEMODE
//        if (mRemoveListener != null && mGestureDetector == null) {
//            if (mRemoveMode == REMOVE_MODE_FLING) {
//                mGestureDetector = new GestureDetector(getContext(), new SimpleOnGestureListener() {
//                    @Override
//                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
//                            float velocityY) {
//                        if (mDragView != null) {
//                            if (velocityX > 1000) {
//                                Rect r = mTempRect;
//                                mDragView.getDrawingRect(r);
//                                if ( e2.getX() > r.right * 2 / 3) {
//                                    // fast fling right with release near the right edge of the screen
//                                    stopDragging();
//                                    mRemoveListener.remove(mSrcDragPos);
//                                    unExpandViews(true);
//                                }
//                            }
//                            // flinging while dragging should have no effect
//                            return true;
//                        }
//                        return false;
//                    }
//                });
//            }
//        }
    	
        if (mDragDropListener != null) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    int x = (int) ev.getX();
                    int y = (int) ev.getY();
                    int itemnum = pointToPosition(x, y);
                    if (itemnum == AdapterView.INVALID_POSITION || itemnum < this.getHeaderViewsCount()) {
                        break;
                    }
                    ViewGroup item = (ViewGroup) getChildAt(itemnum - getFirstVisiblePosition());
                    mDragPointX = x - item.getLeft();
                    mDragPointY = y - item.getTop();
                    mXOffset = ((int)ev.getRawX()) - x;
                    mYOffset = ((int)ev.getRawY()) - y;
                    // The left side of the item is the grabber for dragging the item
                    if (x < 64) {
                        item.setDrawingCacheEnabled(true);
                        // Create a copy of the drawing cache so that it does not get recycled
                        // by the framework when the list tries to clean up memory
                        Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());
                        startDragging(bitmap, x, y);
                        mDragPos = itemnum;
                        mSrcDragPos = mDragPos;
                        mHeight = getHeight();
                        int touchSlop = mTouchSlop;
                        mUpperBound = Math.min(y - touchSlop, mHeight / 3);
                        mLowerBound = Math.max(y + touchSlop, mHeight * 2 /3);
                        return false;
                    }
                    stopDragging();
                    break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }
    
    /*
     * pointToPosition() doesn't consider invisible views, but we
     * need to, so implement a slightly different version.
     */
    private int myPointToPosition(int x, int y) {

        if (y < 0) {
            // when dragging off the top of the screen, calculate position
            // by going back from a visible item
            int pos = myPointToPosition(x, y + mItemHeightNormal);
            if (pos > 0) {
                return pos - 1;
            }
        }

        Rect frame = mTempRect;
        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            child.getHitRect(frame);
            if (frame.contains(x, y)) {
                return getFirstVisiblePosition() + i;
            }
        }
        return INVALID_POSITION;
    }
    
    private int getItemForPosition(int y) {
        int adjustedy = y - mDragPointY - mItemHeightHalf;
        int pos = myPointToPosition(0, adjustedy);
        if (pos >= 0) {
            if (pos <= mSrcDragPos) {
                pos += 1;
            }
        } else if (adjustedy < 0) {
            // this shouldn't happen anymore now that myPointToPosition deals
            // with this situation
            pos = 0;
        }
        return pos;
    }
    
    private void adjustScrollBounds(int y) {
        if (y >= mHeight / 3) {
            mUpperBound = mHeight / 3;
        }
        if (y <= mHeight * 2 / 3) {
            mLowerBound = mHeight * 2 / 3;
        }
    }

    /*
     * Restore size and visibility for all listitems
     */
    private void unExpandViews(boolean deletion) {
        for (int i = 0;; i++) {
            View v = getChildAt(i);
            if (v == null) {
                if (deletion) {
                    // HACK force update of mItemCount
                    int position = getFirstVisiblePosition();
                    int y = getChildAt(0).getTop();
                    setAdapter(getAdapter());
                    setSelectionFromTop(position, y);
                    // end hack
                }
                try {
                    layoutChildren(); // force children to be recreated where needed
                    v = getChildAt(i);
                } catch (IllegalStateException ex) {
                    // layoutChildren throws this sometimes, presumably because we're
                    // in the process of being torn down but are still getting touch
                    // events
                }
                if (v == null) {
                    return;
                }
            }
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = mItemHeightNormal;
            v.setLayoutParams(params);
            v.setVisibility(View.VISIBLE);
        }
    }
    
    /* Adjust visibility and size to make it appear as though
     * an item is being dragged around and other items are making
     * room for it:
     * If dropping the item would result in it still being in the
     * same place, then make the dragged listitem's size normal,
     * but make the item invisible.
     * Otherwise, if the dragged listitem is still on screen, make
     * it as small as possible and expand the item below the insert
     * point.
     * If the dragged item is not on screen, only expand the item
     * below the current insertpoint.
     */
    private void doExpansion() {
        int childnum = mDragPos - getFirstVisiblePosition();
        if (mDragPos > mSrcDragPos) {
            childnum++;
        }
        int numheaders = getHeaderViewsCount();

        View first = getChildAt(mSrcDragPos - getFirstVisiblePosition());
        for (int i = 0;; i++) {
            View vv = getChildAt(i);
            if (vv == null) {
                break;
            }

            int height = mItemHeightNormal;
            int visibility = View.VISIBLE;
            if (mDragPos < numheaders && i == numheaders) {
                // dragging on top of the header item, so adjust the item below
                // instead
                if (vv.equals(first)) {
                    visibility = View.INVISIBLE;
                } else {
                    height = mItemHeightExpanded;
                }
            } else if (vv.equals(first)) {
                // processing the item that is being dragged
                if (mDragPos == mSrcDragPos || getPositionForView(vv) == getCount() - 1) {
                    // hovering over the original location
                    visibility = View.INVISIBLE;
                } else {
                    // not hovering over it
                    // Ideally the item would be completely gone, but neither
                    // setting its size to 0 nor settings visibility to GONE
                    // has the desired effect.
                    height = 1;
                }
            } else if (i == childnum) {
                if (mDragPos >= numheaders && mDragPos < getCount() - 1) {
                    height = mItemHeightExpanded;
                }
            }
            ViewGroup.LayoutParams params = vv.getLayoutParams();
            params.height = height;
            vv.setLayoutParams(params);
            vv.setVisibility(visibility);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//    	 REMOVEMODE
//        if (mGestureDetector != null) {
//            mGestureDetector.onTouchEvent(ev);
//        }
        if ((mDragDropListener != null) && mDragView != null) {
            int action = ev.getAction(); 
            switch (action) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    Rect r = mTempRect;
                    mDragView.getDrawingRect(r);
                    stopDragging();
//                    REMOVEMODE
//                    if (mRemoveMode == REMOVE_MODE_SLIDE && ev.getX() > r.right * 3 / 4) {
//                        if (mRemoveListener != null) {
//                            mRemoveListener.remove(mSrcDragPos);
//                        }
//                        unExpandViews(true);
//                    } else {
                        if (mDragDropListener != null && mDragPos >= 0 && mDragPos < getCount()) {
                        	mDragDropListener.drop(mSrcDragPos, mDragPos);
                        }
                        unExpandViews(false);
//                    }
                    break;
                    
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    int x = (int) ev.getX();
                    int y = (int) ev.getY();
                    dragView(x, y);
                    int itemnum = getItemForPosition(y);
                    if (itemnum >= 0) {
                        if (action == MotionEvent.ACTION_DOWN || itemnum != mDragPos) {
                            if (mDragDropListener != null) {
                            	mDragDropListener.drag(mDragPos, itemnum);
                            }
                            mDragPos = itemnum;
                            doExpansion();
                        }
                        int speed = 0;
                        adjustScrollBounds(y);
                        if (y > mLowerBound) {
                            // scroll the list up a bit
                            if (getLastVisiblePosition() < getCount() - 1) {
                                speed = y > (mHeight + mLowerBound) / 2 ? 16 : 4;
                            } else {
                                speed = 1;
                            }
                        } else if (y < mUpperBound) {
                            // scroll the list down a bit
                            speed = y < mUpperBound / 2 ? -16 : -4;
                            if (getFirstVisiblePosition() == 0
                                    && getChildAt(0).getTop() >= getPaddingTop()) {
                                // if we're already at the top, don't try to scroll, because
                                // it causes the framework to do some extra drawing that messes
                                // up our animation
                                speed = 0;
                            }
                        }
                        if (speed != 0) {
                            smoothScrollBy(speed, 30);
                        }
                    }
                    break;
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }
    
    private void startDragging(Bitmap bm, int x, int y) {
        stopDragging();

        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowParams.x = x - mDragPointX + mXOffset;
        mWindowParams.y = y - mDragPointY + mYOffset;

        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;
        
        Context context = getContext();
        ImageView v = new ImageView(context);
        //int backGroundColor = context.getResources().getColor(R.color.dragndrop_background);
        //v.setBackgroundColor(backGroundColor);
        v.setBackgroundResource(R.drawable.draganddrop_tile_drag);
        v.setPadding(0, 0, 0, 0);
        v.setImageBitmap(bm);
        mDragBitmap = bm;

        mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(v, mWindowParams);
        mDragView = v;
    }
    
    private void dragView(int x, int y) {
//    	REMOVEMODE
//        if (mRemoveMode == REMOVE_MODE_SLIDE) {
//            float alpha = 1.0f;
//            int width = mDragView.getWidth();
//            if (x > width / 2) {
//                alpha = ((float)(width - x)) / (width / 2);
//            }
//            mWindowParams.alpha = alpha;
//        }
//
//        if (mRemoveMode == REMOVE_MODE_FLING || mRemoveMode == REMOVE_MODE_TRASH) {
//            mWindowParams.x = x - mDragPointX + mXOffset;
//        } else {
            mWindowParams.x = 0;
//        }
        mWindowParams.y = y - mDragPointY + mYOffset;
        mWindowManager.updateViewLayout(mDragView, mWindowParams);

        if (mTrashcan != null) {
            int width = mDragView.getWidth();
            if (y > getHeight() * 3 / 4) {
                mTrashcan.setLevel(2);
            } else if (width > 0 && x > width / 4) {
                mTrashcan.setLevel(1);
            } else {
                mTrashcan.setLevel(0);
            }
        }
    }
    
    private void stopDragging() {
        if (mDragView != null) {
            mDragView.setVisibility(GONE);
            WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(mDragView);
            mDragView.setImageDrawable(null);
            mDragView = null;
        }
        if (mDragBitmap != null) {
            mDragBitmap.recycle();
            mDragBitmap = null;
        }
        if (mTrashcan != null) {
            mTrashcan.setLevel(0);
        }
    }

//    REMOVEMODE
//    public void setTrashcan(Drawable trash) {
//        mTrashcan = trash;
//        mRemoveMode = REMOVE_MODE_TRASH;
//    }
    
//	public abstract void getSwipeItem(boolean isRight, int position);
//
//	public abstract void onItemClickListener(ListAdapter adapter, int position);
    
    /**
     * 手势滑动部分
     * @author Will
     */
    class MyGestureDetector extends SimpleOnGestureListener {

		private int temp_position = -1;

		// Detect a single-click and call my own handler.
		@Override
		public boolean onSingleTapUp(MotionEvent e) {

			int pos = pointToPosition((int) e.getX(), (int) e.getY());
			mSwipeListener.onClickItem(pos);
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			temp_position = pointToPosition((int) e.getX(), (int) e.getY());
			return super.onDown(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			//检测垂直偏移量,如果动作过斜,不识别.
			if (Math.abs(e1.getY() - e2.getY()) > REL_SWIPE_MAX_OFF_PATH)
				return false;
			//检测位移和速率.
			if (e1.getX() - e2.getX() > REL_SWIPE_MIN_DISTANCE && Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {

				int pos = TouchInterceptorListView.this.pointToPosition((int) e1.getX(), (int) e2.getY());

				if (pos >= 0 && temp_position == pos){
				}
				mSwipeListener.onSwipeItem(false, pos);
			} else if (e2.getX() - e1.getX() > REL_SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {

				int pos = TouchInterceptorListView.this.pointToPosition((int) e1.getX(), (int) e2.getY());
				if (pos >= 0 && temp_position == pos){
				}
				mSwipeListener.onSwipeItem(true, pos);
			}
			return false;
		}

	}

    public void setDragDropListener(DragDropListener l) {
    	mDragDropListener = l;
    }
    
    public void setSwipeListener(SwipeListener mSwipeListener) {
        this.mSwipeListener = mSwipeListener;
    }
    
    public interface DragDropListener {
        void drag(int from, int to);
        
        void drop(int from, int to);
    }
    
    public interface RemoveListener {
        void remove(int which);
    }
    
    public interface SwipeListener {
        
        void onSwipeItem(boolean isRight, int pos);
        
        void onClickItem(int pos);
    }
    
}
