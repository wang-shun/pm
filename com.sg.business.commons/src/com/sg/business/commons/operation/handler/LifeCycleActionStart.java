package com.sg.business.commons.operation.handler;

import java.util.List;

import com.mobnut.db.model.IContext;
import com.sg.business.model.ILifecycle;


public class LifeCycleActionStart extends AbstractLifecycleAction {

	@Override
	public List<Object[]> checkBeforeExecute(ILifecycle lc, IContext context)
			throws Exception {
		return lc.checkStartAction(context);
	}

	@Override
	public void execute(ILifecycle lc, IContext context) throws Exception {
		lc.doStart(context);		
	}

}
