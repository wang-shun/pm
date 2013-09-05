package com.sg.business.model;

import com.mobnut.commons.util.Utils;
import com.mobnut.db.model.PrimaryObject;

public class BulletinBoard extends PrimaryObject {

	/**
	 * ����/�ظ���
	 */
	public static final String F_PUBLISHER = "publisher";
	
	/**
	 * ����/�ظ�����
	 */
	public static final String F_PUBLISH_DATE = "publish_date";
	
	/**
	 * ������֯ID
	 */
	public static final String F_ORGANIZATION_ID = "organization_id";

	/**
	 * �����ֶ�
	 */
	public static final String F_DESC = "desc";

	/**
	 * ����
	 */
	public static final String F_CONTENT = "content";

	/**
	 * �ϼ��Ĺ���id
	 */
	public static final String F_PARENT_BULLETIN = "parent_id";

	/**
	 * ����,�ļ��б����ֶ�
	 */
	public static final String F_ATTACHMENT = "attachment";
	
	/**
	 * ���ر������ʾ����
	 * 
	 * @return String
	 */
	public String getHTMLLabel() {
		StringBuffer sb = new StringBuffer();


		// ��������
		String label = getLabel();
		label = Utils.getPlainText(label);
		label = Utils.getLimitLengthString(label, 20);
		sb.append("<b>" + label + "</b>");

		return sb.toString();
	}
}