package com.sg.business.management.handler;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.WorkDefinition;
import com.sg.widgets.MessageUtil;
import com.sg.widgets.part.CurrentAccountContext;
import com.sg.widgets.part.NavigatorPanel;
import com.sg.widgets.viewer.CTreeViewer;
import com.sg.widgets.viewer.ViewerControl;

public class WorkDefinitionNavigatorPanel extends NavigatorPanel {

	private CTreeViewer viewer;

	@Override
	protected void upPressed() {
		try {
			WorkDefinition selected = getSelected();
			PrimaryObject[] relativeObjects = selected
					.doMoveUp(new CurrentAccountContext());

			for (int i = 0; i < relativeObjects.length; i++) {
				viewer.refresh(relativeObjects[i]);
			}
			viewer.setSelection(new StructuredSelection(selected), true);
		} catch (Exception e) {
			MessageUtil.showToast(e.getMessage(), SWT.ICON_WARNING);

		}
	}

	@Override
	protected void leftPressed() {
		try {
			WorkDefinition selected = getSelected();

			PrimaryObject[] relativeObjects = selected.doMoveLeft(
					new CurrentAccountContext());

			Object[] expanded = viewer.getExpandedElements();

			for (int i = 0; i < relativeObjects.length; i++) {
				viewer.refresh(relativeObjects[i]);
			}

			Object[] newExpand = new Object[expanded.length + 1];
			System.arraycopy(expanded, 0, newExpand, 0, expanded.length);
			newExpand[expanded.length] = selected;
			viewer.setExpandedElements(newExpand);
			viewer.setSelection(new StructuredSelection(selected), true);

		} catch (Exception e) {
			MessageUtil.showToast(e.getMessage(), SWT.ICON_WARNING);

		}
	}

	@Override
	protected void downPressed() {
		try {
			WorkDefinition selected = getSelected();

			PrimaryObject[] relativeObjects = selected.doMoveDown(
					new CurrentAccountContext());

			for (int i = 0; i < relativeObjects.length; i++) {
				viewer.refresh(relativeObjects[i]);
			}
			viewer.setSelection(new StructuredSelection(selected), true);
		} catch (Exception e) {
			MessageUtil.showToast(e.getMessage(), SWT.ICON_WARNING);

		}
	}

	@Override
	protected void rightPressed() {
		try {
			WorkDefinition selected = getSelected();

			PrimaryObject[] relativeObjects = selected.doMoveRight(
					new CurrentAccountContext());

			Object[] expanded = viewer.getExpandedElements();

			for (int i = 0; i < relativeObjects.length; i++) {
				viewer.refresh(relativeObjects[i]);
			}

			// 需要展开upperNeighbor
			boolean upperNeiborExpended = false;
			for (int i = 0; i < expanded.length; i++) {
				if (expanded[i].equals(relativeObjects[1])) {
					upperNeiborExpended = true;
					break;
				}
			}
			if (!upperNeiborExpended) {
				Object[] newExpand = new Object[expanded.length + 1];
				System.arraycopy(expanded, 0, newExpand, 0, expanded.length);
				newExpand[expanded.length] = relativeObjects[1];
				viewer.setExpandedElements(newExpand);
			} else {
				viewer.setExpandedElements(expanded);
			}

			viewer.setSelection(new StructuredSelection(selected), true);
		} catch (Exception e) {
			MessageUtil.showToast(e.getMessage(), SWT.ICON_WARNING);

		}
	}

	private WorkDefinition getSelected() {
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		Assert.isTrue(selection != null && !selection.isEmpty(), "请先选中节点");

		return (WorkDefinition) selection.getFirstElement();
	}

	public void setViewerControl(ViewerControl vc) {
		this.viewer = (CTreeViewer) vc.getViewer();
	}

}
