package com.fahimk.readabilityclient;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fahimk.jsonobjects.Article;

public class ReadingListAdapter extends BaseAdapter {
	private Context context;
	private List<Article> listArticles;
	
    public ReadingListAdapter(Context context, List<Article> listArticles) {
        this.context = context;
        this.listArticles = listArticles;
    }

    
	public int getCount() {
		return listArticles.size();
	}

	public Object getItem(int position) {
		return listArticles.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
	       Article entry = listArticles.get(position);
	        if (convertView == null) {
	            LayoutInflater inflater = (LayoutInflater) context
	                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            convertView = inflater.inflate(R.layout.list_item, null);
	        }
	        TextView tvDomain = (TextView) convertView.findViewById(R.id.listitem_domain);
	        tvDomain.setText(entry.domain);

	        TextView tvTitle = (TextView) convertView.findViewById(R.id.listitem_title);
	        Log.e("title", entry.title);
	        tvTitle.setText(entry.title);

	        TextView tvContent = (TextView) convertView.findViewById(R.id.listitem_content);
	        String content = entry.content;
	        Log.e("list content", content);
	        int indexOfStart = content.indexOf("<div class=\"post_body\">");
	        String removeTop = content.substring(indexOfStart == -1 ? 0 : indexOfStart);
	        Matcher m = Pattern.compile("<.+?>").matcher(removeTop);
	        String strippedContent = m.replaceAll("");
	        tvContent.setText(strippedContent.trim().substring(0, Math.min(90, strippedContent.length())) + ((strippedContent == "") ? "" : "..." ));
	        
	        return convertView;
	}


}
