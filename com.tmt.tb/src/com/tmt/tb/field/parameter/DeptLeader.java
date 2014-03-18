package com.tmt.tb.field.parameter;

import org.bson.types.ObjectId;

import com.mobnut.db.model.ModelService;
import com.mobnut.db.model.PrimaryObject;
import com.sg.business.commons.field.processparameter.AbstractRoleParameterDelegator;
import com.sg.business.model.Organization;
import com.sg.business.model.Role;

public class DeptLeader extends AbstractRoleParameterDelegator {

	@Override
	protected String getRoldNumber() {
		return Role.ROLE_DEPT_MANAGER_ID;
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
