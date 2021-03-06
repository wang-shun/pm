package com.sg.business.model.dataset.project;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;

import com.mobnut.db.DBActivator;
import com.mobnut.db.model.DataSet;
import com.mobnut.db.model.ModelService;
import com.mobnut.db.model.PrimaryObject;
import com.mobnut.db.model.mongodb.SingleDBCollectionDataSetFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.sg.business.model.IModelConstants;
import com.sg.business.model.Organization;
import com.sg.business.model.Project;
import com.sg.business.model.Role;
import com.sg.business.model.User;
import com.sg.business.model.UserTask;
import com.sg.business.model.Work;
import com.sg.business.model.toolkit.UserToolkit;
import com.sg.widgets.part.CurrentAccountContext;

public class DelayProcess extends SingleDBCollectionDataSetFactory {
	private User user;
	private DBCollection projectCol;
	private DBCollection userCol;
	private DBCollection workCol;

	public DelayProcess() {
		super(IModelConstants.DB, IModelConstants.C_USERTASK);
		String userId = new CurrentAccountContext().getAccountInfo()
				.getConsignerId();
		user = UserToolkit.getUserById(userId);
		projectCol = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_PROJECT);
		userCol = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_USER);
		workCol = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_WORK);
	}

	@Override
	public DataSet getDataSet() {
		List<PrimaryObject> dataItems = new ArrayList<PrimaryObject>();
		DBCollection collection = getCollection();
		DBCursor cur = collection.find(getCondition());
		while (cur.hasNext()) {
			DBObject next = cur.next();
			UserTask usertask = ModelService.createModelObject(next,
					UserTask.class);
			if (isDelayTask(usertask)) {
				dataItems.add(usertask);
			}
		}

		return new DataSet(dataItems);
	}

	private boolean isDelayTask(UserTask usertask) {
		Object workitemid = usertask.getValue(UserTask.F_WORKITEMID);
		Object workid = usertask.getValue(UserTask.F_WORK_ID);
		DBCollection collection = getCollection();
		boolean isDelay = false;
		DBCursor cur = collection.find(
				new BasicDBObject()
						.append(UserTask.F_WORKITEMID, workitemid)
						.append(UserTask.F_WORK_ID, workid)
						.append(UserTask.F_STATUS,
								new BasicDBObject().append("$in", new String[] { //$NON-NLS-1$
										"InProgress", "Completed" })), //$NON-NLS-1$ //$NON-NLS-2$
				new BasicDBObject().append(UserTask.F__CDATE, 1)
						.append(UserTask.F_STATUS, 1)
						.append(UserTask.F_DESC, 1));

		if (cur.count() < 1) {
			return ((new Date().getTime() - usertask.get_cdate().getTime())
					/ (1000 * 60 * 60) > 3);
		}
		while (cur.hasNext()) {
			DBObject next = cur.next();
			String status = (String) next.get(UserTask.F_STATUS);
			Date date = (Date) next.get(UserTask.F__CDATE);

			if ("InProgress".equals(status)) { //$NON-NLS-1$
				usertask.setValue("InProgress", date); //$NON-NLS-1$
				if ((date.getTime() - usertask.get_cdate().getTime())
						/ (1000 * 60 * 60) > 3) {
					isDelay = true;
				}
			} else if ("Completed".equals(status)) { //$NON-NLS-1$
				usertask.setValue("Completed", date); //$NON-NLS-1$
			}
		}

		return isDelay;

	}

	private DBObject getCondition() {
		int year = -1;
		int month = -1;
		Date start = null;
		Date stop = null;
		Calendar calendar = Calendar.getInstance();
		DBObject date = getQueryCondition();
		if (date != null) {
			year = (int) date.get("year"); //$NON-NLS-1$
			month = (int) date.get("month"); //$NON-NLS-1$

			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.MONTH, month);
			calendar.set(Calendar.DATE, 1);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			start = calendar.getTime();
			calendar.add(Calendar.MONTH, 1);
			calendar.add(Calendar.MILLISECOND, -1);
			stop = calendar.getTime();
			setQueryCondition(null);

		} else {
			calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
			calendar.set(Calendar.DATE, 1);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			start = calendar.getTime();
			calendar.add(Calendar.MONTH, 1);
			calendar.add(Calendar.MILLISECOND, -1);
			stop = calendar.getTime();
		}

		DBObject dbo = new BasicDBObject();
		dbo.put(UserTask.F_STATUS, "Reserved"); //$NON-NLS-1$

		dbo.put("$or", //$NON-NLS-1$
				new BasicDBObject[] {
						new BasicDBObject().append(UserTask.F_ACTUALOWNER,
								new BasicDBObject().append("$in", //$NON-NLS-1$
										getUserIdSet())),
						new BasicDBObject().append(UserTask.F_WORK_ID,
								new BasicDBObject().append("$in", //$NON-NLS-1$
										getWorkIdsByFunctionProject())),
						new BasicDBObject().append(UserTask.F_WORK_ID,
								new BasicDBObject().append("$in", //$NON-NLS-1$
										getWorkIdsByChargerProjectIds())),
						new BasicDBObject().append(UserTask.F_ACTUALOWNER,
								user.getUserid()),

				});

		dbo.put(UserTask.F__CDATE, new BasicDBObject().append("$gte", start) //$NON-NLS-1$
				.append("$lte", stop)); //$NON-NLS-1$
		return dbo;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected List<ObjectId> getUserIdSet() {
		Object ids = getOrganizationIdCascade(null).toArray();
		List distinct = userCol.distinct(User.F_USER_ID, new BasicDBObject()
				.append(User.F_ORGANIZATION_ID,
						new BasicDBObject().append("$in", ids))); //$NON-NLS-1$
		return distinct;
	}

	private Collection<? extends ObjectId> getOrganizationIdCascade(
			Organization org) {
		Set<ObjectId> result = new HashSet<ObjectId>();
		List<PrimaryObject> orglist;
		if (org != null) {
			result.add(org.get_id());
			orglist = org.getChildrenOrganization();
		} else {
			orglist = getUsersManagedOrganization();
		}
		for (int i = 0; i < orglist.size(); i++) {
			result.addAll(getOrganizationIdCascade((Organization) orglist
					.get(i)));
		}
		return result;
	}

	private List<PrimaryObject> getUsersManagedOrganization() {
		List<PrimaryObject> orglist = user
				.getRoleGrantedInAllOrganization(Role.ROLE_DEPT_MANAGER_ID);
		List<PrimaryObject> input = new ArrayList<PrimaryObject>();

		for (int i = 0; i < orglist.size(); i++) {
			Organization org = (Organization) orglist.get(i);
			boolean hasParent = false;
			for (int j = 0; j < input.size(); j++) {
				Organization inputOrg = (Organization) input.get(j);
				if (inputOrg.isSuperOf(org)) {
					hasParent = true;
					break;
				}
				if (org.isSuperOf(inputOrg)) {
					input.remove(j);
					break;
				}
			}
			if (!hasParent) {
				input.add(org);
			}
		}

		return input;
	}

	// 返回项目管理员管理的项目IDS
	private List<ObjectId> getUsersFunctionProjectIds() {
		List<PrimaryObject> orglist = user
				.getRoleGrantedInFunctionDepartmentOrganization(Role.ROLE_PROJECT_ADMIN_ID);
		ObjectId[] ids = new ObjectId[orglist.size()];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = orglist.get(i).get_id();
		}
		List<ObjectId> list = new ArrayList<ObjectId>();
		DBCursor pids = projectCol.find(new BasicDBObject().append(
				Project.F_FUNCTION_ORGANIZATION,
				new BasicDBObject().append("$in", ids)), new BasicDBObject() //$NON-NLS-1$
				.append(Project.F__ID, 1));
		while (pids.hasNext()) {
			DBObject next = pids.next();
			list.add((ObjectId) next.get(Project.F__ID));
		}
		return list;
	}

	// 返回项目管理员管理管理的项目下所有的工作IDS
	private List<ObjectId> getWorkIdsByFunctionProject() {
		List<ObjectId> list = new ArrayList<ObjectId>();
		DBCursor wids = workCol.find(
				new BasicDBObject().append(Work.F_PROJECT_ID,
						new BasicDBObject().append("$in", //$NON-NLS-1$
								getUsersFunctionProjectIds())),
				new BasicDBObject().append(Work.F__ID, 1));
		while (wids.hasNext()) {
			DBObject next = wids.next();
			list.add((ObjectId) next.get(Work.F__ID));
		}

		return list;
	}

	// 返回项目负责人负责的项目IDS
	List<ObjectId> getUsersChargerProjectIds() {
		List<ObjectId> list = new ArrayList<ObjectId>();
		DBCursor pids = projectCol
				.find(new BasicDBObject().append(Project.F_CHARGER,
						user.getUserid()),
						new BasicDBObject().append(Project.F__ID, 1));
		while (pids.hasNext()) {
			DBObject next = pids.next();
			list.add((ObjectId) next.get(Project.F__ID));
		}
		return list;
	}

	// 返回项目负责人负责的项目下所有工作的IDS
	private List<ObjectId> getWorkIdsByChargerProjectIds() {
		List<ObjectId> list = new ArrayList<ObjectId>();
		DBCursor wids = workCol.find(
				new BasicDBObject().append(Work.F_PROJECT_ID,
						new BasicDBObject().append("$in", //$NON-NLS-1$
								getUsersChargerProjectIds())),
				new BasicDBObject().append(Work.F__ID, 1));
		while (wids.hasNext()) {
			DBObject next = wids.next();
			list.add((ObjectId) next.get(Work.F__ID));
		}

		return list;
	}

}
