package com.sg.business.visualization.view;

import org.eclipse.birt.chart.model.Chart;

import com.sg.business.visualization.chart.LineChart;
import com.sg.business.visualization.nls.Messages;

public class ProfitRateView extends AbstractDashChartView {

	@Override
	protected Chart getChartData() throws Exception {
		String[] lsText = { Messages.get(local).ProfitRateView_A_0 };
		String[] xAxisText = new String[] { "1", "2", "3", "4", "5", "6", "7", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"8", "9", "10", "11", "12" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		double[] value1 = projectProvider.getProfitRateByYear();

		return LineChart.getChart(xAxisText, lsText, new double[][] { value1 },
				-4);
	}

}
