package com.sg.business.model;

import java.util.List;

import org.bson.types.BasicBSONList;
import org.bson.types.ObjectId;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Image;

import com.mobnut.db.DBActivator;
import com.mobnut.db.model.IContext;
import com.mobnut.db.model.ModelService;
import com.mobnut.db.model.PrimaryObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.sg.business.resource.BusinessResource;
import com.sg.business.resource.nls.Messages;

/**
 * 项目模板
 * <p>
 * 项目模板由业务管理员创建，用于新建项目
 * 
 * @author jinxitao
 * 
 */
public class ProjectTemplate extends PrimaryObject {

	/**
	 * 所属组织
	 */
	public static final String F_ORGANIZATION_ID = "organization_id"; //$NON-NLS-1$

	/**
	 * 预算定义ID
	 */
	public static final String F_BUDGET_ID = "budget_id"; //$NON-NLS-1$

	/**
	 * 工作定义ID
	 */
	public static final String F_WORK_DEFINITON_ID = "workd_id"; //$NON-NLS-1$

	/**
	 * 标准集，用于选配，确定工作和交付物是否必须
	 */
	public static final String F_STANDARD_OPTION_SET = "standardset"; //$NON-NLS-1$

	/**
	 * 产品类型，用于选配，确定工作和交付物是否必须
	 */
	public static final String F_PRODUCTTYPE_OPTION_SET = "producttype"; //$NON-NLS-1$

	/**
	 * 项目类型，用于选配，确定工作和交付物是否必须
	 */
	public static final String F_PROJECTTYPE_OPTION_SET = "projecttype"; //$NON-NLS-1$

	/**
	 * 提交流程的定义
	 */
	public static final String F_WF_COMMIT = "wf_commit"; //$NON-NLS-1$

	/**
	 * 流程定义中的指派
	 */
	public static final String F_WF_COMMIT_ASSIGNMENT = "wf_commit_assignment"; //$NON-NLS-1$

	/**
	 * 流程是否启用
	 */
	public static final String F_WF_COMMIT_ACTIVATED = "wf_commit_activated"; //$NON-NLS-1$

	public static final String POSTFIX_ACTIVATED = "_activated"; //$NON-NLS-1$

	public static final String POSTFIX_ASSIGNMENT = "_assignment"; //$NON-NLS-1$

	/**
	 * 变更流程定义
	 */
	public static final String F_WF_CHANGE = "wf_change"; //$NON-NLS-1$

	/**
	 * 流程是否启用
	 */
	public static final String F_WF_CHANGE_ACTIVATED = "wf_change_activated"; //$NON-NLS-1$

	/**
	 * 变更流程定义中的指派
	 */
	public static final String F_WF_CHANGE_ASSIGNMENT = "wf_change_assignment"; //$NON-NLS-1$

	public static final String F_ACTIVATED = "activated"; //$NON-NLS-1$

	/**
	 * 工时方案，保存的是由工时方案的_id组成的数组
	 */
	public static final String F_WORKTIMEPROGRAMS = "worktimeprograms";

	/**
	 * 统计阶段，从数据库取出的是BSONList，每个元素是字符串
	 */
	public static final String F_STATISTICSSTEP = "statisticsstep";

	/**
	 * 文件夹模板的id
	 */
	public static final String F_FOLDER_DEFINITION_ID = "folderd_id";

	/**
	 * 返回显示图标
	 * 
	 * @return Image
	 */
	@Override
	public Image getImage() {
		return BusinessResource.getImage(BusinessResource.IMAGE_TEMPLATE_16);
	}

