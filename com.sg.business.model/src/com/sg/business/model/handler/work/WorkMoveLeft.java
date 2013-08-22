package com.sg.business.model.handler.work;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.WorkDefinition;
import com.sg.widgets.MessageUtil;
import com.sg.widgets.command.AbstractNavigatorHandler;
import com.sg.widgets.part.CurrentAccountContext;
import com.sg.widgets.viewer.ViewerControl;

public class WorkMoveLeft extends AbstractNavigatorHandler {

	private static final String TITLE = "������������";
	
	@Override
	protected boolean nullSelectionContinue(ExecutionEvent event) {
		Shell shell = HandlerUtil.getActiveShell(event);
		MessageUtil.showToast(shell, TITLE, "����Ҫѡ��һ����������", SWT.ICON_WARNING);
		return false;
	}
	
	@Override
	protected void execute(PrimaryObject selected, ExecutionEvent event) {
		try {
			PrimaryObject[] relativeObjects = ((WorkDefinition)selected).doMoveLeft(new CurrentAccountContext());
	
			ViewerControl vc = getCurrentViewerControl(event);
			TreeViewer viewer = (TreeViewer) vc.getViewer();
			Object[] expanded = viewer.getExpandedElements();
			
			for(int i=0;i<relativeObjects.length;i++){
				viewer.refresh(relativeObjects[i]);
			}
			
			Object[] newExpand = new Object[expanded.length + 1];
			System.arraycopy(expanded, 0, newExpand, 0, expanded.length);
			newExpand[expanded.length] = selected;
			viewer.setExpandedElements(newExpand);
			viewer.setSelection(new StructuredSelection(selected), true);

		} catch (Exception e) {
			Shell shell = HandlerUtil.getActiveShell(event);
			MessageUtil.showToast(shell, TITLE, e.getMessage(), SWT.ICON_WARNING);
		}
		
		
	}


}