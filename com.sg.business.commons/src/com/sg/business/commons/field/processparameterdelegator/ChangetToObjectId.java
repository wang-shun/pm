package com.sg.business.commons.field.processparameterdelegator;

import org.bson.types.ObjectId;

import com.mobnut.db.model.PrimaryObject;
import com.sg.bpm.workflow.taskform.IProcessParameterDelegator;

public class ChangetToObjectId implements IProcessParameterDelegator {

	public ChangetToObjectId() {
	}

	@Override
	public Object getValue(String processParameter, String taskDatakey,
			PrimaryObject taskFormData) {
		Object value = taskFormData.getValue(taskDatakey);
		if (value instanceof ObjectId) {
			ObjectId objectId = (ObjectId) value;
			return objectId.toString();
		}
		return null;
	}

}
