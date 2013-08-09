package com.sg.business.model.dataset.workdef;

import com.mongodb.BasicDBObject;
import com.sg.business.model.IModelConstants;
import com.sg.business.model.WorkDefinition;
import com.sg.widgets.commons.dataset.MasterDetailDataSetFactory;

public class GenericWorkDefinitions extends MasterDetailDataSetFactory {

	public GenericWorkDefinitions() {
		super(IModelConstants.DB, IModelConstants.C_WORK_DEFINITION);
		setQueryCondition(new BasicDBObject().append(
				WorkDefinition.F_WORK_TYPE, WorkDefinition.WORK_TYPE_GENERIC));
	}

	@Override
	protected String getDetailCollectionKey() {
		return WorkDefinition.F_ORGANIZATION_ID;
	}
}
