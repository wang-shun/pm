package com.sg.business.model;

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

import org.bson.types.BasicBSONList;
import org.bson.types.ObjectId;
import org.drools.runtime.process.ProcessInstance;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.jbpm.task.I18NText;
import org.jbpm.task.Status;
import org.jbpm.task.Task;
import org.jbpm.task.TaskData;

import com.mobnut.admin.dataset.Setting;
import com.mobnut.commons.util.Utils;
import com.mobnut.commons.util.file.FileUtil;
import com.mobnut.db.DBActivator;
import com.mobnut.db.file.RemoteFile;
import com.mobnut.db.model.IContext;
import com.mobnut.db.model.IPrimaryObjectEventListener;
import com.mobnut.db.model.ModelService;
import com.mobnut.db.model.PrimaryObject;
import com.mobnut.db.utils.DBUtil;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.sg.bpm.workflow.WorkflowService;
import com.sg.bpm.workflow.model.DroolsProcessDefinition;
import com.sg.bpm.workflow.model.NodeAssignment;
import com.sg.bpm.workflow.runtime.Workflow;
import com.sg.business.model.check.CheckListItem;
import com.sg.business.model.check.ICheckListItem;
import com.sg.business.model.commonlabel.WorkCommonHTMLLable;
import com.sg.business.model.dataset.calendarsetting.CalendarCaculater;
import com.sg.business.model.input.WorkEditorInputFactory;
import com.sg.business.model.roleparameter.WorkRoleParameter;
import com.sg.business.model.toolkit.LifecycleToolkit;
import com.sg.business.model.toolkit.MessageToolkit;
import com.sg.business.model.toolkit.ProjectToolkit;
import com.sg.business.model.toolkit.UserToolkit;
import com.sg.business.resource.BusinessResource;
import com.sg.business.resource.nls.Messages;
import com.sg.widgets.commons.labelprovider.CommonHTMLLabel;
import com.sg.widgets.commons.model.IEditorInputFactory;
import com.sg.widgets.part.BackgroundContext;

/**
 * <p>
 * 工作
 * <p/>
 * 工作用于描述项目中的工作分解结构
 * 
 * @author jinxitao
 * 
 */
