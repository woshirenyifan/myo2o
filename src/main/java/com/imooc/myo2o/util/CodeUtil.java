package com.imooc.myo2o.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class CodeUtil {
	public static boolean checkVerifyCode(HttpServletRequest request) {
		String verifyCodeExpected = (String) request.getSession().getAttribute(
				com.google.code.kaptcha.Constants.KAPTCHA_SESSION_KEY);
		String verifyCodeActual = HttpServletRequestUtil.getString(request,
				"verifyCodeActual");
		if (verifyCodeActual == null
				|| !verifyCodeActual.equalsIgnoreCase(verifyCodeExpected)) {
			return false;
		}
		return true;
	}
	
	/**
	 * 生成二维码图片流
	 * @param content url链接
	 * @param res
	 * @return
	 */
	public static BitMatrix generateQRCodeStream(String content,HttpServletResponse res){
		// 给响应头添加头部信息,主要告诉浏览器返回的是图片流
		res.setHeader("Cache-Control", "no-store");
		res.setHeader("Pragma","no-cache");
		res.setDateHeader("Expires", 0);
		res.setContentType("image/png");
		
		// 设置图片的文字编码和内边框
		Map<EncodeHintType,Object> hints = new HashMap<>();
		hints.put(EncodeHintType.CHARACTER_SET,"UTF-8");
		hints.put(EncodeHintType.MARGIN,0);
		
		BitMatrix bitMatrix;
		try {
			bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 300, 300, hints);
		} catch (WriterException e) {
			e.printStackTrace();
			return null;
		}
		return bitMatrix;
	}
}
