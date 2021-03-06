package com.sg.business.commons.dataset;

import java.util.ArrayList;
import java.util.List;

import com.mobnut.db.model.DataSet;
import com.mobnut.db.model.ModelService;
import com.mobnut.db.model.PrimaryObject;
import com.mongodb.DBObject;
import com.sg.business.model.Document;
import com.sg.business.model.IModelConstants;
import com.sg.widgets.commons.dataset.MasterDetailDataSetFactory;

public abstract class AbstractTableDataSetFactory extends MasterDetailDataSetFactory {

	public AbstractTableDataSetFactory() {
		super(IModelConstants.DB, IModelConstants.C_DOCUMENT);
	}

	@Override
	protected String getDetailCollectionKey() {
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public DataSet getDataSet() {
		if (master != null) {
			if (master instanceof Document) {
				Document document = (Document) master;
				List<PrimaryObject> productList = new ArrayList<PrimaryObject>();
				List<DBObject> product = (List<DBObject>) document
						.getListValue(getDataEditorFieldName());
				for (DBObject object : product) {
					PrimaryObject po = ModelService.createModelObject(object, PrimaryObject.class);
					productList.add(po);
				}

				return new DataSet(productList);
			}
		}

		return super.getDataSet();
	}

	protected abstract String getDataEditorFieldName();

}
