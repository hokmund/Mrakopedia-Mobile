package com.randomname.mrakopedia.ui.pagesummary;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.models.api.pagesummary.TextSection;
import com.randomname.mrakopedia.ui.views.HtmlTagHandler;
import com.randomname.mrakopedia.utils.StringUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by vgrigoryev on 22.01.2016.
 */
public class PageSummaryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<TextSection> sections;
    private Context context;

    public PageSummaryAdapter(ArrayList<TextSection> sections, Context context) {
        this.sections = sections;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return sections.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TextSection.TEXT_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_summary_text_view, parent, false);
                return new TextViewHolder(view);
            case TextSection.IMAGE_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_summary_image_view, parent, false);
                return new ImageViewHolder(view);
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_summary_text_view, parent, false);
                return new TextViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case TextSection.TEXT_TYPE:
                Spannable span = (Spannable) Html.fromHtml(sections.get(position).getText(), null, new HtmlTagHandler());
                span = (Spannable) StringUtils.trimTrailingWhitespace(span);
                ((TextViewHolder) holder).textView.setText(span);
                break;
            case TextSection.IMAGE_TYPE:
                Picasso.with(context)
                        .load(sections.get(position).getText())
                        .into(((ImageViewHolder)holder).imageView);

                break;
            default:
        }
    }

    @Override
    public int getItemCount() {
        return sections == null ? 0 : sections.size();
    }

    private class TextViewHolder extends RecyclerView.ViewHolder {

        protected TextView textView;

        public TextViewHolder(View itemView) {
            super(itemView);
            textView = (TextView)itemView.findViewById(R.id.text_view);
        }
    }

    private class ImageViewHolder extends RecyclerView.ViewHolder {
        protected ImageView imageView;

        public ImageViewHolder(View view) {
            super(view);
            imageView = (ImageView)view.findViewById(R.id.image_view);
        }
    }
}
