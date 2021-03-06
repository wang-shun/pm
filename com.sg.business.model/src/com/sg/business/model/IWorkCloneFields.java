package com.sg.business.model;

import com.sg.business.resource.nls.Messages;

public interface IWorkCloneFields {

	/**
	 * 工作定义的负责角色定义，{@link RoleDefinition},保存了角色定义的Id
	 */
	public static final String F_CHARGER_ROLE_ID = "charger_roled_id"; //$NON-NLS-1$

	/**
	 * 指派者角色定义
	 */
	public static final String F_ASSIGNMENT_CHARGER_ROLE_ID = "assignmentcharger_roled_id"; //$NON-NLS-1$
	/**
	 * 参与者角色定义
	 */
	public static final String F_PARTICIPATE_ROLE_SET = "participate_roled_set"; //$NON-NLS-1$

	/**
	 * 工作定义的同层序号
	 */
	public static final String F_SEQ = "seq"; //$NON-NLS-1$

	/**
	 * 是否是里程碑任务
	 */
	public static final String F_MILESTONE = "milestone"; //$NON-NLS-1$

	/**
	 * 工作流定义,DBObject 类型<br/>
	 * { "kbase" : "com.tmt", "processId" : "com.tmt.ProjectChange",
	 * "processName" : "项目变更过程", "processNamespace" : "com.tmt", "type" :
	 * "RuleFlow", "version" : null }
	 */
	public static final String F_WF_EXECUTE = "wf_execute"; //$NON-NLS-1$

	/**
	 * 工作流定义,DBObject 类型 <br/>
	 * { "kbase" : "com.tmt", "processId" : "com.tmt.ProjectChange",
	 * "processName" : "项目变更过程", "processNamespace" : "com.tmt", "type" :
	 * "RuleFlow", "version" : null }
	 */
	public static final String F_WF_CHANGE = "wf_change"; //$NON-NLS-1$

	/**
	 * 变更工作流是否激活
	 */
	public static final String F_WF_CHANGE_ACTIVATED = "wf_change_activated"; //$NON-NLS-1$

	/**
	 * 执行工作流是否激活
	 */
	public static final String F_WF_EXECUTE_ACTIVATED = "wf_execute_activated"; //$NON-NLS-1$

	/**
	 * 标准工时
	 */
	public static final String F_STANDARD_WORKS = "standardworks"; //$NON-NLS-1$

	/**
	 * 变更流程活动执行人的角色定义
	 */
	public static final String F_WF_CHANGE_ASSIGNMENT = "wf_change_assignment"; //$NON-NLS-1$

	/**
	 * 执行流程的执行人角色定义
	 */
	public static final String F_WF_EXECUTE_ASSIGNMENT = "wf_execute_assignment"; //$NON-NLS-1$

	/**
	 * 是否允许分解工作"
	 */
	public static final String F_SETTING_CAN_BREAKDOWN = "s_canbreakdown"; //$NON-NLS-1$

	/**
	 * 是否允许添加交付物"
	 */
	public static final String F_SETTING_CAN_ADD_DELIVERABLES = "s_canadddeliverables"; //$NON-NLS-1$

	/**
	 * 是否允许修改计划工时"
	 */
	public static final String F_SETTING_CAN_MODIFY_PLANWORKS = "s_canmodifyplanworks"; //$NON-NLS-1$

	/**
	 * 上级工作开始时，本工作自动开始
	 * 
	 */
	public static final String F_SETTING_AUTOSTART_WHEN_PARENT_START = "s_autostartwithparent"; //$NON-NLS-1$

	/**
	 * 上级工作完成时，本工作自动完成"
	 */
	public static final String F_SETTING_AUTOFINISH_WHEN_PARENT_FINISH = "s_autofinishwithparent"; //$NON-NLS-1$

	/**
	 * 是否可以跳过进行中的流程完成工作"
	 */
	public static final String F_SETTING_CAN_SKIP_WORKFLOW_TO_FINISH = "s_canskiptofinish"; //$NON-NLS-1$

	/**
	 * 需启动变更流程实施工作的更改"
	 */
	public static final String F_SETTING_WORKCHANGE_MANDORY = "s_workchangeflowmandory"; //$NON-NLS-1$

	/**
	 * 需启动项目变更流程实施工作的更改
	 */
	public static final String F_SETTING_PROJECTCHANGE_MANDORY = "s_projectchangeflowmandory"; //$NON-NLS-1$

	/**
	 * 提前提醒，提前多少小时
	 */
	public static final String F_REMIND_BEFORE = "remindbefore"; //$NON-NLS-1$

	/**
	 * 分类
	 */
	public static final String F_WORK_CATAGORY = "catagory"; //$NON-NLS-1$

	/**
	 * 计量方式
	 */
	public static final String F_MEASUREMENT = "measurement";//$NON-NLS-1$

	public static final String MEASUREMENT_TYPE_NO_ID = "no";//$NON-NLS-1$

	public static final String MEASUREMENT_TYPE_NO_VALUE = Messages.get().WorkDefinitionNoMeasurement;

	public static final String MEASUREMENT_TYPE_COMMIT_ID = "commit";//$NON-NLS-1$

	public static final String MEASUREMENT_TYPE_COMMIT_VALUE = Messages.get().WorkDefinitionCommitMeasurement;

	public static final String MEASUREMENT_TYPE_STANDARD_ID = "standard";//$NON-NLS-1$

	public static final String MEASUREMENT_TYPE_STANDARD_VALUE = Messages.get().WorkDefinitionStandardMeasurement;

	/**
	 * 工时类型,用于工作和工作定义上，是BasicBSONList类型
	 */
	public static final String F_WORKTIME_PARAX = "worktimepara_x";
	
	/**
	 * 工时
	 */
	public static final String F_WORK_TIME_DATA="worktimedata";
	

	/**
	 *  由工作定义复制过来的工时统计点
	 */
	public static final String F_STATISTICS_POINT = "statisticspoint";

	/**
	 * 由工作定义复制过来的统计阶段
	 */
	public static final String F_STATISTICS_STEP = "statisticsstep";

	/**
	 * 工时方案id，用于工作和工作定义上的,是F_WORKTIME_PARA_X的子字段
	 */
	public static final String F_WORKTIME_PARAX_PROGRAM_ID="program_id";
	
	/**
	 * 工时类型id，用于工作和工作定义上的,是F_WORKTIME_PARA_X的子字段
	 */
	public static final String F_WORKTIME_PARAX_ID="para_id";
	
	/**
	 * 加入项目计算工时，用于独立工作统计工时
	 */
	public static final String F_JOIN_PROJECT_CALCWORKS="joinprojectcalcworks";

	/**
	 * 7.3工时方案，保存的是由工时方案的_id组成的数组，用于独立工作定义
	 */
	public static final String F_WORKTIMEPROGRAMS = "worktimeprograms";
	
	
	/**
	 * 需要复制的设置项
	 */
	public static final String[] SETTING_FIELDS = new String[] {
			F_SETTING_AUTOFINISH_WHEN_PARENT_FINISH,
			F_SETTING_AUTOSTART_WHEN_PARENT_START,
			F_SETTING_CAN_ADD_DELIVERABLES, F_SETTING_CAN_BREAKDOWN,
			F_SETTING_CAN_MODIFY_PLANWORKS,
			F_SETTING_CAN_SKIP_WORKFLOW_TO_FINISH,
			F_SETTING_PROJECTCHANGE_MANDORY, F_SETTING_WORKCHANGE_MANDORY,
			F_REMIND_BEFORE, F_WORK_CATAGORY };

}
