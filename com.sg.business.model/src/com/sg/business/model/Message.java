package com.sg.business.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.types.BasicBSONList;

import com.mobnut.commons.util.Utils;
import com.mobnut.commons.util.file.FileUtil;
import com.mobnut.db.model.IContext;
import com.mobnut.db.model.ModelService;
import com.mobnut.db.model.PrimaryObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sg.business.resource.BusinessResource;
import com.sg.widgets.part.CurrentAccountContext;

public class Message extends PrimaryObject {

	/**
	 * 上级的消息id
	 */
	public static final String F_PARENT_MESSAGE = "parent_id";

	/**
	 * 发送者字段，userid
	 */
	public static final String F_SENDER = "sender";

	/**
	 * 接收者
	 */
	public static final String F_RECIEVER = "reciever";

	/**
	 * 标题字段
	 */
	public static final String F_DESC = "desc";

	/**
	 * 内容
	 */
	public static final String F_CONTENT = "content";

	/**
	 * 发送日期
	 */
	public static final String F_SENDDATE = "senddate";

	/**
	 * 接收日期
	 */
	public static final String F_RECIEVEDDATE = "recievedate";

	/**
	 * 已读标志,DBObject类型字段,保存了用户ID，是否已读<br/>
	 * {"zhonghua": true,"zhansan":true}
	 */
	public static final String F_MARK_READ = "markread";

	/**
	 * 重要度
	 */
	public static final String F_IMPORTANCE = "importance";

	/**
	 * 电子邮件提醒发送日期
	 */
	public static final String F_EMAIL_NOTICE_DATE = "emailnoticedate";

	/**
	 * 目标列表 {{targetid=ObjectId(""),targetclass="com.sg.business.model.Work",
	 * targeteditor="work.editor"},{}}
	 */
	public static final String F_TARGETS = "targets";

	/**
	 * 目标对象
	 */
	public static final String SF_TARGET = "targetid";

	/**
	 * 目标类
	 */
	public static final String SF_TARGET_CLASS = "targetclass";

	/**
	 * 目标编辑器
	 */
	public static final String SF_TARGET_EDITOR = "targeteditor";

	/**
	 * 是否可以修改
	 */
	public static final String SF_TARGET_EDITABLE = "targeteditable";

	/**
	 * 附件,文件列表型字段
	 */
	public static final String F_ATTACHMENT = "attachment";

	/**
	 * 回复编辑器ID
	 */
	public static final String EDITOR_REPLY = "message.editor.reply";

	/**
	 * 新消息编辑器
	 */
	public static final String EDITOR_SEND = "message.editor.create";
	
	/**
	 * 消息查看编辑器
	 */
	public static final String EDITOR_VIEW = "message.editor.view";


	public Message makeReply() {
		Message reply = ModelService.createModelObject(Message.class);
		reply.setValue(F_PARENT_MESSAGE, get_id());
		reply.setValue(F_RECIEVER, getValue(F_SENDER));
		reply.setValue(F_DESC, "RE:" + getDesc());
		return reply;
	}

	public void doMarkRead(IContext context, Boolean isRead) throws Exception {
		Object markReadData = getValue(F_MARK_READ);
		if (!(markReadData instanceof DBObject)) {
			markReadData = new BasicDBObject();
		}
		((DBObject) markReadData).put(context.getAccountInfo().getUserId(),
				isRead);
		setValue(F_MARK_READ, markReadData);
		setValue(F_RECIEVEDDATE, new Date());
		doSave(context);
	}

	public boolean isRead(IContext context) {
		Object markReadData = getValue(F_MARK_READ);
		if (!(markReadData instanceof DBObject)) {
			return false;
		}
		String userId = context.getAccountInfo().getUserId();
		return Boolean.TRUE.equals(((DBObject) markReadData).get(userId));
	}

	/*
	 * @Override public String getHTMLLabel() { boolean isRead = isRead(new
	 * CurrentAccountContext()); StringBuffer sb = new StringBuffer(); String
	 * imageUrl = "<img src='" +(isRead?getImageURLForOpen():getImageURL()) +
	 * "' style='float:left;padding:2px' width='24' height='24' />"; String
	 * label = getLabel();
	 * 
	 * String senderId = (String) getValue(F_SENDER); User sender =
	 * User.getUserById(senderId);
	 * 
	 * SimpleDateFormat sdf = new SimpleDateFormat(
	 * Utils.SDF_DATE_COMPACT_SASH); Date date = (Date) getValue(F_SENDDATE);
	 * String sendDate = sdf.format(date);
	 * 
	 * sb.append(imageUrl); if (isRead) { sb.append(label); } else {
	 * sb.append("<b>"); sb.append(label); sb.append("</b>"); }
	 * sb.append("<br/>"); sb.append("发件人:" + sender + " " + sendDate);
	 * 
	 * return sb.toString(); }
	 */

