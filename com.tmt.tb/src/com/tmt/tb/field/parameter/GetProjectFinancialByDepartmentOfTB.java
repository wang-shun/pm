package com.tmt.tb.field.parameter;

import java.util.List;

import org.bson.types.ObjectId;

import com.mobnut.db.model.ModelService;
import com.mobnut.db.model.PrimaryObject;
import com.sg.bpm.workflow.taskform.IProcessParameterDelegator;
import com.sg.business.model.Organization;
import com.sg.business.model.Role;
import com.sg.business.model.RoleAssignment;
import com.sg.business.model.User;
import com.sg.business.model.toolkit.UserToolkit;

public class GetProjectFinancialByDepartmentOfTB implements
		IProcessParameterDelegator {

	public GetProjectFinancialByDepartmentOfTB() {
	}

	@Override
	public Object getValue(String processParameter, String taskDatakey,
			PrimaryObject taskFormData) {
		Object value = taskFormData.getValue("dept");
		String prjAdmin = null;
		if (value instanceof ObjectId) {
			ObjectId objectId = (ObjectId) value;
			Organization org = ModelService.createModelObject(
					Organization.class,objectId);
			Role role = org.getRole(Role.ROLE_FINANCIAL_MANAGER_ID, 1);
			List<PrimaryObject> assignment = role.getAssignment();
			if (assignment != null) {
				RoleAssignment roleAssignment = (RoleAssignment) assignment.get(0);
				prjAdmin = roleAssignment.getUserid();
			}
		}
		if(prjAdmin == null){
			prjAdmin = ((User) UserToolkit.getAdmin().get(0)).getUserid();
		}
		taskFormData.setValue(taskDatakey, prjAdmin);
		return prjAdmin;
	}

}