package com.sg.business.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.mobnut.db.DBActivator;
import com.mobnut.db.model.ModelService;
import com.mobnut.db.model.PrimaryObject;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public abstract class AbstractWorksSummary implements IWorksSummary {

	private DBCollection colPerformence;
	private DBCollection colAllocate;

	private AggregationOutput performenceResult;
	private AggregationOutput allocateResult;
	protected PrimaryObject data;

	public AbstractWorksSummary(PrimaryObject po) {
		this.data = po;
		colPerformence = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_WORKS_PERFORMENCE);
		colAllocate = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_WORKS_ALLOCATE);
	}

	@Override
	public double getWorksPerformenceSummary(Date start, Date end) {

		if (performenceResult == null) {
			aggregationPerformence();
		}

		return getWorksSummary(start, end, performenceResult);
	}

	@Override
	public double getWorksAllocateSummary(Date start, Date end) {
		if (allocateResult == null) {
			aggregationAllocate();
		}

		return getWorksSummary(start, end, allocateResult);
	}

	private double getWorksSummary(Date start, Date end,
			AggregationOutput aggregation) {
		long startDateValue = start.getTime() / (24 * 60 * 60 * 1000);
		long endDateValue = end.getTime() / (24 * 60 * 60 * 1000);

		double ret = 0d;

		Iterator<DBObject> iter = aggregation.results().iterator();
		while (iter.hasNext()) {
			DBObject data = iter.next();
			Object id = data.get("_id"); //$NON-NLS-1$
			if ((id instanceof Number)
					&& (((Number) id).longValue() >= startDateValue && ((Number) id)
							.longValue() <= endDateValue)) {
				Object val = data.get(WorksPerformence.F_WORKS);
				if (val instanceof Double) {
					ret += ((Double) val).doubleValue();
				}
			}
		}
		return ret;
	}

	@Override
	public double getWorkPerformenceSummaryOfDay(Date date) {
		if (performenceResult == null) {
			aggregationPerformence();
		}

		return getWorksSummaryOfDay(date, performenceResult);
	}

	@Override
	public double getWorksAllocateSummaryOfDay(Date date) {
		if (allocateResult == null) {
			aggregationAllocate();
		}

		return getWorksSummaryOfDay(date, allocateResult);
	}

	private double getWorksSummaryOfDay(Date date, AggregationOutput result) {
		long dateValue = date.getTime() / (24 * 60 * 60 * 1000);

		Iterator<DBObject> iter = result.results().iterator();
		while (iter.hasNext()) {
			DBObject data = iter.next();
			Object id = data.get("_id"); //$NON-NLS-1$
			if ((id instanceof Number)
					&& (((Number) id).longValue() == dateValue)) {
				Object val = data.get(WorksPerformence.F_WORKS);
				if (val instanceof Double) {
					return ((Double) val).doubleValue();
				}
			}
		}
		return 0d;
	}

	private void aggregationPerformence() {
		performenceResult = aggregation(colPerformence);
	}

	private void aggregationAllocate() {
		allocateResult = aggregation(colAllocate);
	}

	private AggregationOutput aggregation(DBCollection col) {
		DBObject match = new BasicDBObject();
		match.put("$match", getMatchCondition(data)); //$NON-NLS-1$

		DBObject group = new BasicDBObject();
		group.put("$group", getGroupCondition(data)); //$NON-NLS-1$

		return col.aggregate(match, group);
	}

	protected abstract Object getGroupCondition(PrimaryObject data);

	protected abstract Object getMatchCondition(PrimaryObject data);

	@Override
	public List<PrimaryObject[]> getWorkOfWorksSummaryOfDateCode(String userid,
			Date date) {
		long dateCode = date.getTime() / (24 * 60 * 60 * 1000);

		ArrayList<PrimaryObject[]> ret = new ArrayList<PrimaryObject[]>();

		DBCursor cur = colPerformence.find(getDateCondition(userid, dateCode));

		Map<Work, AbstractWorksMetadata[]> map = new HashMap<Work, AbstractWorksMetadata[]>();
		while (cur.hasNext()) {
			DBObject next = cur.next();
			ObjectId workid = (ObjectId) next.get(WorksPerformence.F_WORKID);
			// ObjectId _id = (ObjectId) next.get(WorksPerformence.F__ID);
			Work work = ModelService.createModelObject(Work.class, workid);
			WorksPerformence wp = ModelService.createModelObject(next,
					WorksPerformence.class);
			AbstractWorksMetadata[] data = new AbstractWorksMetadata[2];
			data[0] = wp;
			map.put(work, data);
		}

		cur = colAllocate.find(getDateCondition(userid, dateCode));

		while (cur.hasNext()) {
			DBObject next = cur.next();
			ObjectId workid = (ObjectId) next.get(WorksPerformence.F_WORKID);
			// ObjectId _id = (ObjectId) next.get(WorksPerformence.F__ID);
			Work work = ModelService.createModelObject(Work.class, workid);
			WorksAllocate ap = ModelService.createModelObject(next,
					WorksAllocate.class);
			AbstractWorksMetadata[] data = map.get(work);
			if (data == null) {
				data = new AbstractWorksMetadata[2];
			}
			data[1] = ap;
			map.put(work, data);
		}

		Iterator<Work> iter = map.keySet().iterator();
		while (iter.hasNext()) {
			Work work = iter.next();
			AbstractWorksMetadata[] value = map.get(work);
			ret.add(new PrimaryObject[] { work, value[0], value[1] });
		}
		return ret;
	}

	protected BasicDBObject getDateCondition(String userid, long dateCode) {
		return new BasicDBObject().append(
				WorksPerformence.F_USERID, userid).append(
				WorksPerformence.F_DATECODE, dateCode);
	}

	@Override
	public double getWorksPerformenceTotalSummary() {
		if (performenceResult == null) {
			aggregationPerformence();
		}

		double ret = 0d;
		Iterator<DBObject> iter = performenceResult.results().iterator();
		while (iter.hasNext()) {
			DBObject data = iter.next();

			Object val = data.get(WorksPerformence.F_WORKS);
			if (val instanceof Double) {
				ret += ((Double) val).doubleValue();
			}
		}

		return ret;
	}

	@Override
	public double getWorksAllocateTotalSummary() {
		if (allocateResult == null) {
			aggregationAllocate();
		}

		double ret = 0d;
		Iterator<DBObject> iter = allocateResult.results().iterator();
		while (iter.hasNext()) {
			DBObject data = iter.next();

			Object val = data.get(WorksPerformence.F_WORKS);
			if (val instanceof Double) {
				ret += ((Double) val).doubleValue();
			}
		}

		return ret;
	}
}
