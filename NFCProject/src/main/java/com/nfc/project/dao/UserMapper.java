package com.nfc.project.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.nfc.project.vo.UserVO;

public class UserMapper implements RowMapper<UserVO> {

	@Override
	public UserVO mapRow(ResultSet rs, int rowNum) throws SQLException {
		UserVO vo = new UserVO();
		vo.setId(rs.getString("id"));
		vo.setName(rs.getString("name"));
		vo.setPw(rs.getString("pw"));
		vo.setType(rs.getString("type"));
		return vo;
	}

}
