package com.sg.business.model.dataset.visualization;

import java.util.ArrayList;
import java.util.List;

import com.mobnut.db.model.DataSet;
import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.IModelConstants;
import com.sg.business.model.ProjectProvider;
import com.sg.widgets.commons.dataset.MasterDetailDataSetFactory;

public class VisProjectSet extends MasterDetailDataSetFactory {

	public VisProjectSet() {
		super(IModelConstants.DB, IModelConstants.C_PROJECT);
	}

	@Override
	protected String getDetailCollectionKey() {
		return null;
	}

    @Override
    public DataSet getDataSet() {
    	if(master==null){
    		return new DataSet(new ArrayList<PrimaryObject>());
    	}
    	ProjectProvider pp = (ProjectProvider)master;
    	List<PrimaryObject> list=pp.getData();
    	return new DataSet(list);
    }
}
