package com.sg.business.commons.editingsupport;

import com.sg.business.model.Work;

public class PlanStartEditing extends SchedualEditing {

	@Override
	protected String getFieldName() {
		return Work.F_PLAN_START;
	}

}