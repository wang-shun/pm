package com.sg.business.visualization.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;

import com.mobnut.db.DBActivator;
import com.mobnut.db.model.IRelationConditionProvider;
import com.mobnut.db.model.ModelRelation;
import com.mobnut.db.model.PrimaryObject;
import com.mobnut.db.model.mongodb.StructuredDBCollectionDataSetFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sg.business.model.ILifecycle;
import com.sg.business.model.IModelConstants;
import com.sg.business.model.Organization;
import com.sg.business.model.Project;
import com.sg.business.model.User;
import com.sg.business.visualization.data.ProjectSetFolder;
import com.sg.widgets.registry.config.TreeConfigurator;
import com.sg.widgets.viewer.RelationContentProvider;

public class ProjectSetContentProvider extends RelationContentProvider {

	private Object[] aviOrg;
	DBCollection projectcol;
	DBCollection orgcol;

	public ProjectSetContentProvider() {
		super();
		projectcol = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_PROJECT);
		orgcol = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_ORGANIZATION);
	}

	public ProjectSetContentProvider(TreeConfigurator configurator) {
		super(configurator);
		projectcol = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_PROJECT);
		orgcol = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_ORGANIZATION);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		PrimaryObject po = (PrimaryObject) parentElement;
		if (po instanceof ProjectSetFolder) {
			return ((ProjectSetFolder) po).getChildren();
		} else {
			Set<ModelRelation> childrenMRSet = mrmap.get(po.getClass());
			List<PrimaryObject> children = new ArrayList<PrimaryObject>();
			if (childrenMRSet != null && !childrenMRSet.isEmpty()) {
				Iterator<ModelRelation> iter = childrenMRSet.iterator();
				while (iter.hasNext()) {
					ModelRelation mr = iter.next();
					List<PrimaryObject> relationDataList = getRelationByModel(
							mr, po);
					children.addAll(relationDataList);
				}
			}
			return getElements(children);
		}
	}

	@Override
	public boolean hasChildren(Object parentElement) {
		if (parentElement instanceof ProjectSetFolder) {
			return ((ProjectSetFolder) parentElement).hasChildren();
		} else {
			PrimaryObject po = (PrimaryObject) parentElement;
			Set<ModelRelation> childrenMRSet = mrmap.get(po.getClass());
			if (childrenMRSet != null && !childrenMRSet.isEmpty()) {
				Iterator<ModelRelation> iter = childrenMRSet.iterator();
				while (iter.hasNext()) {
					ModelRelation mr = iter.next();
					long count = getRelationCountByModel(mr, po);
					if (count > 0) {
						return true;
					}
				}
			}
			return false;
		}

	}

	public List<PrimaryObject> getRelationByModel(ModelRelation mr,
			PrimaryObject po) {
		Class<? extends PrimaryObject> end2Class = mr.getEnd2Class();
		IRelationConditionProvider irc = mr.getRelationConditionProvider();

		DBObject condition = null;
		if ("organization_organization".equals(mr.getId())) { //$NON-NLS-1$

			if (irc != null) {
				condition = irc.getCondition(po);
			} else {
				condition = new BasicDBObject();
				condition.put(mr.getEnd2Key(), po.getValue(mr.getEnd1Key()));
				condition.put(Organization.F__ID, new BasicDBObject().append(
						"$in", getAvailableOrganization())); //$NON-NLS-1$
			}

		} else if (getRelationName().equals(mr.getId())) { //$NON-NLS-1$
			condition = new BasicDBObject();
			condition.put(User.F_USER_ID,
					new BasicDBObject().append("$in", getAvailableUser(po))); //$NON-NLS-1$
		}
		StructuredDBCollectionDataSetFactory sdf = po
				.getRelationDataSetFactory(end2Class, condition);
		DBObject sort = mr.getSort();
		if (sort != null) {
			sdf.setSort(sort);
		}
		return sdf.getDataSet().getDataItems();
	}

	public long getRelationCountByModel(ModelRelation mr, PrimaryObject po) {
		Class<? extends PrimaryObject> end2Class = mr.getEnd2Class();
		IRelationConditionProvider irc = mr.getRelationConditionProvider();
		DBObject condition = null;
		if ("organization_organization".equals(mr.getId())) { //$NON-NLS-1$

			if (irc != null) {
				condition = irc.getCondition(po);
			} else {
				condition = new BasicDBObject();
				condition.put(mr.getEnd2Key(), po.getValue(mr.getEnd1Key()));
				condition.put(Organization.F__ID, new BasicDBObject().append(
						"$in", getAvailableOrganization())); //$NON-NLS-1$
			}

		} else if (getRelationName().equals(mr.getId())) { //$NON-NLS-1$
			condition = new BasicDBObject();
			condition.put(User.F_USER_ID,
					new BasicDBObject().append("$in", getAvailableUser(po))); //$NON-NLS-1$
		}
		StructuredDBCollectionDataSetFactory sdf = po
				.getRelationDataSetFactory(end2Class, condition);
		return sdf.getTotalCount();
	}

	protected String getRelationName() {
		return "organization_projectmanager";
	}

	protected String getOrganizationFieldName() {
		return Project.F_LAUNCH_ORGANIZATION;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object[] getAvailableOrganization() {
		if (aviOrg == null) {
			Set<ObjectId> set = new HashSet<ObjectId>();
			List prjOrgList = projectcol.distinct(getOrganizationFieldName(),
					new BasicDBObject().append(
							ILifecycle.F_LIFECYCLE,
							new BasicDBObject().append("$in", new String[] { //$NON-NLS-1$
									ILifecycle.STATUS_FINIHED_VALUE,
											ILifecycle.STATUS_WIP_VALUE })));
			set.addAll(prjOrgList);

			List parentOrgList = orgcol.distinct(Organization.F_PARENT_ID,
					new BasicDBObject().append(Organization.F__ID,
							new BasicDBObject().append("$in", prjOrgList))); //$NON-NLS-1$
			while (parentOrgList != null
					&& !parentOrgList.isEmpty()
					|| (parentOrgList.size() == 1 && parentOrgList.get(0) != null)) {
				set.addAll(parentOrgList);
				parentOrgList = orgcol.distinct(Organization.F_PARENT_ID,
						new BasicDBObject().append(Organization.F__ID,
								new BasicDBObject()
										.append("$in", parentOrgList))); //$NON-NLS-1$
			}

			aviOrg = set.toArray(new Object[0]);
		}

		return aviOrg;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object getAvailableUser(PrimaryObject po) {
		Set<ObjectId> set = new HashSet<ObjectId>();
		List prjManagerList = projectcol.distinct(
				getRelatedUserFieldName(),
				new BasicDBObject().append(
						ILifecycle.F_LIFECYCLE,
						new BasicDBObject().append("$in", new String[] { //$NON-NLS-1$
								ILifecycle.STATUS_FINIHED_VALUE,
										ILifecycle.STATUS_WIP_VALUE })).append(
						getOrganizationFieldName(), po.get_id()));
		set.addAll(prjManagerList);
		return set.toArray(new Object[0]);
	}

	protected String getRelatedUserFieldName() {
		return Project.F_CHARGER;
	}

}
