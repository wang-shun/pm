package com.sg.business.commons.operation.handler;

import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.commons.ui.flow.ProcessHistoryDialog;
import com.sg.business.resource.nls.Messages;
import com.sg.widgets.command.AbstractNavigatorHandler;
import com.sg.widgets.viewer.ViewerControl;

public class WorkflowView extends AbstractNavigatorHandler {

	@Override
	protected void execute(PrimaryObject selected, IWorkbenchPart part,
			ViewerControl vc, Command command, Map<String, Object> parameters, IStructuredSelection selection) { // 假定传入的是流程控制对象
		String key = (String) parameters.get("process.key"); //$NON-NLS-1$
		Shell shell = part.getSite().getShell();
		ProcessHistoryDialog pvd = new ProcessHistoryDialog(shell, selected, key,
				"" + selected + Messages.get().WorkflowView_2); //$NON-NLS-1$
		pvd.open();

	}

}
