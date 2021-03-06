package com.tmt.gs.dataset;

import com.mobnut.db.model.mongodb.SingleDBCollectionDataSetFactory;
import com.mongodb.BasicDBObject;
import com.sg.business.model.IModelConstants;
import com.sg.business.model.Organization;

public class ProjectFunctionOrgOfGS extends SingleDBCollectionDataSetFactory {

	public ProjectFunctionOrgOfGS() {
		super(IModelConstants.DB, IModelConstants.C_ORGANIZATION);
		setQueryCondition(new BasicDBObject().append(Organization.F_IS_FUNCTION_DEPARTMENT, Boolean.TRUE));
	}
}
