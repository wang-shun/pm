package com.sg.business.commons.adminfunction;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.mobnut.admin.IFunctionExcutable;

public class CalendarSetting implements IFunctionExcutable {


	@Override
	public void run() {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		try {
			page.showView("commons.admin.calendar"); //$NON-NLS-1$
		} catch (PartInitException e) {
		}
	}

}
