package com.xuexiang.mapandmsg.amap.task;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.core.PoiItem;
import com.xuexiang.mapandmsg.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * @author 夕子
 */
public class GaoDeMapListAdapter extends ListAdapter<PoiItem, GaoDeMapListAdapter.ViewHolder> {

    public GaoDeMapListAdapter() {
        super(new DiffUtil.ItemCallback<PoiItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull @NotNull PoiItem oldItem, @NonNull @NotNull PoiItem newItem) {
                return oldItem == newItem;
            }

            @Override
            public boolean areContentsTheSame(@NonNull @NotNull PoiItem oldItem, @NonNull @NotNull PoiItem newItem) {
                return oldItem.getTitle().equals(newItem.getTitle());
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.map_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        String address = this.getCurrentList().get(position).getTitle();
        String city = this.getCurrentList().get(position).getSnippet();
        holder.textView1.setText(address);
        holder.textView2.setText(city);

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (routeClickListener != null) {
                    routeClickListener.onClick(position);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(position);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longClickListener != null) {
                    longClickListener.onClick(position);
                }
                return true;
            }
        });
    }
    private ArrayList<PoiItem> mList;

    public void setList(ArrayList<PoiItem> mList) {
        this.mList = mList;
    }


    static public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        private TextView textView1;
        private TextView textView2;
        private LinearLayout linearLayout;
        private TextView textView_route;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.list_item_icon);
            textView1 = itemView.findViewById(R.id.list_item_address);
            textView2 = itemView.findViewById(R.id.list_item_city);
            linearLayout = itemView.findViewById(R.id.list_item_ll);
            textView_route = itemView.findViewById(R.id.list_item_route_tv);
        }
    }


    //第一步 定义接口
    public interface OnItemClickListener {
        void onClick(int position);
    }

    private OnItemClickListener listener;

    //第二步， 写一个公共的方法
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public interface OnRouteClickListener {
        void onClick(int position);
    }
    private OnRouteClickListener routeClickListener;
    public void setOnRouteClickListener(OnRouteClickListener routeClickListener) {
        this.routeClickListener = routeClickListener;
    }


    public interface OnItemLongClickListener {
        void onClick(int position);
    }

    private OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }
}
