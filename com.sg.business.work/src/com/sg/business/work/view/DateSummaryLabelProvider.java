package com.sg.business.work.view;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.IWorksSummary;
import com.sg.widgets.Widgets;

public class DateSummaryLabelProvider extends ColumnLabelProvider {

	private int currentYear;
	private int month;
	private int dayOfMonth;
	private double workingHours;

	public DateSummaryLabelProvider(int currentYear, int month, int dayOfMonth) {
		this.currentYear = currentYear;
		this.month = month;

		this.dayOfMonth = dayOfMonth;
	}

	private double getSummary(Object element, boolean isPlan) {

		IWorksSummary ws = ((PrimaryObject) element)
				.getAdapter(IWorksSummary.class);
		if (ws == null) {
			return 0d;
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, currentYear);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DATE, dayOfMonth);
		Date date = cal.getTime();

		double summary = isPlan?ws.getWorksAllocateSummaryOfDay(date):ws.getWorkPerformenceSummaryOfDay(date);
		return summary;
	}

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();

		double summary = getSummary(element,isToday());
		if (summary == 0) {
			cell.setText("");
		} else {
			DecimalFormat df = new DecimalFormat("#########");
			cell.setText(df.format(summary));
		}
		if (summary > workingHours) {
			cell.setForeground(Widgets.getColor(cell.getControl().getDisplay(),
					255, 0, 0));
		} else {

		}

		if (workingHours == 0) {
			cell.setBackground(Widgets.getColor(cell.getControl().getDisplay(),
					225, 225, 225));
		} else if (isToday()) {
			cell.setBackground(Widgets.getColor(cell.getControl().getDisplay(),
					0xe2, 0xf0, 0xb6));
		}
	}

	public boolean isToday() {
		Calendar cal = Calendar.getInstance();
		return (cal.get(Calendar.YEAR) == currentYear)
				&& (cal.get(Calendar.MONTH) == month)
				&& (cal.get(Calendar.DATE) == dayOfMonth);
	}

	public void setWorkingHours(double hours) {
		workingHours = hours;
	}

}
