package com.sg.business.management.handler;

import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.DeliverableDefinition;
import com.sg.business.resource.nls.Messages;
import com.sg.widgets.MessageUtil;
import com.sg.widgets.Widgets;
import com.sg.widgets.command.AbstractNavigatorHandler;
import com.sg.widgets.part.editor.DataObjectDialog;
import com.sg.widgets.registry.config.Configurator;
import com.sg.widgets.registry.config.DataEditorConfigurator;
import com.sg.widgets.viewer.ViewerControl;

public class EditDeliverableDefinition extends AbstractNavigatorHandler {

	private static final String TITLE = Messages.get().EditDeliverableDefinition_0;

	@Override
	protected boolean nullSelectionContinue(IWorkbenchPart part,ViewerControl vc, Command command) {
		Shell shell = part.getSite().getShell();
		MessageUtil.showToast(shell, TITLE, Messages.get().EditDeliverableDefinition_1, SWT.ICON_WARNING);
		return super.nullSelectionContinue(part, vc, command);
	}
	
	@Override
	protected void execute(PrimaryObject selected, IWorkbenchPart part,ViewerControl currentViewerControl, Command command, Map<String, Object> parameters, IStructuredSelection selection) {
		Shell shell = part.getSite().getShell();

		Assert.isNotNull(currentViewerControl);

		selected.addEventListener(currentViewerControl);

		Configurator conf = Widgets.getEditorRegistry().getConfigurator(
				DeliverableDefinition.EDITOR);
		try {
			DataObjectDialog.openDialog(selected, (DataEditorConfigurator) conf,
					true, null, TITLE);
		} catch (Exception e) {
			MessageUtil.showToast(shell, TITLE, e.getMessage(), SWT.ICON_ERROR);
		}

		selected.removeEventListener(currentViewerControl);
	}

}
