package com.sg.business.visualization.action;

import org.eclipse.jface.action.Action;

import com.sg.business.resource.BusinessResource;
import com.sg.business.visualization.chart.CommonChart;
import com.sg.business.visualization.view.AbstractDashChartView;

public class SetChartSubtypeToOverlayAction extends Action {

	private AbstractDashChartView view;

	public SetChartSubtypeToOverlayAction(AbstractDashChartView view) {
		setToolTipText("��������ϵ����ʾ��ʽΪ������ʾ");
		setImageDescriptor(BusinessResource
				.getImageDescriptor(BusinessResource.IMAGE_CHART_SIDEBYSIDE_16));
		this.view = view;
	}

	@Override
	public void run() {
		view.setSubtype(CommonChart.TYPE_SUBTYPE_OVERLAY);
		view.doRefresh();
	}

}