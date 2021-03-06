package com.sg.business.commons.ui.home.basic;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.mobnut.commons.html.HtmlUtil;
import com.mobnut.db.DBActivator;
import com.mobnut.design.ICSSConstants;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.sg.business.commons.ui.UIFrameworkUtils;
import com.sg.business.commons.ui.chart.CommonChart;
import com.sg.business.model.IModelConstants;
import com.sg.business.model.Project;
import com.sg.business.model.Work;
import com.sg.business.model.etl.ProjectETL;
import com.sg.business.resource.nls.Messages;
import com.sg.widgets.birtcharts.ChartCanvas;
import com.sg.widgets.block.tab.TabBlockPage;

public class RevenuePage extends TabBlockPage implements
		ISelectionChangedListener {

	private Label textContent1;

	private Label textContent2;

	private ChartCanvas graphicContent;

	private Font font;

	private String userId;

	private DBCollection projectCol;

	private DBCollection projectMonthDataCol;

	private Calendar now;

	// private static String[] MONTHS = new String[] { "一月份", "二月份", "三月份",
	// "四月份",
	// "五月份", "六月份", "七月份", "八月份", "九月份", "十月份", "十一月份", "十二月份" };;

	// private static final String BLUE = "#33b5e5";
	//
	// private static final String ORANGE = "#ffbb33";
	//
	// private static final String RED = "#ff4444";

	public RevenuePage(Composite parent) {
		super(parent, SWT.NONE);
	}

	// 一月新增销售收入5万元，利润3万元
	// 累计实现销售收入300万元，利润100万元
	// 已完成的14个项目中，7个项目实现盈利，占比50%
	// 平均利润率：10%

	// 一月新增销售收入5万元/一月没有新增销收入，利润3万元/亏损10万元
	// 累计实现销售收入300万元，利润100万元/亏损12万元
	// 已完成的14个项目中/没有与已完成的项目，7个项目实现盈利/没有项目实现盈利，占比50%
	// 平均利润率：10%

	@Override
	protected void createContent(Composite parent) {
		init();
		parent.setLayout(new FormLayout());

		textContent1 = new Label(parent, SWT.NONE);
		HtmlUtil.enableMarkup(textContent1);

		FormData fd = new FormData();
		textContent1.setLayoutData(fd);
		fd.top = new FormAttachment();
		fd.left = new FormAttachment();
		fd.right = new FormAttachment(50);
		fd.height = 120;

		textContent2 = new Label(parent, SWT.NONE);
		HtmlUtil.enableMarkup(textContent2);

		fd = new FormData();
		textContent2.setLayoutData(fd);
		fd.top = new FormAttachment();
		fd.left = new FormAttachment(textContent1);
		fd.right = new FormAttachment(100);
		fd.height = 120;

		Label label = new Label(parent, SWT.NONE);
		HtmlUtil.enableMarkup(label);

		StringBuffer sb = new StringBuffer();
		sb.append("<div style='" + "font-family:微软雅黑;" + "margin:8;"
				+ "width:100%;" + "'>");
		sb.append("<div style='" + "font-size:11pt;" + "color:#6a6a6a;"
				+ "border-bottom:1px dotted #cdcdcd;"
				+ "display:-moz-inline-box; display:inline-block; "
				+ "height:100%;" + "width:580;" + "'>");
		sb.append("<span style='margin:0 0 0 8;'>");
		sb.append("项目各月销售利润");
		sb.append("</span></div></div>");
		label.setText(sb.toString());
		fd = new FormData();
		label.setLayoutData(fd);
		fd.top = new FormAttachment(textContent1);
		fd.left = new FormAttachment();
		fd.right = new FormAttachment(100);

		graphicContent = new ChartCanvas(parent, SWT.NONE, false);
		fd = new FormData();
		graphicContent.setLayoutData(fd);
		fd.top = new FormAttachment(label);
		fd.left = new FormAttachment();
		fd.right = new FormAttachment(100);
		fd.bottom = new FormAttachment(100);

		createPageControl(parent);

	}

	private void createPageControl(Composite parent) {
		Button pageRight = new Button(parent, SWT.NONE);
		pageRight.setData(RWT.CUSTOM_VARIANT, ICSSConstants.CSS_RIGHT2_48);

		FormData fd = new FormData();
		pageRight.setLayoutData(fd);
		fd.top = new FormAttachment(50, -24);
		fd.right = new FormAttachment(100, -1);
		fd.width = 48;
		fd.height = 48;
		pageRight.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pageRight();
			}
		});

		Button pageLeft = new Button(parent, SWT.NONE);
		pageLeft.setData(RWT.CUSTOM_VARIANT, ICSSConstants.CSS_LEFT2_48);

		fd = new FormData();
		pageLeft.setLayoutData(fd);
		fd.top = new FormAttachment(50, -24);
		fd.left = new FormAttachment(0, 1);
		fd.width = 48;
		fd.height = 48;
		pageLeft.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pageLeft();
			}
		});
		pageRight.moveAbove(null);
		pageLeft.moveAbove(null);
	}

	protected void pageLeft() {
		now.add(Calendar.MONTH, -1);
		doRefresh();
	}

	protected void pageRight() {
		now.add(Calendar.MONTH, 1);
		doRefresh();
	}

	private void init() {

		font = new Font(getDisplay(), "微软雅黑", 16, SWT.NORMAL);

		userId = context.getConsignerId();

		projectCol = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_PROJECT);

		projectMonthDataCol = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_PROJECT_MONTH_DATA);

		now = Calendar.getInstance();

	}

	@Override
	public void dispose() {
		font.dispose();
		super.dispose();
	}

	@Override
	public boolean canRefresh() {
		return true;
	}

	@Override
	protected Object doGetData() {
		double[] sumSales = getProjectETL();

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
		cal.set(Calendar.YEAR, now.get(Calendar.YEAR));

		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.DAY_OF_MONTH, -1);

		double[] monthSales = getProjectMonthETL(cal, sumSales);

		BigDecimal d = new BigDecimal((sumSales[0]) / 10000d);
		sumSales[0] = d.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

		d = new BigDecimal((sumSales[1]) / 10000d);
		sumSales[1] = d.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

		d = new BigDecimal((sumSales[2]));
		sumSales[2] = d.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

		d = new BigDecimal((sumSales[4]));
		sumSales[4] = d.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

		/*
		 * 获取当前的月份
		 */
		// String month = MONTHS[cal.get(Calendar.MONTH)];
		String month = "" + (cal.get(Calendar.MONTH) + 1);
		String year = "" + (cal.get(Calendar.YEAR));
		/*
		 * 获取当前的月份，当前用户所有项目的销售收入
		 */
		String monthRevenue = "" + monthSales[0];

		/*
		 * 获取当前的月份，当前用户所有项目的销售利润
		 */
		String monthProfit = "" + monthSales[1];

		/*
		 * 获取当前的月份，当前用户所有项目的销售利润率
		 */
		String monthProfitRate = "" + monthSales[2] + "%";

		/*
		 * 获取累计的，当前用户所有项目的销售收入
		 */

		String sumRevenue = "" + sumSales[0];

		/*
		 * 获取累计的，当前用户所有项目的销售利润
		 */
		String sumProfit = "" + sumSales[1];

		/*
		 * 获取累计的，当前用户所有项目的平均利润率
		 */
		String avgProfitRate = "" + sumSales[2] + "%";

		/*
		 * 获取累计的，当前用户的盈利项目个数
		 */
		String sumProfitable = "" + sumSales[3];

		/*
		 * 获取累计的，当前用户的盈利项目个数/总项目个数
		 */
		String avgsumProfitableRate = "" + sumSales[4] + "%";

		/*
		 * 获取当年每个月的项目销售利润
		 */
		double[] profitEveryMonth = new double[] { monthSales[3],
				monthSales[4], monthSales[5], monthSales[6], monthSales[7],
				monthSales[8], monthSales[9], monthSales[10], monthSales[11],
				monthSales[12], monthSales[13], monthSales[14] };

		HashMap<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("year", year);
		dataMap.put("month", month);
		dataMap.put("monthRevenue", monthRevenue);
		dataMap.put("monthProfit", monthProfit);
		dataMap.put("monthProfitRate", monthProfitRate);
		dataMap.put("sumRevenue", sumRevenue);
		dataMap.put("sumProfit", sumProfit);
		dataMap.put("avgProfitRate", avgProfitRate);
		dataMap.put("sumProfitable", sumProfitable);
		dataMap.put("avgsumProfitableRate", avgsumProfitableRate);
		dataMap.put("profitEveryMonth", profitEveryMonth);
		return dataMap;
	}

	private double[] getProjectMonthETL(Calendar cal, double[] sumSales) {
		double[] monthSales = new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0 };
		BasicDBObject query = new BasicDBObject();
		query.put(
				"$or",
				new BasicDBObject[] {
						new BasicDBObject().append(Project.F_CHARGER, userId),
						new BasicDBObject().append(Project.F_BUSINESS_CHARGER,
								userId) });
		query.put(ProjectETL.F_YEAR, cal.get(Calendar.YEAR));
		query.put(ProjectETL.F_MONTH, cal.get(Calendar.MONTH) + 1);
		DBCursor projectMonthCursor = projectMonthDataCol.find(query);
		while (projectMonthCursor.hasNext()) {
			DBObject dbo = projectMonthCursor.next();
			double salesRevenue = (double) dbo
					.get(ProjectETL.F_MONTH_SALES_REVENUE);
			double salesProfit = (double) dbo
					.get(ProjectETL.F_MONTH_SALES_PROFIT);
			monthSales[0] = monthSales[0] + salesRevenue;
			monthSales[1] = monthSales[1] + salesProfit;
		}
		monthSales[2] = monthSales[0] != 0 ? (monthSales[1] / monthSales[0] * 100)
				: 0;

		query = new BasicDBObject();
		query.put(
				"$match",
				new BasicDBObject().append(ProjectETL.F_YEAR,
						cal.get(Calendar.YEAR)).append(
						"$or",
						new BasicDBObject[] {
								new BasicDBObject().append(Project.F_CHARGER,
										userId),
								new BasicDBObject().append(
										Project.F_BUSINESS_CHARGER, userId) }));

		BasicDBObject group = new BasicDBObject();
		group.put(
				"$group",
				new BasicDBObject().append("_id", "$month").append(
						ProjectETL.F_MONTH_SALES_PROFIT,
						new BasicDBObject().append("$sum", "$"
								+ ProjectETL.F_MONTH_SALES_PROFIT)));

		AggregationOutput aggregationOutput = projectMonthDataCol.aggregate(
				query, group);

		Iterator<DBObject> iterator = aggregationOutput.results().iterator();
		while (iterator.hasNext()) {
			DBObject dbObject = (DBObject) iterator.next();
			int month = (int) dbObject.get("_id");
			monthSales[month + 2] = ((Number) dbObject
					.get(ProjectETL.F_MONTH_SALES_PROFIT)).doubleValue();
		}
		for (int i = 0; i < monthSales.length; i++) {
			if (monthSales[i] != 0d && i != 2) {
				BigDecimal d = new BigDecimal((monthSales[i]) / 10000d);
				monthSales[i] = d.setScale(2, BigDecimal.ROUND_HALF_UP)
						.doubleValue();
			} else if (i == 2) {
				if (monthSales[i] != 0d) {
					BigDecimal d = new BigDecimal(monthSales[i]);
					monthSales[i] = d.setScale(2, BigDecimal.ROUND_HALF_UP)
							.doubleValue();
				}
			}
		}

		return monthSales;
	}

	private double[] getProjectETL() {
		double[] sumSales = new double[] { 0, 0, 0, 0, 0 };
		BasicDBObject query = new BasicDBObject();
		query.put(
				"$or",
				new BasicDBObject[] {
						new BasicDBObject().append(Project.F_CHARGER, userId),
						new BasicDBObject().append(Project.F_BUSINESS_CHARGER,
								userId) });
		DBCursor projectCursor = projectCol.find(query);
		sumSales[4] = projectCursor.size();
		while (projectCursor.hasNext()) {
			DBObject dbo = projectCursor.next();
			Object etl = dbo.get(ProjectETL.F_ETL);
			if (etl != null && etl instanceof DBObject) {
				Object sales_revenue = ((DBObject) etl)
						.get(ProjectETL.F_SALES_REVENUE);
				double salesRevenue = (double) (sales_revenue != null ? sales_revenue
						: 0d);
				Object sales_cost = ((DBObject) etl)
						.get(ProjectETL.F_SALES_COST);
				double salesCost = (double) (sales_cost != null ? sales_cost
						: 0d);
				double salesProfit = salesRevenue - salesCost;
				sumSales[0] = sumSales[0] + salesRevenue;
				sumSales[1] = sumSales[1] + salesProfit;
				if (salesProfit > 0) {
					sumSales[3] = sumSales[3] + 1;
				}
			}

		}
		sumSales[2] = sumSales[0] != 0 ? (sumSales[1] / sumSales[0] * 100) : 0;
		sumSales[4] = sumSales[4] != 0 ? (sumSales[3] / sumSales[4] * 100) : 0;
		return sumSales;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doDisplayData(Object data) {
		String year = "";
		String month = "";
		String monthRevenue = "";
		String monthProfit = "";
		String monthProfitRate = "";
		String sumRevenue = "";
		String sumProfit = "";
		String avgProfitRate = "";
		String sumProfitable = "";
		String avgsumProfitableRate = "";
		double[] profitEveryMonth = new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0 };

		HashMap<String, Object> dataMap = (HashMap<String, Object>) data;
		if (dataMap != null) {
			year = (String) dataMap.get("year");
			month = (String) dataMap.get("month");
			monthRevenue = (String) dataMap.get("monthRevenue");
			monthProfit = (String) dataMap.get("monthProfit");
			monthProfitRate = (String) dataMap.get("monthProfitRate");
			sumRevenue = (String) dataMap.get("sumRevenue");
			sumProfit = (String) dataMap.get("sumProfit");
			avgProfitRate = (String) dataMap.get("avgProfitRate");
			sumProfitable = (String) dataMap.get("sumProfitable");
			avgsumProfitableRate = (String) dataMap.get("avgsumProfitableRate");
			profitEveryMonth = (double[]) dataMap.get("profitEveryMonth");

		}

		StringBuffer sb = new StringBuffer();
		sb.append("<div style='" + "font-family:微软雅黑;" + "margin:8;"
				+ "width:100%;" + "'>");
		sb.append("<div style='" + "font-size:11pt;" + "color:#6a6a6a;"
				+ "border-bottom:1px dotted #cdcdcd;"
				+ "display:-moz-inline-box; display:inline-block; "
				+ "height:100%;" + "width:280;" + "'>");
		sb.append("<span style='margin:0 0 0 8;'>");
		sb.append(year);
		sb.append("年");
		sb.append(month);
		sb.append("月");
		sb.append("项目各项经济指标");
		sb.append("</span>");
		sb.append("</div>");

		sb.append("<div style='" + "font-size:9pt;" + "color:#909090;"
				+ "margin:8 0 0 8;" + "'>");
		sb.append("<span style='width:100'>");
		sb.append("新增销售收入：");
		sb.append("</span><span>");
		sb.append(monthRevenue);
		sb.append("万元");
		sb.append("</span>");
		sb.append("</div>");

		sb.append("<div style='" + "font-size:9pt;" + "color:#909090;"
				+ "margin:0 0 0 8;" + "'>");
		sb.append("<span style='width:100'>");
		sb.append("实现销售利润：");
		sb.append("</span><span>");
		sb.append(monthProfit);
		sb.append("万元");
		sb.append("</span>");
		sb.append("</div>");

		sb.append("<div style='" + "font-size:9pt;" + "color:#909090;"
				+ "margin:0 0 0 8;" + "'>");
		sb.append("<span style='width:100'>");
		sb.append("平均利润率：");
		sb.append("</span><span>");
		sb.append(monthProfitRate);
		sb.append("</span>");
		sb.append("</div>");

		sb.append("</div>");
		textContent1.setText(sb.toString());

		sb = new StringBuffer();
		sb.append("<div style='" + "font-family:微软雅黑;" + "margin:8;"
				+ "width:100%;" + "'>");
		sb.append("<div style='" + "font-size:11pt;" + "color:#6a6a6a;"
				+ "border-bottom:1px dotted #cdcdcd;"
				+ "display:-moz-inline-box; display:inline-block; "
				+ "height:100%;" + "width:280;" + "'>");
		sb.append("<span style='margin:0 0 0 8;'>");
		sb.append("已完成项目累计经济指标");
		sb.append("</span>");
		sb.append("</div>");

		sb.append("<div style='" + "font-size:9pt;" + "color:#909090;"
				+ "margin:8 0 0 8;" + "'>");
		sb.append("<span style='width:100'>");
		sb.append("累计销售收入：");
		sb.append("</span><span>");
		sb.append(sumRevenue);
		sb.append("万元");
		sb.append("</span>");
		sb.append("</div>");

		sb.append("<div style='" + "font-size:9pt;" + "color:#909090;"
				+ "margin:0 0 0 8;" + "'>");
		sb.append("<span style='width:100'>");
		sb.append("累计销售利润：");
		sb.append("</span><span>");
		sb.append(sumProfit);
		sb.append("万元");
		sb.append("</span>");
		sb.append("</div>");

		sb.append("<div style='" + "font-size:9pt;" + "color:#909090;"
				+ "margin:0 0 0 8;" + "'>");
		sb.append("<span style='width:100'>");
		sb.append("平均利润率：");
		sb.append("</span><span>");
		sb.append(avgProfitRate);
		sb.append("</span>");
		sb.append("</div>");

		sb.append("<div style='" + "font-size:9pt;" + "color:#909090;"
				+ "margin:0 0 0 8;" + "'>");
		sb.append("<span style='width:100'>");
		sb.append("盈利项目数：");
		sb.append("</span><span style='width:60'>");
		sb.append(sumProfitable);
		sb.append("</span>");
		sb.append("<span style='width:40'>");
		sb.append("占比：");
		sb.append("</span><span>");
		sb.append(avgsumProfitableRate);
		sb.append("</span>");
		sb.append("</div>");

		sb.append("</div>");
		textContent2.setText(sb.toString());

		setGraphicContent(profitEveryMonth);

	}

	private void setGraphicContent(double[] values) {
		Messages messages = Messages.get(locale);
		String[] lsText = { messages.ProfitVolumnView_0 };
		String[] xAxisText = new String[] { "1", "2", "3", "4", "5", "6", "7", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"8", "9", "10", "11", "12" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		Chart chart = CommonChart.getChart(xAxisText, lsText,
				new double[][] { values }, CommonChart.TYPE_BAR,
				CommonChart.TYPE_SUBTYPE_STACKED, false, 10);
		chart.getLegend().setVisible(false);
		graphicContent.setChart(chart);
		try {
			graphicContent.redrawChart(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		IStructuredSelection sel = (IStructuredSelection) event.getSelection();
		if (sel != null && !sel.isEmpty()) {
			Work work = (Work) sel.getFirstElement();
			select(work);
		}
	}

	protected void select(Work work) {
		UIFrameworkUtils.navigateTo(work, UIFrameworkUtils.NAVIGATE_AUTOSELECT,
				false);
	}

}
