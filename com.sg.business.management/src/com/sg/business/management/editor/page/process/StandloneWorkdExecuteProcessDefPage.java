package com.sg.business.management.editor.page.process;

import com.sg.business.model.WorkDefinition;


public class StandloneWorkdExecuteProcessDefPage extends AbstractWorkdProcessSettingPage{

	@Override
	protected String getProcessKey() {
		return WorkDefinition.F_WF_EXECUTE;
	}
		

}