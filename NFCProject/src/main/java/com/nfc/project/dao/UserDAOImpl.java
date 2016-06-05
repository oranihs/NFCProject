package com.nfc.project.dao;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.lambdaworks.crypto.SCryptUtil;
import com.nfc.project.vo.UserVO;

public class UserDAOImpl implements UserDAO {

	private JdbcTemplate template;

	public void setTemplate(JdbcTemplate template) {
		this.template = template;
	}

	

	@Override
	public boolean haveUser(String id){

		String sql = "select count(*) from user where id=?";
		int result = template.queryForInt(sql, new Object[]{id});
		if(result  == 0){
			System.err.println("HAVE NOT ID");
			return false;
		}else if(result == 1){
			System.err.println("HAVE ID");
			return true;
		}else{
			System.err.println("MULTI USE ID ERROR");
			return false;
		}
	}

	@Override
	public 	String createPW(String password){
		return SCryptUtil.scrypt(password, 16, 16, 16);
	}

	@Override
	public boolean insertUser(UserVO vo){
		String sql = "insert into user(id,name,pw,type) values(?,?,?,?)";

		int result = template.update(sql, vo.getId(), vo.getName(),createPW(vo.getPw()), vo.getType());

		if(result  == 0){
			System.out.println("INSERT USER FAIL");
			return false;
		}else if(result == 1){
			System.out.println("INSERT USER OK");
			return true;
		}else{
			System.err.println("MULTI USE ID ERROR");
			return false;
		}
	}

	@Override
	public boolean pwCheck(String id , String pw){
		String sql = "select user.pw from user where id=?";
		String result = template.queryForObject(sql, new Object[]{id},String.class);

		boolean check = SCryptUtil.check(pw, result);

		if(check){
			System.err.println("PASSWORD CHECK OK");
			return true;
		}else{
			System.err.println("PASSWORD CHECK FAIL");
			return false;
		}
	}

}
