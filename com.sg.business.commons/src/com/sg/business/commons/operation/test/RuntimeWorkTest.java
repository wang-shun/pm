package com.sg.business.commons.operation.test;

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;

import com.mobnut.db.model.IContext;
import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.Deliverable;
import com.sg.business.model.Document;
import com.sg.business.model.Work;
import com.sg.widgets.part.CurrentAccountContext;

public class RuntimeWorkTest extends PropertyTester {

	private static final String PROPERTY_ACTION = "action"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (receiver instanceof Work) {
			Work work = (Work) receiver;
			if (property.equals(PROPERTY_ACTION)) {
				if ("createChildWork".equals(args[0])) { //$NON-NLS-1$
					return work.canCreateChildWork(new CurrentAccountContext());
				} else if ("createDeliverable".equals(args[0])) { //$NON-NLS-1$
					return work
							.canCreateDeliverable(new CurrentAccountContext());
				} else if ("reassignment".equals(args[0])) { //$NON-NLS-1$
					return work.canReassignment(new CurrentAccountContext());
				} else if ("modify".equals(args[0])) { //$NON-NLS-1$
					return work.canEdit(new CurrentAccountContext());
				} else if ("workrecord".equals(args[0])) { //$NON-NLS-1$
					return work.canEditWorkRecord(new CurrentAccountContext());
				} else if ("openproject".equals(args[0])) { //$NON-NLS-1$
					return work.getProjectId() != null;
				} else if ("delete".equals(args[0])) { //$NON-NLS-1$
					return work.canDelete(new CurrentAccountContext());
				}else if ("opendeliverable".equals(args[0])) { //$NON-NLS-1$
					List<PrimaryObject> deliverable = work.getDeliverable();
					return deliverable != null && deliverable.size() > 0;
				}

				// else if ("wfstart".equals(args[0])) {
				// return work.canWorkflowStart(new CurrentAccountContext());
				// }else if ("wffinish".equals(args[0])) {
				// return work.canWorkflowFinish(new CurrentAccountContext());
				// }else if ("wfcancel".equals(args[0])) {
				// return work.canWorkflowCancel(new CurrentAccountContext());
				// }
			}

		} else if (receiver instanceof Deliverable) {
			Deliverable deliverable = (Deliverable) receiver;
			if ("editDeliverable".equals(args[0])) { //$NON-NLS-1$
				return deliverable.canEdit(new CurrentAccountContext());
			} else if ("deleteDeliverable".equals(args[0])) { //$NON-NLS-1$
				return deliverable.canDelete(new CurrentAccountContext());
			} else if ("lock".equals(args[0])) { //$NON-NLS-1$
				IContext context = new CurrentAccountContext();
				if (deliverable.canEdit(context)) {
					Document document = deliverable.getDocument();
					return document.canLock(context);
				} else {
					return false;
				}
			} else if ("unlock".equals(args[0])) { //$NON-NLS-1$
				Document document = deliverable.getDocument();
				return document.canUnLock(new CurrentAccountContext());
			}
		}
		return true;
	}
}
