package com.gsafety.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.gsafety.entity.User;
import com.gsafety.service.UserService;
import com.gsafety.util.ExcelUtils;

@Controller
public class HomeController {

	@Autowired
	private UserService userService;

	@RequestMapping("home")
	public ModelAndView home() {
		return new ModelAndView("upload");
	}
	
	@RequestMapping("upload")
	public void up(@RequestParam("uploadFile") MultipartFile uploadFile) {
		List reuslt = ExcelUtils.readExcel(uploadFile, User.class);
		userService.saveAll(reuslt);
	}

	@RequestMapping("download")
	public void saveAsXlsx( HttpServletResponse response) {
		List<User> list = userService.getAll();
		ExcelUtils.list2Excel(list, "学生信息表", response);
	}
	
	
}
