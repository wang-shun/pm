package com.sg.business.model.bpmservice;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mobnut.db.model.IContext;
import com.mobnut.db.model.PrimaryObject;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.sg.bpm.service.task.ServiceProvider;
import com.sg.bpm.workflow.utils.WorkflowUtils;
import com.sg.business.model.Message;
import com.sg.business.model.toolkit.MessageToolkit;
import com.sg.business.resource.nls.Messages;

public abstract class AbstractMessageService extends ServiceProvider {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, Object> run(Object parameter) {

		HashMap<String, Object> result = new HashMap<String, Object>();
		Object content = getInputValue("content"); //$NON-NLS-1$
		if (content instanceof String) {
			String jsonContent = (String) content;

			try {
				DBObject processData = WorkflowUtils
						.getProcessInfoFromJSON(jsonContent);
				String processId = (String) processData.get("processId"); //$NON-NLS-1$
				String processName = (String) processData.get("processName"); //$NON-NLS-1$

				String messageTitle = getMessageTitle();
				String messageContent = getMessageContent();
				String editId = getEditorId();
				PrimaryObject target = getTarget();

				List<String> receivers = getReceiverList();
				if (receivers != null) {
					BasicDBList receiverList = new BasicDBList();
					for (String receiver : receivers) {
						receiverList.add(receiver);
					}

					if (editId != null) {
						sendMessage(messageTitle, messageContent,
								(List) receiverList, target, editId,
								new BPMServiceContext(processName, processId));
					} else {
						sendMessage((List) receiverList, messageTitle,
								messageContent, new BPMServiceContext(
										processName, processId));
					}
				} else {
					result.put("returnCode", "ERROR"); //$NON-NLS-1$ //$NON-NLS-2$
					result.put("returnMessage", Messages.get().AbstractMessageService_0); //$NON-NLS-1$
				}

			} catch (Exception e) {
				result.put("returnCode", "ERROR"); //$NON-NLS-1$ //$NON-NLS-2$
				result.put("returnMessage", e.getMessage()); //$NON-NLS-1$
			}
		}
		return result;
	}

	public void sendMessage(String messageTitle, String messageContent,
			List<String> receiverList, PrimaryObject target, String editId,
			IContext context) {

		Map<String, Message> msgList = new HashMap<String, Message>();
		
		for (String userId : receiverList) {
			msgList.put(userId, null);
			MessageToolkit.appendNoCommitMessage(msgList, userId, messageTitle,
					messageContent, target, editId, context);
		}
		Iterator<Message> iter = msgList.values().iterator();
		while (iter.hasNext()) {
			Message message = iter.next();
			try {
				message.doSave(context);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void sendMessage(List<String> recievers, String title,
			String content, IContext context) {
		if (recievers != null) {
			Message message = MessageToolkit.makeMessage(recievers, title,
					context.getAccountInfo().getConsignerId(), content);
			try {
				message.doSave(context);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public abstract String getMessageTitle();

	public abstract String getMessageContent();

	public abstract List<String> getReceiverList();

	public abstract String getEditorId();

	public abstract PrimaryObject getTarget();
}
