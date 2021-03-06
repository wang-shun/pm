package com.sg.business.commons.ui.sidebar;

import com.mobnut.db.model.DataSetFactory;
import com.sg.business.model.dataset.projecttemplate.LaunchableWorkSet;

public class Launchablelist extends AbstractListViewSideItem {

	private DataSetFactory dataSet;

	public Launchablelist() {
		super();
		dataSet = new LaunchableWorkSet();
	}

	@Override
	protected DataSetFactory getDataSetFactory() {
		return dataSet;
	}

}
