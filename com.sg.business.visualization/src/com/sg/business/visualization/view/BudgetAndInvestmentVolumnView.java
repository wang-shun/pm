package com.sg.business.visualization.view;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.sg.business.visualization.chart.BarChart;
import com.sg.business.visualization.nls.Messages;
import com.sg.widgets.birtcharts.ChartCanvas;

public class BudgetAndInvestmentVolumnView extends AbstractDashWidgetView {

	private ChartCanvas chart;

	@Override
	protected void drawContent(Composite parent) {
		parent.setLayout(new GridLayout(1, true));

		chart = new ChartCanvas(parent, SWT.NONE) {
			@Override
			public Chart getChart() throws Exception {

				return BudgetAndInvestmentVolumnView.this.getChart();
			}
		};
		layout(chart, 1, 1);
		parent.layout();
	}

	private Chart getChart() throws Exception {
		Messages messages = Messages.get(chart.getDisplay());
		String[] bsText = { messages.BudgetAndInvestmentVolumnView_A_0,
				messages.BudgetAndInvestmentVolumnView_A_1 };
		String[] xAxisText = new String[] { "1", "2", "3", "4", "5", "6", "7", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"8", "9", "10", "11", "12" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		double[] value1 = projectProvider
				.getValueByYear(new String[] { "budget" }); //$NON-NLS-1$
		double[] value2 = projectProvider.getValueByYear(new String[] {
				"investment_designated", "investment_allocated" }); //$NON-NLS-1$ //$NON-NLS-2$

		return BarChart.getChart(xAxisText, bsText, new double[][] { value1,
				value2 }, "Side-by-side", -1); //$NON-NLS-1$
	}
}
