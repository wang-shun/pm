package com.sg.business.visualization.view;

import java.util.List;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.jface.action.Action;

import com.sg.business.commons.ui.chart.CommonChart;
import com.sg.business.resource.nls.Messages;
import com.sg.business.visualization.action.ChartSeriesSwitchAction;
import com.sg.business.visualization.action.SetChartSubtypeToSidebySideAction;
import com.sg.business.visualization.action.SetChartSubtypeToStackedAction;
import com.sg.business.visualization.action.SetChartTypeToBarAction;
import com.sg.business.visualization.action.SetChartTypeToLineAction;

public class ProfitVolumnForSalesView extends AbstractDashChartView {

	@Override
	protected Chart getChartData() throws Exception {
		Messages messages = Messages.get(locale);
		String[] bsText = { messages.ProfitVolumnView_1,
				messages.ProfitVolumnView_0 };
		String[] xAxisText = new String[] { "1", "2", "3", "4", "5", "6", "7", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"8", "9", "10", "11", "12" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		double[][] value1 = projectProvider.getProfitAndCostByYear("sales");

		return CommonChart.getChart(xAxisText, bsText, value1,
				chartType, chartSubType,showSeriesLabel, - 5);
	}
	
	@Override
	protected void initChartParameters() {
		chartType = CommonChart.TYPE_BAR;
		chartSubType = CommonChart.TYPE_SUBTYPE_STACKED;
	}

	@Override
	protected List<Action> getActions() {
		List<Action> result = super.getActions();
		// ����ͼ������
		result.add(new SetChartTypeToBarAction(this));
		result.add(new SetChartTypeToLineAction(this));
		result.add(new SetChartSubtypeToSidebySideAction(this));
		result.add(new SetChartSubtypeToStackedAction(this));
		result.add(new ChartSeriesSwitchAction(this));

		return result;
	}

}
