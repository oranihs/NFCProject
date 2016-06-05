package com.nfc.project.dao;

import com.nfc.project.vo.UserVO;

public interface UserDAO {
	boolean haveUser(String id);
	String createPW(String password);
	boolean insertUser(UserVO vo);
	boolean pwCheck(String id , String pw);
}
