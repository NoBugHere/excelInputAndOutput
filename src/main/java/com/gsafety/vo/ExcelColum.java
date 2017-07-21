package com.gsafety.vo;
/**
 * @ClassName: ExcelColum
 * @Description: excel bean 映射对象
 * @author Kai.Zhang
 * @date 2017年06月22日
 */
public class ExcelColum {
	
	/*bean字段名*/
	private String fieldName;
	
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	/*excel列名*/
	private String columnName;
	
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
	
}
