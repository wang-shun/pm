package com.sg.business.visualization.view;

import org.eclipse.birt.chart.model.Chart;

import com.sg.business.commons.ui.chart.ProjectChartFactory;
import com.sg.business.model.ProjectProvider;

public class DashEcoRevenue extends DashSingleChart {

	@Override
	protected Chart getChartData(ProjectProvider projectProvider) {
		return ProjectChartFactory.getCostAndProfitBar(projectProvider);
	}


}
