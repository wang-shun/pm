package com.sg.sales.dataset;

import com.mobnut.db.model.mongodb.SingleDBCollectionDataSetFactory;
import com.sg.business.model.IModelConstants;
import com.sg.sales.Sales;
import com.sg.sales.model.TeamControl;
import com.sg.widgets.part.CurrentAccountContext;

public class MyWorkCostDataSet extends SingleDBCollectionDataSetFactory {

	public MyWorkCostDataSet() {
		super(IModelConstants.DB, Sales.C_WORKCOST);
		String userid = new CurrentAccountContext().getConsignerId();
		setQueryCondition(TeamControl.getVisitableCondition(userid));
	}

}
