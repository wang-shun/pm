package com.sg.business.management.dnd;

import java.util.List;

import org.eclipse.swt.dnd.DropTargetEvent;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.DeliverableDefinition;
import com.sg.business.model.DocumentDefinition;
import com.sg.business.model.IDeliverable;
import com.sg.business.model.WorkDefinition;
import com.sg.widgets.commons.dnd.DropPrimaryObjectTarget;
import com.sg.widgets.part.CurrentAccountContext;
import com.sg.widgets.viewer.ViewerControl;

public class DropDeliverable extends DropPrimaryObjectTarget {

	@Override
	protected void doDrop(String sourceId, List<PrimaryObject> dragsItems,
			DropTargetEvent event, ViewerControl targetViewerControl) {
		if (dragsItems == null || dragsItems.isEmpty()) {
			return;
		}
		if (event.item == null) {
			return;
		}
		Object targetPo = event.item.getData();
		if (targetPo instanceof WorkDefinition) {
			WorkDefinition workDefinition = (WorkDefinition) targetPo;
			doCreateDeliverableDefinition(workDefinition, dragsItems,
					targetViewerControl);
		}
		// targetViewerControl.getViewer().refresh(role);
		super.doDrop(sourceId, dragsItems, event, targetViewerControl);
	}

	private void doCreateDeliverableDefinition(WorkDefinition workDefinition,
			List<PrimaryObject> dragsItems, ViewerControl targetViewerControl) {
		for (PrimaryObject po : dragsItems) {
			if (po instanceof DocumentDefinition) {
				DocumentDefinition documentDefinition = (DocumentDefinition) po;
				DeliverableDefinition deliverableDefinition = workDefinition
						.makeDeliverableDefinition(documentDefinition,IDeliverable.TYPE_OUTPUT);
				deliverableDefinition.setParentPrimaryObject(workDefinition);
				deliverableDefinition.addEventListener(targetViewerControl);
				try {
					deliverableDefinition.doSave(new CurrentAccountContext());
				} catch (Exception e) {
				}
			}
		}
	}
}
