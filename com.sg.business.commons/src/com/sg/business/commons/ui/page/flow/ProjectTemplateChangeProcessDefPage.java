package com.sg.business.commons.ui.page.flow;

import com.sg.business.model.ProjectTemplate;

public class ProjectTemplateChangeProcessDefPage extends AbstractProjectTemplateProcessPage{

	@Override
	protected String getProcessKey() {
		return ProjectTemplate.F_WF_CHANGE;
	}

}
