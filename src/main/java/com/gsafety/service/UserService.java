package com.gsafety.service;

import java.util.List;

import com.gsafety.entity.User;

public interface UserService {

	void saveAll(List<User> list);

	List<User> getAll();

}
