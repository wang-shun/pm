package com.tmt.tb.field.parameter;

import org.bson.types.ObjectId;

import com.mobnut.db.model.ModelService;
import com.mobnut.db.model.PrimaryObject;
import com.sg.business.commons.field.processparameter.AbstractRoleParameterDelegator;
import com.sg.business.model.Organization;
import com.sg.business.model.Project;
import com.sg.business.taskforms.IRoleConstance;

public class DeptChangeReviewer extends AbstractRoleParameterDelegator {

	@Override
	protected Organization getOrganization(String processParameter,
			String taskDatakey, PrimaryObject taskFormData) {
		Object value = taskFormData.getValue(taskDatakey);
		if (value instanceof ObjectId) {
			Project project = ModelService.createModelObject(Project.class,
					(ObjectId) value);
			return project.getFunctionOrganization();
		}
		return null;
	}

	@Override
	protected int getSelectType() {
		return Organization.ROLE_SEARCH_UP;
	}

	@Override
	protected String getRoldNumber() {
		return IRoleConstance.ROLE_CHANGE_APPROVER_ID;
	}

}
