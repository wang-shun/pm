package com.sg.business.visualization.editor.projectset;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.sg.business.visualization.chart.ProjectChartFactory;
import com.sg.widgets.MessageUtil;
import com.sg.widgets.birtcharts.ChartCanvas;
import com.sg.widgets.viewer.ViewerControl;

public class ProjectSetDashboardRevenue extends AbstractProjectSetPage {

	private ChartCanvas revenuePieChart;

	// ������
	private ChartCanvas profitRateMeter;
	
	// ROI
	private ChartCanvas ROIMeter;

	private ChartCanvas deptProjectBar;
	private ChartCanvas pmProjectBar;

	private CTabItem deptBarTabItem;
	private CTabItem pmBarTabItem;
	private CTabFolder tabFolder;

	@Override
	protected Composite createContent(Composite body) {
		SashForm content = new SashForm(body, SWT.HORIZONTAL);
		Composite tableContent = new Composite(content, SWT.NONE);
		navi.createPartContent(tableContent);

		Composite graphicContent = new Composite(content, SWT.NONE);
		graphicContent.setLayout(new FillLayout());
		createGraphic(graphicContent);

		content.setWeights(new int[] { 3, 2 });
		return content;
	}

	@Override
	protected String getProjectSetPageLabel() {
		String projectSetName = data.getProjectSetName();
		StringBuffer sb = new StringBuffer();
		sb.append("<span style='FONT-FAMILY:΢���ź�;font-size:13pt'>");
		sb.append(projectSetName + " ��Ŀ�������������״��");
		sb.append("</span>");
		return sb.toString();
	}

	private void createGraphic(Composite parent) {

		tabFolder = new CTabFolder(parent, SWT.TOP);
		CTabItem pieTabItem = new CTabItem(tabFolder, SWT.NONE);
		pieTabItem.setText("���۳ɱ�������");
		revenuePieChart = new ChartCanvas(tabFolder, SWT.NONE) {
			@Override
			public Chart getChart() {
				return ProjectChartFactory.getCostAndProfitBar(data);
			}
		};
		pieTabItem.setControl(revenuePieChart);

		CTabItem meterTabItem = new CTabItem(tabFolder, SWT.NONE);
		meterTabItem.setText("�Ǳ���");
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new GridLayout());
		profitRateMeter = new ChartCanvas(composite, SWT.NONE) {
			@Override
			public Chart getChart() {
				return ProjectChartFactory.getProfitRateMeter(data);
			}
		};
		profitRateMeter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));


		ROIMeter = new ChartCanvas(composite, SWT.NONE) {
			@Override
			public Chart getChart() {
				return ProjectChartFactory.getROIMeter(data);
			}
		};
		ROIMeter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));

		meterTabItem.setControl(composite);

		loadChartData();
		
		tabFolder.setSelection(0);
	}

	@Override
	public void parameterChanged(Object[] oldParameters, Object[] newParameters) {
		if (navi != null) {
			ViewerControl viewerControl = navi.getViewerControl();
			if (!viewerControl.getControl().isDisposed()) {
				viewerControl.doReloadData(true, new Runnable() {

					@Override
					public void run() {
						loadChartData();
						redrawChart();
					}
				});
			}
		}
		if (filterLabel != null && !filterLabel.isDisposed()) {
			filterLabel.setText(getParameterText());
			header.layout();
		}

	}

	private void loadChartData() {

		// *****************************************************************************************
		String[] deptParameter = new String[data.sum.subOrganizationProjectProvider
				.size()];
		if (deptParameter.length != 0) {
			if (deptBarTabItem == null) {
				deptBarTabItem = new CTabItem(tabFolder, SWT.NONE);
				deptBarTabItem.setText("��Ŀ�е�����");
				deptProjectBar = new ChartCanvas(tabFolder, SWT.NONE) {
					@Override
					public Chart getChart() {
						return ProjectChartFactory.getDeptRevenueBar(data);
					}
				};
				deptBarTabItem.setControl(deptProjectBar);
			}
		}
		// *****************************************************************************************
		String[] chargerName = new String[data.sum.subChargerProjectProvider
				.size()];
		if (chargerName.length != 0) {
			if (pmBarTabItem == null) {
				pmBarTabItem = new CTabItem(tabFolder, SWT.NONE);
				pmBarTabItem.setText("��Ŀ����");
				pmProjectBar = new ChartCanvas(tabFolder, SWT.NONE) {
					@Override
					public Chart getChart() {
						return ProjectChartFactory.getProjectChargerRevenueBar(data);
					}
				};
				pmBarTabItem.setControl(pmProjectBar);
			}
		}
	}

	private void redrawChart() {
		try {
			ROIMeter.redrawChart();
			deptProjectBar.redrawChart();
			profitRateMeter.redrawChart();
			revenuePieChart.redrawChart();
		} catch (Exception e) {
			MessageUtil.showToast(e);
		}
	}

}