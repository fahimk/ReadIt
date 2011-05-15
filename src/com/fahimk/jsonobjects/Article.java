package com.fahimk.jsonobjects;

public class Article {
    public String url;
    public String domain;
	public String id;
    public String title;
    public String content;
    public String bookmark_id;
    public String favorite;
    public String archive;
    
    public Article(String url, String domain, String id, String title, String content, String bookmark_id, String favorite, String archive) {
		super();
		this.url = url;
		this.domain = domain;
		this.id = id;
		this.title = title;
		this.content = content;
		this.bookmark_id = bookmark_id;
		this.favorite = favorite;
		this.archive = archive;
	}
}
