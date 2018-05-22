package com.kmjd.android.zxhzm.netlistener.netlistener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Filter;
import android.widget.Filterable;

import com.kmjd.android.httpcapture.SDKManager;
import com.kmjd.android.httpcapture.manager.SDKManagerServer;
import com.kmjd.android.zxhzm.netlistener.netlistener.adapter.PreviewAdapter;
import com.kmjd.android.zxhzm.netlistener.netlistener.view.RecycleViewDivider;

import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarLog;

import java.util.ArrayList;
import java.util.List;

public class PreviewActivity extends AppCompatActivity implements Filterable {

    private RecyclerView mRView;
    private PreviewAdapter mAdapter;
    private HarLog mHarLog;
    private List<HarEntry> mHarList = new ArrayList<>();
    private SDKManager sdkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

       Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //关键下面两句话，设置了回退按钮，及点击事件的效果
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initView();
    }

    /**
     * <p>initView</p>
     * @Description  初始化界面
     */
    private void initView(){
        mRView = findViewById(R.id.rv_preview);
        sdkManager = new SDKManagerServer();

        if(MainApplication.isInitProxy){
            mHarLog = sdkManager.getProxyInstance().getHar().getLog();
            mHarList.addAll(mHarLog.getEntries());
        }

        mRView.addItemDecoration(new RecycleViewDivider(this, RecycleViewDivider.VERTICAL_LIST));
        mRView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PreviewAdapter(this, mHarList);
        mRView.setAdapter(mAdapter);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                //初始化过滤结果对象
                FilterResults results = new FilterResults();
                //假如搜索为空的时候，将复制的数据添加到原始数据，用于继续过滤操作
                if (results.values == null) {
                    mHarList.clear();
                    mHarList.addAll(mHarLog.getEntries());
                }
                //关键字为空的时候，搜索结果为复制的结果
                if (constraint == null || constraint.length() == 0) {
                    results.values = mHarLog.getEntries();
                    results.count = mHarLog.getEntries().size();
                } else {
                    String prefixString = constraint.toString();
                    final int count = mHarList.size();
                    //用于存放暂时的过滤结果
                    final ArrayList<HarEntry> newValues = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        final HarEntry value = mHarList.get(i);
                        String url = value.getRequest().getUrl();
                        // 假如含有关键字的时候，添加
                        if (url.contains(prefixString)) {
                            newValues.add(value);
                        } else {
                            //过来空字符开头
                            String[] words = prefixString.split(" ");

                            for (String word : words) {
                                if (url.contains(word)) {
                                    newValues.add(value);
                                    break;
                                }
                            }
                        }
                    }
                    results.values = newValues;
                    results.count = newValues.size();
                }
                return results;//过滤结果
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mHarList.clear();//清除原始数据
                if(results.values instanceof List){
                    mHarList.addAll((List<HarEntry>) results.values);//将过滤结果添加到这个对象
                }
                if (results.count > 0) {
                    mAdapter.notifyDataSetChanged();//有关键字的时候刷新数据
                } else {
                    //关键字不为零但是过滤结果为空刷新数据
                    if (constraint.length() != 0) {
                        mAdapter.notifyDataSetChanged();
                        return;
                    }
                    //加载复制的数据，即为最初的数据
                    mHarList.addAll(mHarLog.getEntries());
                    mAdapter.notifyDataSetChanged();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFilter().filter("http://119.81.201.156:6026");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
