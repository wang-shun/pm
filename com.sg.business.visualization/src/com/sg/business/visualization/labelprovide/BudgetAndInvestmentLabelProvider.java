package com.sg.business.visualization.labelprovide;

import java.math.BigDecimal;
import java.text.NumberFormat;

import com.mobnut.commons.util.Utils;
import com.sg.business.model.Project;

public class BudgetAndInvestmentLabelProvider extends
		AbstractProjectLabelProvider {
	private static final int num = 6;// ����Ϊ5��

	@Override
	protected String getProjectText(Project project) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);

		// ��Ŀ��Ԥ��
		Double budgetValue = project.getBudgetValue();
		String bv = (budgetValue == null) ? "--" : (nf
				.format(budgetValue / 10000));

		// ��Ŀ���з��ɱ�
		Double investment = project.getInvestment();
		String iv = (investment == null) ? "0"
				: (nf.format(investment / 10000));

		StringBuffer sb = new StringBuffer();
		if (budgetValue != null && budgetValue.doubleValue() != 0) {
			// ��õ���ɱ���
			double ratio = investment / budgetValue;
			double _d = ratio > 1 ? 1 / ratio : ratio;
			int scale = new BigDecimal(_d * num).setScale(0,
					BigDecimal.ROUND_HALF_UP).intValue();

			// ����Ԥ����
			if (ratio > 1) {// ��֧��
				for (int i = 0; i < scale; i++) {
					String bar = TinyVisualizationUtil.getColorBar(i + 3,
							"blue", "16%", null, null, null, "14");
					sb.append(bar);
				}
			} else {
				for (int i = 0; i < num; i++) {
					String bar = TinyVisualizationUtil.getColorBar(i + 3,
							"blue", "16%", null, null, null, "14");
					sb.append(bar);
				}
			}

			sb.append("<br/>");

			// ����Ԥ����
			if (ratio > 1) {// ��֧��
				for (int i = 0; i < num; i++) {
					String bar = TinyVisualizationUtil.getColorBar(i + 3,
							"red", "16%", null, null, null, "14");
					sb.append(bar);
				}
			} else {
				for (int i = 0; i < scale; i++) {
					String bar = TinyVisualizationUtil.getColorBar(i + 3,
							"green", "16%", null, null, null, "10");
					sb.append(bar);
				}
			}

		}

		sb.append("<span style='FONT-FAMILY:΢���ź�;font-size:8pt;margin-left:0;word-break : break-all; white-space:normal; display:block; width=1000px'>");
		sb.append("ʵ��/Ԥ�� :");
		sb.append(iv);
		sb.append("/");
		sb.append(bv);

		if (budgetValue != null && budgetValue.doubleValue() != 0) {
			sb.append(" ");
			int ratio = new BigDecimal(100 * investment / budgetValue)
					.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
			sb.append(ratio);
			sb.append("%");
		}
		sb.append(" ");

		// ����״̬
		if (budgetValue == null) {

		} else if (budgetValue.doubleValue() < investment) {
			sb.append("<span style='color:" + Utils.COLOR_RED[10] + "'>");
			sb.append("��֧");
			sb.append("</span>");
		} else {
			boolean maybeOverCost = project.maybeOverCostNow();
			if (maybeOverCost) {
				sb.append("<span style='color:" + Utils.COLOR_YELLOW[10] + "'>");
				sb.append("��֧����");
				sb.append("</span>");
			}
		}
		sb.append("</span>");

		return sb.toString();
	}
}