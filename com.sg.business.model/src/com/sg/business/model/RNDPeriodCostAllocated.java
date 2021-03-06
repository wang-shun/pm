package com.sg.business.model;

import com.mobnut.db.model.PrimaryObject;

/**
 * 直接记录在工作令号下的研发成本
 * @author Administrator
 *
 */
public class RNDPeriodCostAllocated extends PrimaryObject implements IAccountPeriod {

	public static final String F_YEAR = "year"; //$NON-NLS-1$

	public static final String F_MONTH = "month"; //$NON-NLS-1$

	public static final String F_COSTCENTERCODE = "costcenter"; //$NON-NLS-1$

	public static final String F_WORKORDER = "workorder"; //$NON-NLS-1$
	
	@Override
	public Double getAccountValue(String accountNumber) {
		return (Double) getValue(accountNumber);
	}

	public String getCostCode() {
		return (String) getValue(F_COSTCENTERCODE);
	}

	@Override
	public String getLabel() {
		String label = getDesc();
		String costCode = getCostCode();
		if (label == null) {
			return costCode;
		} else {
			return costCode + label;
		}
	}

}