package com.sg.business.model;

import com.mobnut.db.model.PrimaryObject;
import com.mongodb.BasicDBObject;

public class SummaryProjectWorks extends AbstractWorksSummary {


	public SummaryProjectWorks(PrimaryObject po) {
		super(po);
	}

	@Override
	protected Object getMatchCondition(PrimaryObject data) {
		BasicDBObject result = new BasicDBObject().append(WorksPerformence.F_PROJECT_ID,
				data.get_id());
		Object participate = data.getValue("$participate"); //$NON-NLS-1$
		if(participate instanceof String[]){
			result.put(WorksPerformence.F_USERID, new BasicDBObject().append("$in", participate)); //$NON-NLS-1$
		}
		return result;
	}

	@Override
	protected Object getGroupCondition(PrimaryObject data) {
		return new BasicDBObject().append("_id", //$NON-NLS-1$
				"$" + WorksPerformence.F_DATECODE).append( //$NON-NLS-1$
				WorksPerformence.F_WORKS,
				new BasicDBObject().append("$sum", "$" //$NON-NLS-1$ //$NON-NLS-2$
						+ WorksPerformence.F_WORKS));
	}

}
