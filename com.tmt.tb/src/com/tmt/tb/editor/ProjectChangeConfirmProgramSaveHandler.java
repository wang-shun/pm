package com.tmt.tb.editor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.sg.business.model.TaskForm;
import com.sg.business.model.Work;
import com.sg.widgets.commons.model.IEditorSaveHandler;
import com.sg.widgets.part.editor.PrimaryObjectEditorInput;

public class ProjectChangeConfirmProgramSaveHandler implements IEditorSaveHandler {

	public ProjectChangeConfirmProgramSaveHandler() {
	}

	@Override
	public boolean doSaveBefore(PrimaryObjectEditorInput input,
			IProgressMonitor monitor, String operation) throws Exception {
		
		TaskForm taskform = (TaskForm) input.getData();
		String choice = (String) taskform.getValue("choice");
		taskform.setProcessInputValue("choice", choice);
		if("不通过".equals(choice)){
			return true;
		}
		Object ecn = taskform.getValue("ecn");
		if(ecn instanceof List<?>){
			List<?> ecnlist = (List<?>) ecn;
			String var;
			for(Object obj:ecnlist){
				Work work=(Work)obj;
				
				Boolean noskip = (Boolean) work.getValue(Work.F_INTERNAL_DEFAULTSELECTED);
				if(Boolean.TRUE.equals(noskip)){
					Object value = work.getValue(Work.F_PLAN_START);
					if(value==null){
						throw new Exception("请设置计划开始时间。\n"+work);
					}
					value = work.getValue(Work.F_PLAN_FINISH);
					if(value==null){
						throw new Exception("请设置计划完成时间。\n"+work);
					}
					value = work.getValue(Work.F_CHARGER);
					if(value==null){
						throw new Exception("请设置负责人。\n"+work);
					}
					var = (String) work.getValue("chargerpara");
					taskform.setProcessInputValue(var, value);
					var=(String) work.getValue("noskippara");
					taskform.setProcessInputValue(var, "是");

				}else{
					var=(String) work.getValue("noskippara");
					taskform.setProcessInputValue(var, "否");
				}
				
			}
			
		}
		return true;
	}

	@Override
	public boolean doSaveAfter(PrimaryObjectEditorInput input,
			IProgressMonitor monitor, String operation) throws Exception {
		return true;
	}

}
