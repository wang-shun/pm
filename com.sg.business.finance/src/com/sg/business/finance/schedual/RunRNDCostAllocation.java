package com.sg.business.finance.schedual;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import com.mobnut.admin.schedual.registry.ISchedualJobRunnable;
import com.mobnut.commons.Commons;
import com.mobnut.db.DBActivator;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.sg.business.finance.eai.RNDPeriodCostAdapter;
import com.sg.business.model.CostAccount;
import com.sg.business.model.IModelConstants;
import com.sg.business.model.Organization;
import com.sg.business.model.RNDPeriodCost;

/**
 * 每月1号对上月的数据进行获取
 * 
 * @author Administrator
 * 
 */
public class RunRNDCostAllocation implements ISchedualJobRunnable {

	@Override
	public boolean run() throws Exception {
		RNDPeriodCostAdapter adapter = new RNDPeriodCostAdapter();

		Calendar cal = Calendar.getInstance();
//		cal.set(Calendar.MONTH, 1);
		for (int i = 0; i > -23; i--) {
			cal.add(Calendar.MONTH, -1);

			long start = System.currentTimeMillis();
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH) + 1;

			// 获得所有的成本中心代码
			String[] costCodes = getCostCodeArray(year, month);
			String[] costElementArray = getCostElemenArray();

			try {
				Commons.LOGGER.info("准备获取SAP成本中心数据:" + year + "-" + month);
				adapter.runGetData(null, costCodes, costElementArray, year,
						month, null);
			} catch (Exception e) {
				Commons.LOGGER.error("获得SAP成本中心数据失败:" + year + "-" + month, e);
				throw e;
			}
			long end = System.currentTimeMillis();
			Commons.LOGGER.info("获得SAP成本中心数据完成:" + year + "-" + month + " "
					+ (end - start) / 1000);
		}
		
		return true;

	}

	private String[] getCostElemenArray() {
		DBCollection col = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_COSTACCOUNT_ITEM);
		DBCursor cur = col.find();
		String[] result = new String[cur.size()];
		int i = 0;
		while (cur.hasNext()) {
			result[i++] = (String) cur.next().get(
					CostAccount.F_COST_ACCOUNTNUMBER);
		}
		return result;
	}

	private String[] getCostCodeArray(int year, int month) {
		Set<String> costCodeArray = new HashSet<String>();
		DBCollection col = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_ORGANIZATION);
		DBCursor cur = col.find(new BasicDBObject().append(
				"$and",
				new BasicDBObject[] {
						new BasicDBObject().append(
								Organization.F_COST_CENTER_CODE,
								new BasicDBObject().append("$ne", null)),
						new BasicDBObject().append(
								Organization.F_COST_CENTER_CODE,
								new BasicDBObject().append("$ne", "")) }),
				new BasicDBObject().append(Organization.F_COST_CENTER_CODE, 1));

		DBCollection rndcostCol = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_RND_PEROIDCOST_COSTCENTER);
		while (cur.hasNext()) {
			DBObject next = cur.next();
			String costCode = (String) next
					.get(Organization.F_COST_CENTER_CODE);
			// 检查该成本中心是否已经取数
			long cnt = rndcostCol.count(new BasicDBObject()
					.append(RNDPeriodCost.F_COSTCENTERCODE, costCode)
					.append(RNDPeriodCost.F_YEAR, new Integer(year))
					.append(RNDPeriodCost.F_MONTH, new Integer(month)));
			if (cnt == 0) {
				costCodeArray.add(costCode);
			}

		}
		return costCodeArray.toArray(new String[0]);

	}
}
