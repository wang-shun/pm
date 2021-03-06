package com.sg.business.commons.column.labelprovider;

import org.eclipse.swt.graphics.Image;

import com.sg.business.model.ProjectRole;
import com.sg.business.model.Work;
import com.sg.widgets.commons.labelprovider.ConfiguratorColumnLabelProvider;

public class ChargerRoleDefOfWorkLabelProvider extends ConfiguratorColumnLabelProvider {

	@Override
	public String getText(Object element) {
		Work work = (Work) element;
		ProjectRole chargerRoleDef = work.getChargerRoleDefinition(ProjectRole.class);
		if(chargerRoleDef!=null){
			return chargerRoleDef.getLabel();
		}else{
			return ""; //$NON-NLS-1$
		}
	}
	
	@Override
	public Image getImage(Object element) {
		Work work = (Work) element;
		ProjectRole chargerRoleDef = work.getChargerRoleDefinition(ProjectRole.class);
		if(chargerRoleDef!=null){
			return chargerRoleDef.getImage();
		}else{
			return null;
		}
	}

}
