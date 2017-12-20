package com.sina.book.data;

import java.util.List;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.sina.book.R;
import com.sina.book.SinaBookApplication;

/**
 * 主界面返回数据
 * 
 * @author Tsimle
 * 
 */
public class MainInfoResult {
    private Book recommendBook;
    private Book sellFastBook;
    private Book cateBook;
    private Book peopleRecommendBook;
    private UserInfoRec peopleRecommend;
    private String peopleRecommendTime;

    private List<Book> rankBooks;
    private List<FamousRecommend> famousRecommends;
    private List<MainTipItem> mainTips;
    private int nextMainTipIndex = 0;
    private int curMainTipIndex = 0;

    public String getCurMainTipString() {
        if (mainTips == null || mainTips.size() == 0) {
            return null;
        }
        MainTipItem tip = mainTips.get(nextMainTipIndex);
        curMainTipIndex = nextMainTipIndex;
        nextMainTipIndex++;
        if (nextMainTipIndex >= mainTips.size()) {
            nextMainTipIndex = 0;
        }
        return tip.getComment();
    }

    public MainTipItem getCurMainTip() {
        if (mainTips == null || mainTips.size() == 0) {
            return null;
        }
        return mainTips.get(curMainTipIndex);
    }

    public void launchCurMainTip(Context context) {
        MainTipItem item = getCurMainTip();
        if (item != null) {
            item.launch(context);
        }
    }

    public String getPeopleRecommendString() {
        if (peopleRecommendBook != null) {
            return String.format(SinaBookApplication.gContext.getText(R.string.people_recommend_book).toString(),
                    peopleRecommendBook.getTitle());
        }
        return "";
    }

    public SpannableString getPeopleRecommendStyledString() {
        String peopleRecommend = getPeopleRecommendString();
        return addRecommendStringStyle(peopleRecommend);
    }

    public static SpannableString addRecommendStringStyle(String recommendString) {
        int start = recommendString.indexOf("《");
        int end = recommendString.lastIndexOf("》");
        SpannableString ss = new SpannableString(recommendString);
        if (start > 0 && end > 0) {
            ss.setSpan(new ForegroundColorSpan(0xFF3BB1F5), start, end + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ss;
    }

    public String getFamousRecommendString() {
        if (famousRecommends != null && famousRecommends.size() > 0) {
            return String.format(SinaBookApplication.gContext.getText(R.string.famous_recommend_book).toString(),
                    famousRecommends.get(0).getScreenName(), famousRecommends.get(0).getRecoBookTitle());
        }
        return "";
    }

    public SpannableString getFamousRecommendStyledString() {
        String famousRecommend = getFamousRecommendString();
        return addRecommendStringStyle(famousRecommend);
    }

    public String getRankString() {
        if (rankBooks != null && rankBooks.size() > 2) {
            return String.format(SinaBookApplication.gContext.getText(R.string.rank_book).toString(), rankBooks.get(0)
                    .getTitle(), rankBooks.get(1).getTitle(), rankBooks.get(2).getTitle());
        }
        return "";
    }

    public Book getPeopleRecommendBook() {
        return peopleRecommendBook;
    }

    public void setPeopleRecommendBook(Book peopleRecommendBook) {
        this.peopleRecommendBook = peopleRecommendBook;
    }

    public UserInfoRec getPeopleRecommend() {
        return peopleRecommend;
    }

    public void setPeopleRecommend(UserInfoRec peopleRecommend) {
        this.peopleRecommend = peopleRecommend;
    }

    public String getPeopleRecommendTime() {
        return peopleRecommendTime;
    }

    public void setPeopleRecommendTime(String peopleRecommendTime) {
        this.peopleRecommendTime = peopleRecommendTime;
    }

    public List<Book> getRankBooks() {
        return rankBooks;
    }

    public void setRankBooks(List<Book> rankBooks) {
        this.rankBooks = rankBooks;
    }

    public List<FamousRecommend> getFamousRecommends() {
        return famousRecommends;
    }

    public void setFamousRecommends(List<FamousRecommend> famousRecommends) {
        this.famousRecommends = famousRecommends;
    }

    public List<MainTipItem> getMainTips() {
        return mainTips;
    }

    public void setMainTips(List<MainTipItem> mainTips) {
        this.mainTips = mainTips;
    }

    public Book getRecommendBook() {
        return recommendBook;
    }

    public void setRecommendBook(Book recommendBook) {
        this.recommendBook = recommendBook;
    }

    public Book getSellFastBook() {
        return sellFastBook;
    }

    public void setSellFastBook(Book sellFastBook) {
        this.sellFastBook = sellFastBook;
    }

    public Book getCateBook() {
        return cateBook;
    }

    public void setCateBook(Book cateBook) {
        this.cateBook = cateBook;
    }

}
