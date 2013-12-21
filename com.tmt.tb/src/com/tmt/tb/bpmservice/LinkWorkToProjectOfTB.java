package com.tmt.tb.bpmservice;

import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;

import com.mobnut.db.model.ModelService;
import com.mobnut.db.model.PrimaryObject;
import com.mongodb.DBObject;
import com.sg.bpm.service.task.ServiceProvider;
import com.sg.bpm.workflow.utils.WorkflowUtils;
import com.sg.business.model.Project;
import com.sg.business.model.Work;
import com.sg.business.model.bpmservice.BPMServiceContext;
import com.sg.business.model.toolkit.ProjectToolkit;
import com.tmt.tb.nls.Messages;

public class LinkWorkToProjectOfTB extends ServiceProvider {

	@Override
	public Map<String, Object> run(Object parameter) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		Object content = getInputValue("content"); //$NON-NLS-1$
		String _id = (String) getInputValue("project_id"); //$NON-NLS-1$
		if (_id == null) {
			result.put("returnCode", "ERROR"); //$NON-NLS-1$ //$NON-NLS-2$
			result.put("returnMessage", Messages.get().LinkWorkToProjectOfTB_5); //$NON-NLS-1$
		} else {
			if (content instanceof String) {
				String jsonContent = (String) content;
				try {
					ObjectId project_id = new ObjectId(_id);
					DBObject processData = WorkflowUtils
							.getProcessInfoFromJSON(jsonContent);
					String processId = (String) processData.get("processId"); //$NON-NLS-1$
					String processName = (String) processData
							.get("processName"); //$NON-NLS-1$

					PrimaryObject host = WorkflowUtils
							.getHostFromJSON(jsonContent);
					if (host instanceof Work) {
						ObjectId workid = host.get_id();
						Project project = ModelService.createModelObject(
								Project.class, project_id);
						Work work = ModelService.createModelObject(Work.class,
								workid);
						ProjectToolkit.doProjectAddStandloneWork(work, project,
								new BPMServiceContext(processName, processId));

					} else {
						result.put("returnCode", "ERROR"); //$NON-NLS-1$ //$NON-NLS-2$
						result.put("returnMessage", Messages.get().LinkWorkToProjectOfTB_11); //$NON-NLS-1$
					}
				} catch (Exception e) {
					result.put("returnCode", "ERROR"); //$NON-NLS-1$ //$NON-NLS-2$
					result.put("returnMessage", e.getMessage()); //$NON-NLS-1$
				}
			}
		}

		return result;
	}

}
