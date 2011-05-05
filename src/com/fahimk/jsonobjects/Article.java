package com.fahimk.jsonobjects;

public class Article {
    public String url;
    public String domain;
	public String id;
    public String title;
    public String content;
    
    public Article(String url, String domain, String id, String title, String content) {
		super();
		this.url = url;
		this.domain = domain;
		this.id = id;
		this.title = title;
		this.content = content;
	}
}
