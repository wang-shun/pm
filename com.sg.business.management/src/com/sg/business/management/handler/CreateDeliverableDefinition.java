package com.sg.business.management.handler;

import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.AbstractWork;
import com.sg.business.model.Deliverable;
import com.sg.business.resource.nls.Messages;
import com.sg.widgets.MessageUtil;
import com.sg.widgets.Widgets;
import com.sg.widgets.command.AbstractNavigatorHandler;
import com.sg.widgets.part.editor.DataObjectDialog;
import com.sg.widgets.part.view.NavigatorPart;
import com.sg.widgets.registry.config.Configurator;
import com.sg.widgets.registry.config.DataEditorConfigurator;
import com.sg.widgets.viewer.ViewerControl;

public class CreateDeliverableDefinition extends AbstractNavigatorHandler {

	@Override
	protected boolean nullSelectionContinue(IWorkbenchPart part,
			ViewerControl vc, Command command) {
		MessageUtil.showToast(Messages.get().CreateDeliverableDefinition_0, SWT.ICON_WARNING);
		return super.nullSelectionContinue(part, vc, command);
	}

	@Override
	protected void execute(PrimaryObject selected, IWorkbenchPart part,
			ViewerControl currentViewerControl, Command command, Map<String, Object> parameters, IStructuredSelection selection) {

		Shell shell = part.getSite().getShell();

		PrimaryObject po = ((AbstractWork) selected)
				.makeDeliverableDefinition(Deliverable.TYPE_OUTPUT);
		Assert.isNotNull(currentViewerControl);

		// 以下两句很重要，使树currentViewerControl够侦听到保存事件， 更新树上的节点
		// 1. 设置父并不涉及到数据库，主要是维护模型的父子关系，同时ViewerControl也需要父来处理事件响应
		po.setParentPrimaryObject(selected);
		// 2. 侦听po的事件作出合适的响应
		po.addEventListener(currentViewerControl);

		// 使用编辑器打开编辑工作定义
		Configurator conf = Widgets.getEditorRegistry().getConfigurator(
				po.getDefaultEditorId());
		try {
			DataObjectDialog.openDialog(po, (DataEditorConfigurator) conf,
					true, null, Messages.get().CreateDeliverableDefinition_1 + po.getTypeName());
		} catch (Exception e) {
			MessageUtil.showToast(shell, Messages.get().CreateDeliverableDefinition_2 + po.getTypeName(),
					e.getMessage(), SWT.ICON_ERROR);
		}

		// 3. 处理完成后，释放侦听器
		po.removeEventListener(currentViewerControl);

		// 4. 刷新当前页面的文档模板视图
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		NavigatorPart np = (NavigatorPart) page
				.findView("management.documentdefinition"); //$NON-NLS-1$
		if (np != null) {
			np.reloadMaster();
		}
	}

}
