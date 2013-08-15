package com.sg.business.model.dataset.organization;

import com.sg.business.model.IModelConstants;
import com.sg.business.model.Role;
import com.sg.widgets.commons.dataset.MasterDetailDataSetFactory;

public class MemberofOrg extends MasterDetailDataSetFactory {

	public MemberofOrg() {
		super(IModelConstants.DB, IModelConstants.C_USER);
	}

	@Override
	protected String getDetailCollectionKey() {
		return Role.F_ORGANIZATION_ID;
	}
}