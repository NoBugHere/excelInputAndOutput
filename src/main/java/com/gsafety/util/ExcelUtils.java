package com.gsafety.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import com.gsafety.vo.ExcelColum;

/**
 * @ClassName: ExcelUtils
 * @Description: excel工具类
 * @author Kai.Zhang
 * @date 2017年06月22日
 */
public class ExcelUtils {
	
	/**
	 * 读取excel并返回List
	 * @param uploadFile 上传的excel文件
	 * @param clazz 实体类型
	 * @return 转换后的实体集合
	 */
	public static List<Object> readExcel(MultipartFile uploadFile, Class clazz){
		List<Object> resultList = new ArrayList<Object>();
		InputStream inputStream = null;
		Workbook workbook = null;
		try {
			/*获取excel文件的输入流*/
			inputStream = uploadFile.getInputStream();
			/*输入流转为Workbook对象*/
			workbook = new XSSFWorkbook(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*获取第一个Sheet*/
		Sheet sheet = workbook.getSheetAt(0);
		/*获取行数*/
		int rowCount = sheet.getPhysicalNumberOfRows();
		/*解析excel-bean获取map*/
		Map<String,Object> map = ExcelUtils.dom2Map(clazz);
		/*从第二行还是读取，第一行为标题*/
		for (int i = 1; i < rowCount; i++) {
			Row row = sheet.getRow(i);
			Object obj = null;
			try {
				obj = clazz.newInstance();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			for (Entry<String, Object>  entry : map.entrySet() ) {
				int index = Integer.valueOf(entry.getKey());
				if (row.getCell(index) != null) {
					Cell cell = row.getCell(index);
					ExcelColum e = (ExcelColum) entry.getValue();
					String fieldName = e.getFieldName();
					Field[] fields = obj.getClass().getDeclaredFields();
					for (Field field : fields) {
						field.setAccessible(true);
						if( field.getName().equals(fieldName) ){
							Class typeClazz = field.getType();
							/*获取单元格的值*/
							Object value = getValueByTypeAndCell(cell, typeClazz);
							/*为对象赋值*/
							setFieldValue(obj, field, value);
						}
					}
				}
			}
			resultList.add(obj);
		}
		return resultList;
	}
	
	/**
	 * List2Excel并输出
	 * @param list 实体对象集合
	 * @param clazz 
	 * @param response
	 */
	public static void list2Excel(List list, String fileName, HttpServletResponse response){
		Class clazz = list.get(0).getClass();
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");
		Row titleRow = sheet.createRow(0);
		/*设置标题居中*/
		CellStyle titleCellStyle = wb.createCellStyle();
		titleCellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
		/*设置标题加粗*/
		Font titleCellStyleFont = wb.createFont();
		titleCellStyleFont.setBold(true);
		titleCellStyle.setFont( titleCellStyleFont );
		/*解析excel-bean获取map*/
		Map<String, Object> map = ExcelUtils.dom2Map(clazz);
		/*循环生成标题行*/
		for (Entry<String, Object>  entry : map.entrySet() ) {
			if( StringUtils.isNumeric(entry.getKey()) ){
				Cell titleCell = titleRow.createCell(Integer.valueOf(entry.getKey()));
				titleCell.setCellValue(((ExcelColum)entry.getValue()).getColumnName());
				titleCell.setCellStyle(titleCellStyle);
			}
		}
		/*通过list循环生成每行数据*/
		for (int i = 0; i < list.size(); i++) {
			Row row = sheet.createRow(i+1);
			/*通过map循环生成行内每个单元格*/
			for (Entry<String, Object>  entry : map.entrySet() ) {
				if( StringUtils.isNumeric(entry.getKey()) ){
					/*获得实体类的字段名*/
					String fieldName = ((ExcelColum)entry.getValue()).getFieldName();
					Field[] fields = clazz.getDeclaredFields();
					for (Field field : fields) {
						field.setAccessible(true);
						if( field.getName().equals(fieldName) ){
							/*获取字段对应的值*/
							String value = String.valueOf(getFieldValueByName(fieldName, list.get(i)));
							/*如果是Date类型，截取掉毫秒位*/
							if( Date.class == field.getType() ){
								value = value.indexOf(".")!=-1 ? value.substring(0, value.lastIndexOf(".")) : value;	
							}
							/*创建行内待单元格，并赋值*/
							row.createCell(Integer.valueOf(entry.getKey())).setCellValue(value);
						}
					}
				}
			}
		}
		/*Excel文件的输出流*/
		OutputStream output = null;
		response.reset();
		try {
			output = response.getOutputStream();
			/*设置文件名，例如：学生表20170717.xlsx*/
			response.setHeader("Content-disposition", "attachment; filename="
					+new String(fileName.getBytes(), "ISO-8859-1")+new SimpleDateFormat("yyyyMMdd").format(new Date())+".xlsx");
			/*设置文件类型*/
			response.setContentType("application/msexcel");
			/*写入到输出流*/
			wb.write(output);
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 从excel-bean配置文件中读取数据，返回map
	 * @param 实体的类型
	 * @return map { key : 列的索引, value : ExcelColum对象{ columnName : 列名, fieldName : 字段名称 } }
	 */
	public static Map<String,Object> dom2Map(Class clazz){
		Map<String,Object> map = new LinkedHashMap<>();
		String clazzPathName = clazz.getName();
        SAXReader reader = new SAXReader(); 
        Document document;
        Element root = null;
		try {
			document = reader.read(new ClassPathResource("excel-bean.xml").getFile());
			root = document.getRootElement();
		} catch (Exception e) {
			e.printStackTrace();
		}  
		List<Element> ExcelElelist = root.elements();
		for (Element excelEle : ExcelElelist) {
			/*获取xml中配置的类完全限定名*/
			String classAttVla = excelEle.attribute("class").getValue();
			if(clazzPathName.equals(classAttVla)){
				List<Element> ColumElelist = excelEle.elements();
				for (int i = 0; i < ColumElelist.size(); i++) {
					String fieldName = ColumElelist.get(i).element("fieldName").getText();
					String columnName = ColumElelist.get(i).element("columnName").getText();
					ExcelColum excelColum = new ExcelColum();
					excelColum.setFieldName(fieldName);
					excelColum.setColumnName(columnName);
					map.put(String.valueOf(i), excelColum);
				}
			}
		}
		return map;
	}
	
	/**
	 * 通过字段类型和单元格对象获取对应的值
	 * @param typeClazz 字段类型
	 * @param cell 单元格对象
	 * @return 单元格的值
	 */
	private static Object getValueByTypeAndCell(Cell cell, Class typeClazz){
		Object value = null;
		if( String.class == typeClazz ){
			cell.setCellType(Cell.CELL_TYPE_STRING );
			value = cell.getStringCellValue();
		}else if( Integer.class == typeClazz ||  int.class== typeClazz ){
			cell.setCellType(Cell.CELL_TYPE_STRING );
			value = Integer.valueOf(cell.getStringCellValue());
		}else if( Boolean.class== typeClazz ){
			if( cell.getCellType() != Cell.CELL_TYPE_BOOLEAN ){
				cell.setCellType(Cell.CELL_TYPE_BOOLEAN );
			}
			value = cell.getBooleanCellValue();
		}else if( Double.class== typeClazz ||  double.class== typeClazz  ){
			if( cell.getCellType() != Cell.CELL_TYPE_NUMERIC ){
				cell.setCellType(Cell.CELL_TYPE_STRING );
				value = Double.valueOf(cell.getStringCellValue());
			}else{
				value =  cell.getNumericCellValue();
			}
		}else if( Date.class== typeClazz ){
			if( cell.getCellType() != Cell.CELL_TYPE_NUMERIC ){
				cell.setCellType(Cell.CELL_TYPE_STRING );
				try {
					value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cell.getStringCellValue());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}else{
				value = cell.getDateCellValue();
			}
		}
		return value;
	}
	
	/**
	 * 利用反射，根据属性名获取属性值
	 * @param fieldName 字段名称
	 * @param o 目标对象
	 * @return 属性值
	 */
	private static Object getFieldValueByName(String fieldName, Object targetObj) {  
		try {
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			String methodName = "get" + firstLetter + fieldName.substring(1);
			Method method = targetObj.getClass().getDeclaredMethod(methodName);
			Object value = method.invoke(targetObj);
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 利用反射，为对象赋值
	 * @param targetObj 目标对象
	 * @param field 字段
	 * @param value 值
	 */
	private static void setFieldValue(Object targetObj, Field field, Object value){
		String fieldName = field.getName();
		String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		Class typeClazz = field.getType();
		try {
			Method method = targetObj.getClass().getDeclaredMethod(methodName, typeClazz);
			method.invoke(targetObj, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