	/**
	 * 插入新模板数据到数据库中
	 */
	@Override
	public void doInsert(IContext context) throws Exception {
		setValue(F__ID, new ObjectId());// 需要预设ID,否则后面的get_id()取出的是空

		if (getValue(F_WORK_DEFINITON_ID) == null) {
			BasicDBObject wbsRootData = new BasicDBObject();
			wbsRootData.put(WorkDefinition.F_WORK_TYPE, new Integer(
					WorkDefinition.WORK_TYPE_PROJECT));
			wbsRootData.put(WorkDefinition.F_DESC, getDesc());
			wbsRootData.put(WorkDefinition.F_PROJECT_TEMPLATE_ID, get_id());
			ObjectId wbsRootId = new ObjectId();
			wbsRootData.put(WorkDefinition.F__ID, wbsRootId);
			wbsRootData.put(WorkDefinition.F_ROOT_ID, wbsRootId);

			WorkDefinition wbsRoot = ModelService.createModelObject(
					wbsRootData, WorkDefinition.class);
			wbsRoot.doInsert(context);

			setValue(ProjectTemplate.F_WORK_DEFINITON_ID, wbsRoot.get_id());
		}
		// 2014.6.17
		// 判断文件夹模板id为空，就新增一条文件夹模板记录
		// *******************************************
		if (getValue(F_FOLDER_DEFINITION_ID) == null) {
			BasicDBObject folderdRootData = new BasicDBObject();
			folderdRootData.put(FolderDefinition.F_DESC, getDesc());
			folderdRootData.put(FolderDefinition.F_PROJECT_TEMPLATE_ID,
					get_id());
			ObjectId folderRootId = new ObjectId();
			folderdRootData.put(FolderDefinition.F__ID, folderRootId);
			folderdRootData.put(FolderDefinition.F_ROOT_ID, folderRootId);
			folderdRootData.put(
					FolderDefinition.F_IS_PROJECT_TEMPLATE_FOLDERROOT,
					Boolean.TRUE);
			FolderDefinition folderdRoot = ModelService.createModelObject(
					folderdRootData, FolderDefinition.class);
			folderdRoot.doInsert(context);
			setValue(F_FOLDER_DEFINITION_ID, folderdRoot.get_id());
		}

		// *******************************************

		if (getValue(F_BUDGET_ID) == null) {
			BudgetItem biRoot = BudgetItem.COPY_DEFAULT_BUDGET_ITEM();
			biRoot.setValue(BudgetItem.F_PROJECTTEMPLATE_ID, get_id());
			biRoot.doInsert(context);

			setValue(ProjectTemplate.F_BUDGET_ID, biRoot.get_id());
		}

		/*
		 * *****************************************************************************
		 * zhonghua 2013/9/16
		 * 
		 * [bug:18] 项目模版 建立项目模版后自动在角色中增加系统角色“项目经理”，“项目观察者”
		 * 
		 * 由于没有自动添加该角色，流程和WBS中无法引用该角色
		 * 
		 * 自动添加该角色后，该角色应当限制删除或者添加人
		 * 
		 * 并且该角色的成员与项目负责人保持同步
		 * 
		 * 同时需要为项目模板的角色添加进行校验，防止输入系统保留的角色ID
		 */
		RoleDefinition projMgr = makeRoleDefinition(null);
		projMgr.setValue(RoleDefinition.F_ROLE_NUMBER,
				RoleDefinition.ROLE_PROJECT_MANAGER_ID);
		projMgr.setValue(RoleDefinition.F_DESC,
				RoleDefinition.ROLE_PROJECT_MANAGER_TEXT);
		projMgr.doInsert(context);

		projMgr = makeRoleDefinition(null);
		projMgr.setValue(RoleDefinition.F_ROLE_NUMBER,
				RoleDefinition.ROLE_PROJECT_GUEST_ID);
		projMgr.setValue(RoleDefinition.F_DESC,
				RoleDefinition.ROLE_PROJECT_GUEST_ID);

		/*
		 * *****************************************************************************
		 */

		super.doInsert(context);
	}

	/**
	 * 2014.6.25 项目模板的工时方案更新时调用
	 */
	@Override
	public void doUpdate(IContext context) throws Exception {

		workTimeValidate();

		super.doUpdate(context);
	}

	/**
	 * 删除模板
	 */
	@Override
	public void doRemove(IContext context) throws Exception {
		if (isActivated()) {
			throw new Exception(Messages.get().ProjectTemplate_0);
		}

		// 删除预算根
		doRemoveBudgetItemInternal();

		// 删除工作定义
		doRemoveWorkDefinitionsInternal();

		// 删除交付物定义
		doRemoveDeliverableDefinitionsInternal();

		// 删除文件夹模板
		doRemoveFolderDefinitionInternal();

		// 删除角色定义
		doRemoveRoleDefinitionInternal();

		super.doRemove(context);
	}

