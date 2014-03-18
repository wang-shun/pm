package com.tmt.jszx.field.parameter;

import org.bson.types.ObjectId;

import com.mobnut.db.model.ModelService;
import com.mobnut.db.model.PrimaryObject;
import com.sg.business.commons.field.processparameter.AbstractRoleParameterDelegator;
import com.sg.business.model.Organization;
import com.sg.business.taskforms.IRoleConstance;

public class DeputyDirector extends AbstractRoleParameterDelegator {

	@Override
	protected String getRoldNumber() {
		return IRoleConstance.ROLE_DEPUTY_DIRECTOR_ID;
	}

	@Override
	protected int getSelectType() {
		return Organization.ROLE_SEARCH_UP;
	}

	@Override
	protected Organization getOrganization(String processParameter,
			String taskDatakey, PrimaryObject taskFormData) {
		Object value = taskFormData.getValue(taskDatakey);
		if (value instanceof ObjectId) {
			return ModelService.createModelObject(Organization.class,
					(ObjectId) value);
		}
		return null;
	}
}
