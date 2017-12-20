package com.sina.book.reader.charset;

import java.io.File;

import com.sina.book.data.Book;

/**
 * 文件编码检测类
 * 
 * @author Tsimle
 * 
 */
public class EncodingHelper {
    private static EncodingDetect detector = new EncodingDetect();

    /**
     * 一般文件检查编码
     */
    public static String getEncoding(Book book) {
        File file = new File(book.getDownloadInfo().getFilePath());
        String charset = EncodingDetect.CODDING_DEFAULT_GBK;
        if (null == file || !file.exists()) {
            return charset;
        }

        // 当书本不是通过V盘下载(V盘书籍编码不确定)，且为我们下载的书籍<br>
        // 因为我们的书籍做了加密处理，字节码变化，且肯定是使用的GBK编码
        if (file.getName().endsWith(Book.BOOK_SUFFIX)
                || file.getName().endsWith(Book.ONLINE_TMP_SUFFIX)
                || file.getName().endsWith(Book.ONLINE_DAT_SUFFIX)) {
            if (!book.isVDiskBook()) {
                return charset;
            }
        }

        return detector.detectEncoding(file);
    }

    /**
     * 新浪书城下载的文件检测编码<br>
     * 只可能出现UTF-8或GBK
     */
    public static String getOurBookEncoding(File file) {
        return detector.detectMiniEncoding(file);
    }
}
