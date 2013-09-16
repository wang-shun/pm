package com.sg.business.commons.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.AbstractWork;
import com.sg.widgets.MessageUtil;
import com.sg.widgets.command.AbstractNavigatorHandler;
import com.sg.widgets.part.CurrentAccountContext;
import com.sg.widgets.part.INavigatorActionListener;
import com.sg.widgets.viewer.ViewerControl;

public class WorkMoveUp extends AbstractNavigatorHandler {

	private static final String TITLE = "上移工作定义";

	@Override
	protected boolean nullSelectionContinue(ExecutionEvent event) {
		Shell shell = HandlerUtil.getActiveShell(event);
		MessageUtil.showToast(shell, TITLE, "您需要选择一个工作定义", SWT.ICON_WARNING);
		return false;
	}

	@Override
	protected void execute(PrimaryObject selected, ExecutionEvent event) {
		Shell shell = HandlerUtil.getActiveShell(event);
		try {
			PrimaryObject[] relativeObjects = ((AbstractWork) selected)
					.doMoveUp(new CurrentAccountContext());

			ViewerControl vc = getCurrentViewerControl(event);
			ColumnViewer viewer = vc.getViewer();
			for (int i = 0; i < relativeObjects.length; i++) {
				viewer.refresh(relativeObjects[i]);
			}
			viewer.setSelection(new StructuredSelection(selected), true);
			
			/**********************************************************
			 * [BUG:20]
			 * 将更改消息传递到编辑器
			 */
			sendNavigatorActionEvent(event, INavigatorActionListener.CUSTOMER,
					new Integer(INavigatorActionListener.REFRESH));
		} catch (Exception e) {
			MessageUtil.showToast(shell, TITLE, e.getMessage(),
					SWT.ICON_WARNING);
		}

	}

}
