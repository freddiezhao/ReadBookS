package com.sina.book.parser;

import com.sina.book.data.Chapter;
import com.sina.book.data.ChapterList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 章节列表解析器
 */
public class ChapterListParser extends BaseParser {
    
    /**
     * 当更新书籍章节列表时，<br>
     * 可以传入以前的章节总数，当章节总数一致时就不再解析章节数据<br>
     * 减小章节更新的计算量
     */
    private int oldChapterNum; 
    private boolean allBookHasBuy;
    
    public ChapterListParser(int oldChapterNum) {
        this.oldChapterNum = oldChapterNum;
    }
    
    public ChapterListParser() {
        
    }
    
    /**
     * 是否该书全本都已经购买了<br>
     * 全本购买，包月的书使用
     * @param allBookHasBuy
     */
    public void setAllBookHasBuy(boolean allBookHasBuy) {
        this.allBookHasBuy = allBookHasBuy;
    }
    
    @Override
    protected Object parse(String jsonString) throws JSONException {
        ChapterList result = new ChapterList();
        parseDataContent(jsonString);
        JSONObject obj = new JSONObject(jsonString);
        result.setCurCid(obj.optInt("read_cid"));
        result.setBookId(obj.optString("book_id"));
        result.setChapterNum(obj.optString("chapter_num"));
        result.setTotalPage(obj.optInt("total_page"));
        JSONArray chaptersArray = obj.optJSONArray("chapters");
        ArrayList<Chapter> chapters = new ArrayList<Chapter>();
        if (chaptersArray != null) {
            // 这里确保使用真实的章节数，预防服务器给的chapter_num出错的情况
            result.setChapterNum("" + chaptersArray.length());
            if (oldChapterNum <= 0 || oldChapterNum < chaptersArray.length()) {
                for (int j = 0; j < chaptersArray.length(); j++) {
                    Chapter chapter = new Chapter();
                    JSONObject bitem = chaptersArray.getJSONObject(j);
                    chapter.setChapterId(j + 1);
                    chapter.setSerialNumber(bitem.optInt("s_num"));
                    chapter.setGlobalId(bitem.optInt("c_id"));
                    chapter.setTitle(bitem.optString("title"));
                    chapter.setVip(bitem.optString("vip"));
                    chapter.setPrice(bitem.optDouble("price",
                            Chapter.CHAPTER_DEFAUTL_PRICE));
                    if (allBookHasBuy) {
                        chapter.setHasBuy(true);
                        chapter.setVip("N");
                    } else {
                        chapter.setHasBuy("Y".equals(bitem.optString("buy", "N")));
                    }
                    chapters.add(chapter);
                }
            }
        }
        result.setChapters(chapters);
        return result;
    }
}
