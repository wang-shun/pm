package com.sg.business.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.mobnut.commons.util.Utils;
import com.mobnut.db.model.IContext;
import com.mobnut.db.model.PrimaryObject;
import com.sg.business.resource.nls.Messages;

/**
 * 日历设置
 * @author jinxitao
 *
 */
public class CalendarSetting extends PrimaryObject {

	/**
	 * 开始日期
	 */
	public static final String F_START_DATE = "startdate"; //$NON-NLS-1$
	/**
	 * 结束日期
	 */
	public static final String F_END_DATE = "enddate"; //$NON-NLS-1$
	/**
	 * 工作日
	 */
	public static final String F_WORKINGDAY = "workingday"; //$NON-NLS-1$
	/**
	 * 条件
	 */
	public static final String F_CONDITION = "condition"; //$NON-NLS-1$
	/**
	 * 操作符
	 */
	public static final String F_OPERATOR = "operator"; //$NON-NLS-1$
	/**
	 * 值
	 */
	public static final String F_VALUE = "value"; //$NON-NLS-1$
	/**
	 * 优先级
	 */
	public static final String F_SEQ = "seq"; //$NON-NLS-1$
	/**
	 * 每天工时（小时）
	 */
	public static final String F_WORKING_TIME = "worktime"; //$NON-NLS-1$

	/**
	 * 项目Id, 如果为空，该条目为系统日历
	 */
	public static final String F_PROJECT_ID = "project_id"; //$NON-NLS-1$

	/**
	 * 条件的类型，每月的日期
	 */
	public static final String CONDITON_DAY_OF_MONTH = Messages.get().CalendarSetting_6;

	/**
	 * 条件的类型，每周的第几日
	 */
	public static final String CONDITON_DAY_OF_WEEK = Messages.get().CalendarSetting_5;

	public static final String OPERATOR_EQ = Messages.get().CalendarSetting_4;

	public static final String OPERATOR_LT = Messages.get().CalendarSetting_3;

	public static final String OPERATOR_LE = Messages.get().CalendarSetting_2;

	public static final String OPERATOR_GT = Messages.get().CalendarSetting_1;

	public static final String OPERATOR_GE = Messages.get().CalendarSetting_0;
	
	private HashMap<String, Double> workingTimeMap;

	/**
	 * 返回开始日期
	 * @return Calendar
	 */
	private Calendar getStartDate() {
		Date date = (Date) getValue(F_START_DATE);
		return Utils.getDayBegin(date);

	}

	/**
	 * 返回结束日期
	 * @return Calendar
	 */
	private Calendar getEndDate() {
		Date date = (Date) getValue(F_END_DATE);
		return Utils.getDayEnd(date);

	}

	/**
	 * 判断是否为工作日
	 * @return boolean
	 */
	private boolean isWorkingDay() {
		return Boolean.TRUE.equals(getValue(F_WORKINGDAY));
	}

	/**
	 * 返回该工作日的工作时间
	 * @return
	 */
	private Double getWorkingTime() {
		if (!isWorkingDay()) {
			return new Double(0d);
		}
		Double d = (Double) getValue(F_WORKING_TIME);
		if (d == null) {
			return new Double(0d);
		} else {
			return d;
		}
	}

	/**
	 * 计算
	 * @return String
	 */
	private String getOperator() {
		return (String) getValue(F_OPERATOR);
	}

	/**
	 * 判断是否为周条件
	 * @return
	 */
	private boolean isWeekCondition() {
		return CONDITON_DAY_OF_WEEK.equals(getValue(F_CONDITION));
	}

	/**
	 * 判断是否为工作日条件
	 * @return
	 */
	private boolean isDateCondition() {
		return CONDITON_DAY_OF_MONTH.equals(getValue(F_CONDITION));
	}

	/**
	 * 判断是否设置了日历条件
	 * @return boolean
	 */
	private boolean hasCondition() {
		return !Utils.isNullOrEmptyString(getValue(F_CONDITION))
				&& !Utils.isNullOrEmptyString(getValue(F_OPERATOR))
				&& !Utils.isNullOrEmptyString(getValue(F_VALUE));
	}


