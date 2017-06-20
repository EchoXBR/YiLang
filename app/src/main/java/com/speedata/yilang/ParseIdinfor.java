package com.speedata.yilang;

//import com.cyber.tools.GetSqdmByRight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class ParseIdinfor {


	/**
	 * 替换字符串空白部分为空格
	 *
	 * @param code
	 * @return
	 */
	public static String cutCode(String code) {
		String result = code;
		String temporary;
		for (int i = 0; i < code.length() / 4; i++) {
			temporary = code.substring(i * 4, (i + 1) * 4).replace("FFFF",
					"2020").replace("20FF", "2020").replace("FF20", "2020")
					.replace("F20F", "2020").replace("\uF8F5\uF8F5\uF8F5\uF8F5","2020");
			result = result.substring(0, i * 4) + temporary
					+ result.substring((i + 1) * 4);
		}
		return result;

	}

	/**
	 * 替换字符串FF部分为空格
	 * 适用于CPU卡
	 * @param code
	 * @return
	 */
	public static String cutCode1(String code) {
		String result = "";
		String temporary;
		for (int i = 0; i < code.length();i++) {
			if(String.valueOf(code.charAt(i)).equalsIgnoreCase("F") ){
				temporary = code.substring(i ,i+1).replace("F","");
				result += temporary;
			}else{
				temporary = code.substring(i ,i+1);
				result += temporary;
			}
//			if(String.valueOf(code.charAt(i)).equalsIgnoreCase("F") &&String.valueOf(code.charAt(i+1)).equalsIgnoreCase("F")){
//				temporary = code.substring(i , i + 2).replace("FF","").replace("20", "");
//				result += temporary;
//			}else{
//				temporary = code.substring(i , i + 2).trim();
//				result += temporary;
//			}
//			i+=2;
		}
//		System.out.println(result);
		return result;

	}

	/**
	 * 过滤字符串乱码
	 *
	 * @param str
	 * @return
	 */
	public static String stringFilter(String str) {
		str = str
				.replaceAll(
						"[^(\u4e00-\u9fa5)|(\u0030-\u0039)|(\u0061-\u007a)|(\u0041-\u005a)|(\u0028-\u003A)]",
						"");// 过滤
		return str;
	}
	public String readFile(){
		StringBuffer stringBuffer=new StringBuffer();
		try {
			String encoding="GBK";
			String pathName="/sdcard/test/";
			String fileName="file.txt";
			File file=new File(pathName+fileName);
			if(file.isFile() && file.exists()){ //判断文件是否存在
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file),encoding);//考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while((lineTxt = bufferedReader.readLine()) != null){
					System.out.println(lineTxt);
					stringBuffer.append(lineTxt);
				}
				read.close();
			}else{
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		return stringBuffer.toString();
	}
}
