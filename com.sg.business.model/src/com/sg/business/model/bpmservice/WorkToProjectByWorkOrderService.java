package com.sg.business.model.bpmservice;

import java.util.HashMap;
import java.util.Map;

import com.mobnut.db.DBActivator;
import com.mobnut.db.model.ModelService;
import com.mobnut.db.model.PrimaryObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sg.bpm.service.task.ServiceProvider;
import com.sg.bpm.workflow.utils.WorkflowUtils;
import com.sg.business.model.IModelConstants;
import com.sg.business.model.Project;
import com.sg.business.model.Work;
import com.sg.business.model.toolkit.ProjectToolkit;
import com.sg.business.resource.nls.Messages;

public class WorkToProjectByWorkOrderService extends ServiceProvider {

	public WorkToProjectByWorkOrderService() {
	}

	@Override
	public Map<String, Object> run(Object parameter) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		Object content = getInputValue("content"); //$NON-NLS-1$
		if (content instanceof String) {
			String jsonContent = (String) content;
			try {
				PrimaryObject host = WorkflowUtils.getHostFromJSON(jsonContent);
				DBObject processData = WorkflowUtils
						.getProcessInfoFromJSON(jsonContent);
				String processId = (String) processData.get("processId"); //$NON-NLS-1$
				String processName = (String) processData.get("processName"); //$NON-NLS-1$
				if (host instanceof Work) {
					Work work = (Work) host;
					// 根据工作令号获取项目
					Project project = work.getProject();
					if(project!=null){
						return result;
					}
					String workorder = (String) getInputValue("workorder"); //$NON-NLS-1$
					DBCollection col = DBActivator.getCollection(
							IModelConstants.DB, IModelConstants.C_PROJECT);
					DBObject dbo = col.findOne(new BasicDBObject().append(
							Project.F_WORK_ORDER, workorder));
					if (dbo != null) {
						project = ModelService.createModelObject(dbo,
								Project.class);
					}
					// 附加工作至项目中
					if (project != null) {
						ProjectToolkit.doProjectAddStandloneWork(work,project,(new BPMServiceContext(processName,processId)));
//						ObjectId project_id = project.get_id();
//						Folder folderRoot = project.getFolderRoot();
//						Work rootwork = project.getWBSRoot();
//						int maxChildSeq = rootwork.getMaxChildSeq();
//						work.setValue(Work.F_PARENT_ID, rootwork.get_id());
//						work.setValue(Work.F_PROJECT_ID, project_id);
//						work.setValue(Work.F_SEQ, maxChildSeq+1);
//						work.doSave((new BPMServiceContext(processName,
//								processId)));
//
//						// 文档归档
//						List<PrimaryObject> deliverable = work.getDeliverable();
//						for (PrimaryObject del : deliverable) {
//							del.setValue(Deliverable.F_PROJECT_ID, project_id);
//							List<PrimaryObject> deliverableDocuments = work
//									.getDeliverableDocuments();
//							for (PrimaryObject doc : deliverableDocuments) {
//								doc.setValue(Document.F_PROJECT_ID, project_id);
//								doc.setValue(Document.F_FOLDER_ID,
//										folderRoot.get_id());
//								doc.doSave((new BPMServiceContext(processName,
//										processId)));
//							}
//							del.doSave((new BPMServiceContext(processName,
//									processId)));
//
//						}
					}

				} else {
					result.put("returnCode", "ERROR"); //$NON-NLS-1$ //$NON-NLS-2$
					result.put("returnMessage", Messages.get().WorkToProjectByWorkOrderService_0); //$NON-NLS-1$
				}
			} catch (Exception e) {
				result.put("returnCode", "ERROR"); //$NON-NLS-1$ //$NON-NLS-2$
				result.put("returnMessage", e.getMessage()); //$NON-NLS-1$
			}
		}
		return result;
	}

}