	public String getImageURLForReply() {
		return FileUtil.getImageURL(BusinessResource.IMAGE_MESSAGE_REPLY_24,
				BusinessResource.PLUGIN_ID, BusinessResource.IMAGE_FOLDER);
	}

	public String getImageURLForOpen() {
		return FileUtil.getImageURL(BusinessResource.IMAGE_MESSAGE_OPEN_24,
				BusinessResource.PLUGIN_ID, BusinessResource.IMAGE_FOLDER);
	}

	public String getImageURL() {
		return FileUtil.getImageURL(BusinessResource.IMAGE_MESSAGE_24,
				BusinessResource.PLUGIN_ID, BusinessResource.IMAGE_FOLDER);
	}

	public String getHTMLLabel() {
		boolean isReply = isReply();
		boolean isRead = isRead(new CurrentAccountContext());
		StringBuffer sb = new StringBuffer();
		
		//添加日期
		SimpleDateFormat sdf = new SimpleDateFormat(Utils.SDF_DATE_COMPACT_SASH);
		Date date = (Date) getValue(F_SENDDATE);
		String sendDate = sdf.format(date);
		sb.append("<span style='float:right;padding-right:4px'>");
		sb.append(sendDate);
		sb.append("</span>");
		
		//添加图标
		String imageUrl = null;
		if (isRead) {
			imageUrl = "<img src='"
					+ getImageURLForOpen()
					+ "' style='float:left;padding:6px' width='24' height='24' />";
		} else {
			imageUrl = "<img src='"
					+ (isReply ? getImageURLForReply() : getImageURL())
					+ "' style='float:left;padding:6px' width='24' height='24' />";
		}
		sb.append(imageUrl);

		//添加主题
		String label = getLabel();
		label = Utils.getPlainText(label);
		label = Utils.getLimitLengthString(label, 20);
		if (isRead) {
			sb.append(label);
		} else {
			sb.append("<b>" + label + "</b>");
		}

		sb.append("<br/>");
		
		String senderId = (String) getValue(F_SENDER);
		User sender = User.getUserById(senderId);
		sb.append("发件人:" + sender );
		sb.append("  ");
		String recieverLabel = getRecieverLabel();
		sb.append("收件人:" + recieverLabel);
		return sb.toString();
	}

	public String getRecieverLabel() {
		String result = "";
		Object value = getValue(F_RECIEVER);
		if (value instanceof BasicBSONList) {
			BasicBSONList recieverList = (BasicBSONList) value;
			if (recieverList.size() > 0) {
				String userId = (String) recieverList.get(0);
				User user = User.getUserById(userId);
				result = user.getLabel();
				if (recieverList.size() > 1) {
					result += " ...";
				}
			}
		}

		return result;
	}

	public boolean isReply() {
		return getValue(F_PARENT_MESSAGE) != null;
	}

	@Override
	public void doInsert(IContext context) throws Exception {
		setValue(F_SENDDATE, new Date());
		setValue(F_SENDER, context.getAccountInfo().getUserId());
		super.doInsert(context);
	}

	/**
	 * 向消息增加目标导航数据
	 * 
	 * @param target
	 */
	public void appendTargets(PrimaryObject target, String editorId,
			boolean editable) {
		Object value = getValue(F_TARGETS);
		if (!(value instanceof BasicBSONList)) {
			value = new BasicDBList();
		}
		BasicBSONList targets = (BasicBSONList) value;

		DBObject newElement = new BasicDBObject();
		newElement.put(SF_TARGET, target.get_id());
		newElement.put(SF_TARGET_CLASS, target.getClass().getName());
		newElement.put(SF_TARGET_EDITOR, editorId);
		newElement.put(SF_TARGET_EDITABLE, new Boolean(editable));
		if (targets.contains(newElement)) {
			return;
		}
		targets.add(newElement);
		setValue(F_TARGETS, targets);
	}
}
