package com.randomname.mrakopedia.ui.pagesummary;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.randomname.mrakopedia.R;
import com.randomname.mrakopedia.models.api.pagesummary.CategoriesTextSection;
import com.randomname.mrakopedia.models.api.pagesummary.TextSection;
import com.randomname.mrakopedia.ui.views.HtmlTagHandler;
import com.randomname.mrakopedia.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by vgrigoryev on 22.01.2016.
 */
public class PageSummaryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<TextSection> sections;
    private Context context;
    private View.OnClickListener linkClickListener;
    private View.OnClickListener imageClickListener;
    private OnCategoryClickListener categoryClickListener;

    public PageSummaryAdapter(ArrayList<TextSection> sections, Context context, View.OnClickListener linkClickListener, View.OnClickListener imageClickListener, OnCategoryClickListener categoryClickListener) {
        this.sections = sections;
        this.context = context;
        this.linkClickListener = linkClickListener;
        this.imageClickListener = imageClickListener;
        this.categoryClickListener = categoryClickListener;
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
                view.setOnClickListener(imageClickListener);
                return new ImageViewHolder(view);
            case TextSection.TEMPLATE_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_summary_template, parent, false);
                return new TemplateViewHolder(view);
            case TextSection.LINK_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_summary_text_view, parent, false);
                view.setOnClickListener(linkClickListener);
                return new TextViewHolder(view);
            case TextSection.CATEGORY_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_summary_categories_view_holder, parent, false);
                return new CategoriesViewHolder(view);
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
                ((TextViewHolder) holder).textView.setMovementMethod(new LinkMovementMethod());
                ((TextViewHolder) holder).textView.setTextIsSelectable(true);
                break;
            case TextSection.IMAGE_TYPE:
                Picasso.with(context)
                        .load(sections.get(position).getText())
                        .into(((ImageViewHolder) holder).imageView);
                break;
            case TextSection.TEMPLATE_TYPE:
                bindTemplateHolder(holder, sections.get(position).getText());
                break;
            case TextSection.LINK_TYPE:
                ((TextViewHolder) holder).textView.setText(Html.fromHtml("<a href='dummy'>" + sections.get(position).getText() + "</a>"));
                break;
            case TextSection.CATEGORY_TYPE:
                CategoriesViewHolder categoryViewHolder = (CategoriesViewHolder)holder;
                CategoriesTextSection categories = (CategoriesTextSection)sections.get(position);

                categoryViewHolder.categoryTitle.setText(categories.getText());
                categoryViewHolder.adapter.setData(categories.getCategoriesArrayList());

                break;
            default:
        }
    }

    private void bindTemplateHolder(RecyclerView.ViewHolder holder, String text) {
        TemplateViewHolder templateViewHolder = (TemplateViewHolder) holder;
        switch (text) {
            case "NSFW":
                templateViewHolder.imageView.setImageResource(R.drawable.meatboy);
                templateViewHolder.textView.setText(context.getString(R.string.nsfw_description));
                templateViewHolder.wrapper.setBackgroundColor(context.getResources().getColor(R.color.nsfw_color));
                break;
            case "Anomaly":
                templateViewHolder.imageView.setImageResource(R.drawable.warning);
                templateViewHolder.textView.setText(context.getString(R.string.anomaly_description));
                templateViewHolder.wrapper.setBackgroundColor(context.getResources().getColor(R.color.nsfw_color));
                break;
            case "Parody":
                templateViewHolder.imageView.setImageResource(R.drawable.vagan);
                templateViewHolder.textView.setText(context.getString(R.string.parody_description));
                templateViewHolder.wrapper.setBackgroundColor(context.getResources().getColor(R.color.parody_color));
                break;
            case "Save":
                templateViewHolder.imageView.setImageResource(R.drawable.floppydisk);
                templateViewHolder.textView.setText(context.getString(R.string.save_description));
                templateViewHolder.wrapper.setBackgroundColor(context.getResources().getColor(R.color.save_color));
                break;
            case "Vg":
                templateViewHolder.imageView.setImageResource(R.drawable.creeper_vg);
                templateViewHolder.textView.setText(context.getString(R.string.vg_description));
                templateViewHolder.wrapper.setBackgroundColor(context.getResources().getColor(R.color.save_color));
                break;
            case "WTF":
                templateViewHolder.imageView.setImageResource(R.drawable.triangle);
                templateViewHolder.textView.setText(context.getString(R.string.wtf_description));
                templateViewHolder.wrapper.setBackgroundColor(context.getResources().getColor(R.color.nsfw_color));
                break;
            case "Избранное":
                templateViewHolder.imageView.setImageResource(R.drawable.kubok);
                templateViewHolder.textView.setText(context.getString(R.string.favorited_description));
                templateViewHolder.wrapper.setBackgroundColor(context.getResources().getColor(R.color.favorited_color));
                break;
            case "КГАМ":
                templateViewHolder.imageView.setImageResource(R.drawable.pero);
                templateViewHolder.textView.setText(context.getString(R.string.kgam_description));
                templateViewHolder.wrapper.setBackgroundColor(context.getResources().getColor(R.color.save_color));
                break;
            case "Классика":
                templateViewHolder.imageView.setImageResource(R.drawable.kubok);
                templateViewHolder.textView.setText(context.getString(R.string.classic_description));
                templateViewHolder.wrapper.setBackgroundColor(context.getResources().getColor(R.color.favorited_color));
            case "НПЧДХ":
                templateViewHolder.imageView.setImageResource(R.drawable.vagan);
                templateViewHolder.textView.setText(context.getString(R.string.so_bad_so_good_description));
                templateViewHolder.wrapper.setBackgroundColor(context.getResources().getColor(R.color.parody_color));
                break;
            case "Юмор":
                templateViewHolder.imageView.setImageResource(R.drawable.vagan);
                templateViewHolder.textView.setText(context.getString(R.string.humor_description));
                templateViewHolder.wrapper.setBackgroundColor(context.getResources().getColor(R.color.parody_color));
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

    private class TemplateViewHolder extends RecyclerView.ViewHolder {
        protected TextView textView;
        protected ImageView imageView;
        protected RelativeLayout wrapper;

        public TemplateViewHolder(View view) {
            super(view);
            imageView = (ImageView)view.findViewById(R.id.image_view);
            textView = (TextView)view.findViewById(R.id.text_view);
            wrapper = (RelativeLayout)view.findViewById(R.id.wrapper);
        }
    }

    private class CategoriesViewHolder extends RecyclerView.ViewHolder {
        protected TextView categoryTitle;
        protected RecyclerView categoriesRecyclerView;
        protected CategoryHolderAdapter adapter;

        public CategoriesViewHolder(View view) {
            super(view);
            categoryTitle = (TextView)view.findViewById(R.id.categories_title);
            categoriesRecyclerView = (RecyclerView)view.findViewById(R.id.categories_recycler_view);
            categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            adapter = new CategoryHolderAdapter(categoryClickListener);
            categoriesRecyclerView.setAdapter(adapter);
        }
    }

    private class CategoryHolderAdapter extends RecyclerView.Adapter<CategoryHolderAdapter.LinkViewHolder> {

        private ArrayList<String> categoriesTitles;
        private OnCategoryClickListener onCategoryClickListener;

        public CategoryHolderAdapter(OnCategoryClickListener onCategoryClickListener) {
            categoriesTitles = new ArrayList<>();
            this.onCategoryClickListener = onCategoryClickListener;
        }

        public ArrayList<String> getData() {
            return categoriesTitles;
        }

        public void setData(ArrayList<String> categoriesTitles) {
            this.categoriesTitles = categoriesTitles;
            notifyDataSetChanged();
        }

        @Override
        public LinkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_summary_category_text_view, parent, false);
            return new LinkViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return categoriesTitles == null ? 0 : categoriesTitles.size();
        }

        @Override
        public void onBindViewHolder(LinkViewHolder holder, final int position) {
            holder.textView.setText(categoriesTitles.get(position));
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCategoryClickListener.OnCategoryClick(categoriesTitles.get(position));
                }
            });
        }

        protected class LinkViewHolder extends RecyclerView.ViewHolder {
            protected carbon.widget.TextView textView;

            public LinkViewHolder(View itemView) {
                super(itemView);
                textView = (carbon.widget.TextView)itemView.findViewById(R.id.text_view);
            }
        }
    }
}