	private void doRemoveFolderDefinitionInternal() {
		DBCollection col = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_FOLDER_DEFINITION);
		col.remove(new BasicDBObject().append(
				DeliverableDefinition.F_PROJECTTEMPLATE_ID, get_id()));
	}

	/**
	 * 该模板是否已经启用
	 */
	public boolean isActivated() {
		IActivateSwitch adapter = getAdapter(IActivateSwitch.class);
		return adapter.isActivated();
	}

	/**
	 * 删除模板中的交付物定义
	 */
	private void doRemoveDeliverableDefinitionsInternal() {
		DBCollection col = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_DELIEVERABLE_DEFINITION);
		col.remove(new BasicDBObject().append(
				DeliverableDefinition.F_PROJECTTEMPLATE_ID, get_id()));
	}

	/**
	 * 删除模板的角色定义
	 */
	private void doRemoveRoleDefinitionInternal() {
		DBCollection col = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_ROLE_DEFINITION);
		col.remove(new BasicDBObject().append(
				DeliverableDefinition.F_PROJECTTEMPLATE_ID, get_id()));
	}

	/**
	 * 删除模板中工作定义
	 */
	private void doRemoveWorkDefinitionsInternal() {
		DBCollection col = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_WORK_DEFINITION);
		col.remove(new BasicDBObject().append(
				WorkDefinition.F_PROJECT_TEMPLATE_ID, get_id()));
	}

	@Override
	public boolean canEdit(IContext context) {
		if (isActivated()) {
			return false;
		}
		return super.canEdit(context);
	}

	/**
	 * 删除模板中的预算定义
	 */
	private void doRemoveBudgetItemInternal() {
		Object bioid = getValue(F_BUDGET_ID);
		DBCollection col = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_BUDGET_ITEM);
		col.remove(new BasicDBObject().append(F__ID, bioid));
	}

	/**
	 * 新建角色定义
	 * 
	 * @param roled
	 *            ,角色定义
	 * @return RoleDefinition
	 */
	public RoleDefinition makeRoleDefinition(RoleDefinition roled) {
		if (roled == null) {
			BasicDBObject data = new BasicDBObject();
			roled = ModelService.createModelObject(data, RoleDefinition.class);
		}
		roled.setValue(RoleDefinition.F_PROJECT_TEMPLATE_ID, get_id());
		return roled;
	}

	/**
	 * 引用组织角色
	 * 
	 * @param role
	 * @return RoleDefinition
	 */
	public RoleDefinition makeOrganizationRole(Role role) {
		RoleDefinition roled = ModelService
				.createModelObject(RoleDefinition.class);
		roled.setValue(RoleDefinition.F_ORGANIZATION_ROLE_ID, role.get_id());
		roled.setValue(RoleDefinition.F_PROJECT_TEMPLATE_ID, get_id());
		return roled;
	}

	/**
	 * 新建前后置关系定义
	 * 
	 * @return WorkDefinitionConnection
	 */
	public WorkDefinitionConnection makeWorkDefinitionConnection() {
		WorkDefinitionConnection wdc = ModelService
				.createModelObject(WorkDefinitionConnection.class);
		wdc.setValue(WorkDefinitionConnection.F_PROJECT_TEMPLATE_ID, get_id());
		return wdc;
	}

	/**
	 * 判断模板中的角色定义是否是从组织角色引用
	 * 
	 * @param role
	 * @return
	 */
	public boolean hasOrganizationRole(Role role) {
		DBCollection col = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_ROLE_DEFINITION);
		long count = col.count(new BasicDBObject().append(
				RoleDefinition.F_ORGANIZATION_ROLE_ID, role.get_id()).append(
				RoleDefinition.F_PROJECT_TEMPLATE_ID, get_id()));
		return count != 0;
	}

	/**
	 * 返回标准集
	 * 
	 * @return List
	 */
	public List<?> getStandardOptionSet() {
		return (List<?>) getValue(F_STANDARD_OPTION_SET);
	}

	/**
	 * 返回产品类型集合
	 * 
	 * @return List
	 */
	public List<?> getProductOptionSet() {
		return (List<?>) getValue(F_PRODUCTTYPE_OPTION_SET);
	}

	/**
	 * 返回项目类型集合
	 * 
	 * @return List
	 */
	public List<?> getProjectOptionSet() {
		return (List<?>) getValue(F_PROJECTTYPE_OPTION_SET);
	}

	/**
	 * 返回模板归属组织
	 * 
	 * @return Organization
	 */
	public Organization getOrganization() {
		ObjectId orgId = (ObjectId) getValue(F_ORGANIZATION_ID);
		Assert.isNotNull(orgId);
		return ModelService.createModelObject(Organization.class, orgId);
	}

	/**
	 * 返回模版中的所有预算定义
	 * 
	 * @return
	 */
	public List<PrimaryObject> getBudgetItems() {
		return getRelationById(F__ID, BudgetItem.F_PROJECTTEMPLATE_ID,
				BudgetItem.class);
	}

	/**
	 * 返回模板中的所有角色定义
	 * 
	 * @return List
	 */
	public List<PrimaryObject> getRoleDefinitions() {
		return getRelationById(F__ID, RoleDefinition.F_PROJECT_TEMPLATE_ID,
				RoleDefinition.class);
	}

	/**
	 * 
	 * 返回模版中的除根工作外的所有工作定义
	 * 
	 * @return
	 */
	public List<PrimaryObject> getWorkDefinitions() {
		BasicDBObject query = new BasicDBObject().append(
				WorkDefinition.F_PROJECT_TEMPLATE_ID, getValue(F__ID)).append(
				WorkDefinition.F_ROOT_ID,
				new BasicDBObject().append("$ne", getWBSRoot().get_id())); //$NON-NLS-1$
		return getRelationByCondition(WorkDefinition.class, query);
	}

	/**
	 * 返回模版中的所有交付物定义
	 * 
	 * @return
	 */
	public List<PrimaryObject> getDeliverableDefinitions() {
		return getRelationById(F__ID,
				DeliverableDefinition.F_PROJECTTEMPLATE_ID,
				DeliverableDefinition.class);
	}

	/**
	 * 返回模版中的所有前后置关系
	 * 
	 * @return
	 */
	public List<PrimaryObject> getWorkConnections() {
		return getRelationById(F__ID, WorkConnection.F_PROJECT_ID,
				WorkConnection.class);
	}

	/**
	 * 返回项目模版的WBS结构根工作定义
	 * 
	 * @return WorkDefinition
	 */
	public WorkDefinition getWBSRoot() {
		ObjectId workDefinitionid = (ObjectId) getValue(F_WORK_DEFINITON_ID);
		return ModelService.createModelObject(WorkDefinition.class,
				workDefinitionid);
	}

	/**
	 * 返回类型名称
	 * 
	 * @return String
	 */
	@Override
	public String getTypeName() {
		return Messages.get().ProjectTemplate_1;
	}

	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.equals(IProcessControl.class)) {
			return (T) new ProcessControl(this) {
				@Override
				protected Class<? extends PrimaryObject> getRoleDefinitionClass() {
					return RoleDefinition.class;
				}
			};
		} else if (adapter.equals(IActivateSwitch.class)) {
			return (T) new ActivateSwitch(this);
		}
		return super.getAdapter(adapter);
	}

	public ProjectRole getProcessActionAssignment(String key,
			String nodeActorParameter) {
		// 取出角色指派
		DBObject data = (DBObject) getValue(key + POSTFIX_ASSIGNMENT);
		if (data == null) {
			return null;
		}
		ObjectId roleId = (ObjectId) data.get(nodeActorParameter);
		if (roleId != null) {
			return ModelService.createModelObject(ProjectRole.class, roleId);
		}
		return null;
	}

	public void workTimeValidate() throws Exception {
		// 1.检查工时方案
		Object programs = getValue(ProjectTemplate.F_WORKTIMEPROGRAMS);
		if (programs == null) {
			return;
		}
		if (programs instanceof List<?>) {
			if (((List<?>) programs).isEmpty()) {
				return;
			}
		}
		// 2.检查统计阶段是否有值,且有工时方案必须要有统计阶段
		Object stats = getValue(ProjectTemplate.F_STATISTICSSTEP);
		if (stats == null) {
			throw new Exception(Messages.get().WorkTimeValidateOfStatistics);
		}
		if (stats instanceof List<?>) {
			if (((List<?>) stats).isEmpty()) {
				throw new Exception(Messages.get().WorkTimeValidateOfStatistics);
			}
		}
		// 检查工时方案是否存在
		workTimeValidateProgrames();

		// 3.工作定义的计量方式是标准工时时,工作工时参数是否定义,并在工时方案中是否还存在
		workTimeValidateWorkDefinition();

	}

	private void workTimeValidateWorkDefinition() throws Exception {
		DBCollection wkCol = getCollection(IModelConstants.C_WORK_DEFINITION);
		DBObject query = new BasicDBObject();
		query.put(WorkDefinition.F_PROJECT_TEMPLATE_ID, get_id());
		query.put(WorkDefinition.F_MEASUREMENT,
				WorkDefinition.MEASUREMENT_TYPE_STANDARD_ID);
		DBCursor cursor = wkCol.find(query);
		while (cursor.hasNext()) {
			DBObject data = cursor.next();
			WorkDefinition workd = ModelService.createModelObject(data,
					WorkDefinition.class);
			workd.workTimeValidate(this);
		}
	}

	private void workTimeValidateProgrames() throws Exception {
		DBCollection wtCol = getCollection(IModelConstants.C_WORKTIMEPROGRAM);
		BasicBSONList progs = (BasicBSONList) getValue(ProjectTemplate.F_WORKTIMEPROGRAMS);
		for (int i = 0; i < progs.size(); i++) {
			ObjectId progId = (ObjectId) progs.get(i);
			long count = wtCol.count(new BasicDBObject().append(
					WorkTimeProgram.F__ID, progId));
			if (count == 0l) {
				throw new Exception(Messages.get().WorkTimeValidateOfProgram);
			}
		}
	}
}
