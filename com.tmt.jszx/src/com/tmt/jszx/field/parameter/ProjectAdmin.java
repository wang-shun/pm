package com.tmt.jszx.field.parameter;

import org.bson.types.ObjectId;

import com.mobnut.db.model.ModelService;
import com.mobnut.db.model.PrimaryObject;
import com.sg.business.commons.field.processparameter.AbstractRoleParameterDelegator;
import com.sg.business.model.Organization;
import com.sg.business.model.Role;

public class ProjectAdmin  extends AbstractRoleParameterDelegator {


	@Override
	protected String getRoldNumber(Object type) {
		return Role.ROLE_PROJECT_ADMIN_ID;
	}

	@Override
	protected int getSelectType(Object type) {
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

	@Override
	protected Object setType(String processParameter, String taskDatakey,
			PrimaryObject taskFormData) {
		// TODO Auto-generated method stub
		return null;
	}

}
