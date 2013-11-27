package com.sg.business.finance.eai.sap;

import java.util.HashMap;
import java.util.Map;

import com.mobnut.commons.util.Utils;
import com.sap.mw.jco.IFunctionTemplate;
import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO;
import com.sap.mw.jco.JCO.Client;
import com.sap.mw.jco.JCO.FieldIterator;
import com.sap.mw.jco.JCO.Function;
import com.sap.mw.jco.JCO.ParameterList;
import com.sap.mw.jco.JCO.Table;
import com.sg.business.finance.FinanceActivator;
import com.sg.business.finance.SAPConnectionPool;

public class JCO_ZXFUN_PM_YFFY {

	private static final String REPOSITORY_NAME = "MYRepository";

	private static final String PARAMETER_COST_CENTER = "TABLE_IN";

	private static final String PARAMETER_MONTH = "PERIO";

	private static final String PARAMETER_YEAR = "GJAHR";

	private static final String FUNCTION_NAME = "ZXFUN_PM_YFFY";

	/**
	 * 
	 * @param orgCodeArray
	 *            ,公司代码
	 * @param costCodeArray
	 *            ,成本中心代码
	 * @param year
	 *            , 年
	 * @param month
	 *            ,月
	 * @param account
	 *            , 科目
	 * @param costElementArray 
	 * @throws Exception
	 */
	public Map<String, Map<String, Double>> getJSDZB(String[] orgCodeArray,
			String[] costCodeArray, String[] costElementArray, int year, int month, String[] account)
			throws Exception {

		Client client = FinanceActivator.getSAPClient();
		IRepository repository = JCO.createRepository(REPOSITORY_NAME,
				SAPConnectionPool.POOL_NAME);

		IFunctionTemplate ftemplate = repository
				.getFunctionTemplate(FUNCTION_NAME);

		if (ftemplate == null) {
			throw new IllegalArgumentException(
					"Can not get function template, Name:" + FUNCTION_NAME);
		}

		Map<String, Map<String, Double>> ret = new HashMap<String, Map<String, Double>>();

		Function function = ftemplate.getFunction();
		ParameterList input_variable = function.getImportParameterList();
		input_variable.setValue(year, PARAMETER_YEAR);
		input_variable.setValue(month, PARAMETER_MONTH);

		ParameterList input_table = function.getTableParameterList();
		Table tableIn = input_table.getTable(PARAMETER_COST_CENTER);
		for (int i = 0; i < costCodeArray.length; i++) {
			tableIn.appendRow();
			tableIn.setValue(costCodeArray[i], "KOSTL");
		}
		
		for (int i = 0; i < costElementArray.length; i++) {
			tableIn.appendRow();
			tableIn.setValue(costElementArray[i], "KSTAR");
		}

		function.setTableParameterList(input_table);

		client.execute(function);

//		String sRESULT = function.getExportParameterList().getString("RESULT");
//		String sMESSAGE = function.getExportParameterList()
//				.getString("MESSAGE"); //
//		if (sRESULT.equals("E")) {
//			throw new Exception("ERROR: " + sMESSAGE);
//		}

		Table result = function.getTableParameterList().getTable("TABLE_OUT");
		if (result.getNumRows() > 0) {
			while (result.nextRow()) {

				Map<String, Object> row = new HashMap<String, Object>();

				for (FieldIterator e = result.fields(); e.hasMoreElements();) {
					JCO.Field field = e.nextField();
					String key = field.getName();
					row.put(key, field.getValue());

				}

				/**
				 * 转置数据
				 */
				String _costCenterNumber = (String) row.get("KOSTL");
				String _cost = (String) row.get("WKGBTR");
				String _accountNumber = (String) row.get("KSTAR");

				Map<String, Double> rowData = ret.get(_costCenterNumber);
				if (rowData == null) {
					rowData = new HashMap<String, Double>();
					ret.put(_costCenterNumber, rowData);
				}
				Double cost = 0d;
				if (!Utils.isNullOrEmpty(_cost)) {
					try {
						cost = Double.parseDouble(_cost);
					} catch (Exception e) {
					}
				}
				Double _accountSummay = rowData.get(_accountNumber);
				if (_accountSummay == null) {
					_accountSummay = cost;
				} else {
					_accountSummay += cost;
				}
				rowData.put(_accountNumber, _accountSummay);
				Double _rowSummay = rowData.get("summ");
				if (_rowSummay == null) {
					_rowSummay = cost;
				} else {
					_rowSummay += cost;
				}
				rowData.put("summ", _rowSummay);
			}
		}
		return ret;
	}

}
