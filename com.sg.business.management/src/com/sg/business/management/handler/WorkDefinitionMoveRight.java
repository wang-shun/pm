package com.sg.business.management.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.WorkDefinition;
import com.sg.widgets.MessageUtil;
import com.sg.widgets.part.CurrentAccountContext;
import com.sg.widgets.viewer.ViewerControl;

public class WorkDefinitionMoveRight extends AbstractWorkDefinitionHandler {

	private static final String TITLE = "降级工作定义";

	@Override
	protected boolean nullSelectionContinue(ExecutionEvent event) {
		Shell shell = HandlerUtil.getActiveShell(event);
		MessageUtil.showToast(shell, TITLE, "您需要选择一个工作定义", SWT.ICON_WARNING);
		return false;
	}

	@Override
	protected void execute(WorkDefinition selected, ExecutionEvent event) {
		Shell shell = HandlerUtil.getActiveShell(event);
		try {
			PrimaryObject[] relativeObjects = selected
					.doMoveRight(new CurrentAccountContext());

			ViewerControl vc = getCurrentViewerControl(event);
			TreeViewer viewer = (TreeViewer) vc.getViewer();
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
			MessageUtil.showToast(shell, TITLE, e.getMessage(),
					SWT.ICON_WARNING);
		}

	}

}
