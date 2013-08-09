package com.sg.business.organization.editor;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.Organization;
import com.sg.widgets.viewer.ChildPrimaryObjectCreator;


public class SubTeamCreater extends ChildPrimaryObjectCreator{

	@Override
	protected String getMessageForEmptySelection() {
		return "您需要选择上级组织后进行创建";
	}

	@Override
	protected void setParentData(PrimaryObject po) {
		PrimaryObject parentOrg = po.getParentPrimaryObject();
		po.setValue(Organization.F_PARENT_ID, parentOrg.get_id());
	}

}
