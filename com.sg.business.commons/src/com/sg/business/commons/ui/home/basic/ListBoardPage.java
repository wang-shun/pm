package com.sg.business.commons.ui.home.basic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.bson.types.ObjectId;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.mobnut.db.DBActivator;
import com.mobnut.design.ICSSConstants;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sg.business.model.IModelConstants;
import com.sg.business.model.Organization;
import com.sg.business.model.Project;
import com.sg.business.model.User;
import com.sg.business.model.etl.ProjectETL;
import com.sg.business.model.toolkit.UserToolkit;
import com.sg.widgets.block.tab.TabBlockPage;

public class ListBoardPage extends TabBlockPage {

	public static final int BLOCKWIDTH = 300;
	private ListBoard viewer;
	private int year;
	private int month;
	private Organization org;
	private ObjectId user_id;
	private static final int limitNumber = 10;

	public ListBoardPage(Composite parent) {
		super(parent, SWT.NONE);
	}

	@Override
	protected void createContent(Composite parent) {
		init();
		parent.setLayout(new FormLayout());

		viewer = createListViewer(parent);

		createPageControl(parent);

	}

	private void createPageControl(Composite parent) {
		Button pageRight = new Button(parent, SWT.NONE);
		pageRight.setData(RWT.CUSTOM_VARIANT, ICSSConstants.CSS_RIGHT_24);

		FormData fd = new FormData();
		pageRight.setLayoutData(fd);
		fd.top = new FormAttachment(0, 4);
		fd.right = new FormAttachment(100, -12);
		fd.width = 24;
		fd.height = 24;
		pageRight.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				pageRight();
			}
		});

		Button pageLeft = new Button(parent, SWT.NONE);
		pageLeft.setData(RWT.CUSTOM_VARIANT, ICSSConstants.CSS_LEFT_24);

		fd = new FormData();
		pageLeft.setLayoutData(fd);
		fd.top = new FormAttachment(0, 4);
		fd.right = new FormAttachment(pageRight,-4);
		fd.width = 24;
		fd.height = 24;
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
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		calendar.add(Calendar.MONTH, -1);
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH) + 1;
		doRefresh();
	}

	protected void pageRight() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		calendar.add(Calendar.MONTH, 1);
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH) + 1;
		doRefresh();
	}

	private ListBoard createListViewer(Composite parent) {
		ListBoard tabItem = new ListBoard(parent);
		tabItem.setTitle("销售利润排名");
		tabItem.setLeftTitle("排名前十的项目负责人");
		tabItem.setRightTitle("排名前十的项目");
		return tabItem;
	}

	private void init() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		year = cal.get(Calendar.YEAR);
		// year = 2013;
		month = cal.get(Calendar.MONTH) + 1;
		String consignerId = context.getConsignerId();
		User user = UserToolkit.getUserById(consignerId);
		org = user.getFunctionOrganization();
		user_id = user.get_id();
	}

	@Override
	public boolean canRefresh() {
		return true;
	}

	@Override
	protected Object doGetData() {
		List<Object[]> input = new ArrayList<Object[]>();
		List<Object[]> topUserList = getTopListByCharger();
		input.addAll(topUserList);
		Object[] topOrgList = getTopListByPriject();
		input.add(topOrgList);
		return input;
	}

	private List<Object[]> getTopListByCharger() {
		List<Object[]> result = new ArrayList<Object[]>();
		String groupField = ProjectETL.F_MONTH_SALES_PROFIT;
		String groupByField = Project.F_CHARGER;
		BasicDBObject query = new BasicDBObject();
		query.put(
				"$match",
				new BasicDBObject().append(ProjectETL.F_YEAR, year)
						.append(ProjectETL.F_MONTH, month)
						.append(Project.F_FUNCTION_ORGANIZATION, org.get_id()));

		BasicDBObject group = new BasicDBObject();
		group.put(
				"$group",
				new BasicDBObject().append("_id", "$" + groupByField).append(
						groupField,
						new BasicDBObject().append("$sum", "$" + groupField)));

		BasicDBObject sort = new BasicDBObject();
		sort.put("$sort", new BasicDBObject().append(groupField, -1));

		BasicDBObject limit = new BasicDBObject();

		limit.put("$limit", limitNumber);

		DBCollection col = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_PROJECT_MONTH_DATA);
		AggregationOutput aggregationOutput = col.aggregate(query, group, sort,
				limit);
		Iterator<DBObject> iterator = aggregationOutput.results().iterator();
		Object[] userNumbers = new Object[1];
		Object[] results = new Object[limitNumber];
		for (int i = 0; i < results.length; i++) {
			if (iterator.hasNext()) {
				DBObject dbObject = (DBObject) iterator.next();
				Object _id = dbObject.get("_id");
				Double month_sales_profit = (Double) dbObject.get(groupField);
				results[i] = new Object[] { _id, month_sales_profit, i + 1 };
				if (user_id.equals(_id)) {
					userNumbers[0] = i;
				}
			} else {
				results[i] = new Object[] { null, null, i + 1 };
			}
		}
		int userNumber = limitNumber + 1;
		while (iterator.hasNext()) {
			DBObject dbObject = (DBObject) iterator.next();
			Object _id = dbObject.get("_id");
			if (user_id.equals(_id)) {
				userNumbers[0] = userNumber;
				break;
			} else {
				userNumber++;
			}
		}

		result.add(userNumbers);
		result.add(results);
		return result;
	}

	private Object[] getTopListByPriject() {
		String groupField = ProjectETL.F_MONTH_SALES_PROFIT;
		String groupByField = ProjectETL.F_PROJECTID;
		BasicDBObject query = new BasicDBObject();
		query.put(
				"$match",
				new BasicDBObject().append(ProjectETL.F_YEAR, year)
						.append(ProjectETL.F_MONTH, month)
						.append(Project.F_FUNCTION_ORGANIZATION, org.get_id()));

		BasicDBObject group = new BasicDBObject();
		group.put(
				"$group",
				new BasicDBObject().append("_id", "$" + groupByField).append(
						groupField,
						new BasicDBObject().append("$sum", "$" + groupField)));

		BasicDBObject sort = new BasicDBObject();
		sort.put("$sort", new BasicDBObject().append(groupField, -1));

		BasicDBObject limit = new BasicDBObject();

		limit.put("$limit", limitNumber);

		DBCollection col = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_PROJECT_MONTH_DATA);
		AggregationOutput aggregationOutput = col.aggregate(query, group, sort,
				limit);
		Iterator<DBObject> iterator = aggregationOutput.results().iterator();
		Object[] results = new Object[limitNumber];
		for (int i = 0; i < results.length; i++) {
			if (iterator.hasNext()) {
				DBObject dbObject = (DBObject) iterator.next();
				Object _id = dbObject.get("_id");
				Double month_sales_profit = (Double) dbObject.get(groupField);
				results[i] = new Object[] { _id, month_sales_profit, i + 1 };
			} else {
				results[i] = new Object[] { null, null, i + 1 };
			}
		}
		return results;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doDisplayData(Object data) {
		if (data instanceof List) {
			viewer.setInput((List<Object[]>) data);
			viewer.setYear(year);
			viewer.setMonth(month);
			viewer.setOrganization(org);
			viewer.updateLabel(locale);
		}
	}

	public int getContentHeight() {
		return 241;
	}
}
