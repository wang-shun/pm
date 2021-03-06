package com.sg.business.commons.operation.link;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public class LaunchWorkLinkAdapter implements SelectionListener {

	@Override
	public void widgetSelected(SelectionEvent event) {
		if (event.detail == RWT.HYPERLINK) {
			try {
				String action = event.text.substring(
						event.text.lastIndexOf("/") + 1, //$NON-NLS-1$
						event.text.indexOf("@")); //$NON-NLS-1$
				String _data = event.text
						.substring(event.text.indexOf("@") + 1); //$NON-NLS-1$
				if ("launchwork".equals(action)) { //$NON-NLS-1$
					doLaunch(_data, event);
				}
			} catch (Exception e) {
			}
		}
	}

	private void doLaunch(String _data, SelectionEvent event) {
//		WorkDefinition workd = ModelService.createModelObject(WorkDefinition.class,
//				new ObjectId(_data));
//		LaunchWorkWizard.OPEN(workd);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {

	}

}
