package com.sina.book.data;

import java.util.List;

/**
 * 专题Item
 *
 * @author MarkMjw
 * @date 13-12-12.
 */
public class TopicItem {
    private String title;
    private int topicId;
    private String imgUrl;
    private String intro;
    private List<TopicBook> topicBooks;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public List<TopicBook> getTopicBooks() {
        return topicBooks;
    }

    public void setTopicBooks(List<TopicBook> topicBooks) {
        this.topicBooks = topicBooks;
    }

    public TopicBook newTopicBook() {
        return new TopicBook();
    }

    public class TopicBook {
        private String name;

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        private int total;
        private List<Book> books;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Book> getBooks() {
            return books;
        }

        public void setBooks(List<Book> books) {
            this.books = books;
        }
    }
}
