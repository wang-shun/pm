package com.sg.business.organization.view;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.Role;
import com.sg.widgets.MessageUtil;
import com.sg.widgets.commons.dnd.DropPrimaryObjectTarget;
import com.sg.widgets.part.CurrentAccountContext;
import com.sg.widgets.viewer.ViewerControl;


public class DropUserRole extends DropPrimaryObjectTarget{
	
	@Override
	protected void doDrop(String sourceId, List<PrimaryObject> dragsItems,
			DropTargetEvent event, ViewerControl targetViewerControl) {
		if (dragsItems == null || dragsItems.isEmpty()) {
			return;
		}
		if(event.item == null){
			return;
		}
		Role role = (Role) event.item.getData();
		try {
			role.doAssignUsers(dragsItems, new CurrentAccountContext());
			targetViewerControl.getViewer().refresh(role);
		} catch (Exception e) {
			MessageUtil.showToast(e.getMessage(), SWT.ICON_ERROR);
		}
		super.doDrop(sourceId, dragsItems, event, targetViewerControl);
	}

}
