package com.tmt.gs.dataset;

import com.sg.business.commons.dataset.AbstractRoleAssignmentDataSetFactory;
import com.sg.business.model.Organization;
import com.sg.business.taskforms.IRoleConstance;

public class GSBusinessSupportApprover extends AbstractRoleAssignmentDataSetFactory {

	@Override
	protected int getSelectType() {
		return Organization.ROLE_SEARCH_UP;
	}

	@Override
	protected String getRoleNumber() {
		// TODO Auto-generated method stub
		return IRoleConstance.ROLE_SUPPORT_APPROVE_ID;
	}
}
