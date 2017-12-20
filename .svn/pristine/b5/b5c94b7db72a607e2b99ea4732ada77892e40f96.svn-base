package com.sina.book.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 推荐-男生女生出版
 *
 * @author MarkMjw
 * @date 13-8-29.
 */
public class RecommendCmsResult {
    private RecommendToday recommendToday;
    private List<RecommendCate> recommendCates;

    public RecommendToday getRecommendToday() {
        return recommendToday;
    }

    public void setRecommendToday(RecommendToday recommendToday) {
        this.recommendToday = recommendToday;
    }

    public List<RecommendCate> getRecommendCates() {
        return recommendCates;
    }

    public void setRecommendCates(List<RecommendCate> recommendCates) {
        this.recommendCates = recommendCates;
    }

    public void addRecommendCate(RecommendCate recommendCate) {
        if (null == recommendCates) {
            recommendCates = new ArrayList<RecommendCate>();
        }

        recommendCates.add(recommendCate);
    }

    public RecommendToday newRcommendTodayInstance() {
        return new RecommendToday();
    }

    public RecommendCate newRcommendCateInstance() {
        return new RecommendCate();
    }

    public class RecommendToday {
        public int total;
        public String name;
        public String type;
        public List<Book> books;

        @Override
        public String toString() {
            return "RecommendToday{" +
                    "total=" + total +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", books=" + books +
                    '}';
        }
    }

    public class RecommendCate {
        public String id;
        public String name;
        public List<Book> books;

        @Override
        public String toString() {
            return "RecommendCate{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", books=" + books +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "RecommendCmsResult{" +
                "recommendToday=" + recommendToday +
                ", recommendCates=" + recommendCates +
                '}';
    }
}
