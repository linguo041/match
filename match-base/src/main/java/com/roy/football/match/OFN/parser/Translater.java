package com.roy.football.match.OFN.parser;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.gson.reflect.TypeToken;
import com.roy.football.match.httpRequest.HttpRequestService;
import com.roy.football.match.util.GsonConverter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class Translater {
	private final static String TRANS_API = "http://fanyi.youdao.com/openapi.do?keyfrom=f2ec-org&key=1787962561&type=data&doctype=json&version=1.1&q=";

	public String translate (String raw) {
		try {
			String resData = HttpRequestService.getInstance().doHttpRequest(TRANS_API + raw,
					HttpRequestService.GET_METHOD, null, new HashMap<String, String>());
			
			TransResult res = GsonConverter.convertJSonToObjectUseNormal(resData, new TypeToken<TransResult>(){});
			
			if (res != null && res.getErrorCode() == 0) {
				String translated = res.getTranslation().get(0);
				
				if (!raw.equals(translated)) {
					return translated;
				}
			}
		} catch (Exception e) {
			log.error("Unable to translate " + raw, e);
		}
		return null;
	}
	
	@Data
	public static class TransResult {
		private List<String> translation;
		private int errorCode;
	} 
	
	public static void main (String args []) {
		Translater t = new Translater();
		System.out.println(t.translate("Boca Juniors"));
	}
}
