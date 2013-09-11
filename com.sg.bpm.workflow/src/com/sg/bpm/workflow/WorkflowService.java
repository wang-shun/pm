package com.sg.bpm.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jbpm.task.Status;
import org.jbpm.task.Task;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.TaskClient;
import org.jbpm.task.service.responsehandlers.BlockingGetTaskResponseHandler;
import org.jbpm.task.service.responsehandlers.BlockingTaskOperationResponseHandler;
import org.jbpm.task.service.responsehandlers.BlockingTaskSummaryResponseHandler;
import org.osgi.framework.BundleContext;

import com.sg.bpm.service.BPM;
import com.sg.bpm.service.HTService;
import com.sg.bpm.workflow.model.DroolsProcessDefinition;
import com.sg.bpm.workflow.taskform.TaskFormConfig;

/**
 * The activator class controls the plug-in life cycle
 */
public class WorkflowService extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.sg.bpm.workflow"; //$NON-NLS-1$

	// The shared instance
	private static WorkflowService plugin;

	private Map<String, TaskFormConfig> taskCompleteFormMap = new HashMap<String, TaskFormConfig>();

	private TaskClient backgroundTaskClient;

	/**
	 * The constructor
	 */
	public WorkflowService() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		loadConfig();
		initialBPMAccount();
	}

	private void initialBPMAccount() {
		HTService s = BPM.getHumanTaskService();
		s.addParticipateUser("Administrator");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static WorkflowService getDefault() {
		return plugin;
	}

	private void loadConfig() {
		IExtensionRegistry eReg = Platform.getExtensionRegistry();
		IExtensionPoint ePnt = eReg.getExtensionPoint(PLUGIN_ID, "taskForm");
		if (ePnt == null)
			return;
		IExtension[] exts = ePnt.getExtensions();
		for (int i = 0; i < exts.length; i++) {
			IConfigurationElement[] confs = exts[i].getConfigurationElements();
			for (int j = 0; j < confs.length; j++) {
				if ("taskForm".equals(confs[j].getName())) {
					TaskFormConfig element = new TaskFormConfig(confs[j]);
					taskCompleteFormMap.put(element.getTaskFormId(), element);
				}
			}
		}
	}

	public TaskClient getBackgroundTaskClient() {
		if (backgroundTaskClient == null) {
			backgroundTaskClient = createTaskClient("background");
		}
		return backgroundTaskClient;
	}

	/**
	 * 获取某个流程、任务的完成表单
	 * 
	 * @param processDefinitionId
	 *            ，流程id
	 * @param taskName
	 *            ，任务名称
	 * @return
	 */
	public TaskFormConfig getTaskCompleteFormConfig(String processDefinitionId,
			String taskName) {
		return taskCompleteFormMap.get(processDefinitionId + "@" + taskName);
	}


	/**
	 * 获取某个用户的任务客户端，用于流程查询控制
	 * 
	 * @param userId
	 * @return
	 */
	public TaskClient getTaskClient(String userId) {
		return BPM.getHumanTaskService().getTaskClient(userId);
	}

	/**
	 * 创建一个任务客户端，用于后台对流层的查询、控制
	 * 
	 * @param clientId
	 * @return
	 */
	public TaskClient createTaskClient(String clientId) {
		return BPM.getHumanTaskService().createTaskClient(clientId);
	}

	/**
	 * 获取某个用户id的工作流任务
	 * 
	 * @param userid
	 * @return
	 */
	public Task[] getTask(String userid) {
		TaskClient taskClient = getBackgroundTaskClient();

		BlockingTaskSummaryResponseHandler handler = new BlockingTaskSummaryResponseHandler();
		taskClient.getTasksAssignedAsPotentialOwner(userid, "en-UK", handler);

		List<TaskSummary> tslist = handler.getResults();
		BlockingGetTaskResponseHandler getTaskResponsehandler;
		Task[] result = new Task[tslist.size()];
		for (int i = 0; i < tslist.size(); i++) {
			TaskSummary item = tslist.get(i);
			getTaskResponsehandler = new BlockingGetTaskResponseHandler();

			taskClient.getTask(item.getId(), getTaskResponsehandler);

			Task task = getTaskResponsehandler.getTask(1000);
			result[i] = task;
		}

		return result;
	}

	/**
	 * 根据流程Id和流程实例id获得流程
	 * 
	 * @param processId
	 * @param processInstanceId
	 * @return
	 */
	public WorkflowProcessInstance getProcessInstance(String processId,
			long processInstanceId) throws Exception {
		DroolsProcessDefinition dpd = new DroolsProcessDefinition(processId);
		Assert.isNotNull(dpd, "无法确定流程ID对应的流程定义");
		StatefulKnowledgeSession session = dpd.getKnowledgeSession();
		Assert.isNotNull(session, "无法获得流程定义的知识库进程");
		return (WorkflowProcessInstance) session
				.getProcessInstance(processInstanceId);
	}

	/**
	 * 开始任务
	 * 
	 * @param userId
	 * @param taskId
	 * @return
	 */
	public Task startTask(String userId, long taskId) {
		TaskClient taskClient = getTaskClient(userId);
		BlockingTaskOperationResponseHandler oHandler = new BlockingTaskOperationResponseHandler();

		taskClient.start(taskId, userId, oHandler);
		oHandler.waitTillDone(1000);

		BlockingGetTaskResponseHandler gHandler = new BlockingGetTaskResponseHandler();
		taskClient.getTask(taskId, gHandler);
		Task task = gHandler.getTask(1000);
		return task;
	}

	public static boolean canStartTask(String status) {
		// created, ready,reserved 都可以开始
		if (Status.Created.name().equals(status)) {
			return true;
		}
		if (Status.Ready.name().equals(status)) {
			return true;
		}
		if (Status.Reserved.name().equals(status)) {
			return true;
		}
		return false;
	}

	public static boolean canFinishTask(String status) {
		if (Status.Completed.name().equals(status)) {
			return false;
		}
		if (Status.Error.name().equals(status)) {
			return false;
		}
		if (Status.Failed.name().equals(status)) {
			return false;
		}
		if (Status.Obsolete.name().equals(status)) {
			return false;
		}
		if (Status.Exited.name().equals(status)) {
			return false;
		}
		return false;
	}

}
