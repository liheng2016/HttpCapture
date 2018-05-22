package com.kmjd.android.zxhzm.netlistener.netlistener.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.kmjd.android.zxhzm.netlistener.netlistener.HarDetailActivity;
import com.kmjd.android.zxhzm.netlistener.netlistener.R;

import net.lightbody.bmp.core.har.HarEntry;

import java.util.List;

public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.MyViewHolder> {

    private Context mContext;
    private List<HarEntry> mList;

    public PreviewAdapter(Context context, List<HarEntry> harList){
        mContext = context;
        mList = harList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_preview, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        HarEntry harEntry = mList.get(position);
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, HarDetailActivity.class);
                intent.putExtra("pos", position);
                mContext.startActivity(intent);
            }
        });
        holder.tv.setText(harEntry.getRequest().getUrl());
        if(harEntry.getResponse().getStatus()>400){
            holder.iconView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_error_black_24dp));
        }else if(harEntry.getResponse().getStatus()>300){
            holder.iconView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_directions_black_24dp));
        }else if(harEntry.getResponse().getContent().getMimeType().contains("image")) {
            holder.iconView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_photo_black_24dp));
        }else{
            holder.iconView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_description_black_24dp));
        }
        holder.detailTextView.setText("Status:" + harEntry.getResponse().getStatus() +
                " Size:" + harEntry.getResponse().getBodySize() +
                "Bytes Time:" + harEntry.getTime() + "ms");
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv;
        TextView detailTextView;
        View rootView;
        ImageView iconView;

        public MyViewHolder(View view) {
            super(view);
            tv =  view.findViewById(R.id.tv_url);
            detailTextView =  view.findViewById(R.id.tv_detail);
            rootView = view;
            iconView =  view.findViewById(R.id.iv_icon);
        }
    }
}
