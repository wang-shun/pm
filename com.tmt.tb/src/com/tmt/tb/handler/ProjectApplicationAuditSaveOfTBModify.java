package com.tmt.tb.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.sg.business.model.TaskForm;
import com.sg.business.model.Work;
import com.sg.widgets.part.editor.IDataObjectDialogCallback;
import com.sg.widgets.part.editor.PrimaryObjectEditorInput;

public class ProjectApplicationAuditSaveOfTBModify implements
		IDataObjectDialogCallback {

	public ProjectApplicationAuditSaveOfTBModify() {
	}

	@Override
	public boolean doSaveBefore(PrimaryObjectEditorInput input,
			IProgressMonitor monitor, String operation) throws Exception {
		return true;
	}

	@SuppressWarnings("unused")
	@Override
	public boolean doSaveAfter(PrimaryObjectEditorInput input,
			IProgressMonitor monitor, String operation) throws Exception {
		TaskForm taskform = (TaskForm) input.getData();
		Object value = taskform.getValue("prj_manager"); //$NON-NLS-1$
		if (value instanceof String) {
			String userid = (String) value;
			List<String> userList = new ArrayList<String>();
			userList.add(userid);
			Work work = taskform.getWork();
			work.doAddParticipateList(userList);
			String type = (String) taskform.getValue("projecttype");
		}
		return true;
	}

	@Override
	public boolean okPressed() {
		return false;
	}

	@Override
	public void cancelPressed() {

	}

	@Override
	public boolean needSave() {
		return true;
	}

}
