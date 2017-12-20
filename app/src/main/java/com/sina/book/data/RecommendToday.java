package com.sina.book.data;

import java.util.List;

/**
 * Created by apple on 16-2-23.
 */
public class RecommendToday {

    /**
     * books : [{"sid":"","bid":"5348128","src":"websina","title":"我的美女总裁老婆","recommend_name":"[职场]最风骚:我的美女总裁老婆","recommend_intro":"杀手之王化身小保镖，守护极品美女总裁"},{"sid":"","bid":"5348333","src":"websina","title":"逆天武神","recommend_name":"[玄幻]少年微末中崛起:逆天武神","recommend_intro":"亿万位面，千百种修炼法门，孰强孰弱？"},{"sid":"","bid":"5345876","src":"websina","title":"我的刁蛮女神","recommend_name":"[兵王]兵王隐都市:我的刁蛮女神","recommend_intro":" 巅峰兵王归隐都市，为何会沦为小保镖？"}]
     * total : 5
     * status : {"code":0,"msg":"成功"}
     * name : 男生
     * title : 男生爱看
     * index_type : boy
     */

    private int total;

    private String name;
    private String title;
    private String index_type;
    /**
     * sid :
     * bid : 5348128
     * src : websina
     * title : 我的美女总裁老婆
     * recommend_name : [职场]最风骚:我的美女总裁老婆
     * recommend_intro : 杀手之王化身小保镖，守护极品美女总裁
     */

    private List<Book> books;

    public void setTotal(int total) {
        this.total = total;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIndex_type(String index_type) {
        this.index_type = index_type;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public int getTotal() {
        return total;
    }


    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getIndex_type() {
        return index_type;
    }

    public List<Book> getBooks() {
        return books;
    }


    public static class BooksEntity {
        private String sid;
        private String bid;
        private String src;
        private String title;
        private String recommend_name;
        private String recommend_intro;

        public void setSid(String sid) {
            this.sid = sid;
        }

        public void setBid(String bid) {
            this.bid = bid;
        }

        public void setSrc(String src) {
            this.src = src;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setRecommend_name(String recommend_name) {
            this.recommend_name = recommend_name;
        }

        public void setRecommend_intro(String recommend_intro) {
            this.recommend_intro = recommend_intro;
        }

        public String getSid() {
            return sid;
        }

        public String getBid() {
            return bid;
        }

        public String getSrc() {
            return src;
        }

        public String getTitle() {
            return title;
        }

        public String getRecommend_name() {
            return recommend_name;
        }

        public String getRecommend_intro() {
            return recommend_intro;
        }
    }
}
