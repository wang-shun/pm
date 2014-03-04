package com.sg.sales.model;

import java.util.List;

import com.mobnut.db.model.IContext;
import com.mobnut.db.model.PrimaryObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class TeamControl extends PrimaryObject implements ISalesTeam {

	public static final String F_VISITORLIST = "visitor_list";
	public static final String F_OWNERLIST = "owner_list";

	// 获得我可以访问的客户的条件
	public static DBObject getOwnerCondition(String userId) {
		BasicDBObject cond1 = new BasicDBObject().append(
				ISalesTeam.F_CUSTOMER_REP, userId);
		BasicDBObject cond2 = new BasicDBObject().append(
				ISalesTeam.F_SALES_MANAGER, userId);
		BasicDBObject cond3 = new BasicDBObject().append(
				ISalesTeam.F_SALES_SUP, userId);
		BasicDBObject cond4 = new BasicDBObject().append(
				ISalesTeam.F_SERVICE_MANAGER, userId);
		BasicDBObject cond5 = new BasicDBObject().append(F_OWNERLIST, userId);
		return new BasicDBObject().append("$or", new BasicDBObject[] { cond1,
				cond2, cond3, cond4, cond5 });
	}

	public static DBObject getVisitableCondition(String userId) {
		BasicDBObject cond1 = new BasicDBObject().append(
				ISalesTeam.F_CUSTOMER_REP, userId);
		BasicDBObject cond2 = new BasicDBObject().append(
				ISalesTeam.F_SALES_MANAGER, userId);
		BasicDBObject cond3 = new BasicDBObject().append(
				ISalesTeam.F_SALES_SUP, userId);
		BasicDBObject cond4 = new BasicDBObject().append(
				ISalesTeam.F_SERVICE_MANAGER, userId);
		BasicDBObject cond5 = new BasicDBObject().append(F_VISITORLIST, userId);
		return new BasicDBObject().append("$or", new BasicDBObject[] { cond1,
				cond2, cond3, cond4, cond5 });
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addToOwnerList(String userId) {
		Object value = getValue(F_OWNERLIST);
		if(!(value instanceof List)){
			value = new BasicDBList();
		}
		List listValue = (List)value;
		if(!listValue.contains(userId)){
			listValue.add(userId);
			setValue(F_OWNERLIST, listValue);
		}
	}
	
	
	@Override
	public void doInsert(IContext context) throws Exception {
		if (getValue(F_CUSTOMER_REP) == null) {
			setValue(F_CUSTOMER_REP, context.getAccountInfo().getUserId());
		}
		super.doInsert(context);
	}

	public static void duplicateTeam(PrimaryObject from, PrimaryObject to) {
		to.setValue(ISalesTeam.F_CUSTOMER_REP,
				from.getValue(ISalesTeam.F_CUSTOMER_REP));
		to.setValue(ISalesTeam.F_SALES_MANAGER,
				from.getValue(ISalesTeam.F_SALES_MANAGER));
		to.setValue(ISalesTeam.F_SALES_SUP,
				from.getValue(ISalesTeam.F_SALES_SUP));
		to.setValue(ISalesTeam.F_SERVICE_MANAGER,
				from.getValue(ISalesTeam.F_SERVICE_MANAGER));
	}

	public static void checkTeam(PrimaryObject from, PrimaryObject to) {
		Object value = to.getValue(ISalesTeam.F_CUSTOMER_REP);
		if (value != null) {
			return;
		}
		value = to.getValue(ISalesTeam.F_SALES_MANAGER);
		if (value != null) {
			return;
		}
		value = to.getValue(ISalesTeam.F_SALES_SUP);
		if (value != null) {
			return;
		}
		value = to.getValue(ISalesTeam.F_SERVICE_MANAGER);
		if (value != null) {
			return;
		}
		duplicateTeam(from,to);
	}
}
