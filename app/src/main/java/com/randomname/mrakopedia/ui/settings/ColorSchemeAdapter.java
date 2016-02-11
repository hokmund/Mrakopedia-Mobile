package com.randomname.mrakopedia.ui.settings;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.models.realm.ColorScheme;

import java.util.ArrayList;

/**
 * Created by vgrigoryev on 11.02.2016.
 */
public class ColorSchemeAdapter extends RecyclerView.Adapter<ColorSchemeAdapter.ColorSchemeViewHolder> {

    private ArrayList<ColorScheme> colorsList;
    private View.OnLongClickListener onLongClickListener;
    private View.OnClickListener onClickListener;

    public ColorSchemeAdapter(ArrayList<ColorScheme> colorsList) {
        this.colorsList = colorsList;
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public ColorSchemeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_scheme_view_holder, parent, false);
        view.setOnLongClickListener(onLongClickListener);
        view.setOnClickListener(onClickListener);
        return new ColorSchemeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ColorSchemeViewHolder holder, int position) {
        holder.colorView.setBackgroundColor(colorsList.get(position).getBackgroundColor());
        holder.textView.setTextColor(colorsList.get(position).getTextColor());
    }

    @Override
    public int getItemCount() {
        return colorsList == null ? 0 : colorsList.size();
    }

    protected class ColorSchemeViewHolder extends RecyclerView.ViewHolder {
        public View colorView;
        public TextView textView;

        public ColorSchemeViewHolder(View itemView) {
            super(itemView);
            colorView = itemView.findViewById(R.id.color_view);
            textView = (TextView)itemView.findViewById(R.id.text_view);
        }
    }
}
