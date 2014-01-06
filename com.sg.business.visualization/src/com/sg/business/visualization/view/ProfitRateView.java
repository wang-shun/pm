package com.sg.business.visualization.view;

import java.util.List;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.jface.action.Action;

import com.sg.business.visualization.action.ChangeChartTypeToBarAction;
import com.sg.business.visualization.action.ChangeChartTypeToLineAction;
import com.sg.business.visualization.action.ChartSeriesrSwitchAction;
import com.sg.business.visualization.chart.CommonChart;
import com.sg.business.visualization.nls.Messages;

public class ProfitRateView extends AbstractDashChartView {

	@Override
	protected Chart getChartData() throws Exception {

		String[] lsText = { Messages.get(locale).ProfitRateView_A_0 };
		String[] xAxisText = new String[] { "1", "2", "3", "4", "5", "6", "7", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"8", "9", "10", "11", "12" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		double[] value1 = projectProvider.getProfitRateByYear();

		return CommonChart.getChart(xAxisText, lsText,
				new double[][] { value1 }, chartType, chartSubType,
				showSeriesLabel, -4);
	}

	@Override
	protected void initChartParameters() {
		chartType = CommonChart.TYPE_LINE;
		chartSubType = CommonChart.TYPE_SUBTYPE_OVERLAY;
	}

	@Override
	protected List<Action> getActions() {
		List<Action> result = super.getActions();
		// ����ͼ������
		result.add(new ChangeChartTypeToBarAction(this));
		result.add(new ChangeChartTypeToLineAction(this));
		result.add(new ChartSeriesrSwitchAction(this));

		return result;
	}

}
