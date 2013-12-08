package com.sg.business.visualization.labelprovide;

import java.math.BigDecimal;

import com.mobnut.commons.util.Utils;
import com.sg.business.model.Project;
import com.sg.business.model.etl.ProjectPresentation;

public class ProfitDetailLabelProvider extends AbstractProjectLabelProvider {

	@Override
	protected String getProjectText(Project project) {
		ProjectPresentation pres = project.getPresentation();

		StringBuffer sb = new StringBuffer();
		sb.append("<span style='FONT-FAMILY:΢���ź�;font-size:8pt;'>");
		// ��ĩ
		double salesRevenue = pres.getSalesRevenue();
		double totalCost = pres.getSalesCost() + pres.getInvestment();

		// ��ĩ����
		double profit = salesRevenue - totalCost;

		sb.append("<span style='color="
				+ (profit > 0?Utils.COLOR_GREEN[10]:Utils.COLOR_RED[10])
				+ ";display:block; text-align:right;font-weight:bold;font-size:10pt;'>");
		sb.append(getCurrency(profit,10));
		sb.append("</span>");
		
		if (profit > 0 && salesRevenue > 0) {
			sb.append("<span style='color="
					+ Utils.COLOR_BLUE[10]
					+ ";font-size:9pt;margin-left:0;"
					+ "word-break : break-all; white-space:normal; display:block; text-align:right;'>");
			double value = new BigDecimal(100 * profit / salesRevenue).setScale(2,
					BigDecimal.ROUND_HALF_UP).doubleValue();
			sb.append(value);
			sb.append("%");
			sb.append("</span>");
		}
		return sb.toString();
	}

}
