package com.roy.football.match.OFN.out.weixin;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.roy.football.match.OFN.out.OFNExcelData;
import com.roy.football.match.OFN.response.OFNResponseWrapper;
import com.roy.football.match.httpRequest.HttpRequestException;
import com.roy.football.match.httpRequest.HttpRequestService;
import com.roy.football.match.util.DateUtil;
import com.roy.football.match.util.GsonConverter;
import com.roy.football.match.util.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.methods.HttpGet;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class WeiXinPubService {
    private final static String GET_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private final static String SEND_MESSAGE_TO_ALL_URL = "https://api.weixin.qq.com/cgi-bin/message/mass/sendall";

    private WeiXinConfig config;

    private final LoadingCache<String, String> tokenCache = CacheBuilder.newBuilder()
            .initialCapacity(1)
            .maximumSize(10)
            .expireAfterWrite(7000, TimeUnit.SECONDS)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String key) throws Exception {
                    return loadWXToken();
                }
            });

    @PostConstruct
    public void init() {
        String wxAppId = System.getProperty("wxAppId");
        if (StringUtils.isNoneEmpty(wxAppId)) {
            config = new WeiXinConfig();
            config.setAppId(wxAppId);
            config.setAppSecret(System.getProperty("wxAppSecret"));
        }
    }

    public void sendWXMessage (List<OFNExcelData> datas) throws Exception {
        String token = getWXToken();
        if (StringUtils.isEmpty(token)) {
            log.warn("Unable to get WX token.");
            return;
        }
        String url = SEND_MESSAGE_TO_ALL_URL+"?access_token="+token;

        String msg = buildText(datas);
        Map<String, String> headers = new HashMap<String, String>();

        JsonObject content = new JsonObject();

        JsonObject filter = new JsonObject();
        filter.addProperty("is_to_all", false);
        filter.addProperty("tag_id", "admin");
        content.add("filter", filter);

        content.addProperty("msgtype", "text");

        JsonObject data = new JsonObject();
        data.addProperty("content", msg);
        content.add("text", data);

        String response = HttpRequestService.getInstance()
                .doHttpRequest(url, HttpRequestService.POST_METHOD, content.toString(), headers);

        System.out.println(response);
    }

    private String buildText (List<OFNExcelData> datas) {
        StringBuilder sb = new StringBuilder();

        sb.append("<p>").append(DateUtil.formatSimpleDateWithSlash(new Date())).append("<p>");
        sb.append("<table>\n")
                .append("<tr><th width=\"5%\">Match Id</th>")
                .append("<th width=\"5%\">Match Time</th>")
                .append("<th width=\"5%\">League Name</th>")
                .append("<th width=\"5%\">Match Team</th>")
                .append("<th width=\"5%\">Level_all [w%, d%, wg#]</th>")
                .append("<th width=\"5%\">H:G_ha [attDef | winRt]</th>")
                .append("<th width=\"5%\">Hot_all | var H:G</th>")
                .append("<th width=\"5%\">Predict,Main,curr</th>")
                .append("<th width=\"5%\">K_PK[Up, Down]</th>")
                .append("<th width=\"7%\">will_avg will_chg</th>")
                .append("<th width=\"7%\">aomen_audit am_avg</th>")
                .append("<th width=\"6%\">avg jc chg</th>")
                .append("<th width=\"5%\">eu_var exg jc_gain</th>")
                .append("<th width=\"5%\">Kill[~pk !pl @plpk *pu]</th>")
                .append("<th width=\"5%\">Promote</th>")
                .append("<th width=\"5%\">Predict_S</th>")
                .append("<th width=\"5%\">Result</th>")
                .append("<th width=\"10%\">promote_ratio</th></tr>\n");

        for (OFNExcelData data : datas) {
            sb.append("<tr>")
                    .append("<td>").append(data.getMatchDayId()).append("</td>")
                    .append("<td>").append(data.getMatchTime()).append("</td>")
                    .append("<td>").append(data.getLeagueName()).append("</td>")
                    .append("<td>").append(data.getMatchInfor()).append("</td>")
                    .append("<td>").append(data.getLevel()).append("</td>")
                    .append("<td>").append(data.getStateVariation()).append("</td>")
                    .append("<td>").append(data.getOriginPanKou()).append("</td>")
                    .append("<td>").append(data.getPkKillRate()).append("</td>")
                    .append("<td>").append(data.getWill()).append("</td>")
                    .append("<td>").append(data.getAomen()).append("</td>")
                    .append("<td>").append(data.getJincai()).append("</td>")
                    .append("<td>").append(data.getJincaiJY()).append("</td>")
                    .append("<td>").append(data.getKill()).append("</td>")
                    .append("<td>").append(data.getPromote()).append("</td>")
                    .append("<td>").append(data.getPredictScore()).append("</td>")
                    .append("<td>").append(data.getResult()).append("</td>")
                    .append("<td>").append(data.getPromoteRatio()).append("</td>")
                    .append("</tr>\n");
        }

        sb.append("</table>\n");

        return sb.toString();
    }

    private String getWXToken () throws ExecutionException {
        return tokenCache.get("token");
    }

    private String loadWXToken () throws HttpRequestException {
        if (config == null) {
            return null;
        }

        Map<String, String> headers = new HashMap<String, String>();

        String url = GET_TOKEN_URL+"?grant_type=client_credential&appid="+config.getAppId()+"&secret="+config.getAppSecret();
        String resData = HttpRequestService.getInstance()
                .doHttpRequest(url, HttpRequestService.GET_METHOD, null, headers);

        WXTokenData token = GsonConverter.convertJSonToObjectUseNormal(resData,
                new TypeToken<WXTokenData>(){});
        if (token != null) {
            return token.getAccess_token();
        }

        return null;
    }

    @Data
    private static class WeiXinConfig {
        private String appId;
        private String appSecret;
    }

    public static void main(String[] args) throws Exception {
        WeiXinPubService service = new WeiXinPubService();
        service.init();

        String t = "this is test..";
        OFNExcelData d = new OFNExcelData();
        d.setMatchDayId(2323L);
        d.setAomen(t);
        d.setJincai(t);
        d.setBifa(t);
        d.setHostGuestComp(t);
        d.setJincaiJY(t);
        d.setLeagueName(t);
        d.setLevel(t);
        d.setMatchInfor(t);
        d.setPredictScore(t);
        d.setPromote(t);
        List<OFNExcelData> datas = Lists.newArrayList(d);

        service.sendWXMessage(datas);
    }
}
