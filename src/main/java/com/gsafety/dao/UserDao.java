package com.gsafety.dao;

import java.util.List;

import com.gsafety.entity.User;

public interface UserDao {

	void saveAll(List<User> list);

	List<User> getAll();

}
