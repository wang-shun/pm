package com.sg.business.model.commonlabel;

import java.util.Date;

import com.mobnut.commons.util.Utils;
import com.mobnut.db.model.ModelService;
import com.sg.business.model.BulletinBoard;
import com.sg.business.model.Organization;
import com.sg.business.model.toolkit.UserToolkit;
import com.sg.widgets.commons.labelprovider.CommonHTMLLabel;

public class BulletinBoardCommonHTMLLable extends CommonHTMLLabel {

	private BulletinBoard bulletinBoard;

	public BulletinBoardCommonHTMLLable(BulletinBoard bulletinBoard) {
		this.bulletinBoard = bulletinBoard;
	}

	@Override
	public String getHTML() {
		StringBuffer sb = new StringBuffer();
		// 设置发布人
		String publisher = UserToolkit.getUserById(bulletinBoard.getPublisher())
				.getUsername();

		// 设置标题
		String label = bulletinBoard.getLabel();
		label = Utils.getPlainText(label);
		label = Utils.getLimitLengthString(label, 20);

		// 设置内容
		String content = bulletinBoard.getContent();
		content = Utils.getPlainText(content);
		content = Utils.getLimitLengthString(content, 40);

		Date date = bulletinBoard.getPublishDate();
		String publishDate = String.format(Utils.FORMATE_DATE_COMPACT_SASH,
				date);

		// 设置发布部门
		String org = ((Organization) ModelService.createModelObject(
				Organization.class, bulletinBoard.getOrganizationId())).getDesc();

		// 显示标题和内容
		sb.append("<span style='FONT-FAMILY:微软雅黑;font-size:9pt'>"); //$NON-NLS-1$

		sb.append("<span style='float:right;padding-right:4px;'><small>"); //$NON-NLS-1$
		sb.append(publisher);
		sb.append("  "); //$NON-NLS-1$
		sb.append(publishDate);

		sb.append("</small></span>"); //$NON-NLS-1$

		sb.append(label);
		sb.append("</span>"); //$NON-NLS-1$

		sb.append("<br/><small style='color:#909090'>"); //$NON-NLS-1$

		sb.append("<span style='float:right;padding-right:4px;'>"); //$NON-NLS-1$
		sb.append(org);
		sb.append("</span>"); //$NON-NLS-1$

		sb.append(content);

		sb.append("</small>"); //$NON-NLS-1$

		return sb.toString();
	}

}
