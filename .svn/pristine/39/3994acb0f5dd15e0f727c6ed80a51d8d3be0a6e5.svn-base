package com.sina.book.data;

import java.util.ArrayList;

public class PartitionItem {
    private String id;
    private String name;
    private String img;
    private boolean mIsFavorite;
    
    private ArrayList<Book> mBookLists = new ArrayList<Book>();

    public ArrayList<Book> getBookLists() {
        return mBookLists;
    }

    public void setmBookLists(ArrayList<Book> mBookLists) {
        this.mBookLists = mBookLists;
    }
    
    public void addBookToList(ArrayList<Book> items){
        if(items!=null){
            mBookLists.addAll(items);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }    
    
    public boolean getIsFavorite() {
		return mIsFavorite;
	}

	public void setIsFavorite(boolean isFavorite) {
		this.mIsFavorite = isFavorite;
	}
}
