package com.sg.business.project.editor.page;

import com.sg.business.model.Project;

public class ProjectChangeProcessPage extends AbstractWorkProcessPage {

	@Override
	protected String getWorkflowKey() {
		return Project.F_WF_CHANGE;
	}


}