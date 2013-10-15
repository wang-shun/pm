package com.sg.business.model;

import java.util.Date;
import java.util.List;

import com.mobnut.db.model.PrimaryObject;

public interface IWorksSummary {

	/**
	 * 获得某个时间段的实际工时
	 * @param start
	 * @param end
	 * @return
	 */
	double getWorksSummary(Date start, Date end);

	/**
	 * 获得某个日期的实际工时
	 * @param date
	 * @param cache 
	 * @return
	 */
	double getWorksSummaryOfDay(Date date);

	/**
	 * 获得某个日期的工作记录的工作
	 * @param dateCode
	 * @return
	 */
	List<PrimaryObject[]> getWorkOfWorksSummaryOfDateCode(String userid,Date date);

}
