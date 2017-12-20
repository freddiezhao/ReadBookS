package com.sina.book.useraction;

import java.util.HashMap;

/**
 * 用户行为统计工具类
 *
 * @Author: MarkMjw
 * @Date: 13-5-3 下午2:12
 */
public final class UserActionUtil {
    private static HashMap<String, String> sCateTagMap = new HashMap<String, String>();

    static {
        sCateTagMap.put("都市", Constants.CLICK_CATE_CITY);
        sCateTagMap.put("言情", Constants.CLICK_CATE_ROMANCE);
        sCateTagMap.put("官场", Constants.CLICK_CATE_OFFICIAL);
        sCateTagMap.put("军事", Constants.CLICK_CATE_ARMY);
        sCateTagMap.put("玄幻", Constants.CLICK_CATE_FANTASY);
        sCateTagMap.put("武侠", Constants.CLICK_CATE_KUNGFU);
        sCateTagMap.put("科幻", Constants.CLICK_CATE_SCIENCE);
        sCateTagMap.put("游戏", Constants.CLICK_CATE_GAME);
        sCateTagMap.put("悬疑", Constants.CLICK_CATE_SUSPENSE);
        sCateTagMap.put("恐怖", Constants.CLICK_CATE_TERROR);
        sCateTagMap.put("校园", Constants.CLICK_CATE_CAMPUS);
        sCateTagMap.put("影视", Constants.CLICK_CATE_MOVIE);
        sCateTagMap.put("历史", Constants.CLICK_CATE_HISTORY);
        sCateTagMap.put("商战", Constants.CLICK_CATE_MARKET);
        sCateTagMap.put("励志成功", Constants.CLICK_CATE_SUCCESS);
        sCateTagMap.put("人物传记", Constants.CLICK_CATE_PEOPLE);
        sCateTagMap.put("婚姻情感", Constants.CLICK_CATE_MARRIAGE);
        sCateTagMap.put("政史军事", Constants.CLICK_CATE_POLITICAL);
        sCateTagMap.put("经典名著", Constants.CLICK_CATE_CLASSIC);
        sCateTagMap.put("人文社科", Constants.CLICK_CATE_SOCIETY);
        sCateTagMap.put("美文其他", Constants.CLICK_CATE_OTHERS);
    }

    /**
     * 通过分类名称获取对应的行为tag
     *
     * @param cateName
     * @return
     */
    public static String getActionCateTag(String cateName) {
        return sCateTagMap.get(cateName);
    }
}
