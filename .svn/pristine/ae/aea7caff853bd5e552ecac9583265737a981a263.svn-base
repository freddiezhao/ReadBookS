package com.sina.book.image;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.download.DownBookJob;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.util.LogUtil;
import com.sina.book.util.StorageUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 清理图片缓存，缩放图片等功能方法
 * 
 * @author Tsimle
 * 
 */
public class ImageUtil {

    /**
     * 图片缓存的清理参数
     */
    public static int IMAGE_CACHE_LIMIT = 500;
    public static int IMAGE_CACHE_CLEAR_LIMIT = 200;
    public static long IMAGE_CACHE_EXPIRE = 8L;
    /**
     * 间隔48小时尝试清理图片
     */
    public static long IMAGE_CACHE_CLEAR_DUR_TIME = 172800000L;

    public static final String IMG_CACHE_PREF_FILE = "img_cache";
    /**
     * 存储图片缓存的清理时间
     */
    private static final String KEY_IMG_CACHE_TIME = "key_img_clear";

    private static final String TMP_IMAGE_PREFIX = ".tmp";
    private static final String JPG_IMAGE_PREFIX = ".jpg";
    private static final String PNG_IMAGE_PREFIX = ".png";

    public static Bitmap getBitmapFromFile(String dirpath, String tempFileName,
            int scaleWidth, int scaleHeight) {
        if (!StorageUtil.isSDCardExist()) {
            return null;
        }
        Bitmap bitmap = null;
        try {
            File tempFile = new File(dirpath, tempFileName);
            if (tempFile.exists()) {
                bitmap = getResizeBitmap(tempFile.getAbsolutePath(),
                        scaleWidth, scaleHeight, false);

                // 更新文件的访问时间，防止被清理
                tempFile.setLastModified(System.currentTimeMillis());
            }
        } catch (OutOfMemoryError error) {
            // do nothing
        }
        return bitmap;
    }

    public static Bitmap getMustScaleBitmapFromFile(String dirpath,
            String tempFileName, int scaleWidth, int scaleHeight) {
        if (!StorageUtil.isSDCardExist()) {
            return null;
        }
        Bitmap bitmap = null;
        try {
            File tempFile = new File(dirpath, tempFileName);
            if (tempFile.exists()) {
                bitmap = getResizeBitmap(tempFile.getAbsolutePath(),
                        scaleWidth, scaleHeight, true);

                // 更新文件的访问时间，防止被清理
                tempFile.setLastModified(System.currentTimeMillis());
            }
        } catch (OutOfMemoryError error) {
            // do nothing
        }
        return bitmap;
    }

