package com.tmt.xt.nls;

import java.util.Locale;

import org.eclipse.swt.widgets.Display;

import com.mobnut.commons.util.NLS2;

public class Messages {
	private static final String BUNDLE_NAME = "com.tmt.xt.nls.messages"; //$NON-NLS-1$
	public String AddProductItem_1;
	public String SupportServiceOfXT_0;
	public String SupportServiceOfXT_4;
	public String SupportServiceOfXT_5;
	public String SupportServiceOfXT_6;
	public String SupportServiceOfXT_7;
	public String SupportServiceOfXT_9;

	public static Messages get(Display display) {
		return NLS2.getMessage(BUNDLE_NAME, Messages.class, display);
	}

	public static Messages get(Locale local) {
		return NLS2.getMessage(BUNDLE_NAME, Messages.class, local);
	}
	
	public static Messages get() {
		return NLS2.getMessage(BUNDLE_NAME, Messages.class);
	}

}