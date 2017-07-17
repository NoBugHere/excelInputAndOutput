package com.gsafety.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gsafety.dao.UserDao;
import com.gsafety.entity.User;
import com.gsafety.service.UserService;
@Service("userService")
public class UserServiceImpl implements UserService {
	
	@Autowired
	private UserDao userDao;
	
	@Override
	@Transactional
	public void saveAll(List<User> list) {
		userDao.saveAll(list);
	}

	@Override
	public List<User> getAll() {
		return userDao.getAll();
	}

}