    public static Bitmap getBitmapFromFile(String path, int scaleWidth,
            int scaleHeight) {
        if (!StorageUtil.isSDCardExist()) {
            return null;
        }
        Bitmap bitmap = null;
        try {
            File tempFile = new File(path);
            if (tempFile.exists()) {
                bitmap = getResizeBitmap(tempFile.getAbsolutePath(),
                        scaleWidth, scaleHeight, false);
            }
        } catch (OutOfMemoryError error) {
            // do nothing
            error.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap getBitmapFromStream(InputStream in, int scaleWidth,
            int scaleHeight) throws IOException {
        Bitmap bitmap = null;
        byte[] bytes = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int len = 0;

            while ((len = in.read(b, 0, 1024)) != -1) {
                baos.write(b, 0, len);
                baos.flush();
            }
            bytes = baos.toByteArray();
            bitmap = getResizeBitmap(bytes, scaleWidth, scaleHeight);
        } catch (IOException e) {
            throw e;
        } catch (OutOfMemoryError error) {
            // do nothing
        } finally {
            if (baos != null) {
                baos.close();
            }
        }
        return bitmap;
    }

    public static Bitmap getBitmapFromAssetsFile(Context context,
            String filePath) {
        Bitmap bitmap = null;
        InputStream is = null;
        AssetManager am = context.getAssets();
        try {
            is = am.open(filePath);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            LogUtil.w(e);
        } catch (OutOfMemoryError error) {
            // do nothing
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LogUtil.w(e);
                }
            }
        }
        return bitmap;
    }

    public static Bitmap getBitmapFromResId(Context context, int id,
            boolean needScale) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPurgeable = true;
            opt.inInputShareable = true;
            if (needScale) {
                opt.inPreferredConfig = Bitmap.Config.RGB_565;
            }
            InputStream is = context.getResources().openRawResource(id);
            bitmap = BitmapFactory.decodeStream(is, null, opt);
        } catch (OutOfMemoryError e) {
            ImageLoader.getInstance().oomHandled();
            // do nothing
        } catch (Exception e) {
            // do nothing
        }
        return bitmap;
    }

    /**
     * 根据URL获取图片在文件系统中的文件名
     * 
     * @param url
     * @return
     */
    public static String getTempFileName(String url) {
        String tempFileName = null;
        if (null != url && !"".equals(url.trim())) {
            tempFileName = url.replace('/', '_').replace(':', '_')
                    .replace("?", "_")
                    .replace(JPG_IMAGE_PREFIX, TMP_IMAGE_PREFIX)
                    .replace(PNG_IMAGE_PREFIX, TMP_IMAGE_PREFIX);
            if (!tempFileName.endsWith(TMP_IMAGE_PREFIX)) {
                tempFileName = tempFileName + TMP_IMAGE_PREFIX;
            }
        }
        return tempFileName;
    }

    /**
     * 根据图片清理参数，清理图片缓存
     */
    public static boolean clearDiskCache(Context context) {
        LogUtil.d("try to clearDiskCache");
        if (!StorageUtil.isSDCardExist()) {
            return false;
        }

        if (needImgCacheClear()) {
            final String imgDir = StorageUtil
                    .getDirByType(StorageUtil.DIR_TYPE_IMAGE);
            // 清理图片缓存时不能清理的
            ArrayList<String> imgUrls = new ArrayList<String>();
            ArrayList<DownBookJob> jobs = DownBookManager.getInstance()
                    .getAllJobs();
            if (jobs != null) {
                for (DownBookJob job : jobs) {
                    imgUrls.add(job.getBook().getDownloadInfo().getImageUrl());
                }
            }
            final List<String> finalImgUrls = imgUrls;

            new Thread() {
                public void run() {
                    File imgDirFile = new File(imgDir);
                    excuteClear(imgDirFile, finalImgUrls);
                };
            }.start();

            saveImgCacheClearTime();
            return true;
        }
        return false;
    }

    /**
     * 清理图片缓存
     */
    private static void excuteClear(File dirFile, List<String> exceptUrls) {
        try {
            File[] arrayOfFile = null;
            long now = 0L;
            int fileLength = 0;
            if ((dirFile.exists()) && (dirFile.isDirectory())) {
                now = new Date().getTime();
                arrayOfFile = dirFile.listFiles();
                if (arrayOfFile != null)
                    fileLength = arrayOfFile.length;
            }
            // 如果缓存图片的数量小于图片缓存上限
            if (fileLength < IMAGE_CACHE_LIMIT) {
                return;
            }

            // 对文件按时间排序
            Arrays.sort(arrayOfFile, new FileTimeCompartor());

            int clearNum = 0;
            int needClearTotal = fileLength - IMAGE_CACHE_CLEAR_LIMIT;
            for (int i = 0; i < fileLength; i++) {
                File paramFile = arrayOfFile[i];
                // 如果缓存图片的时间为IMAGE_CACHE_EXPIRE以内，即最经常使用的
                if ((now - paramFile.lastModified()) / 3600000L < IMAGE_CACHE_EXPIRE) {
                    break;
                }
                if ((paramFile.exists()) && (paramFile.isFile())) {
                    String path = paramFile.getAbsolutePath();
                    if (!exceptUrls.contains(path)
                            && !path.endsWith(JPG_IMAGE_PREFIX)
                            && !path.endsWith(PNG_IMAGE_PREFIX)) {
                        paramFile.delete();
                        clearNum++;
                    }
                }
                // 如果缓存图片的数量已经小于图片缓存上限，不再清理
                if (clearNum >= needClearTotal) {
                    break;
                }
            }
            LogUtil.d("clearDiskCache-fileLength :" + fileLength
                    + "needClearTotal :" + needClearTotal + "clearNum :"
                    + clearNum);
        } catch (Exception e) {
            // do not care
        }
    }

    /**
     * 存储图片缓存清理的时间
     * 
     */
    private static void saveImgCacheClearTime() {
        SharedPreferences preferences = SinaBookApplication.gContext
                .getSharedPreferences(IMG_CACHE_PREF_FILE, Context.MODE_PRIVATE);
        long curtime = System.currentTimeMillis();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(KEY_IMG_CACHE_TIME, curtime);
        editor.commit();
    }

    /**
     * 是否需要清理图片缓存
     * 
     */
    private static boolean needImgCacheClear() {
        SharedPreferences preferences = SinaBookApplication.gContext
                .getSharedPreferences(IMG_CACHE_PREF_FILE, Context.MODE_PRIVATE);
        long curtime = System.currentTimeMillis();
        long preftime = preferences.getLong(KEY_IMG_CACHE_TIME, 0L);
        LogUtil.d("clear disk cache--curtime:" + curtime + " preftime:"
                + preftime);
        // 间隔3小时，就再次尝试清理文件的图片缓存
        if (curtime - preftime > IMAGE_CACHE_CLEAR_DUR_TIME) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 返回resize之后的bitmap
     * 
     * @param filePath
     * @param width
     * @param height
     * @param mustScale
     * @return
     */
    public static Bitmap getResizeBitmap(String filePath, int width,
            int height, boolean mustScale) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }

        try {
            Bitmap bitmap = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            // 1 测量大小
            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            if (options.mCancel || options.outWidth == -1
                    || options.outHeight == -1) {
                return null;
            }

            // 2 输出bitmap
            if (mustScale) {
                options.inSampleSize = mustScaleComputeSampleSize(options,
                        width, height);
            } else {
                options.inSampleSize = computeSampleSize(options, -1, width
                        * height);
            }

            options.inJustDecodeBounds = false;
            if (options.outWidth > 400) {
                // 使用RGB_565，每个像素使用2个字节，节省一半内存空间
                options.inPreferredConfig = Bitmap.Config.RGB_565;
            }

            bitmap = BitmapFactory.decodeFile(filePath, options);
            return bitmap;

        } catch (Exception e) {
            LogUtil.w(e);
        } catch (OutOfMemoryError error) {
            // 实在要出现内存不足的问题，清空缓存，调GC，没办法
            LogUtil.w(error);
            ImageLoader.getInstance().oomHandled();
            System.gc();
        }
        return null;
    }

    /**
     * 返回resize之后的bitmap
     *
     * @param bytes
     * @param width
     * @param height
     * @return
     */
    public static Bitmap getResizeBitmap(byte[] bytes, int width, int height) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try {
            Bitmap bitmap = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            // 1 测量大小
            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            if (options.mCancel || options.outWidth == -1
                    || options.outHeight == -1) {
                return null;
            }

            // 2 输出bitmap
            options.inSampleSize = computeSampleSize(options, -1, width
                    * height);
            options.inJustDecodeBounds = false;
            if (options.outWidth > 300) {
                // 使用RGB_565，每个像素使用2个字节，节省一半内存空间
                options.inPreferredConfig = Bitmap.Config.RGB_565;
            }

            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
                    options);
            return bitmap;

        } catch (Exception e) {
            LogUtil.w(e);
        } catch (OutOfMemoryError error) {
            // 实在要出现内存不足的问题，清空缓存，调GC，没办法
            LogUtil.w(error);
            ImageLoader.getInstance().oomHandled();
            System.gc();
        }
        return null;
    }

    /**
     * 将两张位图拼接成一张(横向拼接)
     *
     * @param first  第一张图片
     * @param second 第二张图片
     * @return
     */
    public static Bitmap add2Bitmap(Bitmap first, Bitmap second) {
        Bitmap result = null;
        try {
            int width = first.getWidth() + second.getWidth();
            int height = Math.max(first.getHeight(), second.getHeight());

            result = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            Canvas canvas = new Canvas(result);

            canvas.drawBitmap(first, 0, 0, null);
            canvas.drawBitmap(second, first.getWidth(), 0, null);
        } catch (Exception e) {
            LogUtil.w(e);
        } catch (OutOfMemoryError error) {
            // 实在要出现内存不足的问题，清空缓存，调GC，没办法
            LogUtil.w(error);
            ImageLoader.getInstance().oomHandled();
            System.gc();
        }
        return result;
    }

    /**
     * 缩放图片
     * 
     * @param bitmap
     *            原图
     * @param w
     *            宽
     * @param h
     *            高
     */
    public static Bitmap zoom(Bitmap bitmap, int w, int h) {
        if (null == bitmap) {
            return bitmap;
        }

        try {
            float scaleWidth = w * 1.0f / bitmap.getWidth();
            float scaleHeight = h * 1.0f / bitmap.getHeight();

            // 重新生成一个放大/缩小后图片
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);

            Bitmap result = Bitmap.createBitmap(w, h, Config.ARGB_8888);
            Canvas canvas = new Canvas(result);

            canvas.drawBitmap(bitmap, matrix, null);

            if (bitmap != result && bitmap != ImageLoader.getDefaultPic()) {
                bitmap.recycle();
                bitmap = null;
            }
            return result;
        } catch (OutOfMemoryError e) {
            ImageLoader.getInstance().oomHandled();
            return null;
        }
    }

    /**
     * 获得圆角图片
     * 
     * @param bm
     * @param roundPx
     * @return
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bm, float roundPx) {
        try {
            Bitmap bitmapOrg = bm;
            Bitmap output = Bitmap.createBitmap(bitmapOrg.getWidth(),
                    bitmapOrg.getHeight(), Config.ARGB_8888);

            Canvas canvas = new Canvas(output);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmapOrg.getWidth(),
                    bitmapOrg.getHeight());
            final RectF rectF = new RectF(rect);

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(0xff424242);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmapOrg, rect, rect, paint);
            bitmapOrg.recycle();

            return output;
        } catch (OutOfMemoryError e) {
            ImageLoader.getInstance().oomHandled();
            return null;
        }
    }

    /**
     * Drawable转 Bitmap
     * 
     * @param drawable
     * @return
     */
    public static Bitmap drawable2Bitmap(Drawable drawable) {
        Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Config.ARGB_8888
                : Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private static int mustScaleComputeSampleSize(
            BitmapFactory.Options options, int width, int height) {
        double w = options.outWidth;
        double h = options.outHeight;
        return (int) Math.max(Math.ceil(w / width), Math.ceil(h / height));
    }

    private static int computeSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = initialSize;
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        if (roundedSize < 1) {
            roundedSize = 1;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.sqrt(w * h
                / maxNumOfPixels);
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
                Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    private static class FileTimeCompartor implements Comparator<File> {
        @Override
        public int compare(File object1, File object2) {
            if (object1.lastModified() - object2.lastModified() < 0) {
                return -1;
            } else if (object1.lastModified() - object2.lastModified() > 0) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
