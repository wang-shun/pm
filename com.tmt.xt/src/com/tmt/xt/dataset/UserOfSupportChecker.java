package com.tmt.xt.dataset;

import com.sg.business.commons.dataset.AbstractRoleAssignmentDataSetFactory;
import com.sg.business.model.Organization;
import com.sg.business.taskforms.IRoleConstance;

public class UserOfSupportChecker extends AbstractRoleAssignmentDataSetFactory {

	@Override
	protected int getSelectType() {
		return Organization.ROLE_SEARCH_UP;
	}

	@Override
	protected String getRoleNumber() {
		return IRoleConstance.ROLE_SUPPORT_CHECKER_ID;
	}

}
