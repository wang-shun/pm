package com.sg.business.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Image;

import com.mobnut.commons.util.Utils;
import com.mobnut.db.model.IContext;
import com.mobnut.db.model.PrimaryObject;
import com.sg.business.resource.BusinessResource;

public class DocumentDefinition extends PrimaryObject {

	public static final String F_ORGANIZATION_ID = "organization_id";

	@Override
	public Image getImage() {
		return BusinessResource
				.getImage(BusinessResource.IMAGE_DOCUMENT_DEF_16);
	}

	@Override
	public void doRemove(IContext context) throws Exception {
		// ɾ���ĵ�ģ��ʱ��Ҫ�����ĵ��Ƿ񱻽����������ã����������ɾ��
		List<PrimaryObject> projectTemplates = getReferenceProjectTemplates();
		if (projectTemplates.size() > 0) {

			throw new Exception(
					"�ĵ�ģ�屻��Ŀģ��Ĺ������������ã�������Ŀģ��ɾ�����ø��ĵ�ģ��Ľ����ﶨ�����ɾ���ĵ�ģ�塣\n��Ŀģ��:\n"
							+ Utils.getLimitLengthString(
									projectTemplates.toString(), 80));
		}

		List<PrimaryObject> genericWorkDefinitions = getReferenceGenericAndStandloneWorkDefinition();
		if (genericWorkDefinitions.size() > 0) {

			throw new Exception(
					"�ĵ�ģ�屻ͨ�ù���������߶�����������Ľ��������ã�����ɾ�����ø��ĵ�ģ��Ľ����ﶨ�����ɾ���ĵ�ģ�塣\n��������:\n"
							+ Utils.getLimitLengthString(
									genericWorkDefinitions.toString(), 80));
		}

		super.doRemove(context);
	}

	/**
	 * ���ʹ�ø��ĵ���ͨ�ù�������
	 * 
	 * @return
	 */
	public List<PrimaryObject> getReferenceGenericAndStandloneWorkDefinition() {
		List<PrimaryObject> deliverableDefinitions = getReferenceDeliverableDefinitions();
		List<PrimaryObject> result = new ArrayList<PrimaryObject>();
		for (PrimaryObject primaryObject : deliverableDefinitions) {
			DeliverableDefinition deliverableDefinition = (DeliverableDefinition) primaryObject;
			WorkDefinition workDefinition = deliverableDefinition
					.getWorkDefinition();
			Assert.isNotNull(workDefinition);

			int type = workDefinition.getWorkDefinitionType();
			if (type == WorkDefinition.WORK_TYPE_GENERIC
					|| type == WorkDefinition.WORK_TYPE_STANDLONE) {
				WorkDefinition root = workDefinition.getRoot();
				if (!result.contains(root)) {
					result.add(root);
				}
			}
		}

		return result;
	}

	/**
	 * ���ʹ�ø��ĵ�ģ��Ľ����ﶨ��
	 * 
	 * @return
	 */
	public List<PrimaryObject> getReferenceDeliverableDefinitions() {
		return getRelationById(F__ID,
				DeliverableDefinition.F_DOCUMENT_DEFINITION_ID,
				DeliverableDefinition.class);
	}

	/**
	 * ���ʹ�ø��ĵ�ģ�����Ŀģ��
	 * 
	 * @return
	 */
	public List<PrimaryObject> getReferenceProjectTemplates() {
		List<PrimaryObject> deliverableDefinitions = getReferenceDeliverableDefinitions();
		List<PrimaryObject> result = new ArrayList<PrimaryObject>();
		for (PrimaryObject primaryObject : deliverableDefinitions) {
			DeliverableDefinition deliverableDefinition = (DeliverableDefinition) primaryObject;
			WorkDefinition workDefinition = deliverableDefinition
					.getWorkDefinition();
			Assert.isNotNull(workDefinition);

			int type = workDefinition.getWorkDefinitionType();
			if (type == WorkDefinition.WORK_TYPE_PROJECT) {
				ProjectTemplate projectTemplate = workDefinition
						.getProjectTemplate();
				if (!result.contains(projectTemplate)) {
					result.add(projectTemplate);
				}
			}
		}
		return result;
	}

}