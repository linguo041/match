package com.roy.football.match.OFN.out;

import com.roy.football.match.util.DateUtil;

import java.util.Date;
import java.util.List;

public class PlainDataFormater {

    public static String buildText (List<OFNExcelData> datas) {
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
            if (data != null) {
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
        }

        sb.append("</table>\n");

        return sb.toString();
    }
}
