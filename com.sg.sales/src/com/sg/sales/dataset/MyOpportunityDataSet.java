package com.sg.sales.dataset;

import com.mobnut.db.model.mongodb.SingleDBCollectionDataSetFactory;
import com.sg.business.model.IModelConstants;
import com.sg.sales.Sales;
import com.sg.sales.model.TeamControled;
import com.sg.widgets.part.CurrentAccountContext;

public class MyOpportunityDataSet extends SingleDBCollectionDataSetFactory {

	public MyOpportunityDataSet() {
		super(IModelConstants.DB, Sales.C_OPPORTUNITY);
		String userid = new CurrentAccountContext().getConsignerId();
		setQueryCondition(TeamControled.getVisitableCondition(userid));
	}

}
