package com.sg.business.visualization.sorter;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.Project;
import com.sg.business.model.etl.ProjectPresentation;
import com.sg.widgets.commons.sorter.PrimaryObjectSortorFactory;
import com.sg.widgets.registry.config.ColumnConfigurator;

public class OverCostSorter extends PrimaryObjectSortorFactory {

	@Override
	protected Object getValue(PrimaryObject po, ColumnConfigurator configurator) {
		ProjectPresentation project = ((Project) po).getPresentation();
		double bc = project.getBudgetValue();
		double ac = project.getDesignatedInvestment();
		if(bc == 0d){
			return 0d;
		}
		return (ac-bc)/bc;
	}

}
