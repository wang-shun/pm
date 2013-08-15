package com.sg.business.model.dataset.projecttemplate;

import com.mongodb.BasicDBObject;
import com.sg.business.model.IModelConstants;
import com.sg.business.model.WorkDefinition;
import com.sg.widgets.commons.dataset.MasterDetailDataSetFactory;

public class StandloneWorkDefOfOrg extends
		MasterDetailDataSetFactory {

	public StandloneWorkDefOfOrg() {
		super(IModelConstants.DB, IModelConstants.C_WORK_DEFINITION);
		setQueryCondition(new BasicDBObject().append(
				WorkDefinition.F_WORK_TYPE, WorkDefinition.WORK_TYPE_STANDLONE)
				.append(WorkDefinition.F_PARENT_ID, null));
	}

	@Override
	protected String getDetailCollectionKey() {
		return WorkDefinition.F_ORGANIZATION_ID;
	}

}