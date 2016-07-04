package com.ileja.wifidisplay;

import android.app.Presentation;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.media.MediaRouter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PresentActivity extends AppCompatActivity {

    @Bind(R.id.mint)
    TextView mint;
    @Bind(R.id.sec)
    TextView sec;
    @Bind(R.id.top)
    LinearLayout top;
    @Bind(R.id.lv_display)
    ListView lv_display;
    private long timeusedinsec;
    private Display[] presentationDisplays;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    // 添加更新ui的代码
                    updateView();
                    mHandler.sendEmptyMessageDelayed(1, 10);
                    break;
                case 0:
                    break;
            }
        }
    };

    /**
     * 更新秒表
     */
    private void updateView() {
        timeusedinsec += 1;
        int minute = (int) (timeusedinsec / 60) % 60;
        int second = (int) (timeusedinsec % 60);
        if (minute < 10)
            mint.setText("0" + minute);
        else
            mint.setText("" + minute);
        if (second < 10)
            sec.setText("0" + second);
        else
            sec.setText("" + second);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_present);
        ButterKnife.bind(this);
    }

    /**
     * MediaRouter 方式直接搜索绑定display
     * @param v
     */
    public void click1(View v) {
        MediaRouter mediaRouter = (MediaRouter) this.getSystemService(Context.MEDIA_ROUTER_SERVICE);
        MediaRouter.RouteInfo route = mediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        if (route != null) {
            Display presentationDisplay = route.getPresentationDisplay();
            if (presentationDisplay != null) {
                DemoPresentation presentation = new DemoPresentation(this, presentationDisplay);
                presentation.show();
                Log.e("PresentationDemo", "展示");
            } else {
                Log.e("PresentationDemo", "presentationDisplay为null");
            }
        } else {
            Log.e("PresentationDemo", "route为null");
        }
    }

    /**
     * DisplayManager方式搜索所有的display，如果存在，显示在ListView上
     * @param v
     */
    public void click2(View v) {
        DisplayManager displayManager = (DisplayManager) this.getSystemService(Context.DISPLAY_SERVICE);
        //获得周围所有display的数组
        presentationDisplays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        if (presentationDisplays.length > 0) {
            DisplayListAdapter adapter = new DisplayListAdapter();
            lv_display.setAdapter(adapter);
            lv_display.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Display display = presentationDisplays[position];
                    if (display != null) {
                        DemoPresentation presentation = new DemoPresentation(PresentActivity.this, display);
                        presentation.show();
                        mHandler.sendEmptyMessage(1);
                        Log.e("PresentationDemo", "展示");
                    } else {
                        Log.e("PresentationDemo", "presentationDisplay为null");
                    }
                }
            });
        } else {
            Toast.makeText(this, "搜索不到任何设备", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 自定义辅助屏幕显示的内容
     */
    private final static class DemoPresentation extends Presentation {
        private TextView mint;
        private TextView sec;
        private Button start;
        private Button reset;
        private long timeusedinsec;
        private boolean isstop = false;
        private Context ctx;
        private Handler mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        // 添加更新ui的代码
                        updateView();
                        mHandler.sendEmptyMessageDelayed(1, 10);
                        break;
                    case 0:
                        break;
                }
            }
        };

        public DemoPresentation(Context context, Display display) {
            super(context, display);
            this.ctx = context;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // Be sure to call the super class.
            super.onCreate(savedInstanceState);
            Resources r = getContext().getResources();
            setContentView(R.layout.present_content_layout);
            initViews();
        }

        private void initViews() {
            mint = (TextView) findViewById(R.id.mint);
            sec = (TextView) findViewById(R.id.sec);
            mHandler.sendEmptyMessage(1);
        }

        private void updateView() {
            timeusedinsec += 1;
            int minute = (int) (timeusedinsec / 60) % 60;
            int second = (int) (timeusedinsec % 60);
            if (minute < 10)
                mint.setText("0" + minute);
            else
                mint.setText("" + minute);
            if (second < 10)
                sec.setText("0" + second);
            else
                sec.setText("" + second);
        }
    }

    /**
     * List adapter.
     * Shows information about all displays.
     */
    private class DisplayListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return presentationDisplays.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                view = View.inflate(PresentActivity.this, R.layout.present_list_item, null);
                viewHolder.display_id = (TextView) view.findViewById(R.id.display_id);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }
            Display display = presentationDisplays[position];
            String displayName = display.getName();
            viewHolder.display_id.setText(displayName);
            return view;
        }
    }

    private class ViewHolder {
        private TextView display_id;
    }
}
