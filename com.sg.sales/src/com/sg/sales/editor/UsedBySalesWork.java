package com.sg.sales.editor;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.Work;
import com.sg.sales.model.ISalesWork;
import com.sg.widgets.part.editor.IEditorUsedBy;

public class UsedBySalesWork implements IEditorUsedBy {

	public UsedBySalesWork() {
	}

	@Override
	public boolean usedBy(PrimaryObject po, Object key, String editorId) {
		Object catagory = po.getValue(Work.F_WORK_CATAGORY);
		if (ISalesWork.WORK_CATAGORY_SALES.equals(catagory)) {
			Work work = (Work) po;
			if (Work.STATUS_ONREADY_VALUE.equals(work.getLifecycleStatus())) {
				return "work.plan.sales.edit.1".equals(editorId);
			} else {
				if (work.isExecuteWorkflowActivateAndAvailable()) {
					return "work.plan.sales.edit.2".equals(editorId);
				} else {
					return "work.plan.sales.edit.3".equals(editorId);
				}
			}
		}else if(ISalesWork.WORK_CATAGORY_SALES_APPLYEXPENSE.equals(catagory)){
			Work work = (Work) po;
			if (Work.STATUS_ONREADY_VALUE.equals(work.getLifecycleStatus())) {
				return "work.plan.sales.applyexpense".equals(editorId);
			} else {
				return "work.plan.sales.applyexpense.inprogress".equals(editorId);
			}
			
		}
		return false;
	}

}