	/**
	 * 返回日历设置记录值
	 * @return String
	 */
	private String getConditionValue() {
		String value = (String) getValue(F_VALUE);
		return value.replaceAll("，", ","); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * 保存
	 */
	@Override
	public boolean doSave(IContext context) throws Exception {
		workingTimeMap = null;
		return super.doSave(context);
	}
	
	/**
	 * 返回日历工作时间
	 * @return
	 */
	public Map<String, Double> getCalendarWorkingTime() {
		if(workingTimeMap == null){
			initTimeMap();
		}
		return workingTimeMap;
	}

	private void initTimeMap() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd"); //$NON-NLS-1$
		Calendar start = getStartDate();
		Calendar end = getEndDate();

		workingTimeMap = new HashMap<String, Double>();
		Calendar current = start;
		while (current.before(end)) {
			String key = sdf.format(current.getTime());
			Double workingTime = getWorkingTime(current);
			if (workingTime != null) {
				workingTimeMap.put(key, workingTime);
			}
			current.set(Calendar.DATE, current.get(Calendar.DATE) + 1);
		}
	}

	private Double getWorkingTime(Calendar cal) {
		if (!hasCondition()) {
			// 如果无条件，直接获取工时
			return getWorkingTime();
		}

		String value = (String) getValue(F_VALUE);

		if (isWeekCondition()) {
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			if (OPERATOR_EQ.equals(getOperator())) {
				// 如果取等于，可以使用逗号分隔的值
				String[] values = getConditionValue().split(","); //$NON-NLS-1$
				for (int i = 0; i < values.length; i++) {
					try {
						int v = Integer.parseInt(values[i]);
						v = (v == 7) ? 1 : (v + 1);

						if (dayOfWeek == v) {
							// 该日期包含在条件中
							return getWorkingTime();
						}
					} catch (Exception e) {
					}
				}
			} else {
				try {
					int v = Integer.parseInt(value);
					v = (v == 7) ? 1 : (v + 1);

					if (OPERATOR_GE.equals(getOperator())) {
						if (dayOfWeek >= v) {
							return getWorkingTime();
						}
					} else if (OPERATOR_GT.equals(getOperator())) {
						if (dayOfWeek > v) {
							return getWorkingTime();
						}
					} else if (OPERATOR_LE.equals(getOperator())) {
						if (dayOfWeek <= v) {
							return getWorkingTime();
						}
					} else if (OPERATOR_LT.equals(getOperator())) {
						if (dayOfWeek < v) {
							return getWorkingTime();
						}
					}
				} catch (Exception e) {
				}
			}
		} else if (isDateCondition()) {
			int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

			if (OPERATOR_EQ.equals(getOperator())) {
				// 如果取等于，可以使用逗号分隔的值
				String[] values = getConditionValue().split(","); //$NON-NLS-1$
				for (int i = 0; i < values.length; i++) {
					try {
						int v = Integer.parseInt(values[i]);
						if (dayOfMonth == v) {
							// 该日期包含在条件中
							return getWorkingTime();
						}
					} catch (Exception e) {
					}
				}
			} else {
				try {
					int v = Integer.parseInt(value);
					if (OPERATOR_GE.equals(getOperator())) {
						if (dayOfMonth >= v) {
							return getWorkingTime();
						}
					} else if (OPERATOR_GT.equals(getOperator())) {
						if (dayOfMonth > v) {
							return getWorkingTime();
						}
					} else if (OPERATOR_LE.equals(getOperator())) {
						if (dayOfMonth <= v) {
							return getWorkingTime();
						}
					} else if (OPERATOR_LT.equals(getOperator())) {
						if (dayOfMonth < v) {
							return getWorkingTime();
						}
					}
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	/**
	 * 按格式返回工作日
	 * @param yyyyMMddDate
	 * @return
	 */
	public Double getCalendarWorkingTime(String yyyyMMddDate) {
		Map<String, Double> map = getCalendarWorkingTime();
		return map.get(yyyyMMddDate);
	}
	
	@Override
	public boolean canEdit(IContext context) {
		// TODO Auto-generated method stub
		return super.canEdit(context);
	}
	
	@Override
	public boolean canRead(IContext context) {
		// TODO Auto-generated method stub
		return super.canRead(context);
	}
	
	@Override
	public boolean canDelete(IContext context) {
		// TODO Auto-generated method stub
		return super.canDelete(context);
	}
	
	
}
