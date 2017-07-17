package com.gsafety.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.gsafety.dao.UserDao;
import com.gsafety.entity.User;
@Repository("userDao")
public class UserDaoImpl implements UserDao {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public void saveAll(List<User> list) {
		for (User user : list) {
			sessionFactory.getCurrentSession().save(user);
		}
	}

	@Override
	public List<User> getAll() {
		List<User> resultList = sessionFactory.getCurrentSession().createQuery("from User").list();
		return resultList;
	}

}
