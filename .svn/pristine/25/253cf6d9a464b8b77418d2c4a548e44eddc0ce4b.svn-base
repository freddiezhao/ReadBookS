package com.sina.book.util;

/**
 * 数值辅助类
 * 
 * @author 杨芹勍
 */
public final class NumericHelper {

	// /**
	// * 解析css尺寸
	// * @param value
	// * @param defFont
	// * @param parentWidth
	// * @return
	// */
	// public final static int parseCssDimension(String value, Font defFont, int
	// parentWidth) {
	//
	// // 百分比
	// if (StringHelper.endWithIgnoreCase(value, "%")) {
	// String valstr = value.substring(0, value.length() - 1);
	// int percentValue = Integer.parseInt(valstr);
	//
	// return parentWidth * percentValue / 100;
	// } else
	// // 判断结尾是否px、pt、em
	// if (StringHelper.endWithIgnoreCase(value, "pt")) {
	// String valstr = value.substring(0, value.length() - 2);
	// int ptValue = Integer.parseInt(valstr);
	//
	// return ptValue * 4 / 3;
	// } else if (StringHelper.endWithIgnoreCase(value, "em")) {
	// String valstr = value.substring(0, value.length() - 2);
	//
	// // 判断是否有小数点，则用Float类进行转换
	// int posIdx = valstr.indexOf('.');
	// if (posIdx >= 0) {
	// Float emValue = Float.parse(valstr, 10);
	// emValue = emValue.Mul(defFont.getHeight());
	//
	// long result = emValue.toLong();
	// emValue = null;
	//
	// return (int) result;
	// } else {
	// return Integer.parseInt(valstr) * defFont.getHeight();
	// }

	public final static int parseInt(String val) {
		if ("-1".equals(val)) {
			return -1;
		}
		return parseInt(val, 0);
	}

	/**
	 * 解析字符串成整形变量
	 * 
	 * @param val
	 *            字符串值
	 * @return 整形变量结果
	 */
	public final static int parseInt(String val, int emptyVal) {
		if (val == null)
			return emptyVal;

		// 去除尾部的字符
		int size = val.length();

		if (size == 0) {
			return emptyVal;
		} else {
			int n = 0;
			for (; n < size; ++n) {
				char partVal = val.charAt(n);
				if (partVal < 48 || partVal > 57) {
					break;
				}
			}

			try {
				String str = val.substring(0, n);

				if (str.length() == 0) {
					return emptyVal;
				}

				int result = Integer.parseInt(str);
				return result;
			} catch (Exception ex) {
				ex.printStackTrace();
				return emptyVal;
			}
		}
	}

	/**
	 * 判断字符串是否数字
	 * 
	 * @param str
	 *            字符串
	 * @return 是否为数字
	 */
	public final static boolean isNumeric(String str) {
		int len = str.length();
		for (int n = 0; n < len; n++) {
			char ch = str.charAt(n);
			if (!java.lang.Character.isDigit(ch)) {
				return false;
			}
		}

		return true;
	}

	// /**
	// * 转换数字至字符串，并在字符串前加上指定个数的“0” 如: numberFillZero(3, 4); 获得: 0003
	// * @param num 数字
	// * @param digit 深度
	// * @return 返回字符串
	// */
	// public final static String numberFillZero(int num, int digit) {
	// int nBase = 1;
	// int i;
	// for (i = 0; i < (digit - 1); ++i) {
	// nBase *= 10;
	// }
	//
	// String strNum = "";
	// for (; nBase != 0;) {
	// strNum = strNum + (num / nBase);
	//
	// num = num % nBase;
	// nBase /= 10;
	// }
	//
	// return strNum;
	// }

}