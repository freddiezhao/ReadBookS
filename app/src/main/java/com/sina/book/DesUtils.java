package com.sina.book;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;

import android.util.Log;

public class DesUtils
{
	// DES默认秘钥
	private static String	strDefaultKey	= "sina";
	private Cipher			encryptCipher	= null;
	private Cipher			decryptCipher	= null;

	public static String byteArr2HexStr(byte[] arrB) throws Exception
	{
		int iLen = arrB.length;
		// 每个byte用两个字符才能表示，所以字符串的长度是数组长度的两倍
		StringBuffer sb = new StringBuffer(iLen * 2);
		for (int i = 0; i < iLen; i++) {
			int intTmp = arrB[i];
			// 把负数转换为正数
			while (intTmp < 0) {
				intTmp = intTmp + 256;
			}
			// 小于0F的数需要在前面补0
			if (intTmp < 16) {
				sb.append("0");
			}
			sb.append(Integer.toString(intTmp, 16));
		}
		return sb.toString();
	}

	public static byte[] hexStr2ByteArr(String strIn) throws Exception
	{
		byte[] arrB = strIn.getBytes();
		int iLen = arrB.length;
		// 两个字符表示一个字节，所以字节数组长度是字符串长度除以2
		byte[] arrOut = new byte[iLen / 2];
		for (int i = 0; i < iLen; i = i + 2) {
			String strTmp = new String(arrB, i, 2);
			arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
		}
		return arrOut;
	}

	public DesUtils()
	{
		this(strDefaultKey);
	}

	public DesUtils(String strKey)
	{
		// Security.addProvider(null);
		Key key;
		try {
			key = getKey(strKey.getBytes());
			encryptCipher = Cipher.getInstance("DES");
			encryptCipher.init(Cipher.ENCRYPT_MODE, key);
			decryptCipher = Cipher.getInstance("DES");
			decryptCipher.init(Cipher.DECRYPT_MODE, key);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public byte[] encrypt(byte[] arrB) throws Exception
	{
		return encryptCipher.doFinal(arrB);
	}

	public String encrypt(String strIn) throws Exception
	{
		return byteArr2HexStr(encrypt(strIn.getBytes()));
	}

	public byte[] decrypt(byte[] arrB) throws Exception
	{
		return decryptCipher.doFinal(arrB);
	}

	public String decrypt(String strIn) throws Exception
	{
		return new String(decrypt(hexStr2ByteArr(strIn)));
	}

	private Key getKey(byte[] arrBTmp) throws Exception
	{
		// 创建一个空的8位字节数组（默认值为0）
		byte[] arrB = new byte[8];
		// 将原始字节数组转换为8位
		for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {
			arrB[i] = arrBTmp[i];
		}
		// 生成密钥
		Key key = new javax.crypto.spec.SecretKeySpec(arrB, "DES");
		return key;
	}

	public static String encryptString(String text)
	{
		DesUtils des = new DesUtils();
		String newText = null;
		try {
			newText = des.encrypt(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newText;
	}

	public static String decryptString(String text)
	{
		DesUtils des = new DesUtils();
		String newText = null;
		try {
			newText = des.decrypt(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newText;
	}

	public static void test()
	{
		try {
			String test = "http://192.168.16.240:8082/epub/OEBPS/Styles/stylesheet.css";
			String test2 = "http://192.168.16.240:8082/epub/OEBPS/Styles/styleswwwwheet.jpg";
			// 注意这里，自定义的加密的KEY要和解密的KEY一致，这就是钥匙，如果你上锁了，却忘了钥匙，那么是解密不了的
			DesUtils des = new DesUtils("1");// 自定义密钥

			Log.d("ouyang", "加密前的字符：" + test);
			Log.d("ouyang", "加密后的字符：" + des.encrypt(test));
			Log.d("ouyang", "加密后的字符：" + des.encryptToSHA(test2));
			Log.d("ouyang", "解密后的字符：" + des.decrypt(des.encrypt(test)));
//			Log.d("ouyang", "解密后的字符：" + des.decrypt(des.encrypt(test2)));

			// System.out.println("加密前的字符：" + test);
			// System.out.println("加密后的字符：" + des.encrypt(test));
			// System.out.println("解密后的字符：" + des.decrypt(des.encrypt(test)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	/**
	 *  SHA加密
	 * @param info
	 * @return
	 */
	public String encryptToSHA(String info)
	{
		byte[] digesta = null;
		try {
			// 得到一个SHA-1的消息摘要
			MessageDigest alga = MessageDigest.getInstance("SHA-1");
			// 添加要进行计算摘要的信息
			alga.update(info.getBytes());
			// 得到该摘要
			digesta = alga.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		// 将摘要转为字符串
		String rs = byte2hex(digesta);
		return rs;
	}
	
	public String byte2hex(byte[] b) {       
        String hs = "";       
        String stmp = "";       
        for (int n = 0; n < b.length; n++) {       
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));       
            if (stmp.length() == 1) {       
                hs = hs + "0" + stmp;       
            } else {       
                hs = hs + stmp;       
            }       
        }       
        return hs.toUpperCase();       
    }       

	public byte[] hex2byte(String hex) {       
        byte[] ret = new byte[8];       
        byte[] tmp = hex.getBytes();       
        for (int i = 0; i < 8; i++) {       
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);       
        }       
        return ret;       
    }
	
	public static byte uniteBytes(byte src0, byte src1) {       
        byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))       
                .byteValue();       
        _b0 = (byte) (_b0 << 4);       
        byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))       
                .byteValue();       
        byte ret = (byte) (_b0 ^ _b1);       
        return ret;       
    } 

}