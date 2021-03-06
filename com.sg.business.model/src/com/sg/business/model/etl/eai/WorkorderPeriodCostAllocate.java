package com.sg.business.model.etl.eai;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mobnut.commons.util.Utils;
import com.mobnut.db.DBActivator;
import com.mobnut.db.model.ModelService;
import com.mobnut.db.model.PrimaryObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.sg.business.model.IModelConstants;
import com.sg.business.model.Organization;
import com.sg.business.model.Project;
import com.sg.business.model.RNDPeriodCost;
import com.sg.business.model.WorkOrderPeriodCost;
import com.sg.business.model.WorksPerformence;
import com.sg.business.resource.nls.Messages;

public class WorkorderPeriodCostAllocate {

	public static final String COSECENTERCODE = "cost"; //$NON-NLS-1$
	public static final String ACCOUNTNUMERS = "account"; //$NON-NLS-1$
	public static final String YEAR = "year"; //$NON-NLS-1$
	public static final String MONTH = "month"; //$NON-NLS-1$
	public static final String RNDCOST = "rndcost"; //$NON-NLS-1$
	private DBCollection costAllocateCol;
	private DBCollection workPerformenceCol;
	private DBCollection projectCol;

	public WorkorderPeriodCostAllocate() {
		costAllocateCol = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_RND_PEROIDCOST_ALLOCATION);
		workPerformenceCol = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_WORKS_PERFORMENCE);
		projectCol = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_PROJECT);
	}

	public Collection<? extends WorkOrderPeriodCost> getData(
			Map<String, Object> parameter) {

		Object year = parameter.get(YEAR);
		Object month = parameter.get(MONTH);
		if (!(year instanceof Integer) || !(month instanceof Integer)) {
			throw new IllegalArgumentException(Messages.get().WorkorderPeriodCostAllocate_5);
		}

		Object costCenterCode = parameter.get(COSECENTERCODE);
		if (!(costCenterCode instanceof String)) {
			throw new IllegalArgumentException(Messages.get().WorkorderPeriodCostAllocate_6);
		}

		Object account = parameter.get(ACCOUNTNUMERS);
		if (account != null && !(account instanceof String[])) {
			throw new IllegalArgumentException(Messages.get().WorkorderPeriodCostAllocate_7);
		}

		Object rndCost = parameter.get(RNDCOST);
		if (!(rndCost instanceof RNDPeriodCost)) {
			throw new IllegalArgumentException(Messages.get().WorkorderPeriodCostAllocate_8);
		}

		// 1. 根据成本中心获得组织
		RNDPeriodCost rndpc = ((RNDPeriodCost) rndCost);
		Organization org = rndpc.getOrganization();

		if (org == null) {
			throw new IllegalArgumentException(Messages.get().WorkorderPeriodCostAllocate_9);
		}

		// 准备保存工作令号对应工时的数据
		Map<String, Double> workOrderMapWorks = new HashMap<String, Double>();

		Organization company = org.getCompany();
		if (company == null) {
			throw new IllegalArgumentException(Messages.get().WorkorderPeriodCostAllocate_10);
		}
		
		
		// 2. 级联获得组织下非成本中心组织的所有员工userid
		Set<String> userIds = getCostCenterUserIdList(org);
		String[] userIdArr = userIds.toArray(new String[0]);

		// 3. 获得人员在期间的工时记录
		BasicDBObject query = new BasicDBObject();
		query.put(WorksPerformence.F_USERID,
				new BasicDBObject().append("$in", userIdArr)); //$NON-NLS-1$

		Date[] period = getStartAndEnd((Integer) year, (Integer) month);
		long start = period[0].getTime() / (24 * 60 * 60 * 1000);
		long end = period[1].getTime() / (24 * 60 * 60 * 1000);

		BasicDBObject startCondition = new BasicDBObject().append(
				WorksPerformence.F_DATECODE,
				new BasicDBObject().append("$gte", new Long(start))); //$NON-NLS-1$
		BasicDBObject endCondition = new BasicDBObject().append(
				WorksPerformence.F_DATECODE,
				new BasicDBObject().append("$lte", new Long(end))); //$NON-NLS-1$

		query.put("$and", new BasicDBObject[] { startCondition, endCondition }); //$NON-NLS-1$

		DBObject fields = new BasicDBObject();
		fields.put(WorksPerformence.F_DATECODE, 1);
		fields.put(WorksPerformence.F_WORKS, 1);
		fields.put(WorksPerformence.F_PROJECT_ID, 1);

		DBCursor cur = workPerformenceCol.find(query, fields);
		while (cur.hasNext()) {
			DBObject data = cur.next();
			Object projectId = data.get(WorksPerformence.F_PROJECT_ID);
			if (projectId == null) {
				continue;
			}

			// 取工时
			Object works = data.get(WorksPerformence.F_WORKS);
			if (!(works instanceof Number)) {
				continue;
			}

			// 4. 根据工时记录的项目id逐条查询工作令号
			DBObject projectWorkOrderData = projectCol.findOne(
					new BasicDBObject().append(Project.F__ID, projectId),
					new BasicDBObject().append(Project.F_WORK_ORDER, 1));
			if (projectWorkOrderData == null) {
				continue;
			}

			Object workOrders = projectWorkOrderData.get(Project.F_WORK_ORDER);
			if (!(workOrders instanceof List<?>)) {
				continue;
			}

			// 应当归属到本成本中心的工作令号
			Set<String> effectiveWorkOrders = new HashSet<String>();

			List<?> list = (List<?>) workOrders;
			// 如果出现多条工作令号的，检查该组织工作令号的对应是否正确，如果是该成本中心的工作令号，将工时数据保存至该工作令号
			for (int i = 0; i < list.size(); i++) {
				String workOrder = (String) list.get(i);
				if (company.hasWorkOrder(workOrder)) {
					effectiveWorkOrders.add(workOrder);
				}
			}
			// 将工时平摊到工作令号
			double worksPerOrder = ((Double) works)
					/ (effectiveWorkOrders.size());
			Iterator<String> iter = effectiveWorkOrders.iterator();
			while (iter.hasNext()) {
				String workOrder = iter.next();

				// 累计到工时记录表
				Double value = workOrderMapWorks.get(workOrder);
				if (value == null) {
					value = worksPerOrder;
				} else {
					value = value.doubleValue() + worksPerOrder;
				}
				workOrderMapWorks.put(workOrder, value);
			}
		}

		// 全部处理完,计算总工时数
		Iterator<Double> iterator = workOrderMapWorks.values().iterator();
		double total = 0d;
		while (iterator.hasNext()) {
			total += iterator.next();
		}

		List<DBObject> toBeInsert = new ArrayList<DBObject>();

		Iterator<String> iter = workOrderMapWorks.keySet().iterator();
		while (iter.hasNext()) {
			String workOrderNumber = iter.next();
			DBObject wopc = new BasicDBObject();
			wopc.put(WorkOrderPeriodCost.F_WORKORDER, workOrderNumber);
			wopc.put(WorkOrderPeriodCost.F_YEAR, year);
			wopc.put(WorkOrderPeriodCost.F_MONTH, month);
			wopc.put(WorkOrderPeriodCost.F_COSTCENTERCODE, costCenterCode);

			// 保存工时分配数据
			// 获得该工作令号的累计工时
			Double works = workOrderMapWorks.get(workOrderNumber);
			double ratio = works / total;

			Iterator<String> iter2 = rndpc.get_data().keySet().iterator();
			while (iter2.hasNext()) {
				String key = iter2.next();
				if (!Utils.isNumbers(key)) {// 不是数字类型的字段忽略
					continue;
				}
				Object cost = rndpc.getValue(key);
				if (cost == null) {
					wopc.put(key, 0d);
				} else {
					double value = ((Double) cost) * ratio;
					wopc.put(key, value);
				}
			}

			toBeInsert.add(wopc);
		}

		if(toBeInsert.size()>0){
			costAllocateCol.insert(toBeInsert);
		}

		//准备返回
		ArrayList<WorkOrderPeriodCost> result = new ArrayList<WorkOrderPeriodCost>();
		for (int i = 0; i < toBeInsert.size(); i++) {
			result.add(ModelService.createModelObject(toBeInsert.get(i),
					WorkOrderPeriodCost.class));
		}

		return result;
	}

	/**
	 * 获得当前组织下所有的员工id, 级联搜索下级非成本中心的组织
	 * 
	 * @param org
	 * @return
	 */
	private Set<String> getCostCenterUserIdList(Organization org) {
		Set<String> result = new HashSet<String>();
		List<String> ids = org.getMemberIds(false);
		result.addAll(ids);

		List<PrimaryObject> childrenOrgs = org.getChildrenOrganization();
		for (int i = 0; i < childrenOrgs.size(); i++) {
			Organization childOrg = (Organization) childrenOrgs.get(i);
			if (childOrg.isCostCenter()) {
				continue;
			}
			result.addAll(getCostCenterUserIdList(childOrg));
		}
		return result;
	}

	public Date[] getStartAndEnd(Integer year, Integer month) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date start = cal.getTime();
		cal.add(Calendar.MONTH, 1);
		cal.add(Calendar.MILLISECOND, -1);
		Date end = cal.getTime();
		return new Date[] { start, end };
	}

}