public class Work extends AbstractWork implements IProjectRelative, ISchedual,
		ILifecycle, IReferenceContainer {

	/**
	 * 设定工作交付物的编辑器
	 */
	public static final String EDIT_WORK_DELIVERABLE = "edit.work.deliverable"; //$NON-NLS-1$

	/**
	 * 带流程叶子工作编辑器
	 */
	public static final String EDIT_WORK_PLAN_1 = "edit.work.plan.1"; //$NON-NLS-1$

	public static final String TEMPLATE_DELIVERABLE = "template_deliverable"; //$NON-NLS-1$

	/**
	 * 不带流程叶子工作编辑器
	 */
	public static final String EDIT_WORK_PLAN_0 = "edit.work.plan.0"; //$NON-NLS-1$

	/**
	 * 工作的编辑器ID
	 */
	public static final String EDITOR = "view.work"; //$NON-NLS-1$

	/**
	 * 工作设置的编辑器ID
	 */
	public static final String EDITOR_SETTING = "editor.work.setting"; //$NON-NLS-1$

	/**
	 * 创建工作的编辑器ID
	 */
	public static final String EDITOR_CREATE_RUNTIME_WORK = "editor.create.runtimework"; //$NON-NLS-1$

	/**
	 * 编辑工作计划
	 */
	// public static final String EDITOR_WORK_PLAN = "edit.work.plan";

	/**
	 * 必需的，不可删除，布尔类型的字段
	 */
	public static final String F_MANDATORY = "mandatory"; //$NON-NLS-1$

	/**
	 * 归档的，不可删除，布尔类型的字段
	 */
	public static final String F_ARCHIVE = "archive"; //$NON-NLS-1$

	/**
	 * 负责人的id userid
	 */
	public static final String F_CHARGER = "chargerid"; //$NON-NLS-1$

	/**
	 * 指派者的id
	 */
	public static final String F_ASSIGNER = "assignerid"; //$NON-NLS-1$

	/**
	 * 工作承担者
	 */
	public static final String F_PARTICIPATE = "participate"; //$NON-NLS-1$

	/**
	 * 工作变更流程执行者
	 */
	public static final String F_WF_CHANGE_ACTORS = "wf_change_actors"; //$NON-NLS-1$

	/**
	 * 工作执行流程执行者
	 */
	public static final String F_WF_EXECUTE_ACTORS = "wf_execute_actors"; //$NON-NLS-1$

	/**
	 * 发起新工作的向导编辑器
	 */
	public static final String EDITOR_LAUNCH = "editor.work.launch"; //$NON-NLS-1$

	/**
	 * 描述
	 */
	public static final String F_DESCRIPTION = "description"; //$NON-NLS-1$

	public static final String F_IS_PROJECT_WBSROOT = "iswbsroot"; //$NON-NLS-1$

	public static final String F_MARK = "marked"; //$NON-NLS-1$

	public static final String F_RECORD = "record"; //$NON-NLS-1$

	public static final String F_WORK_DEFINITION_ID = "workd_id"; //$NON-NLS-1$

	public static final String F_WORK_DEFINITION_NAME = "workddesc"; //$NON-NLS-1$

	public static final String F_USE_PROJECT_ROLE = "useprojectrole"; //$NON-NLS-1$

	public static final String F_PERFORMENCE = "performence"; //$NON-NLS-1$

	public static final String F_STARTIMMEDIATELY = "startImmediately";//$NON-NLS-1$

	/**
	 * 是否达成目标
	 */
	public static final String F_ACHIEVED = "achieved";

	/**
	 * 禁止支出
	 */
	public static final String F_EXPENSE_FORBIDDEN = "expense_forbidden";//$NON-NLS-1$

	public static final String[] ARCHIVE_FIELDS = new String[] {
			F_ASSIGNMENT_CHARGER_ROLE_ID, F_CHARGER_ROLE_ID,
			F_PARTICIPATE_ROLE_SET, F_SETTING_AUTOFINISH_WHEN_PARENT_FINISH,
			F_SETTING_AUTOSTART_WHEN_PARENT_START,
			F_SETTING_CAN_ADD_DELIVERABLES, F_SETTING_CAN_BREAKDOWN,
			F_SETTING_CAN_MODIFY_PLANWORKS,
			F_SETTING_CAN_SKIP_WORKFLOW_TO_FINISH,
			F_SETTING_PROJECTCHANGE_MANDORY, F_SETTING_WORKCHANGE_MANDORY,
			F_SETTING_AUTOFINISH_WHEN_PARENT_FINISH, F_WF_CHANGE_ASSIGNMENT,
			F_WF_EXECUTE_ASSIGNMENT, F_TARGETS };

	private Double overCount = null;

	// /**
	// * 绩效记录字段
	// */
	// private static final String F_SF_PERFORMENCE_USERID = "userid";
	//
	// private static final String F_SF_PERFORMENCE_DATE = "date";
	//
	// private static final String F_SF_PERFORMENCE_ACTUALWORKS = "actualworks";
	//
	// /**
	// * 绩效，工时分配表，{日期1:{张三:value,李四:value},日期2:{王五:value,李四:value}}
	// */
	// private static final String F_PERFORMENCE_WORKS_ALLOCATE_TABLE =
	// "performence_works_allocate";
	//
	// private static final String F_PERFORMENCE_ISSUMMARY =
	// "performence_issummary";

	/**
	 * 根据状态返回不同的图标
	 */
	@Override
	public Image getImage() {
		String lc = getLifecycleStatus();
		if (STATUS_CANCELED_VALUE.equals(lc)) {
			return BusinessResource
					.getImage(BusinessResource.IMAGE_WORK_CANCEL_16);
		} else if (STATUS_FINIHED_VALUE.equals(lc)) {
			return BusinessResource
					.getImage(BusinessResource.IMAGE_WORK_FINISH_16);
		} else if (STATUS_NONE_VALUE.equals(lc)) {
			return BusinessResource.getImage(BusinessResource.IMAGE_WORK_16);
		} else if (STATUS_ONREADY_VALUE.equals(lc)) {
			return BusinessResource.getImage(BusinessResource.IMAGE_WORK_16);
		} else if (STATUS_PAUSED_VALUE.equals(lc)) {
			return BusinessResource
					.getImage(BusinessResource.IMAGE_WORK_PAUSE_16);
		} else if (STATUS_WIP_VALUE.equals(lc)) {
			return BusinessResource
					.getImage(BusinessResource.IMAGE_WORK_WIP_16);
		}
		return super.getImage();
	}

	/**
	 * 返回工作所属项目
	 * 
	 * @return Project
	 */
	public Project getProject() {
		ObjectId ptId = (ObjectId) getValue(F_PROJECT_ID);
		if (ptId != null) {
			return ModelService.createModelObject(Project.class, ptId);
		} else {
			return null;
		}
	}

	/**
	 * 构建下级工作
	 * 
	 * @return Work
	 */
	@Override
	public Work makeChildWork() {
		DBObject data = new BasicDBObject();
		data.put(F_PARENT_ID, get_id());
		data.put(F_ROOT_ID, getValue(F_ROOT_ID));

		int seq = getMaxChildSeq();
		data.put(F_SEQ, new Integer(seq + 1));

		data.put(F_PROJECT_ID, getValue(F_PROJECT_ID));

		// 设置一些基本的选项设定
		data.put(F_SETTING_CAN_ADD_DELIVERABLES, Boolean.TRUE);
		data.put(F_SETTING_CAN_BREAKDOWN, Boolean.TRUE);
		data.put(F_SETTING_CAN_MODIFY_PLANWORKS, Boolean.TRUE);

		Work po = ModelService.createModelObject(data, Work.class);
		return po;
	}

	/**
	 * 返回工作所属项目
	 * 
	 * @return PrimaryObject
	 */
	@Override
	public PrimaryObject getHoster() {
		return getProject();
	}

	/**
	 * 返回工作所属项目_id
	 * 
	 * @return
	 */
	public ObjectId getProjectId() {
		return (ObjectId) getValue(F_PROJECT_ID);
	}

	/**
	 * 返回工作计划开始时间
	 * 
	 * @return Date
	 */
	public Date getPlanStart() {
		Object value = getValue(F_PLAN_START);
		if (value instanceof Date) {
			return Utils.getDayBegin((Date) value).getTime();
		} else {
			return null;
		}

		// List<PrimaryObject> children = getChildrenWork();
		// if (children.size() == 0) {
		// Object value = getValue(F_PLAN_START);
		// if (value instanceof Date) {
		// return Utils.getDayBegin((Date) value).getTime();
		// } else {
		// return null;
		// }
		// } else {
		// Date start = null;
		// for (int i = 0; i < children.size(); i++) {
		// Work child = (Work) children.get(i);
		// Date s = child.getPlanStart();
		// if (s != null && (start == null || s.before(start))) {
		// start = s;
		// }
		// }
		//
		// // 如果下级没有计划开始，取自己的计划开始
		// if (start != null) {
		// return start;
		// } else {
		// Object value = getValue(F_PLAN_START);
		// if (value instanceof Date) {
		// return Utils.getDayBegin((Date) value).getTime();
		// } else {
		// return null;
		// }
		// }
		//
		// }
	}

	/**
	 * 返回工作计划完成时间
	 * 
	 * @return Date
	 */
	public Date getPlanFinish() {
		Object value = getValue(F_PLAN_FINISH);
		if (value instanceof Date) {
			return Utils.getDayEnd((Date) value).getTime();
		} else {
			return null;
		}

		// List<PrimaryObject> children = getChildrenWork();
		// if (children.size() == 0) {
		// Object value = getValue(F_PLAN_FINISH);
		// if (value instanceof Date) {
		// return Utils.getDayEnd((Date) value).getTime();
		// } else {
		// return null;
		// }
		// } else {
		// Date finish = null;
		// for (int i = 0; i < children.size(); i++) {
		// Work child = (Work) children.get(i);
		// Date f = child.getPlanFinish();
		// if (f != null && (finish == null || f.after(finish))) {
		// finish = f;
		// }
		// }
		//
		// if (finish != null) {
		// return finish;
		// } else {
		// Object value = getValue(F_PLAN_FINISH);
		// if (value instanceof Date) {
		// return Utils.getDayEnd((Date) value).getTime();
		// } else {
		// return null;
		// }
		// }
		//
		// }
	}

	/**
	 * 返回工作实际开始时间
	 * 
	 * @return Date
	 */
	public Date getActualStart() {
		Date d = (Date) getValue(F_ACTUAL_START);
		if (d != null) {
			return Utils.getDayBegin(d).getTime();
		} else {
			return null;
		}

		// List<PrimaryObject> children = getChildrenWork();
		// if (children.size() == 0) {
		// Date d = (Date) getValue(F_ACTUAL_START);
		// if (d != null) {
		// return Utils.getDayBegin(d).getTime();
		// } else {
		// return null;
		// }
		// } else {
		// Date start = null;
		// for (int i = 0; i < children.size(); i++) {
		// Work child = (Work) children.get(i);
		// Date s = child.getActualStart();
		// if (s != null && (start == null || s.before(start))) {
		// start = s;
		// }
		// }
		// return start;
		// }
	}

	/**
	 * 返回工作实际完成时间
	 * 
	 * @return Date
	 */
	public Date getActualFinish() {
		Date d = (Date) getValue(F_ACTUAL_FINISH);
		if (d != null) {
			return Utils.getDayEnd(d).getTime();
		} else {
			return null;
		}
		// List<PrimaryObject> children = getChildrenWork();
		// if (children.size() == 0) {
		// Date d = (Date) getValue(F_ACTUAL_FINISH);
		// if (d != null) {
		// return Utils.getDayEnd(d).getTime();
		// } else {
		// return null;
		// }
		// } else {
		// Date finish = null;
		// for (int i = 0; i < children.size(); i++) {
		// Work child = (Work) children.get(i);
		// Date f = child.getActualFinish();
		// if (f != null && (finish == null || f.after(finish))) {
		// finish = f;
		// }
		// }
		// return finish;
		// }
	}

	/**
	 * 返回工作的实际工时
	 * 
	 * @return Double
	 */
	public Double getActualWorks() {
		return getDoubleValue(F_ACTUAL_WORKS);

		// List<PrimaryObject> children = getChildrenWork();
		// if (children.size() == 0) {
		// return (Double) getValue(F_ACTUAL_WORKS);
		// } else {
		// Double works = null;
		// for (int i = 0; i < children.size(); i++) {
		// Work child = (Work) children.get(i);
		// Double w = child.getActualWorks();
		// if (w != null) {
		// if (works == null) {
		// works = w;
		// } else {
		// works = works + w;
		// }
		// }
		// }
		// return works;
		// }
	}

	/**
	 * 返回工作的计划工时
	 * 
	 * @return Double
	 */
	public Double getPlanWorks() {
		return getDoubleValue(F_PLAN_WORKS);

		// List<PrimaryObject> children = getChildrenWork();
		// if (children.size() == 0) {
		// return (Double) getValue(F_PLAN_WORKS);
		// } else {
		// Double works = null;
		// for (int i = 0; i < children.size(); i++) {
		// Work child = (Work) children.get(i);
		// Double w = child.getPlanWorks();
		// if (w != null) {
		// if (works == null) {
		// works = w;
		// } else {
		// works = works + w;
		// }
		// }
		// }
		// return works;
		// }
	}

	/**
	 * 返回工作的实际工期
	 * 
	 * @return Integer
	 */
	public Integer getActualDuration() {
		return getIntegerValue(F_ACTUAL_DURATION);

		// List<PrimaryObject> children = getChildrenWork();
		// if (children.size() == 0) {
		// return (Integer) getValue(F_ACTUAL_DURATION);
		// } else {
		// Integer duration = null;
		// for (int i = 0; i < children.size(); i++) {
		// Work child = (Work) children.get(i);
		// Integer d = child.getActualDuration();
		// if (d != null) {
		// if (duration == null) {
		// duration = d;
		// } else if (duration < d) {
		// duration = d;
		// }
		// }
		// }
		// return duration;
		// }
	}

	/**
	 * 返回工作的计划工期
	 * 
	 * @return Integer
	 */
	public Integer getPlanDuration() {
		return getIntegerValue(F_PLAN_DURATION);

		// List<PrimaryObject> children = getChildrenWork();
		// if (children.size() == 0) {
		// return (Integer) getValue(F_PLAN_DURATION);
		// } else {
		// Integer duration = null;
		// for (int i = 0; i < children.size(); i++) {
		// Work child = (Work) children.get(i);
		// Integer d = child.getPlanDuration();
		// if (d != null) {
		// if (duration == null) {
		// duration = d;
		// } else if (duration < d) {
		// duration = d;
		// }
		// }
		// }
		// return duration;
		// }
	}

	/**
	 * 新建工作交付物
	 * 
	 * @return Deliverable
	 */
	@Override
	public Deliverable makeDeliverableDefinition(String type) {
		return makeDeliverableDefinition(null, type);
	}

	/**
	 * 新建工作交付物
	 * 
	 * @param docd
	 *            ,文档模板定义
	 * @return Deliverable
	 */
	public Deliverable makeDeliverableDefinition(DocumentDefinition docd,
			String type) {
		DBObject data = new BasicDBObject();
		data.put(Deliverable.F_WORK_ID, get_id());

		data.put(Deliverable.F_PROJECT_ID, getValue(F_PROJECT_ID));
		data.put(Deliverable.F_TYPE, type);

		if (docd != null) {
			data.put(Deliverable.F_DOCUMENT_DEFINITION_ID, docd.get_id());
			data.put(Deliverable.F_DESC, docd.getDesc());
		}

		Deliverable po = ModelService
				.createModelObject(data, Deliverable.class);

		return po;
	}

	/**
	 * 返回类型名称
	 * 
	 * @return String
	 */
	@Override
	public String getTypeName() {
		return Messages.get().Work_22;
	}

	/**
	 * 返回工作默认编辑器ID
	 * 
	 * @return String
	 */
	@Override
	public String getDefaultEditorId() {
		return EDITOR;
	}

	/**
	 * 判断工作的属性是否只读
	 * 
	 * @param field
	 *            ,工作的属性
	 * @param context
	 * @return boolean
	 */
	public boolean canEdit(String field, IContext context) {
		return true;
	}

	@Override
	public boolean canCheck() {
		// 未完成和未取消的
		String lc = getLifecycleStatus();
		return (!STATUS_CANCELED_VALUE.equals(lc))
				&& (!STATUS_FINIHED_VALUE.equals(lc));
	}

	/**
	 * 能否点击启动
	 */
	@Override
	public boolean canStart() {
		// 检查本工作生命周期状态是否符合:准备中，无状态，已暂停
		// 如果不是这些状态(已完成、已取消、进行中)，返回false
		String lifeCycle = getLifecycleStatus();
		if (STATUS_CANCELED_VALUE.equals(lifeCycle)
				|| STATUS_FINIHED_VALUE.equals(lifeCycle)
				|| STATUS_WIP_VALUE.equals(lifeCycle)) {
			return false;
		}

		return true;
	}

	/**
	 * 能否点击暂停
	 */
	@Override
	public boolean canPause() {
		// 检查本工作生命周期状态是否符合:进行中
		// 如果不是这些状态(已完成、已取消、准备中、无状态、已暂停)，返回false
		String lifeCycle = getLifecycleStatus();
		if (STATUS_WIP_VALUE.equals(lifeCycle)) {
			return true;
		}

		return false;
	}

	/**
	 * 能否点击取消
	 */
	@Override
	public boolean canCancel() {
		// 检查本工作生命周期状态是否符合:已暂停,进行中，准备中
		String lifeCycle = getLifecycleStatus();
		return STATUS_WIP_VALUE.equals(lifeCycle)
				|| STATUS_PAUSED_VALUE.equals(lifeCycle)
				|| STATUS_ONREADY_VALUE.equals(lifeCycle)
				|| STATUS_NONE_VALUE.equals(lifeCycle);
	}

	@Override
	public boolean canFinish() {
		// 1.首先检查本工作生命周期状态是否符合:已暂停,进行中
		// 如果不是这些状态(已完成、准备中、无状态、已取消)，返回false
		String lifeCycle = getLifecycleStatus();
		return STATUS_WIP_VALUE.equals(lifeCycle)
				|| STATUS_PAUSED_VALUE.equals(lifeCycle);
	}

	@Override
	public boolean canCommit() {
		String lc = getLifecycleStatus();

		return STATUS_NONE_VALUE.equals(lc);
	}

	/**
	 * 判断流程是否可以点击开始
	 * 
	 * @param context
	 * @return
	 */
	@Deprecated
	public boolean canWorkflowStart(IContext context) {
		String lc = getLifecycleStatus();
		return ILifecycle.STATUS_WIP_VALUE.equals(lc);
	}

	/**
	 * 判断流程是否可以点击完成
	 * 
	 * @param context
	 * @return
	 */
	@Deprecated
	public boolean canWorkflowFinish(IContext context) {
		String lc = getLifecycleStatus();
		return ILifecycle.STATUS_WIP_VALUE.equals(lc);
	}

	/**
	 * 判断流程是否可以点击中止
	 * 
	 * @param context
	 * @return
	 */
	@Deprecated
	public boolean canWorkflowCancel(IContext context) {
		String lc = getLifecycleStatus();
		return ILifecycle.STATUS_WIP_VALUE.equals(lc);
	}

	/**
	 * 检查工作是否可以取消
	 * 
	 * @param context
	 * @throws Exception
	 */
	// public void cancelCheck(IContext context) throws Exception {
	// // 1.判断是否是本级的负责人
	// String userId = context.getAccountInfo().getConsignerId();
	// Work parent = (Work) getParent();
	// if (parent != null) {
	// if (!userId.equals(parent.getChargerId())) {
	// throw new Exception("不是工作负责人，" + parent);
	// }
	// } else {
	// throw new Exception("本工作不能取消，" + this);
	// }
	//
	// if (isMandatory()) {
	// throw new Exception("本工作不能取消，" + this);
	// }
	//
	// // 2.判断上级工作生命周期状态是否符合：进行中
	// // 如果不在进行中，返回false
	// Work parentWork = (Work) getParent();
	// if (parentWork != null) {
	// if (!STATUS_WIP_VALUE.equals(parentWork.getLifecycleStatus())) {
	// throw new Exception("上级工作不在进行中，" + this);
	// }
	// } else {
	// Project project = getProject();
	// if (project != null) {
	// if (!STATUS_WIP_VALUE.equals(project.getLifecycleStatus())) {
	// throw new Exception("项目不在进行中，" + this);
	// }
	// }
	// }
	// }

	public boolean isMandatory() {
		return Boolean.TRUE.equals(getValue(F_MANDATORY)) || isMilestone();
	}

	public boolean isArchive() {
		return Boolean.TRUE.equals(getValue(F_ARCHIVE));
	}

	/**
	 * 能否点击编辑
	 */
	@Override
	public boolean canEdit(IContext context) {
		// 1.首先检查本工作生命周期状态是否符合:准备中，进行中，无状态，
		// 如果不是这些状态(已完成、已取消、已暂停)，返回false
		String lifeCycle = getLifecycleStatus();
		if (STATUS_CANCELED_VALUE.equals(lifeCycle)
				|| STATUS_FINIHED_VALUE.equals(lifeCycle)
				|| STATUS_PAUSED_VALUE.equals(lifeCycle)) {
			return false;
		}

		if (isProjectWBSRoot()) {
			return false;
		}
		// 项目启动后,里程碑工作不允许编辑
		// if (!isStandloneWork()) {
		// Project project = getProject();
		// if (project != null) {
		// String projectLifecycle = project.getLifecycleStatus();
		// if (STATUS_WIP_VALUE.equals(projectLifecycle)) {
		// if (isMilestone()) {
		// return false;
		// }
		// }
		// }
		// }
		// 判断是否为该工作或上级工作的指派者
		if (hasPermissionForReassignment(context)) {
			return true;
		}
		// 2.判断是否为该工作或上级工作的负责人或项目的项目经理
		return hasPermission(context);
	}

	/**
	 * 能够点击发送消息
	 * 
	 * @param currentAccountContext
	 * @return
	 */
	// public boolean canSendMessage(IContext context) {
	// // 1.首先检查本工作生命周期状态是否符合:准备中，进行中，无状态，
	// // 如果不是这些状态(已完成、已取消、已暂停)，返回false
	// String lifeCycle = getLifecycleStatus();
	// if (STATUS_CANCELED_VALUE.equals(lifeCycle)
	// || STATUS_FINIHED_VALUE.equals(lifeCycle)
	// || STATUS_PAUSED_VALUE.equals(lifeCycle)) {
	// return false;
	// }
	//
	// if (isProjectWBSRoot()) {
	// return false;
	// }
	// if (hasPermissionForReassignment(context)) {
	// return true;
	// }
	// return hasPermission(context);
	// }

	/**
	 * 能否点击删除
	 */
	@Override
	public boolean canDelete(IContext context) {
		// 1.首先检查本工作生命周期状态是否符合:准备中，无状态，
		// 如果不是这些状态(已完成、已取消、已暂停、进行中)，返回false
		String lifeCycle = (String) getValue(F_LIFECYCLE);
		if (STATUS_CANCELED_VALUE.equals(lifeCycle)
				|| STATUS_FINIHED_VALUE.equals(lifeCycle)
				|| STATUS_PAUSED_VALUE.equals(lifeCycle)
				|| STATUS_WIP_VALUE.equals(lifeCycle)) {
			return false;
		}

		if (isMandatory()) {
			return false;
		}
		if (isProjectWBSRoot()) {
			return false;
		}

		// 项目启动后,里程碑工作不允许删除
		if (!isStandloneWork()) {
			Project project = getProject();
			if (project != null) {
				String projectLifecycle = project.getLifecycleStatus();
				if (STATUS_WIP_VALUE.equals(projectLifecycle)) {
					if (isMilestone()) {
						return false;
					}
				}
			}
		}
		// 2.判断是否为该工作或上级工作的负责人或项目的项目经理
		return hasPermission(context);
	}

	/**
	 * 能否编辑工作记录
	 * 
	 * @param context
	 * @return
	 */
	public boolean canEditWorkRecord(IContext context) {
		// 1.首先检查本工作生命周期状态是否符合:准备中，进行中，无状态，
		// 如果不是这些状态(已完成、已取消、已暂停)，返回false
		String lifeCycle = (String) getValue(F_LIFECYCLE);
		if (STATUS_CANCELED_VALUE.equals(lifeCycle)
				|| STATUS_FINIHED_VALUE.equals(lifeCycle)
				|| STATUS_PAUSED_VALUE.equals(lifeCycle)) {
			return false;
		}

		// 2. 当是摘要工作时，返回false
		if (isSummaryWork()) {
			return false;
		}

		// 3.判断是否为该工作或上级工作的负责人或项目的项目经理
		return hasPermission(context);
	}

	/**
	 * 能否添加工作
	 * 
	 * @param context
	 * @return
	 */
	public boolean canCreateChildWork(IContext context) {
		// 1.首先检查本工作生命周期状态是否符合:准备中，进行中，无状态，
		// 如果不是这些状态(已完成、已取消、已暂停)，返回false
		String lifeCycle = (String) getValue(F_LIFECYCLE);
		if (STATUS_CANCELED_VALUE.equals(lifeCycle)
				|| STATUS_FINIHED_VALUE.equals(lifeCycle)
				|| STATUS_PAUSED_VALUE.equals(lifeCycle)) {
			return false;
		}

		// 2. 当是摘要工作时，是否设置了允许分解，如果没有，返回false
		if (!isSummaryWork()
				&& !Boolean.TRUE.equals(getValue(F_SETTING_CAN_BREAKDOWN))) {
			return false;
		}

		// 3.判断是否为该工作或上级工作的负责人或项目的项目经理
		return hasPermission(context);
	}

	/**
	 * 能否添加交付物
	 * 
	 * @param context
	 * @return
	 */
	public boolean canCreateDeliverable(IContext context) {
		// 1.首先检查本工作生命周期状态是否符合:准备中，进行中，无状态，
		// 如果不是这些状态(已完成、已取消、已暂停)，返回false
		String lifeCycle = (String) getValue(F_LIFECYCLE);
		if (STATUS_CANCELED_VALUE.equals(lifeCycle)
				|| STATUS_FINIHED_VALUE.equals(lifeCycle)
				|| STATUS_PAUSED_VALUE.equals(lifeCycle)) {
			return false;
		}

		// 2. 当是摘要工作时，返回false
		if (isSummaryWork()) {
			return false;
		}

		// 3.如果设置了不能添加交付物，返回假
		if (!Boolean.TRUE.equals(getValue(F_SETTING_CAN_ADD_DELIVERABLES))) {
			return false;
		}

		// 4.判断是否为该工作或上级工作的承担者或项目的项目经理
		if (hasPermission(context)) {
			return true;
		} else {
			String userId = context.getAccountInfo().getConsignerId();
			BasicBSONList participatesIdList = getParticipatesIdList();
			if (participatesIdList != null) {
				for (Object object : participatesIdList) {
					String participatesId = (String) object;
					if (userId.equals(participatesId)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	/**
	 * 能否进行工作指派
	 * 
	 * @param context
	 * @return
	 */
	public boolean canReassignment(IContext context) {
		// 1.首先检查本工作生命周期状态是否符合:准备中，进行中，无状态，
		// 如果不是这些状态(已完成、已取消、已暂停)，返回false
		String lifeCycle = (String) getValue(F_LIFECYCLE);
		if (STATUS_CANCELED_VALUE.equals(lifeCycle)
				|| STATUS_FINIHED_VALUE.equals(lifeCycle)
				|| STATUS_PAUSED_VALUE.equals(lifeCycle)) {
			return false;
		}

		// 2.判断是否为该工作或上级工作的指派者
		return hasPermissionForReassignment(context);
	}

	/**
	 * 检查工作是否可以启动
	 * 
	 * @param context
	 * @throws Exception
	 */
	public List<Object[]> checkStartAction(IContext context) throws Exception {
		List<Object[]> message = new ArrayList<Object[]>();
		// 1.判断是否是本级的负责人
		// 如果不在进行中，返回false
		String userId = context.getAccountInfo().getConsignerId();
		if (isProjectWBSRoot()) {
			Project project = getProject();
			if (!userId.equals(project.getChargerId())) {
				throw new Exception(Messages.get().Work_23 + this);
			}

			if (project != null) {
				if (!STATUS_WIP_VALUE.equals(project.getLifecycleStatus())) {
					throw new Exception(Messages.get().Work_24 + this);
				}
			}
		} else {
			if (!userId.equals(getChargerId())) {
				throw new Exception(Messages.get().Work_25 + this);
			}
			Work parentWork = (Work) getParent();
			if (parentWork != null) {
				if (!STATUS_WIP_VALUE.equals(parentWork.getLifecycleStatus())) {
					throw new Exception(Messages.get().Work_26 + this);
				}
			}
		}

		// 3.判断本工作及其下级工作的必要信息是否录入
		message.addAll(checkCascadeStart(false));

		// 检查工作工时
		workTimeValidate();

		return message;
	}

	@Override
	public List<Object[]> checkCancelAction(IContext context) throws Exception {
		try {
			reload();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 先判断是否顶级工作
		// 1.判断是否是本级的负责人
		// 2.判断上级工作生命周期状态是否符合：进行中
		// 如果不在进行中，返回false
		String userId = context.getAccountInfo().getConsignerId();
		Project project = getProject();
		if (isProjectWBSRoot()) {
			if (!userId.equals(project.getChargerId())) {
				throw new Exception(Messages.get().Work_27 + this);
			}

			if (project != null) {
				if (!STATUS_WIP_VALUE.equals(project.getLifecycleStatus())) {
					throw new Exception(Messages.get().Work_28 + this);
				}
			}
		} else {
			// 1.判断是否是本级的负责人
			Work parent = (Work) getParent();
			if (parent != null) {
				if (!STATUS_WIP_VALUE.equals(parent.getLifecycleStatus())) {
					throw new Exception(Messages.get().Work_29 + this);
				}
				if (parent.isProjectWBSRoot()) {
					if (!userId.equals(project.getChargerId())) {
						throw new Exception(Messages.get().Work_30 + this);
					}
				} else {
					if (!parent.hasPermission(context)) {
						throw new Exception(Messages.get().Work_31 + parent);
					}
				}
			} else {
				if (project != null) {
					if (!STATUS_WIP_VALUE.equals(project.getLifecycleStatus())) {
						throw new Exception(Messages.get().Work_32 + this);
					}
				} else {
					if (!isStandloneWork()) {
						throw new Exception(Messages.get().Work_33 + this);
					}
				}
			}

			if (isMandatory()) {
				throw new Exception(Messages.get().Work_34 + this);
			}
		}

		return null;
	}

	/**
	 * 检查工作是否可以完成
	 * 
	 * @param context
	 * @throws Exception
	 */
	@Override
	public List<Object[]> checkFinishAction(IContext context) throws Exception {
		List<Object[]> message = new ArrayList<Object[]>();
		// 先判断是否顶级工作
		// 1.判断是否是本级的负责人
		// 2.判断上级工作生命周期状态是否符合：进行中
		// 如果不在进行中，返回false
		String userId = context.getAccountInfo().getConsignerId();
		if (isProjectWBSRoot()) {
			Project project = getProject();
			if (!userId.equals(project.getChargerId())) {
				throw new Exception(Messages.get().Work_35 + this);
			}

			if (project != null) {
				if (!STATUS_WIP_VALUE.equals(project.getLifecycleStatus())) {
					throw new Exception(Messages.get().Work_36 + this);
				}
			}
		} else {
			if (!userId.equals(getChargerId())) {
				throw new Exception(Messages.get().Work_37 + this);
			}
			Work parentWork = (Work) getParent();
			if (parentWork != null) {
				if (!STATUS_WIP_VALUE.equals(parentWork.getLifecycleStatus())) {
					throw new Exception(Messages.get().Work_38 + this);
				}
			}
		}

		// 检查流程是否完成
		if (!Boolean.TRUE
				.equals(getValue(F_SETTING_CAN_SKIP_WORKFLOW_TO_FINISH))) {
			ProcessInstance pi = getExecuteProcess();
			if (pi != null) {
				if (pi.getState() != ProcessInstance.STATE_COMPLETED
						|| pi.getState() != ProcessInstance.STATE_ABORTED) {
					throw new Exception(Messages.get().Work_39 + this);
				}
			}
		}

		// 检查下级必须的交付物是否上传了附件
		List<PrimaryObject> delis = getDeliverable();
		if (delis != null) {
			for (int i = 0; i < delis.size(); i++) {
				Deliverable deli = (Deliverable) delis.get(i);
				if (deli.isMandatory()) {
					Document doc = deli.getDocument();
					doc.checkMandatory();
				}
			}
		}

		// 3.判断下级级联完成的工作是否可以完成，非级联完成的工作是否已经在已完成状态或已取消状态
		message.addAll(checkCascadeFinish(get_id()));
		return message;

	}

	@Override
	public List<Object[]> checkPauseAction(IContext context) throws Exception {
		try {
			reload();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 先判断是否顶级工作
		// 1.判断是否是本级的负责人
		// 2.判断上级工作生命周期状态是否符合：进行中
		// 如果不在进行中，返回false
		String userId = context.getAccountInfo().getConsignerId();
		if (isProjectWBSRoot()) {
			Project project = getProject();
			if (!userId.equals(project.getChargerId())) {
				throw new Exception(Messages.get().Work_40 + this);
			}

			if (project != null) {
				if (!STATUS_WIP_VALUE.equals(project.getLifecycleStatus())) {
					throw new Exception(Messages.get().Work_41 + this);
				}
			}
		} else {
			if (!userId.equals(getChargerId())) {
				throw new Exception(Messages.get().Work_42 + this);
			}
			Work parentWork = (Work) getParent();
			if (parentWork != null) {
				if (!STATUS_WIP_VALUE.equals(parentWork.getLifecycleStatus())) {
					throw new Exception(Messages.get().Work_43 + this);
				}
			}
		}
		return null;
	}

	/**
	 * 计算工期
	 * 
	 * @param fStart
	 *            ,开始日期
	 * @param fFinish
	 *            ,完成日期
	 * @param fDuration
	 *            ,工期
	 * @throws Exception
	 */
	public void checkAndCalculateDuration(String fStart, String fFinish,
			String fDuration) throws Exception {
		Date start = (Date) getValue(fStart);
		if (start != null) {
			start = Utils.getDayBegin(start).getTime();
		}

		Date finish = (Date) getValue(fFinish);
		if (finish != null) {
			finish = Utils.getDayEnd(finish).getTime();
		}

		if (start != null && finish != null) {
			// 检查是否合法
			if (start.after(finish)) {
				throw new Exception(Messages.get().Work_44);
			}

			// 计算工期
			Calendar sdate = Utils.getDayBegin(start);
			Calendar edate = Utils.getDayEnd(finish);
			long l = (edate.getTimeInMillis() - sdate.getTimeInMillis())
					/ (1000 * 60 * 60 * 24);
			setValue(fDuration, new Integer((int) l));
		}
	}

	/**
	 * 增加检测 工作的开始时间不能早于项目的开始时间，结束时间不能晚于项目的结束时间
	 * 
	 * @throws Exception
	 */
	public void checkProjectTimeline() throws Exception {
		Project project = getProject();
		if (!isProjectWork()) {
			return;
		}

		Date start = getPlanStart();
		if (start != null) {
			start = Utils.getDayBegin(start).getTime();
		}

		Date finish = getPlanFinish();
		if (finish != null) {
			finish = Utils.getDayEnd(finish).getTime();
		}

		if (start == null && finish == null) {
			return;
		}

		Date projstart = project.getPlanStart();
		if (projstart != null) {
			projstart = Utils.getDayBegin(projstart).getTime();

			if (start != null && start.before(projstart)) {
				throw new Exception(Messages.get().Work_45);
			}

		} else {
			return;
		}

		Date projfinish = project.getPlanFinish();
		if (projfinish != null) {
			projfinish = Utils.getDayEnd(projfinish).getTime();

			if (finish != null && finish.after(projfinish)) {
				throw new Exception(Messages.get().Work_46);
			}
		} else {
			return;
		}

	}

	/**
	 * 项目检查
	 * 
	 * @return
	 */
	public List<ICheckListItem> checkPlan() {
		Project project = getProject();
		Map<ObjectId, List<PrimaryObject>> ram = project.getRoleAssignmentMap();
		return checkPlan(project, ram);
	}

	/**
	 * 1. 检查工作的计划时间：错误，没有设定计划开始、计划完成、计划工时的叶工作 <br/>
	 * 2. 检查工作的负责人 ：错误，没有设定负责人，而且没有设定指派者的叶工作 <br/>
	 * 3. 工作的流程设定 ：警告，没有指明流程执行者的工作 <br/>
	 * 4. 交付物检查 <br/>
	 * 4.1. 检查工作是否具有交付物：警告，没有交付物的叶工作 4.2. 检查交付物文档没有电子文件作为模板：警告
	 * 
	 * @param project
	 * 
	 * @param roleMap
	 * 
	 * @return
	 */
	public List<ICheckListItem> checkPlan(Project project,
			Map<ObjectId, List<PrimaryObject>> roleMap) {
		ArrayList<ICheckListItem> result = new ArrayList<ICheckListItem>();
		List<PrimaryObject> childrenWork = getChildrenWork();
		if (childrenWork.size() > 0) {// 如果有下级，直接返回下级的检查结果
			for (int i = 0; i < childrenWork.size(); i++) {
				Work childWork = (Work) childrenWork.get(i);
				result.addAll(childWork.checkPlan(project, roleMap));
			}
		}
		if (isMilestone()) {
			// ****************************************************************************************
			// 1 检查工作的计划开始和计划完成
			Object value = getPlanStart();
			boolean passed = true;
			if (value == null) {
				CheckListItem checkItem = new CheckListItem(
						Messages.get().Work_47, Messages.get().Work_48,
						Messages.get().Work_49, ICheckListItem.ERROR);
				checkItem.setData(project);
				checkItem.setSelection(this);
				checkItem.setEditorId(Project.EDITOR_CREATE_PLAN);
				checkItem.setEditorPageId(Project.EDITOR_PAGE_WBS);
				checkItem.setKey(F_PLAN_START);
				result.add(checkItem);
				passed = false;
			}

			value = getPlanFinish();
			if (value == null) {
				CheckListItem checkItem = new CheckListItem(
						Messages.get().Work_50, Messages.get().Work_51,
						Messages.get().Work_52, ICheckListItem.ERROR);
				checkItem.setData(project);
				checkItem.setSelection(this);
				checkItem.setEditorId(Project.EDITOR_CREATE_PLAN);
				checkItem.setEditorPageId(Project.EDITOR_PAGE_WBS);
				checkItem.setKey(F_PLAN_START);
				result.add(checkItem);
				passed = false;
			}

			value = getPlanWorks();
			if (value == null) {
				CheckListItem checkItem = new CheckListItem(
						Messages.get().Work_53, Messages.get().Work_54,
						Messages.get().Work_55, ICheckListItem.WARRING);
				checkItem.setData(project);
				checkItem.setSelection(this);
				checkItem.setEditorId(Project.EDITOR_CREATE_PLAN);
				checkItem.setEditorPageId(Project.EDITOR_PAGE_WBS);
				checkItem.setKey(F_PLAN_WORKS);
				result.add(checkItem);
				passed = false;
			}

			value = getDesc();
			if (value == null) {
				CheckListItem checkItem = new CheckListItem(
						Messages.get().Work_56, Messages.get().Work_57,
						Messages.get().Work_58, ICheckListItem.ERROR);
				checkItem.setData(project);
				checkItem.setSelection(this);
				checkItem.setEditorId(Project.EDITOR_CREATE_PLAN);
				checkItem.setEditorPageId(Project.EDITOR_PAGE_WBS);
				checkItem.setKey(F_DESC);
				result.add(checkItem);
			}

			if (passed) {
				CheckListItem checkItem = new CheckListItem(
						Messages.get().Work_59);
				checkItem.setData(project);
				checkItem.setSelection(this);
				result.add(checkItem);
			}
			passed = true;

			// ****************************************************************************************
			// 2 检查负责人
			value = getCharger();
			if (value == null) {
				CheckListItem checkItem = new CheckListItem(
						Messages.get().Work_60, Messages.get().Work_61,
						Messages.get().Work_62, ICheckListItem.ERROR);
				checkItem.setData(project);
				checkItem.setSelection(this);
				checkItem.setEditorId(Project.EDITOR_CREATE_PLAN);
				checkItem.setEditorPageId(Project.EDITOR_PAGE_WBS);
				checkItem.setKey(F_CHARGER);
				result.add(checkItem);
				passed = false;
			}

			// 3.检查参与者
			value = getParticipatesIdList();
			if (value == null || ((BasicBSONList) value).isEmpty()) {
				CheckListItem checkItem = new CheckListItem(
						Messages.get().Work_63, Messages.get().Work_64,
						Messages.get().Work_65, ICheckListItem.WARRING);
				checkItem.setData(project);
				checkItem.setSelection(this);
				checkItem.setEditorId(Project.EDITOR_CREATE_PLAN);
				checkItem.setEditorPageId(Project.EDITOR_PAGE_WBS);
				checkItem.setKey(F_PARTICIPATE);
				result.add(checkItem);
				passed = false;
			}
			if (passed) {
				CheckListItem checkItem = new CheckListItem(
						Messages.get().Work_66);
				checkItem.setData(project);
				checkItem.setSelection(this);
				result.add(checkItem);
			}
			passed = true;

			IProcessControl pc = (IProcessControl) getAdapter(IProcessControl.class);
			// 4.1 检查工作变更的流程 ：错误，没有指明流程负责人
			String title = Messages.get().Work_67;
			String process = F_WF_CHANGE;
			String editorId = Project.EDITOR_CREATE_PLAN;
			String pageId = Project.EDITOR_PAGE_WBS;
			passed = ProjectToolkit.checkProcessInternal(this, pc, result,
					roleMap, title, process, editorId, pageId);
			if (passed) {
				CheckListItem checkItem = new CheckListItem(title);
				checkItem.setData(project);
				checkItem.setSelection(this);
				result.add(checkItem);
			}

			// 4.2 检查项目提交的流程 ：错误，没有指明流程负责人
			title = Messages.get().Work_68;
			process = F_WF_EXECUTE;
			passed = ProjectToolkit.checkProcessInternal(this, pc, result,
					roleMap, title, process, editorId, pageId);
			if (passed) {
				CheckListItem checkItem = new CheckListItem(title);
				checkItem.setData(project);
				checkItem.setSelection(this);
				result.add(checkItem);
			}

			passed = true;
			// 检查工作交付物
			List<PrimaryObject> docs = getDeliverableDocuments();
			if (docs.isEmpty()) {
				CheckListItem checkItem = new CheckListItem(
						Messages.get().Work_69, Messages.get().Work_70,
						Messages.get().Work_71, ICheckListItem.WARRING);
				checkItem.setData(project);
				checkItem.setSelection(this);
				checkItem.setKey(F_PARTICIPATE);
				result.add(checkItem);
				passed = false;
			}
			if (passed) {
				CheckListItem checkItem = new CheckListItem(
						Messages.get().Work_72);
				checkItem.setData(project);
				checkItem.setSelection(this);
				result.add(checkItem);
			}
		}

		return result;
	}

	protected List<Object[]> checkWorkStart(boolean warningCheck) {
		List<Object[]> message = new ArrayList<Object[]>();
		// 1.检查工作的计划开始和计划完成
		Object value = getPlanStart();
		if (value == null) {
			message.add(new Object[] { Messages.get().Work_73, this,
					warningCheck ? SWT.ICON_WARNING : SWT.ICON_ERROR,
					EDIT_WORK_PLAN_0 });
		}
		value = getPlanFinish();
		if (value == null) {
			message.add(new Object[] { Messages.get().Work_74, this,
					warningCheck ? SWT.ICON_WARNING : SWT.ICON_ERROR,
					EDIT_WORK_PLAN_0 });
		}
		// 2.检查工作的计划工时
		// 如果是独立工作可以跳过本步骤
		if (!isStandloneWork()) {
			value = getPlanWorks();
			if (value == null) {
				message.add(new Object[] { Messages.get().Work_75, this,
						SWT.ICON_WARNING, EDIT_WORK_PLAN_0 });
			}
		}
		// 3.检查工作名称
		value = getDesc();
		if (Utils.isNullOrEmptyString(value)) {
			message.add(new Object[] { Messages.get().Work_76, this,
					SWT.ICON_ERROR, EDIT_WORK_PLAN_0 });
		}
		// 4.检查负责人
		value = getCharger();
		if (value == null) {
			message.add(new Object[] { Messages.get().Work_77, this,
					warningCheck ? SWT.ICON_WARNING : SWT.ICON_ERROR,
					EDIT_WORK_PLAN_0 });
		}
		// 5.检查参与者
		value = getParticipatesIdList();
		if (!(value instanceof List) || ((List<?>) value).isEmpty()) {
			message.add(new Object[] { Messages.get().Work_78, this,
					SWT.ICON_WARNING, EDIT_WORK_PLAN_0 });
		}

		// // 6.1.检查工作变更的流程 ：错误，没有指明流程负责人
		// String process = F_WF_CHANGE;
		// if (!ProjectToolkit.checkProcessInternal(this, process)) {
		// throw new Exception("该工作变更流程没有指明流程负责人，" + this);
		// }

		IProcessControl pc = (IProcessControl) getAdapter(IProcessControl.class);

		// 6.2.检查工作执行的流程 ：错误，没有指明流程负责人
		if (!ProjectToolkit.checkProcessInternal(pc, F_WF_EXECUTE)) {
			message.add(new Object[] { Messages.get().Work_79, this,
					warningCheck ? SWT.ICON_WARNING : SWT.ICON_ERROR,
					EDIT_WORK_PLAN_1 });
		}

		// 7.检查工作交付物,警告
		List<PrimaryObject> docs = getDeliverableDocuments();
		if (docs.isEmpty()) {
			message.add(new Object[] { Messages.get().Work_80, this,
					SWT.ICON_WARNING, EDITOR });
		}
		return message;
	}

	/**
	 * 检查下级级联完成的工作是否可以完成，非级联完成的工作是否已经在已完成状态或已取消状态
	 * 
	 * @param id
	 * @return
	 */
	protected List<Object[]> checkCascadeFinish(ObjectId id) {
		try {
			reload();
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<Object[]> message = new ArrayList<Object[]>();
		// 1.判断非级联完成的工作是否已经在已完成状态、已取消、准备中、无状态的状态
		DBObject condition = new BasicDBObject();
		condition.put(F_PARENT_ID, id);
		condition.put(F_LIFECYCLE,
				new BasicDBObject().append("$in", new String[] { //$NON-NLS-1$
						STATUS_PAUSED_VALUE, STATUS_WIP_VALUE }));
		condition.put(F_SETTING_AUTOFINISH_WHEN_PARENT_FINISH,
				new BasicDBObject().append("$ne", Boolean.TRUE)); //$NON-NLS-1$
		long count = getRelationCountByCondition(Work.class, condition);
		if (count > 0) {
			message.add(new Object[] { Messages.get().Work_83, this,
					SWT.ICON_ERROR, EDITOR });
		}

		IProcessControl pc = (IProcessControl) getAdapter(IProcessControl.class);

		// 2.循环得到下级级联的暂停和进行中状态的工作,
		// 2.1判断取出工作是否可以完成，判断其是否可以跳过流程完成工作
		// 2.2判断取出工作的下级非级联完成的工作是否可以完成
		condition.put(F_SETTING_AUTOFINISH_WHEN_PARENT_FINISH, Boolean.TRUE);
		List<PrimaryObject> childrenWork = getRelationByCondition(Work.class,
				condition);
		if (childrenWork.size() > 0) {
			for (int i = 0; i < childrenWork.size(); i++) {
				Work childWork = (Work) childrenWork.get(i);
				if (pc.isWorkflowActivate(F_WF_EXECUTE)
						&& !Boolean.TRUE
								.equals(childWork
										.getValue(F_SETTING_CAN_SKIP_WORKFLOW_TO_FINISH))) {
					message.add(new Object[] { Messages.get().Work_84, this,
							SWT.ICON_ERROR, EDITOR });
				}
				message.addAll(checkCascadeFinish(childWork.get_id()));
			}
		}

		// 3.判断非级联完成的工作是否存在是必须的，是必须时，必须完成该工作
		condition = new BasicDBObject();
		condition.put(F_SETTING_AUTOFINISH_WHEN_PARENT_FINISH,
				new BasicDBObject().append("$ne", Boolean.TRUE));//$NON-NLS-1$
		condition.put(F_PARENT_ID, id);
		condition
				.put("$or",//$NON-NLS-1$
						new BasicDBObject[] {
								new BasicDBObject().append(F_MILESTONE,
										Boolean.TRUE),
								new BasicDBObject().append(F_MANDATORY,
										Boolean.TRUE) });
		condition.put(F_LIFECYCLE, new BasicDBObject().append(
				"$in", new String[] {//$NON-NLS-1$
				STATUS_PAUSED_VALUE, STATUS_WIP_VALUE, STATUS_NONE_VALUE, null,
						STATUS_ONREADY_VALUE }));

		count = getRelationCountByCondition(Work.class, condition);
		if (count > 0) {
			message.add(new Object[] { Messages.get().Work_88, this,
					SWT.ICON_ERROR, EDITOR });
		}

		return message;
	}

	/**
	 * 项目的工作检查必要信息是否录入
	 * 
	 * 2013-10-31 修改 zhonghua
	 * 
	 * 级联检查下级工作时，只考虑需同步启动的工作
	 * 
	 * @return
	 */
	protected List<Object[]> checkCascadeStart(boolean warningCheck) {
		try {
			reload();
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<Object[]> message = new ArrayList<Object[]>();
		List<PrimaryObject> childrenWork = getChildrenWork();
		if (childrenWork.size() > 0) {// 如果有下级，返回下级的检查结果
			for (int i = 0; i < childrenWork.size(); i++) {
				Work childWork = (Work) childrenWork.get(i);
				// 通过warningCheck，降低下级的检查标准
				if (Boolean.TRUE.equals(childWork
						.getValue(F_SETTING_AUTOSTART_WHEN_PARENT_START))) {
					message.addAll(childWork.checkCascadeStart(warningCheck));
				}
			}
		}

		// 非级联启动工作不检查
		if (!isProjectWBSRoot()) {
			message.addAll(checkWorkStart(warningCheck));
		}
		return message;
	}

	/**
	 * 是否有权限进行指派，该工作或上级工作的指派者时有权限
	 * 
	 * @param context
	 * @return
	 */
	private boolean hasPermissionForReassignment(IContext context) {
		String userId = context.getAccountInfo().getConsignerId();
		// 判断是否是本级的指派者
		if (userId.equals(getAssignerId())) {
			return true;
		} else {
			Work parent = (Work) getParent();
			if (parent != null) {
				return parent.hasPermissionForReassignment(context);
			} else {
				return false;
			}
		}
	}

	/**
	 * 是否有权限创建子工作、创建交付物、编辑当前工作 该工作或上级工作的负责人或项目的项目经理时有权限
	 * 
	 * @param context
	 * @return
	 */
	public boolean hasPermission(IContext context) {
		String userId = context.getAccountInfo().getConsignerId();
		// 判断是否是本级的负责人
		if (userId.equals(getChargerId())) {
			return true;
		} else {
			Work parent = (Work) getParent();
			if (parent != null && !parent.isEmpty()) {
				return parent.hasPermission(context);
			} else {
				// 是Root工作，判断是否是项目经理
				Project project = getProject();
				if (project != null) {
					return userId.equals(project.getChargerId());
				} else {
					return false;
				}
			}
		}
	}

	/**
	 * 返回所有上级共组
	 * 
	 * @return List
	 */
	public List<Work> getAllParents() {
		List<Work> result = new ArrayList<Work>();
		result.add(this);
		Work parent = (Work) getParent();
		while (parent != null && !parent.isEmpty()) {
			result.add(parent);
			parent = (Work) parent.getParent();
		}
		return result;
	}

	/**
	 * 返回流程执行者
	 * 
	 * @param wfRoleAss
	 * @param wfActors
	 * @param roleAssign
	 * @return BasicDBObject
	 */
	private BasicDBObject getWorkFlowActors(boolean bOverride,
			DBObject wfActors, DBObject wfRoleAss,
			Map<ObjectId, List<PrimaryObject>> roleAssign) {
		AbstractRoleAssignment assItem;
		List<PrimaryObject> assignments;
		String userid;
		BasicDBObject wfRoleActors = new BasicDBObject();

		Iterator<String> iter = wfRoleAss.keySet().iterator();
		while (iter.hasNext()) {
			String actionName = iter.next();
			String wfActor = null;
			if (wfActors != null) {
				wfActor = (String) wfActors.get(actionName);
			}
			if (wfActor == null || bOverride) {
				ObjectId actorRoleId = (ObjectId) wfRoleAss.get(actionName);
				if (actorRoleId != null) {
					assignments = roleAssign.get(actorRoleId);
					/**
					 * 
					 * 只处理该角色只有一个成员的情况
					 * 
					 */
					if (assignments != null && assignments.size() == 1) {
						// String[] actorList = new String[assignments.size()];
						// for (int j = 0; j < assignments.size(); j++) {
						// assItem = (AbstractRoleAssignment)
						// assignments.get(j);
						// userid = assItem.getUserid();
						// actorList[j] = userid;
						// }
						// wfRoleActors.put(actionName, actorList);

						// 只考虑指派一个人
						assItem = (AbstractRoleAssignment) assignments.get(0);
						userid = assItem.getUserid();
						wfRoleActors.put(actionName, userid);
					}
				}
			} else {
				wfRoleActors.put(actionName, wfActor);
			}
		}
		return wfRoleActors;
	}

	/**
	 * 返回工作的负责人角色
	 * 
	 * @return ProjectRole
	 */
	public ProjectRole getChargerRoleDefinition() {
		return getChargerRoleDefinition(ProjectRole.class);
	}

	/**
	 * 获取工作对应的交付物
	 * 
	 * @return
	 */
	public List<PrimaryObject> getDeliverable() {
		return getRelationById(F__ID, Deliverable.F_WORK_ID, Deliverable.class);
	}

	/**
	 * 获取工作对应的交付物文档
	 * 
	 * @return
	 */
	public List<PrimaryObject> getDeliverableDocuments() {
		List<PrimaryObject> result = new ArrayList<PrimaryObject>();
		List<PrimaryObject> d = getDeliverable();
		for (int i = 0; i < d.size(); i++) {
			Deliverable ditem = (Deliverable) d.get(i);
			Document doc = ditem.getDocument();
			if (doc != null) {
				result.add(doc);
			}
		}
		return result;
	}

	/**
	 * 获取负责人
	 * 
	 * @return
	 */
	public User getCharger() {
		String userId = getChargerId();
		if (Utils.isNullOrEmpty(userId)) {
			return null;
		}
		return UserToolkit.getUserById(userId);
	}

	public User getAssigner() {
		String userId = getAssignerId();
		if (Utils.isNullOrEmpty(userId)) {
			return null;
		}
		return UserToolkit.getUserById(userId);
	}

	/**
	 * 获取负责人ID
	 * 
	 * @return
	 */
	public String getChargerId() {
		return (String) getValue(F_CHARGER);
	}

	/**
	 * 获取指派人ID
	 * 
	 * @return
	 */
	public String getAssignerId() {
		return (String) getValue(F_ASSIGNER);
	}

	/**
	 * 获取工作承担者
	 * 
	 * @return
	 */
	public BasicBSONList getParticipatesIdList() {
		return (BasicBSONList) getValue(F_PARTICIPATE);
	}

	@Override
	public String getLifecycleStatus() {
		String lc = (String) getValue(F_LIFECYCLE);
		if (lc == null) {
			return STATUS_NONE_VALUE;
		} else {
			return lc;
		}
	}

	@Override
	public String getLifecycleStatusText() {
		String lc = getLifecycleStatus();
		return LifecycleToolkit.getLifecycleStatusText(lc);
	}

	/**
	 * 发送到工作的负责人、参与者、指派者（消息需要关联到工作）<br/>
	 * 发送消息将考虑合并消息，发送至同一人的同一类型的消息应当整合为一条<br/>
	 * 
	 * @param messageList
	 *            , 传入待发生的消息列表，如有相同用户的可以合并
	 * @param context
	 * @return
	 */
	public Map<String, Message> getCommitMessage(
			Map<String, Message> messageList, String title, IContext context) {
		// 1. 取工作负责人
		appendMessageForCharger(messageList, title, context);

		// 2. 取工作指派者
		appendMessageForAssigner(messageList, title, context);

		// 3. 获取参与者
		appendMessageForParticipate(messageList, title, context);

		// 4. 获取流程的执行人
		appendMessageForChangeWorkflowActor(messageList, title, context);

		appendMessageForExecuteWorkflowActor(messageList, title, context);

		List<PrimaryObject> children = getChildrenWork();
		for (int i = 0; i < children.size(); i++) {
			Work childwork = (Work) children.get(i);
			childwork.getCommitMessage(messageList, title, context);
		}
		return messageList;
	}

	public void appendMessageForCharger(Map<String, Message> messageList,
			String title, IContext context) {
		MessageToolkit
				.appendMessage(
						messageList,
						getChargerId(),
						title,
						Messages.get().Work_89 + ": " + getLabel(), this, EDITOR, context); //$NON-NLS-1$
	}

	public void appendMessageForAssigner(Map<String, Message> messageList,
			String title, IContext context) {
		MessageToolkit
				.appendMessage(
						messageList,
						getChargerId(),
						title,
						Messages.get().Work_91 + ": " + getLabel(), this, EDITOR, context); //$NON-NLS-2$
	}

	public void appendMessageForParticipate(Map<String, Message> messageList,
			String title, IContext context) {
		MessageToolkit
				.appendMessage(
						messageList,
						getChargerId(),
						title,
						Messages.get().Work_93 + ": " + getLabel(), this, EDITOR, context); //$NON-NLS-1$
	}

	public void appendMessageForExecuteWorkflowActor(
			Map<String, Message> messageList, String title, IContext context) {
		MessageToolkit.appendWorkflowActorMessage(this, messageList,
				F_WF_EXECUTE, Messages.get().Work_95, title, context
						.getAccountInfo().getConsignerId(), null);
	}

	public void appendMessageForChangeWorkflowActor(
			Map<String, Message> messageList, String title, IContext context) {
		MessageToolkit.appendWorkflowActorMessage(this, messageList,
				F_WF_CHANGE, Messages.get().Work_96, title, context
						.getAccountInfo().getConsignerId(), null);
	}

	/**
	 * 绑定工作流定义
	 * 
	 * @param workflowKey
	 *            ,工作流关键字，执行流程或者是变更流程 {@link #F_WF_EXECUTE} {@link #F_WF_CHANGE}
	 * @param workflowDefinition
	 */
	public void bindingWorkflowDefinition(String workflowKey,
			DBObject workflowDefinition) {
		setValue(workflowKey, workflowDefinition.get("KEY")); //$NON-NLS-1$
		setValue(workflowKey + IProcessControl.POSTFIX_ACTIVATED,
				workflowDefinition.get(IProcessControl.POSTFIX_ACTIVATED));
		setValue(workflowKey + IProcessControl.POSTFIX_ACTORS,
				workflowDefinition.get(IProcessControl.POSTFIX_ACTORS));
		setValue(workflowKey + IProcessControl.POSTFIX_ASSIGNMENT,
				workflowDefinition.get(IProcessControl.POSTFIX_ASSIGNMENT));
	}

	/**
	 * 保存工作
	 * 
	 * @return boolean
	 */
	@Override
	public boolean doSave(IContext context) throws Exception {

		/**
		 * BUG:10006
		 */
		String lc = getLifecycleStatus();
		if (lc.equals(STATUS_NONE_VALUE)) {
			setValue(F_LIFECYCLE, STATUS_ONREADY_VALUE);
		}

		checkAndCalculateDuration(F_PLAN_START, F_PLAN_FINISH, F_PLAN_DURATION);

		checkAndCalculateDuration(F_ACTUAL_START, F_ACTUAL_FINISH,
				F_ACTUAL_DURATION);

		checkProjectTimeline();

		super.doSave(context);

		resetCaculateCache();

		// 计算计划工时分配
		doCaculateWorksAllocated(context);

		// 重新计算上级工作的工时
		Work parent = (Work) getParent();
		if (parent != null) {
			parent.doReCaculateParentWork(false);
		}

		return true;

	}

	public void workTimeValidate() throws Exception {
		if (isStandloneWork()) {
			workTimeValidateOfStandloneWork();
		} else if (isProjectWork()) {
			workTimeValidateOfProjectWork(getProject());
		}
	}

	@Override
	public void doRemove(IContext context) throws Exception {
		if (!canDelete(context)) {
			return;
		}
		Work parent = (Work) getParent();
		doDelectIterator(context);
		// 计算计划工时分配
		doCaculateWorksAllocated(context);
		if (parent != null) {
			parent.doReCaculateParentWork(false);
		}
	}

	private void doDelectIterator(IContext context) throws Exception {
		if (hasChildrenWork()) {
			List<PrimaryObject> childrenWorks = getChildrenWork();
			for (PrimaryObject po : childrenWorks) {
				Work childrenWork = (Work) po;
				childrenWork.doDelectIterator(context);
			}
		}
		DBCollection col = getCollection();
		WriteResult ws = col.remove(
				new BasicDBObject().append(F__ID, get_id()),
				WriteConcern.NORMAL);
		checkWriteResult(ws);
		fireEvent(IPrimaryObjectEventListener.REMOVE);

		DBUtil.SAVELOG(context.getAccountInfo().getUserId(),
				Messages.get().Work_98, new Date(), getLabel()
						+ "\n" + getDbName() + "\\" + getCollectionName() //$NON-NLS-1$ //$NON-NLS-2$
						+ Messages.get().Work_101 + get_id(), getDbName());
	}

	private void doReCaculateParentWork(boolean useJob) {
		if (useJob) {
			Job job = new Job("重新计算工作") { //$NON-NLS-1$

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					Work.this.caculate();
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}

			};

			job.schedule();
		} else {
			this.caculate();
		}

	}

	protected void caculate() {
		DBCollection col = getCollection();
		// 计算开始时间和完成时间
		Object planStart = getChildrenValue(F_PLAN_START, 1, col);
		setValue(F_PLAN_START, planStart);

		Object planFinish = getChildrenValue(F_PLAN_FINISH, -1, col);
		setValue(F_PLAN_FINISH, planFinish);

		// Object actualStart = getChildrenValue(F_ACTUAL_START, 1, col);
		// setValue(F_ACTUAL_START, actualStart);
		//
		// Object actualFinish = getChildrenValue(F_ACTUAL_FINISH, -1, col);
		// setValue(F_ACTUAL_FINISH, actualFinish);

		// 计算计划工时和实际工时
		DBObject result = getChildrenGroupValue(F_PLAN_WORKS, F_ACTUAL_WORKS,
				"$sum", col); //$NON-NLS-1$
		Object planWorks = result == null ? null : result.get("result" //$NON-NLS-1$
				+ F_PLAN_WORKS);
		if (planWorks instanceof Number) {
			planWorks = new Double(((Number) planWorks).doubleValue());
		}
		setValue(F_PLAN_WORKS, planWorks);

		Object actualWorks = result == null ? null : result.get("result" //$NON-NLS-1$
				+ F_ACTUAL_WORKS);
		if (actualWorks instanceof Number) {
			actualWorks = new Double(((Number) actualWorks).doubleValue());
		}
		setValue(F_ACTUAL_WORKS, actualWorks);

		// 计算计划工期和实际工期
		result = getChildrenGroupValue(F_PLAN_DURATION, F_ACTUAL_DURATION,
				"$max", col); //$NON-NLS-1$
		Object planDuration = result == null ? null : result.get("result" //$NON-NLS-1$
				+ F_PLAN_DURATION);
		setValue(F_PLAN_DURATION, planDuration);

		Object actualDuration = result == null ? null : result.get("result" //$NON-NLS-1$
				+ F_ACTUAL_DURATION);
		setValue(F_ACTUAL_DURATION, actualDuration);

		DBObject val = new BasicDBObject();
		val.put(F_PLAN_START, planStart);
		val.put(F_PLAN_FINISH, planFinish);
		// val.put(F_ACTUAL_START, actualStart);
		// val.put(F_ACTUAL_FINISH, actualFinish);
		val.put(F_PLAN_WORKS, planWorks);
		val.put(F_ACTUAL_WORKS, actualWorks);
		val.put(F_PLAN_DURATION, planDuration);
		val.put(F_ACTUAL_DURATION, actualDuration);
		col.update(new BasicDBObject().append(F__ID, get_id()),
				new BasicDBObject().append("$set", val)); //$NON-NLS-1$

		Work parent = (Work) getParent();
		if (parent != null) {
			parent.caculate();
		}
	}

	private DBObject getChildrenGroupValue(String field1, String field2,
			String op, DBCollection col) {
		DBObject group = new BasicDBObject();
		group.put("$group", //$NON-NLS-1$
				new BasicDBObject().append(F__ID, "$" + F_PARENT_ID) //$NON-NLS-1$
						.append("result" + field1, //$NON-NLS-1$
								new BasicDBObject().append(op, "$" + field1)) //$NON-NLS-1$
						.append("result" + field2, //$NON-NLS-1$
								new BasicDBObject().append(op, "$" + field2))); //$NON-NLS-1$

		DBObject match = new BasicDBObject();
		match.put("$match", new BasicDBObject().append(F__ID, get_id())); //$NON-NLS-1$

		AggregationOutput agg = col.aggregate(group, match);
		Iterator<DBObject> iter = agg.results().iterator();
		if (iter.hasNext()) {
			return iter.next();
		}
		return null;
	}

	private Object getChildrenValue(String field, int i, DBCollection col) {
		DBCursor cursor = col.find(
				new BasicDBObject().append(F_PARENT_ID, get_id()).append(field,
						new BasicDBObject().append("$ne", null)), //$NON-NLS-1$
				new BasicDBObject().append(field, 1)).sort(
				new BasicDBObject().append(field, i));
		if (cursor.hasNext()) {
			return cursor.next().get(field);
		}
		return null;
	}

	private void resetCaculateCache() {
		overCount = null;
	}

	@Override
	public void doUpdate(IContext context) throws Exception {
		// 同步负责人、流程活动执行人到工作的参与者。
		ensureParticipatesConsistency();
		super.doUpdate(context);
	}

	@Override
	public void doInsert(IContext context) throws Exception {
		ObjectId id = get_id();
		if (id == null) {
			id = new ObjectId();
			setValue(F__ID, id);

			if (getValue(F_PARENT_ID) == null) {// 根工作
				setValue(F_ROOT_ID, id);
			} else {
				AbstractWork parent = getParent();
				ObjectId rootId = (ObjectId) parent.getValue(F_ROOT_ID);
				setValue(F_ROOT_ID, rootId);
			}
		}
		if (isStandloneWork()) {
			copyWorkDefinitionForStandloneWork(context);

			// 处理文档的复制
			copyDeliveryFromWorkDefinitionForStandloneWork(context);
		}

		// 同步负责人、流程活动执行人到工作的参与者。
		ensureParticipatesConsistency();

		// 缺省可以添加交付物
		Object value = getValue(F_SETTING_CAN_ADD_DELIVERABLES);
		if (value == null) {
			setValue(F_SETTING_CAN_ADD_DELIVERABLES, Boolean.TRUE);
		}

		super.doInsert(context);
	}

	private void copyDeliveryFromWorkDefinitionForStandloneWork(IContext context)
			throws Exception {
		WorkDefinition workdef = getWorkDefinition();
		if (workdef == null) {
			return;
		}

		// 处理文档
		Map<ObjectId, DBObject> documentsToInsert = new HashMap<ObjectId, DBObject>();
		List<DBObject> deliverableToInsert = new ArrayList<DBObject>();
		List<DBObject[]> fileToCopy = new ArrayList<DBObject[]>();

		DBCollection deliveryDefCol = getCollection(IModelConstants.C_DELIEVERABLE_DEFINITION);
		DBCursor deliCur = deliveryDefCol.find(new BasicDBObject().append(
				DeliverableDefinition.F_WORK_DEFINITION_ID,
				workdef.getValue(WorkDefinition.F__ID)));
		while (deliCur.hasNext()) {
			DBObject delidata = deliCur.next();
			// 根据模板的交付物定义创建交付物关系
			DBObject deliverableData = new BasicDBObject();

			// 设置工作Id
			deliverableData.put(Deliverable.F_WORK_ID, get_id());

			// 获得文档模板
			ObjectId documentTemplateId = (ObjectId) delidata
					.get(DeliverableDefinition.F_DOCUMENT_DEFINITION_ID);
			DBObject documentData = copyDocumentFromTemplate(documentsToInsert,
					fileToCopy, documentTemplateId);
			documentsToInsert.put(documentTemplateId, documentData);
			ObjectId documentId = (ObjectId) documentData.get(Document.F__ID);
			deliverableData.put(Deliverable.F_DOCUMENT_ID, documentId);
			deliverableToInsert.add(deliverableData);
		}

		// 保存文档
		DBCollection docCol = getCollection(IModelConstants.C_DOCUMENT);
		Collection<DBObject> collection = documentsToInsert.values();
		WriteResult ws;
		if (!collection.isEmpty()) {
			DBObject[] docList = new DBObject[collection.size()];
			int i = 0;
			for (DBObject documentObject : collection) {
				Document document = ModelService.createModelObject(
						documentObject, Document.class);
				document.initVerStatus();
				document.initVersionNumber();
				document.initInsertDefault(document.get_data(), context);
				document.generateCode(this);
				docList[i++] = document.get_data();
			}
			ws = docCol.insert(docList, WriteConcern.NORMAL);
			checkWriteResult(ws);
		}

		// 保存交付物
		DBCollection deliCol = getCollection(IModelConstants.C_DELIEVERABLE);
		if (!deliverableToInsert.isEmpty()) {
			ws = deliCol.insert(deliverableToInsert, WriteConcern.NORMAL);
			checkWriteResult(ws);
		}

		// 保存文件
		for (DBObject[] dbObjects : fileToCopy) {
			DBObject src = dbObjects[0];
			DBObject tgt = dbObjects[1];

			String srcDB = (String) src.get(RemoteFile.F_DB);
			String srcFilename = (String) src.get(RemoteFile.F_FILENAME);
			String srcNamespace = (String) src.get(RemoteFile.F_NAMESPACE);
			ObjectId srcID = (ObjectId) src.get(RemoteFile.F_ID);

			String tgtDB = (String) tgt.get(RemoteFile.F_DB);
			String tgtFilename = (String) tgt.get(RemoteFile.F_FILENAME);
			String tgtNamespace = (String) tgt.get(RemoteFile.F_NAMESPACE);
			ObjectId tgtID = (ObjectId) tgt.get(RemoteFile.F_ID);

			FileUtil.copyGridFSFile(srcID, srcDB, srcFilename, srcNamespace,
					tgtID, tgtDB, tgtFilename, tgtNamespace);
		}

	}

	/**
	 * 仅用于独立工作，从工作定义中复制角色
	 * 
	 * @param context
	 * @return
	 * @throws Exception
	 */
	private Map<ObjectId, DBObject> copyRoleDefinition(IContext context)
			throws Exception {
		// 准备返回值
		HashMap<ObjectId, DBObject> result = new HashMap<ObjectId, DBObject>();

		WorkDefinition workd = getWorkDefinition();
		if (workd == null) {
			return result;
		}
		ObjectId workDefinitionId = workd.get_id();

		DBCollection col_roled = getCollection(IModelConstants.C_ROLE_DEFINITION);

		// 查找模板的角色定义
		DBCursor cur = col_roled.find(new BasicDBObject().append(
				RoleDefinition.F_WORKDEFINITION_ID, workDefinitionId),
				new BasicDBObject().append(
						RoleDefinition.F_ORGANIZATION_ROLE_ID, 1));
		while (cur.hasNext()) {
			DBObject roleddata = cur.next();
			ObjectId oldId = (ObjectId) roleddata.get(F__ID);
			BasicDBObject newRoleData = new BasicDBObject()
					.append(RoleDefinition.F_WORK_ID, get_id())
					.append(RoleDefinition.F_ORGANIZATION_ROLE_ID,
							roleddata
									.get(RoleDefinition.F_ORGANIZATION_ROLE_ID))
					.append(RoleDefinition.F__ID, new ObjectId());
			result.put(oldId, newRoleData);
		}

		if (!result.isEmpty()) {
			DBObject[] insertData = result.values().toArray(new DBObject[0]);

			// 插入到数据库
			WriteResult ws = col_roled.insert(insertData, WriteConcern.NORMAL);
			checkWriteResult(ws);
		}

		return result;
	}

	public WorkDefinition getWorkDefinition() {
		Object value = getValue(F_WORK_DEFINITION_ID);
		if (value instanceof ObjectId) {
			return ModelService.createModelObject(WorkDefinition.class,
					(ObjectId) value);
		}
		return null;
	}

	/**
	 * 确保工作的参与者包括工作的负责人、流程执行人
	 */
	public void ensureParticipatesConsistency() {
		// 获取工作的负责人
		String chargerId = getChargerId();
		if (chargerId != null) {
			addParticipate(chargerId);
		}

		IProcessControl pc = (IProcessControl) getAdapter(IProcessControl.class);

		// 获得流程的执行人
		if (pc.isWorkflowActivate(F_WF_EXECUTE)) {
			DBObject processActorsMap = pc.getProcessActorsData(F_WF_EXECUTE);
			if (processActorsMap != null) {
				Iterator<String> iter = processActorsMap.keySet().iterator();
				while (iter.hasNext()) {
					String key = iter.next();
					String userId = (String) processActorsMap.get(key);
					if (userId != null) {
						addParticipate(userId);
					}
				}
			}
		}

	}

	public void addParticipate(String chargerId) {
		Assert.isTrue(chargerId != null, Messages.get().Work_118);
		BasicBSONList participatesIdList = getParticipatesIdList();
		if (participatesIdList == null) {
			participatesIdList = new BasicDBList();
			setValue(F_PARTICIPATE, participatesIdList);
		}
		if (!participatesIdList.contains(chargerId)) {
			participatesIdList.add(chargerId);
		}
	}

	/**
	 * 为工作及下级工作的负责人,参与者,工作流的执行者指定用户
	 * 
	 * @param bOverride
	 * 
	 * @param roleAssign
	 * @param context
	 * @throws Exception
	 */
	public void doAssignment(boolean bOverride,
			Map<ObjectId, List<PrimaryObject>> roleAssign, IContext context)
			throws Exception {
		AbstractRoleAssignment assItem;
		List<PrimaryObject> assignments;
		String userid;
		boolean modified = false;

		String lc = getLifecycleStatus();
		if (!STATUS_WIP_VALUE.equals(lc) && !STATUS_CANCELED_VALUE.equals(lc)
				&& !STATUS_FINIHED_VALUE.equals(lc)) {
			ObjectId roleId;
			// 设置负责人
			if (getChargerId() == null || bOverride) {
				roleId = (ObjectId) getValue(F_CHARGER_ROLE_ID);
				if (roleId != null) {
					assignments = roleAssign.get(roleId);
					if (assignments != null && !assignments.isEmpty()) {
						assItem = (AbstractRoleAssignment) assignments.get(0);
						userid = assItem.getUserid();
						setValue(F_CHARGER, userid);
						modified = true;
					}
				}
			}

			// 设置指派者
			if (getAssignerId() == null || bOverride) {
				roleId = (ObjectId) getValue(F_ASSIGNMENT_CHARGER_ROLE_ID);
				if (roleId != null) {
					assignments = roleAssign.get(roleId);
					if (assignments != null && !assignments.isEmpty()) {
						assItem = (AbstractRoleAssignment) assignments.get(0);
						userid = assItem.getUserid();
						setValue(F_ASSIGNER, userid);
						modified = true;
					}
				}
			}

			// 设置参与者
			BasicBSONList participatesIdList = getParticipatesIdList();
			if (participatesIdList == null || participatesIdList.size() == 0
					|| bOverride) {
				BasicBSONList roleIds = (BasicBSONList) getValue(F_PARTICIPATE_ROLE_SET);
				if (roleIds != null && roleIds.size() > 0) {
					BasicBSONList participates = new BasicDBList();
					for (int i = 0; i < roleIds.size(); i++) {
						DBObject object = (DBObject) roleIds.get(i);
						assignments = roleAssign.get(object.get(F__ID));
						if (assignments != null && !assignments.isEmpty()) {
							for (int j = 0; j < assignments.size(); j++) {
								assItem = (AbstractRoleAssignment) assignments
										.get(j);
								userid = assItem.getUserid();
								participates.add(userid);
							}
						}
					}
					if (participates.size() > 0) {
						setValue(F_PARTICIPATE, participates);
						modified = true;
					}
				}
			}

			// 设置变更工作流执行人

			DBObject wfRoleAss = (DBObject) getValue(F_WF_CHANGE_ASSIGNMENT);
			if (wfRoleAss != null) {
				BasicDBObject wfChangeActors = (BasicDBObject) getValue(F_WF_CHANGE_ACTORS);
				BasicDBObject wfRoleActors = getWorkFlowActors(bOverride,
						wfChangeActors, wfRoleAss, roleAssign);
				if (!wfRoleActors.isEmpty()) {
					setValue(F_WF_CHANGE_ACTORS, wfRoleActors);
					modified = true;
				}
			}

			// 设置执行工作流的执行人
			wfRoleAss = (DBObject) getValue(F_WF_EXECUTE_ASSIGNMENT);
			if (wfRoleAss != null) {
				BasicDBObject wfExecuteActors = (BasicDBObject) getValue(F_WF_EXECUTE_ACTORS);
				BasicDBObject wfRoleActors = getWorkFlowActors(bOverride,
						wfExecuteActors, wfRoleAss, roleAssign);
				if (!wfRoleActors.isEmpty()) {
					setValue(F_WF_EXECUTE_ACTORS, wfRoleActors);
					modified = true;
				}
			}
			if (modified) {
				doSave(context);
			}
		}
		// 设置下级
		List<PrimaryObject> children = getChildrenWork();
		for (int i = 0; i < children.size(); i++) {
			Work child = (Work) children.get(i);
			child.doAssignment(bOverride, roleAssign, context);
		}
	}

	/**
	 * 新建交付物
	 * 
	 * @param doc
	 *            ,文档
	 * @param context
	 * @return Deliverable
	 * @throws Exception
	 */
	public Deliverable doAddDeliverable(Document doc, String type,
			IContext context) throws Exception {
		Deliverable deli = makeDeliverableDefinition(type);
		deli.setValue(Deliverable.F_DOCUMENT_ID, doc.get_id());
		deli.doInsert(context);
		return deli;

	}

	/**
	 * 启动工作
	 */
	@SuppressWarnings("unchecked")
	public Object doStart(IContext context) throws Exception {
		// 准备保存的对象
		DBObject update = new BasicDBObject();
		Map<String, Object> params = new HashMap<String, Object>();

		if (!isProjectWBSRoot()) {
			// 判断能否启动，检查状态
			Assert.isTrue(canStart(), Messages.get().Work_119);
			// 调用前处理
			doStartBefore(context, params);

			// 判定是否使用执行工作流
			IProcessControl pc = (IProcessControl) getAdapter(IProcessControl.class);
			if (pc.isWorkflowActivate(F_WF_EXECUTE)) {
				// 如果是，启动工作流
				String lc = getLifecycleStatus();
				if (!STATUS_PAUSED_VALUE.equals(lc)) {

					Workflow wf = pc.getWorkflow(F_WF_EXECUTE);
					DBObject actors = pc.getProcessActorsData(F_WF_EXECUTE);
					Map<String, String> actorParameter = null;
					if (actors != null) {
						actorParameter = actors.toMap();
					}

					// 检查流程角色是否都已经指派到人
					checkProcessActorParameter(pc, actorParameter);

					ProcessInstance processInstance = wf.startHumanProcess(
							actorParameter, params);
					Assert.isNotNull(processInstance, Messages.get().Work_120
							+ this);

					update.put(F_WF_EXECUTE
							+ IProcessControl.POSTFIX_INSTANCEID,
							processInstance.getId());
				}
			}
		}

		// 启动下级同步启动的工作
		List<PrimaryObject> children = getChildrenWork();
		for (int i = 0; i < children.size(); i++) {
			Work childWork = (Work) children.get(i);
			// 检查下级的工作是否设置了与上级同步启动
			if (Boolean.TRUE.equals(childWork
					.getValue(F_SETTING_AUTOSTART_WHEN_PARENT_START))) {
				// 启动下级工作
				childWork.doStart(context);
			}
		}

		// 标记工作的进行中
		update.put(F_LIFECYCLE, STATUS_WIP_VALUE);
		// 设置工作的实际开始时间
		update.put(F_ACTUAL_START, new Date());

		DBCollection col = getCollection();
		DBObject newData = col.findAndModify(
				new BasicDBObject().append(F__ID, get_id()), null, null, false,
				new BasicDBObject().append("$set", update), true, false); //$NON-NLS-1$
		set_data(newData);

		// 提示工作启动
		// doNoticeWorkAction(context, Messages.get().Work_122);

		// 调用后处理
		doStartAfter(context, params);

		return null;
	}

	private void checkProcessActorParameter(IProcessControl pc,
			Map<String, String> actorParameter) throws Exception {
		DroolsProcessDefinition procd = pc.getProcessDefinition(F_WF_EXECUTE);
		Iterator<NodeAssignment> iter = procd.getNodesAssignment().iterator();
		while (iter.hasNext()) {
			NodeAssignment nas = iter.next();

			if (!nas.isRuleAssignment() && nas.forceAssignment()) {

				String ass = actorParameter.get(nas.getNodeActorParameter());
				if (Utils.isNullOrEmpty(ass)) {
					throw new Exception(Messages.get().Work_123 + this + ":" //$NON-NLS-2$
							+ procd.getProcessName());
				}
			}
		}
	}

	/**
	 * 暂停工作
	 */
	public Object doPause(IContext context) throws Exception {
		Assert.isTrue(canPause(), Messages.get().Work_125);
		Map<String, Object> params = new HashMap<String, Object>();
		doPauseBefore(context, params);

		// 暂停流程

		DBObject update = new BasicDBObject();

		List<PrimaryObject> children = getChildrenWork();
		for (int i = 0; i < children.size(); i++) {
			Work childWork = (Work) children.get(i);
			// 检查下级的工作状态是否为进行中
			if (STATUS_WIP_VALUE.equals(childWork.getValue(F_LIFECYCLE))) {
				// 暂停下级工作
				childWork.doPause(context);
			}
		}

		// 标记工作已暂停
		update.put(F_LIFECYCLE, STATUS_PAUSED_VALUE);
		DBCollection col = getCollection();
		DBObject newData = col.findAndModify(
				new BasicDBObject().append(F__ID, get_id()), null, null, false,
				new BasicDBObject().append("$set", update), true, false); //$NON-NLS-1$

		set_data(newData);

		// 提示工作已暂停
		// doNoticeWorkAction(context, Messages.get().Work_127);

		// 后处理
		doPauseAfter(context, params);

		return null;

	}

	/**
	 * 取消工作
	 */
	public Object doCancel(IContext context) throws Exception {
		Assert.isTrue(canCancel(), Messages.get().Work_128);
		Map<String, Object> params = new HashMap<String, Object>();
		doCancelBefore(context, params);

		List<PrimaryObject> children = getChildrenWork();
		for (int i = 0; i < children.size(); i++) {
			Work childWork = (Work) children.get(i);
			// // 检查下级的工作状态是否为进行中或者已暂停
			// if (STATUS_WIP_VALUE.equals(childWork.getValue(F_LIFECYCLE))
			// || STATUS_PAUSED_VALUE.equals(childWork
			// .getValue(F_LIFECYCLE))) {
			// }
			// 取消下级工作
			childWork.checkCancelAction(context);
			childWork.doCancel(context);
		}

		cancelExecuteProcessInstance(context);

		DBObject update = new BasicDBObject();
		// 标记工作已取消
		update.put(F_LIFECYCLE, STATUS_CANCELED_VALUE);

		DBCollection col = getCollection();
		DBObject newData = col.findAndModify(
				new BasicDBObject().append(F__ID, get_id()), null, null, false,
				new BasicDBObject().append("$set", update), true, false); //$NON-NLS-1$
		set_data(newData);

		doSaveProcessHistoryToDocument(context);

		// 提示工作已取消
		// doNoticeWorkAction(context, Messages.get().Work_130);
		doCancelAfter(context, params);

		return null;

	}

	public void doSaveProcessHistoryToDocument(IContext context) {
		// 将工作的流程记录存储到交付物文档中
		if (isExecuteWorkflowActivateAndAvailable()) {
			IProcessControl ip = Work.this.getAdapter(IProcessControl.class);
			BasicBSONList historys = ip.getWorkflowHistroyData();

			if (historys != null && historys.size() > 0) {
				DBObject wfHistory = new BasicDBObject();
				wfHistory.put(F_DESC, getDesc());
				wfHistory.put(F__CDATE, new Date());
				wfHistory.put(IDocumentProcess.F_HISTORY, historys);
				wfHistory.put(IDocumentProcess.F_WORK_ID, get_id());
				wfHistory.put(IDocumentProcess.F_PROCESS_INSTANCEID,
						getExecuteProcessId());
				DroolsProcessDefinition pd = ip
						.getProcessDefinition(F_WF_EXECUTE);
				wfHistory.put(IDocumentProcess.F_PROCESSID, pd.getProcessId());
				wfHistory.put(IDocumentProcess.F_PROCESSNAME,
						pd.getProcessName());

				try {
					doWFHistoryToDocument(wfHistory, context);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void cancelExecuteProcessInstance(IContext context)
			throws Exception {
		IProcessControl pc = (IProcessControl) getAdapter(IProcessControl.class);
		if (pc.isWorkflowActivate(F_WF_EXECUTE)) {

			Workflow wf = pc.getWorkflow(F_WF_EXECUTE);
			List<UserTask> reservedTasks = getAllUserTasks(Status.Reserved
					.name());
			for (int i = 0; i < reservedTasks.size(); i++) {
				UserTask userTask = reservedTasks.get(i);
				TaskData taskData = userTask.getTask().getTaskData();
				long workItemId = taskData.getWorkItemId();
				try {
					wf.abortWorkItem(workItemId);
				} catch (Exception e) {
					e.printStackTrace();
				}
				// userTask.setValue(UserTask.F_STATUS, Status.Exited.name());
				// userTask.doSave(context);
			}
			try {
				Long instanceId = getExecuteProcessId();
				wf.abortProcess(instanceId.longValue());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Long getExecuteProcessId() {
		return getLongValue(F_WF_EXECUTE + IProcessControl.POSTFIX_INSTANCEID)
				.longValue();
	}

	public ProcessInstance getExecuteProcess() {
		IProcessControl pc = getAdapter(IProcessControl.class);
		if (pc.isWorkflowActivate(F_WF_EXECUTE)) {
			Long pid = getExecuteProcessId();
			if (pid != null) {
				Workflow wf = pc.getWorkflow(F_WF_EXECUTE);
				return wf.getProcess(pid);
			}
		}

		return null;
	}

	public Object doFinish(IContext context) throws Exception {
		Assert.isTrue(canFinish(), Messages.get().Work_132);
		Map<String, Object> params = new HashMap<String, Object>();
		doFinishBefore(context, params);

		/*
		 * DBCollection col = getCollection(); DBObject query=new
		 * BasicDBObject().append(Work.F__ID,get_id()); DBObject update =
		 * col.findOne(query); update.put(F_LIFECYCLE,
		 * Work.STATUS_FINIHED_VALUE); update.put(F_ACTUAL_FINISH, new Date());
		 * DBObject sort=new BasicDBObject().append(F__ID, -1);
		 * 
		 * col.findAndModify(query, null, sort, false, update, false, false);
		 * 
		 * //查询下级 BasicDBObject queryCondition = new BasicDBObject();
		 * //设置查询条件，该工作的所有下级工作 queryCondition.put(Work.F_PARENT_ID,get_id());
		 * //设置查询条件，该工作所有正在进行中和已暂停的下级工作 queryCondition.put(Work.F_LIFECYCLE,new
		 * BasicDBObject().append("$in", new String[] { Work.STATUS_WIP_VALUE,
		 * Work.STATUS_PAUSED_VALUE})); //查询，返回该工作所有正在进行中的下级工作 DBCursor cur =
		 * col.find(queryCondition); while(cur.hasNext()){ DBObject dbobject =
		 * cur.next(); Work work = ModelService.createModelObject(dbobject,
		 * Work.class); work.doFinish(context);
		 * 
		 * }
		 */

		// 检查流程是否完成
		// if (Boolean.TRUE
		// .equals(getValue(F_SETTING_CAN_SKIP_WORKFLOW_TO_FINISH))) {
		// ProcessInstance pi = getExecuteProcess();
		// if (pi != null) {
		// if (pi.getState() != ProcessInstance.STATE_COMPLETED
		// || pi.getState() != ProcessInstance.STATE_ABORTED) {
		// IProcessControl pc = (IProcessControl)
		// getAdapter(IProcessControl.class);
		// if (pc.isWorkflowActivate(F_WF_EXECUTE)) {
		// Workflow wf = pc.getWorkflow(F_WF_EXECUTE);
		// Long instanceId = getExecuteProcessId();
		// wf.abortProcess(instanceId.longValue());
		// }
		// }
		// }
		// }

		DBObject update = new BasicDBObject();
		List<PrimaryObject> children = getChildrenWork();
		for (int i = 0; i < children.size(); i++) {
			Work childWork = (Work) children.get(i);
			// 检查下级的工作状态是否为进行中或已暂停
			String childLC = childWork.getLifecycleStatus();
			if (STATUS_WIP_VALUE.equals(childLC)
					|| STATUS_PAUSED_VALUE.equals(childWork
							.getValue(F_LIFECYCLE))) {
				// 完成下级工作
				childWork.doFinish(context);
			} else if (STATUS_CANCELED_VALUE.equals(childLC)
					|| STATUS_FINIHED_VALUE.equals(childLC)) {
			} else {
				// 取消工作
				childWork.doCancel(context);

			}
		}

		// 标记工作已完成
		update.put(F_LIFECYCLE, STATUS_FINIHED_VALUE);
		// 设置工作的实际完成时间
		update.put(F_ACTUAL_FINISH, new Date());
		// 设置实际工时
		try {
			double actualWorks = calculateActualWorks();
			update.put(F_ACTUAL_WORKS, actualWorks);
		} catch (Exception e) {
		}

		DBCollection col = getCollection();
		DBObject newData = col.findAndModify(
				new BasicDBObject().append(F__ID, get_id()), null, null, false,
				new BasicDBObject().append("$set", update), true, false); //$NON-NLS-1$
		set_data(newData);

		// 提示工作已完成
		// doNoticeWorkAction(context, Messages.get().Work_135);
		doFinishAfter(context, params);

		doCalculateWorkPerformence(context);
		return null;

	}

	/**
	 * 将工作的流程记录存储到本工作及其子工作的交付物文档中
	 * 
	 * @param context
	 * @param wfHistory
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void doWFHistoryToDocument(DBObject wfHistory, IContext context)
			throws Exception {
		/**
		 * 考虑到保存历史的方式更改为每次人工任务完成都进行保存。重写保存的代码。
		 */

		/*
		 * List<PrimaryObject> documentList = getOutputDeliverableDocuments();
		 * DBCollection col = getCollection(IModelConstants.C_DOCUMENT); for
		 * (PrimaryObject po : documentList) { Document document = (Document)
		 * po; WriteResult ws = col.update(new BasicDBObject().append(
		 * Document.F__ID, document.get_id()), new BasicDBObject()
		 * .append("$push", new BasicDBObject().append( Document.F_WF_HISTORY,
		 * wfHistory))); checkWriteResult(ws); }
		 */
		List<PrimaryObject> documentList = getOutputDeliverableDocuments();
		DBCollection col = getCollection(IModelConstants.C_DOCUMENT);
		for (PrimaryObject po : documentList) {
			Document document = (Document) po;
			wfHistory.put(IDocumentProcess.F_MAJOR_VID,
					document.getValue(Document.F_MAJOR_VID));
			wfHistory.put(IDocumentProcess.F_SECOND_VID,
					document.getValue(Document.F_SECOND_VID));
			// 查找已经保存过的历史
			Object historyList = document.getValue(Document.F_WF_HISTORY);
			if (historyList instanceof List<?>) {
				for (int i = 0; i < ((List<?>) historyList).size(); i++) {
					DBObject historyProcessRec = (DBObject) ((List<?>) historyList)
							.get(i);
					// 取出流程实例的ID
					Object pid = historyProcessRec
							.get(IDocumentProcess.F_PROCESS_INSTANCEID);
					if (pid.equals(wfHistory
							.get(IDocumentProcess.F_PROCESS_INSTANCEID))) {
						((List<?>) historyList).remove(i);
						break;
					}
				}
			} else {
				historyList = new ArrayList<DBObject>();
			}
			((List) historyList).add(wfHistory);

			WriteResult ws = col.update(
					new BasicDBObject().append(Document.F__ID,
							document.get_id()),
					new BasicDBObject().append(
							"$set", new BasicDBObject().append( //$NON-NLS-1$
									Document.F_WF_HISTORY, historyList)));
			checkWriteResult(ws);
		}

		/**
		 * zhonghua: 认为以下的处理欠妥.暂时注释
		 * 
		 * isExecuteWorkflowActivateAndAvailable 是指具有流程，且流程激活的工作，
		 * 与是否保存本级别的流程历史是否有关系？
		 */
		List<PrimaryObject> childrenWorkList = getChildrenWork();
		for (PrimaryObject po : childrenWorkList) {
			Work childrenWork = (Work) po;
			childrenWork.doWFHistoryToDocument(wfHistory, context);
		}
	}

	/**
	 * 计算处理计划工时的分配
	 * 
	 * @param context
	 */
	public void doCaculateWorksAllocated(IContext context) {
		final String userid = context.getAccountInfo().getUserId();
		Job job = new Job(Messages.get().Work_137) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				// 如果该工作是准备中，进行时才能够计算分配
				String lc = getLifecycleStatus();
				if (!Utils.inArray(lc, new String[] { STATUS_ONREADY_VALUE,
						STATUS_WIP_VALUE })) {
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}

				// 只计算项目工作
				if (!isProjectWork()) {
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}
				Project project = getProject();
				lc = project.getLifecycleStatus();
				if (!Utils.inArray(lc, new String[] { STATUS_ONREADY_VALUE,
						STATUS_WIP_VALUE })) {
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}

				// 记录上级工作，准备删除
				List<ObjectId> syncRemove = new ArrayList<ObjectId>();
				Work parent = (Work) getParent();
				int loopcount = 0;
				while (parent != null && loopcount < 20) {
					loopcount++;
					if (syncRemove.contains(parent.get_id())) {
						break;
					}
					syncRemove.add(parent.get_id());
					parent = (Work) getParent();
				}

				// 如果是摘要工作，准备删除
				if (isSummaryWork()) {
					syncRemove.add(get_id());
				}

				// 得到计划工时
				Double works = getPlanWorks();
				if (works == null || works.doubleValue() == 0d) {
					syncRemove.add(get_id());
				}

				// 得到实际开始时间和实际完成时间
				Date start = getPlanStart();
				Date finish = getPlanFinish();
				if (start == null || finish == null) {
					syncRemove.add(get_id());
				}

				DBCollection col = getCollection(IModelConstants.C_WORKS_ALLOCATE);
				col.remove(new BasicDBObject().append(WorksAllocate.F_WORKID,
						new BasicDBObject().append("$in", syncRemove)), //$NON-NLS-1$
						WriteConcern.NORMAL);

				if (works == null || works.doubleValue() == 0d || start == null
						|| finish == null) {
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}

				// 如果手工填报过工时，不自动计算
				if (hasManualRecordAllocate()) {
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}

				// 得到日历计算器
				CalendarCaculater calCaculater = project.getCalendarCaculater();

				Calendar currentCal = Calendar.getInstance();
				currentCal.setTime(start);
				Calendar finishCal = Calendar.getInstance();
				finishCal.setTime(finish);

				// 遍历计算每天的工时
				// 按人天进行平均
				int workDays = calCaculater.getWorkingDays(start, finish);
				double personDayWorks = works / workDays;

				List<WorksPerformence> records = new ArrayList<WorksPerformence>();

				String chargerId = getChargerId();

				while (currentCal.before(finishCal)) {
					Date date = currentCal.getTime();
					double workingTime = calCaculater.getWorkingTime(date);

					// 排除非工作时间
					if (workingTime > 0) {

						long dateCode = currentCal.getTimeInMillis()
								/ (24 * 60 * 60 * 1000);
						WorksPerformence po = makeWorksPerformence(chargerId,
								new Long(dateCode));
						po.setValue(WorksPerformence.F_WORKS, new Double(
								personDayWorks));
						po.setValue(WorksPerformence.F_DESC, "[系统计算]"); //$NON-NLS-1$
						records.add(po);
					}
					currentCal.add(Calendar.DATE, 1);
				}

				DBObject[] data = new DBObject[records.size()];
				for (int i = 0; i < records.size(); i++) {
					data[i] = records.get(i).get_data();
				}
				col.insert(data, WriteConcern.NORMAL);

				try {
					DBUtil.SAVELOG(userid, Messages.get().Work_140, new Date(),
							Messages.get().Work_141, IModelConstants.DB);
				} catch (Exception e) {
				}
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}

		};

		job.setUser(false);
		job.schedule();
	}

	protected boolean hasManualRecordAllocate() {
		return false;
	}

	/**
	 * 计算实际工时分摊
	 * 
	 * @param context
	 */
	public void doCalculateWorkPerformence(final IContext context) {
		final String userid = context.getAccountInfo().getUserId();

		Job job = new Job("计算实际工时分摊") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				// 如果该工作完成状态才能够计算绩效
				String lc = getLifecycleStatus();
				if (!STATUS_FINIHED_VALUE.equals(lc)) {
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}

				// 只计算项目工作
				if (!isProjectWork()) {
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}

				// 不计算摘要工作
				if (isSummaryWork()) {
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}

				// 如果手工填报过工时，不自动计算
				if (hasManualRecordPerformence()) {
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}

				// 得到实际工时，如果没有，获取计划工时
				Double works = getActualWorks();
				if (works == null) {
					works = getPlanWorks();
				}
				if (works == null) {
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}

				// 得到实际开始时间和实际完成时间
				Date start = getActualStart();
				Date finish = getActualFinish();
				if (start == null || finish == null) {
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}

				// 得到工作的参与者(不计算参与者)
				// BasicBSONList participatesIds = getParticipatesIdList();
				// if (participatesIds == null || participatesIds.size() < 1) {
				// return; // }

				// 得到日历计算器
				Project project = getProject();
				CalendarCaculater calCaculater = project.getCalendarCaculater();

				Calendar currentCal = Calendar.getInstance();
				currentCal.setTime(start);
				Calendar finishCal = Calendar.getInstance();
				finishCal.setTime(finish);

				// 遍历计算每天的工时 // 按人天进行平均
				int workDays = calCaculater.getWorkingDays(start, finish);
				double personDayWorks = works / workDays;

				List<WorksPerformence> records = new ArrayList<WorksPerformence>();

				String chargerId = getChargerId();

				while (currentCal.before(finishCal)) {
					Date date = currentCal.getTime();
					double workingTime = calCaculater.getWorkingTime(date);

					// 排除非工作时间
					if (workingTime > 0) {
						long dateCode = currentCal.getTimeInMillis()
								/ (24 * 60 * 60 * 1000);
						WorksPerformence po = makeWorksPerformence(chargerId,
								new Long(dateCode));
						po.setValue(WorksPerformence.F_WORKS, new Double(
								personDayWorks));
						po.setValue(WorksPerformence.F_DESC,
								Messages.get().Work_143);
						records.add(po);
					}

					currentCal.add(Calendar.DATE, 1);
				}

				DBCollection col = getCollection(IModelConstants.C_WORKS_PERFORMENCE);
				DBObject[] data = new DBObject[records.size()];
				for (int i = 0; i < records.size(); i++) {
					data[i] = records.get(i).get_data();
				}
				col.insert(data, WriteConcern.NORMAL);

				try {
					DBUtil.SAVELOG(userid, Messages.get().Work_144, new Date(),
							Messages.get().Work_145, IModelConstants.DB);
				} catch (Exception e) {
				}

				return org.eclipse.core.runtime.Status.OK_STATUS;
			}
		};
		job.setUser(false);
		job.schedule();
	}

	public double calculateActualWorks() throws Exception {
		Double actualWorks = null;
		// 获取计量方式F_MEASUREMENT
		Object measurement = getValue(F_MEASUREMENT);
		if (MEASUREMENT_TYPE_NO_ID.equals(measurement)) {
			// 判断工作的工时计量方式F_MEASUREMENT的值是无工时
			actualWorks = 0d;
		} else if (MEASUREMENT_TYPE_COMMIT_ID.equals(measurement)
				|| measurement == null) {
			// 如果工作的计量方式是填报工时,计算实际工时
			actualWorks = caculateActualWorksForCommitableWork();
		} else if (MEASUREMENT_TYPE_STANDARD_ID.equals(measurement)) {
			// 判断工作的工时计量方式F_MEASUREMENT的值是计划工时
			// 在work类中使用getProject()获取工作所在的项目
			Project project = getProject();
			if (project != null) {
				// 当项目的工作是标准工时制时,将计划工时设置到实际工时,
				// 如果独立工作被附加到项目中，会使用项目的工时方案，工时参数选项，重新计算工时
				actualWorks = calculatePlanWorksFromStandardForProjectWork(project);
			} else {
				// 对于独立工作而言，直接取出标准工时
				actualWorks = (Double) getValue(F_STANDARD_WORKS);
			}
		}
		return actualWorks == null ? 0d : actualWorks.doubleValue();
	}

	/**
	 * 计算并返回工作的计划工时,
	 * 
	 * 工作的计量方式是无工时制时返回0，
	 * 
	 * 是填报工时时，返回计划工时字段本身的值，
	 * 
	 * 标准制时，如果是项目工作，按项目的工时方案及工时参数选项计算计划工时，出错时抛出异常
	 * 
	 * 如果是独立工作，返回标准工时
	 * 
	 * @return
	 * @throws Exception
	 */
	public Double calculatePlanWorks() throws Exception {
		Double planWorks = null;
		// 获取计量方式F_MEASUREMENT
		Object measurement = getValue(F_MEASUREMENT);
		if (MEASUREMENT_TYPE_NO_ID.equals(measurement)) {
			// 判断工作的工时计量方式F_MEASUREMENT的值是无工时
			planWorks = 0d;
		} else if (MEASUREMENT_TYPE_COMMIT_ID.equals(measurement)
				|| measurement == null) {
			// 如果工作的计量方式是填报工时，就不做任何处理，直接返回工作的planWorks
			planWorks = getPlanWorks();
		} else if (MEASUREMENT_TYPE_STANDARD_ID.equals(measurement)) {
			// 判断工作的工时计量方式F_MEASUREMENT的值是计划工时
			// 在work类中使用getProject()获取工作所在的项目
			Project project = getProject();
			if (project != null) {
				planWorks = calculatePlanWorksFromStandardForProjectWork(project);
			} else {
				// 对于独立工作而言，直接取出标准工时
				planWorks = (Double) getValue(F_STANDARD_WORKS);
			}
		}
		return planWorks;
	}

	private double caculateActualWorksForCommitableWork() {
		double actualWorks = 0d;
		// 判断工作的工时计量方式F_MEASUREMENT的值是填报工时或者为空
		// 读取works performance表，根据work_id分组求和（F_WORKS）
		DBCollection col = getCollection(IModelConstants.C_WORKS_PERFORMENCE);
		DBObject match = new BasicDBObject()
				.append("$match", new BasicDBObject().append(
						WorksPerformence.F_WORKID, get_id()));
		DBObject group = new BasicDBObject().append(
				"$group",
				new BasicDBObject().append(F__ID,
						"$" + WorksPerformence.F_WORKID).append(
						"total",
						new BasicDBObject().append("$sum", "$"
								+ WorksPerformence.F_WORKS)));
		AggregationOutput aggResult = col.aggregate(match, group);
		Iterator<DBObject> results = aggResult.results().iterator();
		if (results.hasNext()) {
			DBObject result = results.next();
			Object total = result.get("total");
			if (total instanceof Number) {
				actualWorks = ((Number) total).doubleValue();
			}
		}
		return actualWorks;
	}

	/**
	 * 根据工作上标准工时制计算项目工作的实际工时
	 * 
	 * @param project
	 * @return
	 * @throws Exception
	 */
	private double calculatePlanWorksFromStandardForProjectWork(Project project)
			throws Exception {
		Assert.isNotNull(project);
		// 获取项目的工时方案
		WorkTimeProgram workTimeProgram = project.getWorkTimeProgram();
		if (workTimeProgram == null) {
			throw new Exception(Messages.get().NotFoundOfWorkTimeProgram);
		}
		Set<ObjectId> paraXOptionIdSet = new HashSet<ObjectId>();
		// 获取项目中的列类型选项id的set集合
		Set<ObjectId> paraYOptionIdSet = project.getWorkTimeParaYOptionIds();
		if (paraYOptionIdSet.isEmpty()) {
			throw new Exception(Messages.get().ParaYOptionIdIsNull);
		}
		// 获取工作上的工时类型
		BasicBSONList paraXs = (BasicBSONList) getValue(F_WORKTIME_PARAX);
		if (paraXs == null) {
			throw new Exception(Messages.get().NoParaX);
		}
		for (int i = 0; i < paraXs.size(); i++) {
			DBObject paraX = (DBObject) paraXs.get(i);
			ObjectId paraXId = (ObjectId) paraX.get(F_WORKTIME_PARAX_ID);
			// ObjectId paraXId = (ObjectId) paraX.get(F__ID);
			if (paraXId != null) {
				// 根据工时类型id获取项目上的工时类型选项
				DBObject paraXOption = project.getParaXOption(paraXId);
				ObjectId paraXOptionId = (ObjectId) paraXOption.get(F__ID);
				paraXOptionIdSet.add(paraXOptionId);
				// BasicBSONList paraXOptions = (BasicBSONList) project
				// .getParaXOption(paraXId);
				// if (paraXOptions != null) {
				// for (int j = 0; j < paraXOptions.size(); j++) {
				// DBObject paraXOption = (DBObject) paraXOptions.get(j);
				// ObjectId paraXOptionId = (ObjectId) paraXOption
				// .get(F__ID);
				// // 将工时类型选项id加入到set集合
				// paraXOptionIdSet.add(paraXOptionId);
				// }
				// }
			}
		}

		// 两层循环遍历，获得列类型选项id和工时类型选项id
		double summary = 0d;
		Iterator<ObjectId> wIter = paraXOptionIdSet.iterator();
		while (wIter.hasNext()) {
			Iterator<ObjectId> cIter = paraYOptionIdSet.iterator();
			ObjectId wId = wIter.next();
			while (cIter.hasNext()) {
				ObjectId cId = cIter.next();
				// 根据项目上获取的列类型选项id和工作上工时类型与项目上工时类型匹配，从项目中取出的工时类型选项id获取工时数据
				Double value = workTimeProgram.getWorkTimeData(wId, cId);
				if (value != null) {
					summary += value;
				}
			}
		}
		return summary;

	}

	/**
	 * 发送工作操作完成的通知
	 * 
	 * @param context
	 *            当前的上下文
	 * @param actionName
	 *            操作的文本名称
	 * @return
	 * @throws Exception
	 *             发送消息出现的错误
	 */
	private Message doNoticeWorkActionInternal(IContext context,
			String actionName) throws Exception {
		// 设置收件人
		BasicBSONList participatesIdList = getParticipatesIdList();
		if (participatesIdList == null || participatesIdList.isEmpty()) {
			return null;
		}
		// 排除自己
		participatesIdList.remove(context.getAccountInfo().getConsignerId());

		// 设置通知标题
		Project project = getProject();

		String title = (project == null ? "" : project.getLabel()) + " " + this //$NON-NLS-1$ //$NON-NLS-2$
				+ " " + actionName; //$NON-NLS-1$

		// 设置通知内容
		StringBuffer sb = new StringBuffer();
		sb.append("<span style='font-size:14px'>"); //$NON-NLS-1$
		sb.append(Messages.get().Work_150);
		sb.append("</span><br/><br/>"); //$NON-NLS-1$
		sb.append(Messages.get().Work_152);
		sb.append("<br/><br/>"); //$NON-NLS-1$

		sb.append(context.getAccountInfo().getUserId() + "|" //$NON-NLS-1$
				+ context.getAccountInfo().getUserName());
		sb.append(", "); //$NON-NLS-1$
		sb.append(actionName);
		sb.append(Messages.get().Work_156);
		sb.append("\""); //$NON-NLS-1$
		sb.append(this);
		sb.append("\""); //$NON-NLS-1$
		if (isProjectWork()) {
			sb.append(" \""); //$NON-NLS-1$
			sb.append(Messages.get().Work_160);
			sb.append(getProject());
			sb.append(" \""); //$NON-NLS-1$
		}

		sb.append("<br/><br/>"); //$NON-NLS-1$
		sb.append(Messages.get().Work_163);

		Message message = MessageToolkit.makeMessage(participatesIdList, title,
				context.getAccountInfo().getConsignerId(), sb.toString());

		MessageToolkit.appendEndMessage(message);

		// 设置导航附件
		message.appendTargets(this, EDITOR, false);

		message.doSave(context);

		return message;
	}

	@SuppressWarnings("unused")
	private void doNoticeWorkAction(final IContext context,
			final String actionName) throws Exception {
		// doNoticeWorkActionInternal(context, actionName);

		Job job = new Job(Messages.get().Work_164) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					doNoticeWorkActionInternal(context, actionName);
				} catch (Exception e) {
					e.printStackTrace();
				}

				return org.eclipse.core.runtime.Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	public BasicBSONList getTargetList() {
		return (BasicBSONList) getValue(F_TARGETS);
	}

	/**
	 * 获得流程某个活动执行者对应的用户
	 * 
	 * @param nodeActor
	 * @return
	 */
	public List<User> getPermittedUserOfWorkflowActor(String processKey,
			String nodeActor) {
		IProcessControl pc = (IProcessControl) getAdapter(IProcessControl.class);

		DBObject processRoleDefinitions = pc
				.getProcessRoleAssignmentData(processKey);
		ObjectId _id = (ObjectId) processRoleDefinitions.get(nodeActor);

		List<PrimaryObject> ralist;
		if (getProjectId() != null) {
			ProjectRole roled = ModelService.createModelObject(
					ProjectRole.class, _id);
			ralist = roled.getAssignment();

		} else {
			Role role = ModelService.createModelObject(Role.class, _id);
			// TODO 使用TYPE为TYPE_WORK_PROCESS的RoleParameter，传入工作ID进行人员指派
			IRoleParameter roleParameter = getAdapter(IRoleParameter.class);
			ralist = role.getAssignment(roleParameter);
		}
		List<User> result = new ArrayList<User>();
		if (ralist != null) {
			for (int i = 0; i < ralist.size(); i++) {
				AbstractRoleAssignment ra = (AbstractRoleAssignment) ralist
						.get(i);
				String userid = ra.getUserid();
				User user = UserToolkit.getUserById(userid);
				result.add(user);
			}
		}

		return result;
	}

	private void doStartAfter(IContext context, Map<String, Object> params)
			throws Exception {
		ModelActivator.executeEvent(this, "start.after", params); //$NON-NLS-1$
	}

	private void doStartBefore(IContext context, Map<String, Object> params)
			throws Exception {
		ModelActivator.executeEvent(this, "start.before", params); //$NON-NLS-1$
	}

	private void doPauseAfter(IContext context, Map<String, Object> params)
			throws Exception {
		ModelActivator.executeEvent(this, "pause.after", params); //$NON-NLS-1$

	}

	private void doPauseBefore(IContext context, Map<String, Object> params)
			throws Exception {
		ModelActivator.executeEvent(this, "pause.before", params); //$NON-NLS-1$

	}

	private void doFinishAfter(IContext context, Map<String, Object> params)
			throws Exception {
		ModelActivator.executeEvent(this, "finish.after", params); //$NON-NLS-1$
	}

	private void doFinishBefore(IContext context, Map<String, Object> params)
			throws Exception {
		ModelActivator.executeEvent(this, "finish.before", params); //$NON-NLS-1$
	}

	private void doCancelAfter(IContext context, Map<String, Object> params)
			throws Exception {
		ModelActivator.executeEvent(this, "cancel.after", params); //$NON-NLS-1$

	}

	private void doCancelBefore(IContext context, Map<String, Object> params)
			throws Exception {
		ModelActivator.executeEvent(this, "cancel.before", params); //$NON-NLS-1$

	}

	// public boolean doUpdateTask(String key, Task task,
	// String userid) throws Exception {
	// // IProcessControl pc = (IProcessControl)
	// getAdapter(IProcessControl.class);
	//
	// TaskData taskData = task.getTaskData();
	// Status status = taskData.getStatus();
	//
	// /*
	// * 获得当前用户的现有流程任务数据 * 如果任务id和状态一致，那么就无需继续更新操作
	// */
	//
	// DBCollection col = getCollection(IModelConstants.C_USERTASK);
	//
	// BasicDBObject query = new BasicDBObject();
	// query.put(UserTask.F_WORK_ID, get_id());
	// query.put(UserTask.F_USERID, userid);
	// query.put(UserTask.F_TASKID, task.getId());
	// query.put(UserTask.F_STATUS, status.name());
	// query.put(UserTask.F_PROCESSKEY, key);
	//
	// long cnt = col.count(query);
	//
	// if (cnt > 0) {
	// return false;
	// }
	//
	// /*
	// * 构造需要保存的任务数据
	// */
	//
	// DBObject data = new BasicDBObject();
	//
	// // 保存任务id
	// data.put(UserTask.F_TASKID, task.getId());
	//
	// // 保存任务名称
	// List<I18NText> names = task.getNames();
	// Assert.isLegal(names != null && names.size() > 0, "流程活动名称没有定义");
	// String taskName = names.get(0).getText();
	// data.put(UserTask.F_DESC, taskName);
	//
	// // 保存任务描述
	// List<I18NText> descriptions = task.getDescriptions();
	// if (descriptions != null && descriptions.size() > 0) {
	// String taskComment = descriptions.get(0).getText();
	// data.put(UserTask.F_DESCRIPTION, taskComment);
	// }
	//
	// // 保存任务的实际执行人id
	// org.jbpm.task.User actualOwner = taskData.getActualOwner();
	// String actorId = actualOwner.getId();
	// data.put(UserTask.F_ACTUALOWNER, actorId);
	//
	// // 保存任务的创建者
	// org.jbpm.task.User createdBy = taskData.getCreatedBy();
	// data.put(UserTask.F_CREATEDBY, createdBy.getId());
	//
	// // 任务的创建时间
	// Date createdOn = taskData.getCreatedOn();
	// data.put(UserTask.F_CREATEDON, createdOn);
	//
	// // 任务的流程定义id
	// String processId = taskData.getProcessId();
	// data.put(UserTask.F_PROCESSID, processId);
	//
	// // 任务的流程实例id
	// long processInstanceId = taskData.getProcessInstanceId();
	// data.put(UserTask.F_PROCESSINSTANCEID, new Long(processInstanceId));
	//
	// // 任务状态
	// data.put(UserTask.F_STATUS, status.name());
	//
	// // 任务的workitem ID
	// long workItemId = taskData.getWorkItemId();
	// data.put(UserTask.F_WORKITEMID, new Long(workItemId));
	//
	// WriteResult ws = col.insert(data);
	// checkWriteResult(ws);
	// /**
	// * 以下代码已不适用于保存任务和任务历史，故删除
	// */
	// // // 发送任务消息，并保存
	// // BackgroundContext context = new BackgroundContext();
	// // Message message = doNoticeWorkflow(actorId, taskName, key, context);
	// // Assert.isNotNull(message, "消息发送失败");
	// // data.put(IProcessControl.F_WF_TASK_NOTICEDATE,
	// // message.getValue(Message.F_SENDDATE));
	//
	// /*
	// * 由于PrimaryObject对象可能在不同的用户进程中存在多个副本，副本之间数据并不能维护一致。
	// *
	// * 因此，调用doSave()方法进行保存，将覆盖其他用户的数据。 使用
	// * doSave()方法进行保存适用于后保存的对象覆盖先保存的记录的场景。
	// *
	// *
	// * 但对于更新工作流信息而言不适用。因此，必须使用col.updata的方式进行更新，
	// *
	// * 并且将更新后的结果回写到_data
	// *
	// * 以下的代码展示如何直接调用col的更新方法
	// *
	// * 1. 使用 findAndModify获得更新后的对象值
	// * 注意：直接调用col.update是不能获得更新后数据的，进而PrimaryObject ._data的数据就无法同步更新
	// *
	// * 2. 了解findAndModify完整方法的各项参数
	// */
	//
	// // // 流程的字段名称
	// // String field = key + IProcessControl.POSTFIX_TASK;
	// //
	// // // 获得本模型对应的集合
	// // DBCollection col = getCollection();
	// //
	// // DBObject nd = col
	// // .findAndModify(
	// // // 查询条件，与update方法的query一致
	// // new BasicDBObject().append(F__ID, get_id()),
	// // // 返回字段，与find方法的返回字段一致
	// // new BasicDBObject().append(field, 1),
	// // // 排序，与find方法的排序一致
	// // null,
	// // // 删除，如果为真，满足查询条件的将被删除
	// // false,
	// // // 更新条件，与update的更新一致
	// // new BasicDBObject().append("$set", new BasicDBObject()
	// // .append(field + "." + userid, data)),
	// // // 是否返回更新后的对象
	// // true,
	// // // 如果文档不存在是否插入
	// // false);
	// //
	// // // 新的对象不能为空
	// // Assert.isNotNull(nd);
	// //
	// // // 使用获得的新对象刷新_data的值
	// // Object value = nd.get(field);
	// //
	// // setValue(field, value);
	//
	// return true;
	// }

	public UserTask doSaveUserTask(String flowKey, Task task,
			Map<String, Object> taskMetaData, String userid) throws Exception {

		TaskData taskData = task.getTaskData();
		Status status = taskData.getStatus();

		DBCollection col = getCollection(IModelConstants.C_USERTASK);

		BasicDBObject query = new BasicDBObject();
		query.put(UserTask.F_WORK_ID, get_id());
		query.put(UserTask.F_USERID, userid);
		query.put(UserTask.F_TASKID, task.getId());
		query.put(UserTask.F_STATUS, status.name());
		query.put(UserTask.F_PROCESSKEY, flowKey);

		DBObject data = col.findOne(query);

		UserTask userTask;

		if (data != null) {
			userTask = ModelService.createModelObject(data, UserTask.class);
		} else {
			/*
			 * 构造需要保存的任务数据
			 */
			userTask = ModelService.createModelObject(UserTask.class);

			// 保存任务id
			userTask.setValue(UserTask.F_WORK_ID, get_id());

			userTask.setValue(UserTask.F_WORK_DESC, getDesc());

			// 任务状态
			userTask.setValue(UserTask.F_STATUS, status.name());

			userTask.setValue(UserTask.F_USERID, userid);

			userTask.setValue(UserTask.F_TASKID, task.getId());

			userTask.setValue(UserTask.F_PROCESSKEY, flowKey);

			// 保存任务名称
			List<I18NText> names = task.getNames();
			Assert.isLegal(names != null && names.size() > 0,
					Messages.get().Work_173);
			String taskName = names.get(0).getText();
			userTask.setValue(UserTask.F_DESC, taskName);
			userTask.setValue(UserTask.F_TASK_NAME, taskName);// 兼容history

			// 保存任务描述
			List<I18NText> descriptions = task.getDescriptions();
			if (descriptions != null && descriptions.size() > 0) {
				String taskComment = descriptions.get(0).getText();
				userTask.setValue(UserTask.F_DESCRIPTION, taskComment);
			}

			// 保存任务的实际执行人id
			org.jbpm.task.User actualOwner = taskData.getActualOwner();
			String actorId = actualOwner.getId();
			userTask.setValue(UserTask.F_ACTUALOWNER, actorId);

			// 保存任务的创建者
			org.jbpm.task.User createdBy = taskData.getCreatedBy();
			userTask.setValue(UserTask.F_CREATEDBY, createdBy.getId());

			// 任务的创建时间
			Date createdOn = taskData.getCreatedOn();
			userTask.setValue(UserTask.F_CREATEDON, createdOn);

			// 任务的流程定义id
			String processId = taskData.getProcessId();
			userTask.setValue(UserTask.F_PROCESSID, processId);

			// 流程名称
			DroolsProcessDefinition pd = new DroolsProcessDefinition(processId);
			String processName = pd.getProcess().getName();
			userTask.setValue(UserTask.F_PROCESSNAME, processName);

			// 任务的流程实例id
			long processInstanceId = taskData.getProcessInstanceId();
			userTask.setValue(UserTask.F_PROCESSINSTANCEID, new Long(
					processInstanceId));

			// 任务的workitem ID
			long workItemId = taskData.getWorkItemId();
			userTask.setValue(UserTask.F_WORKITEMID, new Long(workItemId));

			// 该任务未完成
			userTask.setValue(UserTask.F_LIFECYCLE_CHANGE_FLAG, Boolean.FALSE);

		}

		// 保存表单数据
		// 将taskformData补充到当前的任务数据中
		if (taskMetaData != null && !taskMetaData.isEmpty()) {
			Iterator<String> iterator = taskMetaData.keySet().iterator();
			while (iterator.hasNext()) {
				String next = iterator.next();
				String field = "form_" + next; //$NON-NLS-1$
				userTask.setValue(field, taskMetaData.get(next));
			}
		}

		userTask.doSave(new BackgroundContext());
		return userTask;
	}

	public boolean isProjectWBSRoot() {
		return Boolean.TRUE.equals(getValue(F_IS_PROJECT_WBSROOT));
	}

	public void doStartTask(String processKey, UserTask userTask,
			IContext context) throws Exception {
		String lc = getLifecycleStatus();
		Assert.isTrue(ILifecycle.STATUS_WIP_VALUE.equals(lc),
				Messages.get().Work_175);

		// Task task = userTask.getTask();
		// Assert.isNotNull(task, "无法获得当前的流程任务");

		String taskstatus = userTask.getStatus();
		boolean canStartTask = WorkflowService.canStartTask(taskstatus);
		Assert.isTrue(canStartTask, Messages.get().Work_176);

		Long taskId = userTask.getTaskId();
		String userId = context.getAccountInfo().getConsignerId();
		Task task = WorkflowService.getDefault().startTask(userId, taskId);

		Assert.isNotNull(task, Messages.get().Work_177);

		// 提取当前的任务数据
		Map<String, Object> taskMetaData = new HashMap<String, Object>();
		taskMetaData.put(IProcessControl.F_WF_TASK_ACTOR, context
				.getAccountInfo().getUserId());
		taskMetaData.put(IProcessControl.F_WF_TASK_STARTDATE, new Date());
		taskMetaData.put(IProcessControl.F_WF_TASK_ACTION,
				IProcessControl.TASK_ACTION_START);

		UserTask newUserTask = doSaveUserTask(processKey, task, taskMetaData,
				userId);
		doSaveWorkflowHistroy(processKey, newUserTask, taskMetaData, context);

		/**
		 * 移到了UserTask的保存事件
		 */
		// 发送任务消息，并保存

		// List<I18NText> names = task.getNames();
		// String taskName = "";
		// if (names != null && names.size() > 0) {
		// taskName = names.get(0).getText();
		// }
		// doNoticeWorkflow(userId, taskName, processKey, "已启动", context);
		// data.put(IProcessControl.F_WF_TASK_NOTICEDATE,
		// message.getValue(Message.F_SENDDATE));
	}

	/**
	 * 完成processKey指定流程的当前任务
	 * 
	 * @param processKey
	 *            ，流程key 目前只有{@link IWorkCloneFields#F_WF_EXECUTE}
	 *            {@link IWorkCloneFields#F_WF_CHANGE}<br/>
	 *            可以支持绑定更多的流程定义
	 * @param executeTask
	 * @param taskMetaData
	 * @param context
	 * @throws Exception
	 */
	public void doCompleteTask(String processKey, UserTask executeTask,
			Map<String, Object> inputParameter,
			Map<String, Object> taskMetaData, IContext context)
			throws Exception {
		String lc = getLifecycleStatus();
		Assert.isTrue(ILifecycle.STATUS_WIP_VALUE.equals(lc),
				Messages.get().Work_178);

		// Task task = getTask(processKey, context);
		// Assert.isNotNull(task, "无法获得当前的流程任务");

		String taskstatus = executeTask.getStatus();
		boolean canStartTask = WorkflowService.canFinishTask(taskstatus);
		Assert.isTrue(canStartTask, Messages.get().Work_179);

		Long taskId = executeTask.getTaskId();
		String userId = context.getAccountInfo().getConsignerId();
		Task task = WorkflowService.getDefault().completeTask(taskId, userId,
				inputParameter);

		Assert.isNotNull(task, Messages.get().Work_180);

		taskMetaData.put(IProcessControl.F_WF_TASK_ACTOR, context
				.getAccountInfo().getUserId());
		taskMetaData.put(IProcessControl.F_WF_TASK_FINISHDATE, new Date());
		taskMetaData.put(IProcessControl.F_WF_TASK_ACTION,
				IProcessControl.TASK_ACTION_COMPLETE);

		UserTask newUserTask = doSaveUserTask(processKey, task, taskMetaData,
				userId);

		// 将工作的流程记录存储到交付物文档中
		doSaveProcessHistoryToDocument(context);

		doSaveWorkflowHistroy(processKey, newUserTask, taskMetaData, context);

		/**
		 * 移到了UserTask的保存事件
		 */
		// // 发送任务消息，并保存
		//
		// List<I18NText> names = task.getNames();
		// String taskName = "";
		// if (names != null && names.size() > 0) {
		// taskName = names.get(0).getText();
		// }
		// doNoticeWorkflow(userId, taskName, processKey, "已完成", context);
	}

	public List<UserTask> getReservedUserTasks(String userId) {
		return getUserTasks(userId, Status.Reserved.name());
	}

	public long countReservedUserTasks(String userId) {
		return countUserTasks(userId, Status.Reserved.name());
	}

	public List<UserTask> getInprogressUserTasks(String userId) {
		return getUserTasks(userId, Status.InProgress.name());
	}

	public long countInprogressUserTasks(String userId) {
		return countUserTasks(userId, Status.InProgress.name());
	}

	public long countReservedAndInprogressUserTasks(String userId) {
		DBCollection col = getCollection(IModelConstants.C_USERTASK);
		DBObject query = new BasicDBObject();
		query.put(UserTask.F_WORK_ID, get_id());
		query.put(UserTask.F_USERID, userId);
		query.put(UserTask.F_LIFECYCLE_CHANGE_FLAG, Boolean.FALSE);
		query.put(
				"$or", //$NON-NLS-1$
				new BasicDBObject[] {
						new BasicDBObject().append(UserTask.F_STATUS,
								Status.Reserved.name()),
						new BasicDBObject().append(UserTask.F_STATUS,
								Status.InProgress.name()) });
		return col.count(query);
	}

	public List<UserTask> getAllUserTasks(String status) {
		DBCollection col = getCollection(IModelConstants.C_USERTASK);
		DBObject query = new BasicDBObject();
		query.put(UserTask.F_WORK_ID, get_id());
		query.put(UserTask.F_LIFECYCLE_CHANGE_FLAG, Boolean.FALSE);
		query.put(UserTask.F_STATUS, status);
		DBCursor cur = col.find(query);
		List<UserTask> result = new ArrayList<UserTask>();
		while (cur.hasNext()) {
			DBObject data = cur.next();
			result.add(ModelService.createModelObject(data, UserTask.class));
		}

		return result;
	}

	public List<UserTask> getUserTasks(String userId, String status) {
		DBCollection col = getCollection(IModelConstants.C_USERTASK);
		DBObject query = new BasicDBObject();
		query.put(UserTask.F_WORK_ID, get_id());
		query.put(UserTask.F_USERID, userId);
		query.put(UserTask.F_LIFECYCLE_CHANGE_FLAG, Boolean.FALSE);
		query.put(UserTask.F_STATUS, status);
		DBCursor cur = col.find(query);
		List<UserTask> result = new ArrayList<UserTask>();
		while (cur.hasNext()) {
			DBObject data = cur.next();
			result.add(ModelService.createModelObject(data, UserTask.class));
		}

		return result;
	}

	public long countUserTasks(String userId, String status) {
		DBCollection col = getCollection(IModelConstants.C_USERTASK);
		DBObject query = new BasicDBObject();
		query.put(UserTask.F_WORK_ID, get_id());
		query.put(UserTask.F_USERID, userId);
		query.put(UserTask.F_LIFECYCLE_CHANGE_FLAG, Boolean.FALSE);
		query.put(UserTask.F_STATUS, status);
		return col.count(query);
	}

	/**
	 * 获取最近的显示的任务
	 * 
	 * @return
	 */
	public UserTask getLastDisplayTask(String userId) {
		DBCollection col = getCollection(IModelConstants.C_USERTASK);
		DBObject query = new BasicDBObject();
		query.put(UserTask.F_WORK_ID, get_id());
		DBCursor cur = col.find(query).sort(
				new BasicDBObject().append(UserTask.F__ID, -1));
		UserTask otherUserReservedTask = null;
		// 如果有用户Id的任务，显示该用户Id的任务
		UserTask userReservedTask = null;
		UserTask laskTask = null;
		while (cur.hasNext()) {

			UserTask ut = ModelService.createModelObject(cur.next(),
					UserTask.class);

			if (ut.isReserved()) {
				if (ut.getUserId().equals(userId)) {
					if (userReservedTask == null) {
						userReservedTask = ut;
					}
				} else {
					if (otherUserReservedTask == null) {
						otherUserReservedTask = ut;
					}
				}
			} else {
				if (laskTask == null) {
					laskTask = ut;
				}
			}

			// 有用户预留的任务优先返回
			if (userReservedTask != null) {
				return userReservedTask;
			}

			if (otherUserReservedTask != null) {
				return otherUserReservedTask;
			}

			if (laskTask != null) {
				return laskTask;
			}

		}

		return null;
	}

	// /**
	// * replaced by getReservedUserTasks()
	// * @param processKey
	// * @param context
	// * @return
	// * @throws Exception
	// */
	// @Deprecated
	// public Task getTask(String processKey, IContext context) throws Exception
	// {
	// String userid = context.getAccountInfo().getConsignerId();
	//
	// DBCollection col = getCollection(IModelConstants.C_USERTASK);
	// DBObject query = new BasicDBObject();
	// query.put(UserTask.F_PROCESSKEY, processKey);
	// query.put(UserTask.F_USERID, userid);
	// query.put(UserTask.F_WORK_ID, get_id());
	//
	// DBObject fields = new BasicDBObject();
	// fields.put(UserTask.F_TASKID, 1);
	// DBCursor cur = col.find(query, fields);
	//
	// while (cur.hasNext()) {
	// Long taskId = (Long) cur.next().get(UserTask.F_TASKID);
	//
	// return WorkflowService.getDefault().getUserTask(userid, taskId);
	// }
	//
	// // if (data != null) {
	// // Long taskId = (Long) data.get(IProcessControl.F_WF_TASK_ID);
	// // Assert.isNotNull(taskId);
	// // Task task = WorkflowService.getDefault()
	// // .getUserTask(userid, taskId);
	// // return task;
	// // }
	// return null;
	// }
	//
	// @Deprecated
	// public Task getExecuteTask(IContext context) throws Exception {
	// return getTask(Work.F_WF_EXECUTE, context);
	// }

	/**
	 * 保存流程的历史记录,本方法已被取代，即将删除
	 * 
	 * @param key
	 * @param newUserTask
	 * @param taskFormData
	 * @param context
	 * @throws Exception
	 */
	@Deprecated
	private void doSaveWorkflowHistroy(String key, UserTask newUserTask,
			Map<String, Object> taskFormData, IContext context)
			throws Exception {
		DBObject currentData = newUserTask.get_data();

		// 将taskformData补充到当前的任务数据中
		if (taskFormData != null && !taskFormData.isEmpty()) {
			Iterator<String> iterator = taskFormData.keySet().iterator();
			while (iterator.hasNext()) {
				String next = iterator.next();
				String field = "form_" + next; //$NON-NLS-1$
				currentData.put(field, taskFormData.get(next));
			}
		}

		// 将当前的任务数据append到历史数组中
		String histroyField = key + IProcessControl.POSTFIX_HISTORY;

		DBCollection col = getCollection();
		WriteResult wr = col.update(
				new BasicDBObject().append(F__ID, get_id()),
				new BasicDBObject().append("$push", //$NON-NLS-1$
						new BasicDBObject().append(histroyField, currentData)));

		checkWriteResult(wr);

	}

	public void mark(String userId, boolean marked) throws Exception {
		DBCollection col = getCollection();
		WriteResult ws = col.update(
				new BasicDBObject().append(F__ID, get_id()),
				new BasicDBObject().append("$set", new BasicDBObject().append( //$NON-NLS-1$
						F_MARK + "." + userId, marked))); //$NON-NLS-1$
		checkWriteResult(ws);
		DBObject data = (DBObject) getValue(F_MARK);
		if (data == null) {
			data = new BasicDBObject();
			setValue(F_MARK, data);
		}
		data.put(userId, marked);
	}

	public boolean isMarked(String userId) {
		DBObject data = (DBObject) getValue(F_MARK);
		return data != null && Boolean.TRUE.equals(data.get(userId));
	}

	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.equals(IProcessControl.class)) {
			return (T) new ProcessControl(this) {
				@Override
				protected Class<? extends PrimaryObject> getRoleDefinitionClass() {
					if (isProjectWork()) {
						return ProjectRole.class;
					} else {
						// 由项目计划构造的提交工作是独立工作，但是使用了项目的角色
						if (forceUseProjectRole()) {
							return ProjectRole.class;
						} else {
							return RoleDefinition.class;
						}
					}
				}
			};
		} else if (adapter == DummyWork.class) {
			DummyWork dummyWork = ModelService
					.createModelObject(DummyWork.class);
			dummyWork.setSource(this);
			return (T) dummyWork;
		} else if (adapter == CommonHTMLLabel.class) {
			return (T) (new WorkCommonHTMLLable(this));
		} else if (adapter == IEditorInputFactory.class) {
			if (isProjectWBSRoot()) {
				Project project = getProject();
				setValue(F_CHARGER, project.getChargerId());
				setValue(F_PARTICIPATE, project.getParticipatesIdList());
			}
			return (T) (new WorkEditorInputFactory(this));
		} else if (adapter == IRoleParameter.class) {
			return (T) (new WorkRoleParameter(this));
		}
		return super.getAdapter(adapter);
	}

	protected boolean forceUseProjectRole() {
		return Boolean.TRUE.equals(getValue(F_USE_PROJECT_ROLE));
	}

	@Deprecated
	public WorkRecord makeWorkRecord() {
		DBObject data = new BasicDBObject();
		data.put(WorkRecord.F_WORK_ID, get_id());
		WorkRecord po = ModelService.createModelObject(data, WorkRecord.class);
		return po;
	}

	public List<WorkRecord> getWorkRecord() {
		Object record = getValue(F_RECORD, true);
		List<WorkRecord> result = new ArrayList<WorkRecord>();
		if (record instanceof List<?>) {
			for (int i = 0; i < ((List<?>) record).size(); i++) {
				Object data = ((List<?>) record).get(i);
				if (data instanceof DBObject) {
					result.add(0, ModelService.createModelObject(
							(DBObject) data, WorkRecord.class));
				}
			}
		}
		return result;
	}

	public boolean isExecuteWorkflowActivateAndAvailable() {
		IProcessControl ip = getAdapter(IProcessControl.class);
		return ip.isWorkflowActivateAndAvailable(F_WF_EXECUTE);
	}

	/**
	 * 修改工作负责人、指派者、参与者和工作流程执行者
	 * 
	 * @param fromUserId
	 *            : 需要修改的人员
	 * @param toUserId
	 *            : 修改成该人员
	 */
	public String changeWorkUser(String fromUserId, String toUserId) {
		if (canChangeWorkUser(fromUserId, toUserId)) {
			String changeFiled = ""; //$NON-NLS-1$
			BasicDBObject object = new BasicDBObject();
			// 修改负责人
			if (fromUserId.equals(getChargerId())) {
				object.put(F_CHARGER, toUserId);
				changeFiled = changeFiled + Messages.get().Work_187;
			}
			// 修改指派者
			if (fromUserId.equals(getAssignerId())) {
				object.put(F_ASSIGNER, toUserId);
				if (changeFiled != "") { //$NON-NLS-1$
					changeFiled = changeFiled + "、"; //$NON-NLS-1$
				}
				changeFiled = changeFiled + Messages.get().Work_190;
			}
			// 修改参与者
			List<?> oldParticipatesIdList = getParticipatesIdList();
			BasicBSONList newParticipatesIdList = new BasicDBList();
			if (oldParticipatesIdList != null) {
				boolean bchange = false;
				for (int i = 0; i < oldParticipatesIdList.size(); i++) {
					String userId = (String) oldParticipatesIdList.get(i);
					if (userId.equals(fromUserId)) {
						bchange = true;
						newParticipatesIdList.add(toUserId);
					}
					newParticipatesIdList.add(userId);
				}
				if (bchange) {
					object.put(F_PARTICIPATE, newParticipatesIdList);
					if (changeFiled != "") { //$NON-NLS-1$
						changeFiled = changeFiled + "、"; //$NON-NLS-1$
					}
					changeFiled = changeFiled + Messages.get().Work_193;
				}
			}

			// 工作流程执行人
			// 执行工作流程
			if (changeWorkFlowActors(fromUserId, toUserId, F_WF_EXECUTE, object)) {
				if (changeFiled != "") { //$NON-NLS-1$
					changeFiled = changeFiled + "、"; //$NON-NLS-1$
				}
				changeFiled = changeFiled + Messages.get().Work_196;
			}

			// 变更工作流程
			if (changeWorkFlowActors(fromUserId, toUserId, F_WF_CHANGE, object)) {
				if (changeFiled != "") { //$NON-NLS-1$
					changeFiled = changeFiled + "、"; //$NON-NLS-1$
				}
				changeFiled = changeFiled + Messages.get().Work_199;
			}

			if (object.size() > 0) {
				DBCollection userCol = DBActivator.getCollection(
						IModelConstants.DB, IModelConstants.C_WORK);
				userCol.update(new BasicDBObject().append(F__ID, get_id()),
						new BasicDBObject().append("$set", object), false, true); //$NON-NLS-1$
			}
			if (changeFiled != "") { //$NON-NLS-1$
				return "\"" + getDesc() + Messages.get().Work_203 + changeFiled; //$NON-NLS-1$
			} else {
				return null;
			}
		}
		return null;
	}

	private boolean canChangeWorkUser(String changedUserId, String changeUserId) {
		return true;
	}

	/**
	 * 检查能否修改该工作
	 * 
	 * @param changedUserId
	 * @param changeUserId
	 * @return
	 */
	public List<Object[]> checkChangeWorkUser(String changedUserId,
			String changeUserId) {
		List<Object[]> message = new ArrayList<Object[]>();
		String lifecycleStatus = getLifecycleStatus();

		if (ILifecycle.STATUS_CANCELED_VALUE.equals(lifecycleStatus)) {
			message.add(new Object[] { Messages.get().Work_204, this,
					SWT.ICON_ERROR });
		} else if (ILifecycle.STATUS_FINIHED_VALUE.equals(lifecycleStatus)) {
			message.add(new Object[] { Messages.get().Work_205, this,
					SWT.ICON_ERROR });
		} else if (ILifecycle.STATUS_WIP_VALUE.equals(getLifecycleStatus())) {
			message.add(new Object[] { Messages.get().Work_206, this,
					SWT.ICON_WARNING });
		} else if (ILifecycle.STATUS_PAUSED_VALUE.equals(getLifecycleStatus())) {
			message.add(new Object[] { Messages.get().Work_207, this,
					SWT.ICON_WARNING });
		}

		return message;
	}

	private boolean changeWorkFlowActors(String changedUserId,
			String changeUserId, String process, BasicDBObject object) {
		IProcessControl ip = getAdapter(IProcessControl.class);
		boolean hasChange = false;
		DBObject actorsData = ip.getProcessActorsData(process);
		if (actorsData != null) {
			Iterator<String> iter = actorsData.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				String userid = (String) actorsData.get(key);
				if (changedUserId.equals(userid)) {
					object.put(process + IProcessControl.POSTFIX_ACTORS + "." //$NON-NLS-1$
							+ key, changedUserId);
					hasChange = true;
				}
			}
		}
		return hasChange;
	}

	public int getRemindBefore() {
		Object value = getValue(F_REMIND_BEFORE);
		if (value instanceof Integer) {
			return ((Integer) value).intValue();
		} else {
			Object data = Setting
					.getSystemSetting(IModelConstants.S_S_WORK_REMIND_BEFORE);
			try {
				return Integer.parseInt((String) data);
			} catch (Exception e) {
				return 0;
			}
		}
	}

	public boolean isRemindNow() {
		int remindBefore = getRemindBefore();
		if (remindBefore > 0) {
			Date now = new Date();
			Date _planFinish = getPlanFinish();
			return _planFinish != null
					&& remindBefore > 0
					&& (_planFinish.getTime() - now.getTime()) < remindBefore * 3600000;
		}
		return false;
	}

	public boolean isDelayFinish() {
		Date _planFinish = getPlanFinish();
		Date _actualFinish = getActualFinish();

		if (_planFinish == null) {
			return false;
		}
		if (_actualFinish != null) {
			return _actualFinish.after(_planFinish);
		} else {
			return new Date().after(_planFinish);
		}
	}

	public boolean isDelayFinishEst() {
		Date _planFinish = getPlanFinish();

		if (_planFinish == null) {
			return false;
		}
		if (!isDelayFinish()) {
			int remindBefore = getRemindBefore();
			long timeMillis = new Date().getTime();
			long planFinishTime = _planFinish.getTime();
			return planFinishTime - timeMillis <= remindBefore * 60 * 60 * 1000;
		}
		return true;
	}

	public boolean isDelayStart() {
		Date _planStart = getPlanStart();
		Date _actualStart = getActualStart();

		if (_planStart == null) {
			return false;
		}
		if (_actualStart != null) {
			return _actualStart.after(_planStart);
		} else {
			return new Date().after(_planStart);
		}
	}

	public boolean isAdvanceFinish() {
		Date _planFinish = getPlanFinish();
		Date _actualFinish = getActualFinish();

		if (_planFinish == null) {
			return false;
		}

		if (_actualFinish != null) {
			return _actualFinish.before(_planFinish);
		} else {
			return false;
		}
	}

	public boolean isAdvanceStart() {
		Date _planStart = getPlanStart();
		Date _actualStart = getActualStart();

		if (_planStart == null) {
			return false;
		}

		if (_actualStart != null) {
			return _actualStart.before(_planStart);
		} else {
			return false;
		}
	}

	public boolean isStandloneWork() {
		Object type = getValue(F_WORK_TYPE);
		return type instanceof Integer
				&& ((Integer) type).intValue() == WORK_TYPE_STANDLONE;
	}

	// @Override
	// public boolean isSummaryWork() {
	// Object value = getValue(F_PERFORMENCE_ISSUMMARY);
	// if(value == null){
	// return super.isSummaryWork();
	// }else{
	// return Boolean.TRUE.equals(value);
	// }
	// }

	public boolean isProjectWork() {
		return !isStandloneWork();
	}

	private void copyWorkDefinitionForStandloneWork(IContext context)
			throws Exception {
		WorkDefinition wd = getWorkDefinition();
		if (wd == null) {
			return;
		}
		if (!isStandloneWork()) {
			return;
		}
		Map<ObjectId, DBObject> rolemap = copyRoleDefinition(context);

		IProcessControl ipc = getAdapter(IProcessControl.class);

		// 处理角色定义
		DBObject radata = ipc.getProcessRoleAssignmentData(F_WF_EXECUTE);
		Iterator<String> iterator;
		if (radata != null) {
			iterator = radata.keySet().iterator();
			while (iterator.hasNext()) {
				String parameter = iterator.next();
				ObjectId value = (ObjectId) radata.get(parameter);
				DBObject newRoleDef = rolemap.get(value);
				if (newRoleDef != null) {
					RoleDefinition rd = ModelService.createModelObject(
							newRoleDef, RoleDefinition.class);
					ipc.setProcessActionAssignment(F_WF_EXECUTE, parameter, rd);
				}
			}
		}

		/*
		 * 设置与工时管理有关的值，包括计量方式、标准工时、工时类型和统计点
		 */
		// 工时类型
		setValue(F_WORKTIME_PARAX, wd.getValue(F_WORKTIME_PARAX));

		// 计量方式
		setValue(F_MEASUREMENT, wd.getValue(F_MEASUREMENT));

		// 标准工时
		setValue(F_STANDARD_WORKS, wd.getValue(F_STANDARD_WORKS));

		// 设置计划工时
		setValue(F_PLAN_WORKS, wd.getValue(F_STANDARD_WORKS));

		// 设置统计阶段
		setValue(F_STATISTICS_STEP, wd.getValue(F_STATISTICS_STEP));

		// 统计点
		setValue(F_STATISTICS_POINT, wd.getValue(F_STATISTICS_POINT));

		// 加入项目计算工时
		setValue(F_JOIN_PROJECT_CALCWORKS,
				wd.getValue(F_JOIN_PROJECT_CALCWORKS));

		// 处理用户设置
		DBObject acdata = ipc.getProcessActorsData(F_WF_EXECUTE);
		if (acdata == null) {
			radata = ipc.getProcessRoleAssignmentData(F_WF_EXECUTE);
			if (radata != null) {

				iterator = radata.keySet().iterator();
				while (iterator.hasNext()) {
					String parameter = iterator.next();
					ObjectId value = (ObjectId) radata.get(parameter);
					RoleDefinition rd = ModelService.createModelObject(
							RoleDefinition.class, value);
					Role orole = rd.getOrganizationRole();
					IRoleParameter roleParameter = getAdapter(IRoleParameter.class);
					List<PrimaryObject> roleAss = orole
							.getAssignment(roleParameter);
					if (!roleAss.isEmpty()) {
						if (roleAss.size() > 1) {
							break;
						} else {
							ipc.setProcessActionActor(F_WF_EXECUTE, parameter,
									((AbstractRoleAssignment) roleAss.get(0))
											.getUserid());
						}
					}
				}
			}
		}

	}

	private DBObject copyDocumentFromTemplate(
			Map<ObjectId, DBObject> documentsToInsert,
			List<DBObject[]> fileToCopy, ObjectId documentTemplateId) {
		DBObject documentData;
		DBCollection docdCol = getCollection(IModelConstants.C_DOCUMENT_DEFINITION);
		DBObject documentTemplate = docdCol.findOne(new BasicDBObject().append(
				Document.F__ID, documentTemplateId));
		documentData = new BasicDBObject();

		documentData.put(Document.F__ID, new ObjectId());

		documentData.put(Document.F_WORK_ID, get_id());

		Object value = documentTemplate.get(DocumentDefinition.F_DESC);
		if (value != null) {
			documentData.put(Document.F_DESC, value);
		}

		value = documentTemplate.get(DocumentDefinition.F_DESC_EN);
		if (value != null) {
			documentData.put(Document.F_DESC_EN, value);
		}

		value = new Boolean(Boolean.TRUE.equals(documentTemplate
				.get(DocumentDefinition.F_ATTACHMENT_CANNOT_EMPTY)));
		documentData.put(Document.F_ATTACHMENT_CANNOT_EMPTY, value);

		value = documentTemplate.get(DocumentDefinition.F_DESCRIPTION);
		if (value != null) {
			documentData.put(Document.F_DESCRIPTION, value);
		}

		value = documentTemplate.get(DocumentDefinition.F_DOCUMENT_EDITORID);
		if (value != null) {
			documentData.put(Document.F__EDITOR, value);
		}

		// 根据文档的附件创建文件
		BasicBSONList templateFiles = (BasicBSONList) documentTemplate
				.get(DocumentDefinition.F_TEMPLATEFILE);
		if (templateFiles != null) {
			BasicBSONList documentFiles = new BasicBSONList();
			for (int i = 0; i < templateFiles.size(); i++) {
				DBObject templateFile = (DBObject) templateFiles.get(i);
				DBObject documentFile = new BasicDBObject();
				documentFile.put(RemoteFile.F_ID, new ObjectId());
				documentFile.put(RemoteFile.F_FILENAME,
						templateFile.get(RemoteFile.F_FILENAME));
				documentFile.put(RemoteFile.F_NAMESPACE,
						Document.FILE_NAMESPACE);
				documentFile.put(RemoteFile.F_DB, Document.FILE_DB);
				documentFiles.add(documentFile);
				fileToCopy.add(new DBObject[] { templateFile, documentFile });
			}
			documentData.put(Document.F_VAULT, documentFiles);
		}
		// 完成文档创建
		documentsToInsert.put(documentTemplateId, documentData);

		return documentData;
	}

	/**
	 * 通过通用工作定义创建新工作
	 * 
	 * @param workd
	 * @param context
	 * @throws Exception
	 */
	public void doCreateChildFromGenericWorkDefinition(WorkDefinition workdef,
			IContext context) throws Exception {
		// 1.处理workd
		ObjectId tgtParentId = get_id();

		ObjectId tgtRootId = getRoot().get_id();

		Project project = getProject();

		HashMap<ObjectId, DBObject> worksToBeInsert = new HashMap<ObjectId, DBObject>();

		HashMap<ObjectId, DBObject> documentsToInsert = new HashMap<ObjectId, DBObject>();

		List<DBObject> deliverableToInsert = new ArrayList<DBObject>();

		List<DBObject[]> fileToCopy = new ArrayList<DBObject[]>();

		Map<ObjectId, DBObject> roleMap = new HashMap<ObjectId, DBObject>();

		int seq = getMaxChildSeq();

		ObjectId folderRootId = project.getFolderRootId();

		ObjectId srcParent = workdef.get_id();

		DBObject targetParentWorkData = ProjectToolkit
				.getWorkFromWorkDefinition(tgtParentId, tgtRootId, project,
						roleMap, folderRootId, documentsToInsert,
						deliverableToInsert, fileToCopy, context,
						project.get_id(), new Integer(seq + 1),
						workdef.get_data(), null);
		worksToBeInsert.put(srcParent, targetParentWorkData);
		tgtParentId = (ObjectId) targetParentWorkData.get(F__ID);

		ProjectToolkit.copyWBSTemplate(srcParent, tgtParentId, tgtRootId,
				project, roleMap, worksToBeInsert, folderRootId,
				documentsToInsert, deliverableToInsert, fileToCopy, context);

		// 保存工作
		DBCollection workCol = getCollection(IModelConstants.C_WORK);
		Collection<DBObject> collection = worksToBeInsert.values();
		WriteResult ws;
		if (!collection.isEmpty()) {
			ws = workCol.insert(collection.toArray(new DBObject[0]),
					WriteConcern.NORMAL);
			checkWriteResult(ws);
		}

		// 保存文档
		DBCollection docCol = getCollection(IModelConstants.C_DOCUMENT);
		ws = docCol.remove(new BasicDBObject().append(Document.F_PROJECT_ID,
				get_id()));
		checkWriteResult(ws);

		collection = documentsToInsert.values();
		if (!collection.isEmpty()) {
			ws = docCol.insert(collection.toArray(new DBObject[0]),
					WriteConcern.NORMAL);
			checkWriteResult(ws);
		}

		// 保存交付物
		DBCollection deliCol = getCollection(IModelConstants.C_DELIEVERABLE);
		ws = deliCol.remove(new BasicDBObject().append(
				Deliverable.F_PROJECT_ID, get_id()));
		checkWriteResult(ws);

		if (!deliverableToInsert.isEmpty()) {
			ws = deliCol.insert(deliverableToInsert, WriteConcern.NORMAL);
			checkWriteResult(ws);
		}

		// 保存文件
		for (DBObject[] dbObjects : fileToCopy) {
			DBObject src = dbObjects[0];
			DBObject tgt = dbObjects[1];

			String srcDB = (String) src.get(RemoteFile.F_DB);
			String srcFilename = (String) src.get(RemoteFile.F_FILENAME);
			String srcNamespace = (String) src.get(RemoteFile.F_NAMESPACE);
			ObjectId srcID = (ObjectId) src.get(RemoteFile.F_ID);

			String tgtDB = (String) tgt.get(RemoteFile.F_DB);
			String tgtFilename = (String) tgt.get(RemoteFile.F_FILENAME);
			String tgtNamespace = (String) tgt.get(RemoteFile.F_NAMESPACE);
			ObjectId tgtID = (ObjectId) tgt.get(RemoteFile.F_ID);

			FileUtil.copyGridFSFile(srcID, srcDB, srcFilename, srcNamespace,
					tgtID, tgtDB, tgtFilename, tgtNamespace);
		}
	}

	/**
	 * 获得超量分配的倍数（按计划工时）
	 * 
	 * @return
	 * @throws Exception
	 */
	public double getOverloadCount() throws Exception {
		if (overCount != null) {
			return overCount;
		}

		if (!isProjectWork()) {
			throw new Exception(Messages.get().Work_209);
		}
		BasicBSONList idlist = getParticipatesIdList();
		if (idlist == null || idlist.size() < 1) {
			throw new Exception(Messages.get().Work_210);
		}
		Double planWorks = getPlanWorks();
		if (planWorks == null) {
			throw new Exception(Messages.get().Work_211);
		}

		// 获取计划工作天数
		Date planStart = getPlanStart();
		Date planFinih = getPlanFinish();

		CalendarCaculater cc = getProject().getCalendarCaculater();
		double hours = cc.getWorkingHours(planStart, planFinih);
		// 按参与者数量X工作时间可用的获得满额工时
		double totalWorkHourAvailable = hours * idlist.size();
		if (totalWorkHourAvailable == 0) {
			throw new Exception(Messages.get().Work_212);
		}
		overCount = planWorks / totalWorkHourAvailable;
		return overCount;
	}

	// /**
	// * 获得某个参与者在某天的实际工时
	// *
	// * @param userid
	// * @param date
	// * @return
	// */
	// public double getParticipatesActualWorks(String userid, Date date) {
	//
	// // 首先读取performence记录
	// BasicBSONList performence = getPerformence();
	// if (performence != null) {// 根据绩效记录获取
	// for (int i = 0; i < performence.size(); i++) {
	// DBObject item = (DBObject) performence.get(i);
	// String _userid = (String) item.get(F_SF_PERFORMENCE_USERID);
	// if (userid.equals(_userid)) {
	// Date _date = (Date) item.get(F_SF_PERFORMENCE_DATE);
	// // 按天比较，相同
	// if (date.getTime() / (24 * 60 * 60 * 1000) == _date
	// .getTime() / (24 * 60 * 60 * 1000)) {
	// Double value = (Double) item
	// .get(F_SF_PERFORMENCE_ACTUALWORKS);
	// if (value != null) {
	// return value.doubleValue();
	// } else {
	// return 0d;
	// }
	// }
	// }
	// }
	// return 0d;
	// } else {
	// // 没有绩效记录的，如果工作已经完成，按照计划工时，根据参与者数量和工期进行分摊
	// String ls = getLifecycleStatus();
	// if (STATUS_FINIHED_VALUE.equals(ls)) {
	// Double works = getActualWorks();
	// if (works == null) {
	// works = getPlanWorks();
	// }
	// if (works == null) {
	// return 0d;
	// }
	//
	// //当天是否在实际开始和时间完成之间
	// Date as = getActualStart();
	// Date af = getActualFinish();
	// if(date.getTime()<as.getTime()||date.getTime()>af.getTime()){
	// return 0d;
	// }
	//
	// // 取当天是否为工作日
	// Double workingTimeOfDateUseCache = getWorkingTimeOfDateUseCache(date);
	// if (workingTimeOfDateUseCache==0d) {
	// return 0d;
	// }
	//
	// BasicBSONList ids = getParticipatesIdList();
	// if (ids != null) {
	// return works / ids.size();
	// } else {
	// return 0d;
	// }
	// } else {
	// return 0d;
	// }
	// }
	// }

	// private Double getWorkingTimeOfDateUseCache(Date date) {
	// long key = date.getTime() / (24 * 60 * 60 * 1000);
	// if (workingDateUseCache == null) {
	// workingDateUseCache = new HashMap<Long, Double>();
	// }
	// Double workingTime = workingDateUseCache.get(key);
	// if (workingTime == null) {
	// Project project = getProject();
	// CalendarCaculater cc;
	// if (project != null) {
	// cc = project.getCalendarCaculater();
	// } else {
	// List<PrimaryObject> conditions = new SystemCalendar()
	// .getDataSet().getDataItems();
	// cc = new CalendarCaculater(conditions);
	// }
	// workingTime = cc.getWorkingTime(date);
	// workingDateUseCache.put(key, workingTime);
	// }
	// return workingTime;
	// }
	//
	//
	// private DBObject calculateWorksAllocateTable() {
	// BasicBSONList performence = getPerformence();
	// if(performence!=null){
	// return calculateWorksAllocateTableFromPerformenceRecord();
	// }
	//
	//
	// //从实际开始日期开始计算
	// Date start = getActualStart();
	// Date finish = getActualFinish();
	//
	//
	//
	// return null;
	// }

	public DBObject getPerformence() {
		return (DBObject) getValue(F_PERFORMENCE);
	}

	public double getParticipatesActualWorks(String userid, Date date) {
		String key = "" + date.getTime() / (24 * 60 * 60 * 1000); //$NON-NLS-1$

		DBCollection col = getCollection(IModelConstants.C_WORKS_PERFORMENCE);
		col.find(new BasicDBObject()
				.append(WorksPerformence.F_WORKID, get_id()));

		DBObject performence = getPerformence();
		if (performence != null) {
			DBObject userPerformence = (DBObject) performence.get(userid);
			if (userPerformence != null) {
				Double value = (Double) userPerformence.get("" + key); //$NON-NLS-1$
				if (value != null) {
					return value.doubleValue();
				}
			}
		}
		return 0;
	}

	/**
	 * 设置某个参与者在某天的本工作的工时
	 * 
	 * @param userid
	 * @param date
	 * @param works
	 */
	public void doSetParticipatesActualWorks(String userid, Date date,
			double works) {
		// long datefield = date.getTime() / (24 * 60 * 60 * 1000);
		// String key = F_PERFORMENCE + "." + userid + "." + datefield;
		// value = new BasicDBObject().append(key, val)
		// getCollection().update(new BasicDBObject().append(F__ID, get_id()),
		// o);
	}

	public WorksPerformence getWorksPerformence(Date date, String userid) {
		Long dateCode = new Long(date.getTime() / (24 * 60 * 60 * 1000));

		DBCollection col = getCollection(IModelConstants.C_WORKS_PERFORMENCE);
		DBObject data = col.findOne(new BasicDBObject()
				.append(WorksPerformence.F_WORKID, get_id())
				.append(WorksPerformence.F_USERID, userid)
				.append(WorksPerformence.F_DATECODE, dateCode));
		if (data != null) {
			return ModelService.createModelObject(data, WorksPerformence.class);
		}
		return null;
	}

	public boolean hasManualRecordPerformence() {
		DBCollection col = getCollection(IModelConstants.C_WORKS_PERFORMENCE);
		long count = col.count(new BasicDBObject().append(
				WorksPerformence.F_WORKID, get_id()));
		return count > 0;
	}

	public WorksPerformence makeTodayWorksPerformence(String userid) {
		Long dateCode = new Long(new Date().getTime() / (24 * 60 * 60 * 1000));
		return makeWorksPerformence(userid, dateCode);
	}

	public WorksPerformence makeWorksPerformence(String userid, Long dateCode) {
		DBObject data = new BasicDBObject();
		WorksPerformence po = ModelService.createModelObject(data,
				WorksPerformence.class);
		po.setValue(WorksPerformence.F_WORKID, get_id());
		po.setValue(WorksPerformence.F_USERID, userid);
		po.setValue(WorksPerformence.F_COMMITDATE, new Date());
		po.setValue(WorksPerformence.F_DATECODE, dateCode);

		Project project = getProject();
		if (project != null) {
			po.setValue(WorksPerformence.F_PROJECTDESC, project.getLabel());
			po.setValue(WorksPerformence.F_PROJECT_ID, project.get_id());
		}

		po.setValue(WorksPerformence.F_WORKDESC, getLabel());
		po.setValue(WorksPerformence.F_PLANWORKS, getPlanWorks());

		return po;
	}

	public void doAddParticipateList(List<?> userList) throws Exception {
		if (userList == null) {
			throw new Exception(Messages.get().Work_215);
		}
		DBCollection workCol = getCollection();

		DBObject update = new BasicDBObject().append("$addToSet", //$NON-NLS-1$
				new BasicDBObject().append(Work.F_PARTICIPATE,
						new BasicDBObject().append("$each", //$NON-NLS-1$
								userList.toArray(new String[0]))));

		WriteResult ws = workCol.update(
				new BasicDBObject().append(Work.F__ID, get_id()), update,
				false, false);
		checkWriteResult(ws);
	}

	public void doChangeDeliverableLifeCycleStatus(IContext context,
			String operation) {
		try {
			List<PrimaryObject> documents = getOutputDeliverableDocuments();
			for (PrimaryObject po : documents) {
				Document document = (Document) po;
				document.doSetLifeCycleStatus(context, operation);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<PrimaryObject> getOutputDeliverableDocuments() {
		List<PrimaryObject> result = new ArrayList<PrimaryObject>();
		List<PrimaryObject> d = getOutputDeliverable();
		for (int i = 0; i < d.size(); i++) {
			Deliverable ditem = (Deliverable) d.get(i);
			Document doc = ditem.getDocument();
			if (doc != null) {
				result.add(doc);
			}
		}
		return result;
	}

	public List<ObjectId> getOutputDeliverableDocumentIds() {
		List<ObjectId> result = new ArrayList<ObjectId>();
		List<PrimaryObject> d = getOutputDeliverable();
		for (int i = 0; i < d.size(); i++) {
			Deliverable ditem = (Deliverable) d.get(i);
			Document doc = ditem.getDocument();
			if (doc != null) {
				result.add(doc.get_id());
			}
		}
		return result;
	}

	public List<PrimaryObject> getOutputDeliverable() {
		BasicDBObject condition = new BasicDBObject();
		condition.put(Deliverable.F_WORK_ID, get_id());
		condition.put(
				"$or", //$NON-NLS-1$
				new BasicDBObject[] {
						new BasicDBObject().append(Deliverable.F_TYPE,
								Deliverable.TYPE_OUTPUT),
						new BasicDBObject().append(Deliverable.F_TYPE, null) });
		return getRelationByCondition(Deliverable.class, condition);
	}

	public void doSetDocumentLock(IContext context, boolean locked) {
		try {
			List<PrimaryObject> documents = getOutputDeliverableDocuments();
			for (PrimaryObject po : documents) {
				Document document = (Document) po;
				if (locked) {
					document.doLock(context);
				} else {
					document.doUnLock(context);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getEditorId() {
		String editorId;
		IProcessControl ipc = (IProcessControl) getAdapter(IProcessControl.class);
		if (isSummaryWork()) {
			editorId = "edit.work.plan.2"; //$NON-NLS-1$
		} else {
			if (isStandloneWork()) {
				if (ipc.isWorkflowActivateAndAvailable(Work.F_WF_EXECUTE)) {
					editorId = "edit.work.plan.4"; //$NON-NLS-1$
				} else {
					editorId = "edit.work.plan.3"; //$NON-NLS-1$
				}
			} else {
				if (ipc.isWorkflowActivateAndAvailable(Work.F_WF_EXECUTE)) {
					editorId = "edit.work.plan.1"; //$NON-NLS-1$
				} else {
					editorId = "edit.work.plan.0"; //$NON-NLS-1$
				}
			}
		}

		return editorId;
	}

	public Deliverable getDeliverable(Document document) {
		List<PrimaryObject> deliverableList = getDeliverable();
		for (int i = 0; i < deliverableList.size(); i++) {
			Deliverable deliverable = (Deliverable) deliverableList.get(i);
			Document doc = deliverable.getDocument();
			if (document.get_id().equals(doc.get_id())) {
				return deliverable;
			}
		}
		return null;
	}

	public String getMeasurementLabel() {
		String measurement = getMeasurement();
		if (measurement != null) {
			if (MEASUREMENT_TYPE_NO_ID.equals(measurement)) {
				return MEASUREMENT_TYPE_NO_VALUE;
			} else if (MEASUREMENT_TYPE_COMMIT_ID.equals(measurement)) {
				return MEASUREMENT_TYPE_COMMIT_VALUE;
			} else if (MEASUREMENT_TYPE_STANDARD_ID.equals(measurement)) {
				return MEASUREMENT_TYPE_STANDARD_VALUE;
			}
		}
		return "";
	}

	public String getMeasurement() {
		return getStringValue(F_MEASUREMENT);
	}

	public void doAssignment(IContext context) throws Exception {
		ObjectId assRoledId = (ObjectId) getValue(Work.F_ASSIGNMENT_CHARGER_ROLE_ID);
		List<PrimaryObject> childrenWork = getChildrenWork();
		for (PrimaryObject po : childrenWork) {
			Work work = (Work) po;
			ObjectId assChildreRoledId = (ObjectId) work
					.getValue(Work.F_ASSIGNMENT_CHARGER_ROLE_ID);
			if (assRoledId.equals(assChildreRoledId)) {
				work.setValue(F_CHARGER, getValue(F_CHARGER));
				work.setValue(F_ASSIGNER, getValue(F_ASSIGNER));
				work.setValue(F_PARTICIPATE, getValue(F_PARTICIPATE));
				work.doSave(context);
			}
			if (work.isSummaryWork()) {
				work.doAssignment(context);
			}
		}
	}

	/**
	 * 独立工作的工时验证
	 * 
	 * @throws Exception
	 */
	public void workTimeValidateOfStandloneWork() throws Exception {
		// 区分独立工作的计量方式
		if (MEASUREMENT_TYPE_STANDARD_ID.equals(getMeasurement())) {
			// 判断独立工作是否加入项目计算工时
			if (Boolean.TRUE.equals(getValue(F_JOIN_PROJECT_CALCWORKS))) {
				// 独立工作的是否有工时方案、工时参数
				// 独立工作是否有统计阶段
				Object step = getValue(F_STATISTICS_STEP);
				if (step instanceof List<?>) {
					if (((List<?>)step).isEmpty()) {
						throw new Exception(
								Messages.get().WorkTimeValidateOfStatistics);
					}
				}else{
					throw new Exception(
							Messages.get().WorkTimeValidateOfStatistics);
				}
				DBCollection programCol = getCollection(IModelConstants.C_WORKTIMEPROGRAM);

				Object parax = getValue(F_WORKTIME_PARAX);
				if (parax instanceof List<?>) {
					if (((List<?>) parax).isEmpty()) {
						throw new Exception(
								Messages.get().WorkTimeValidateOfWorksParaX);
					}
				} else {
					throw new Exception(
							Messages.get().WorkTimeValidateOfWorksParaX);
				}

				WorkDefinition workd = getWorkDefinition();
				BasicBSONList programIds = (BasicBSONList) workd
						.getValue(WorkDefinition.F_WORKTIMEPROGRAMS);

				Iterator<?> iterator = ((List<?>) parax).iterator();
				while (iterator.hasNext()) {
					DBObject paraX = (DBObject) iterator.next();
					// 工作上工时参数对应的工时方案的id
					ObjectId programId = (ObjectId) paraX
							.get(F_WORKTIME_PARAX_PROGRAM_ID);
					if (programIds == null) {
						long count = programCol.count(new BasicDBObject()
								.append(WorkTimeProgram.F__ID, programId));
						if (count == 0) {
							throw new Exception(
									Messages.get().WorkTimeValidateOfProgramInWorkDefinition);
						}
					} else if (!programIds.contains(programId)) {
						throw new Exception(
								Messages.get().WorkTimeValidateOfProgramInProjectTemplate);
					}
					ObjectId paraxId = (ObjectId) paraX
							.get(F_WORKTIME_PARAX_ID);
					WorkTimeProgram program = ModelService.createModelObject(
							WorkTimeProgram.class, programId);
					BasicBSONList types = (BasicBSONList) program
							.getValue(WorkTimeProgram.F_WORKTIME_PARA_X);
					boolean contains = false;
					for (int i = 0; i < types.size(); i++) {
						DBObject type = (DBObject) types.get(i);
						ObjectId typeId = (ObjectId) type.get(F__ID);
						if (typeId.equals(paraxId)) {
							contains = true;
							break;
						}
					}
					if (!contains) {
						throw new Exception(
								Messages.get().WorkTimeValidateOfProgramInWorkDefinition);
					}
				}

			}
		}
	}

	/**
	 * 项目工作的工时验证
	 * 
	 * @param project
	 *            是项目,可以传空
	 * @throws Exception
	 */
	public void workTimeValidateOfProjectWork(Project project) throws Exception {
		if (project == null) {
			project = getProject();
		}
		// 如果不是标准工时工作,无需进行检查
		if (!MEASUREMENT_TYPE_STANDARD_ID.equals(getMeasurement())) {
			return;
		}
		ObjectId projectWorksProgramId = (ObjectId) project
				.getValue(Project.F_WORKTIMEPROGRAM_ID);
		// 1. 必须要有统计阶段
		Object step = getValue(F_STATISTICS_STEP);
		if (step == null) {
			throw new Exception(Messages.get().WorkTimeValidateOfStatistics);
		}
		// 需要检查工时统计阶段是否包含在项目的工时统计阶段中
		BasicBSONList steps = (BasicBSONList) project
				.getValue(Project.F_STATISTICSS_STEP);
		if (!steps.contains(step)) {
			throw new Exception(Messages.get().WorkTimeValidateOfStatistics);
		}

		// 2. 检查工时方案和工时参数

		Object parax = getValue(F_WORKTIME_PARAX);
		if (parax instanceof List<?>) {
			if (((List<?>) parax).isEmpty()) {
				throw new Exception(Messages.get().WorkTimeValidateOfParaInWork);
			}
		} else {
			throw new Exception(Messages.get().WorkTimeValidateOfParaInWork);
		}

		boolean containsProgram = false;

		Iterator<?> iterator = ((List<?>) parax).iterator();
		while (iterator.hasNext()) {
			DBObject paraX = (DBObject) iterator.next();
			ObjectId programId = (ObjectId) paraX
					.get(F_WORKTIME_PARAX_PROGRAM_ID);
			if (projectWorksProgramId.equals(programId)) {
				ObjectId paraxId = (ObjectId) paraX.get(F_WORKTIME_PARAX_ID);
				WorkTimeProgram program = ModelService.createModelObject(
						WorkTimeProgram.class, programId);
				BasicBSONList types = (BasicBSONList) program
						.getValue(WorkTimeProgram.F_WORKTIME_PARA_X);
				boolean contains = false;
				for (int i = 0; i < types.size(); i++) {
					DBObject type = (DBObject) types.get(i);
					ObjectId typeId = (ObjectId) type.get(F__ID);
					if (typeId.equals(paraxId)) {
						contains = true;
						break;
					}
				}
				if (!contains) {
					throw new Exception(
							Messages.get().WorkTimeValidateOfParaOptionInProgram);
				}
				containsProgram = true;
				break;
			}
		}
		if (!containsProgram) {
			throw new Exception(Messages.get().WorkTimeValidateOfParaInProject);
		}
	}

}
