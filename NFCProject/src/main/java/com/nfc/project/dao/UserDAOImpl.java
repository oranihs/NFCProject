package com.nfc.project.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;

import com.lambdaworks.crypto.SCryptUtil;
import com.nfc.project.vo.LessonVO;
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
	
	
	int findLessonList(ArrayList<LessonVO> list , String lessonCode){
		for (int i = 0; i < list.size(); i++) {
			if(list.get(i).getLessonCode().equals(lessonCode)){
				return i;
			}
		}
		
		return -1;
	}
	
	
	
	
	public ArrayList<LessonVO> selectUserLessonList(String id){
		
		ArrayList<LessonVO> lessonList =null;
		
		String sql = "select  u.lessonCode , u.ClassNo , l.lessonName, l.teacher, t.lessonTime , p.placeNo"
					+ " from lessonuser as u natural join lesson as l  natural join lessonplacetime as t natural join place p"
					+ " where id = ?";
		List<Map<String, Object>> resultList =  template.queryForList(sql, new Object[]{id});
		
		LessonVO vo;
		String before ="";
		for(int i = 0 ; i < resultList.size() ; i++){
			String lessonCode = (String) resultList.get(i).get("LessonCode");
			
			if(findLessonList(lessonList, lessonCode) == -1){
				vo = new LessonVO();
				vo.setClassNo((String) resultList.get(i).get("classNo"));
				vo.setLessonCode((String) resultList.get(i).get("LessonCode"));
				vo.setLessonName((String) resultList.get(i).get("lessonName"));
				vo.setTeacher((String) resultList.get(i).get("teacher"));
				ArrayList<String> lessonTimeList = new ArrayList<String>();
				lessonTimeList.add((String) resultList.get(i).get("lessonTime"));
				vo.setLessonTime(lessonTimeList);

				ArrayList<String> lessonPlaceList = new ArrayList<String>();
				lessonTimeList.add((String) resultList.get(i).get("placeNo"));
				vo.setPlaceNo(lessonPlaceList);
			}else{
				int getNo = findLessonList(lessonList, lessonCode);
				lessonList.get(getNo).getLessonTime().add((String) resultList.get(i).get("lessonTime"));
				lessonList.get(getNo).getPlaceNo().add((String) resultList.get(i).get("placeNo"));
			}
		}
		
		return lessonList;
	}



	public JSONArray selectUserCheckList(String id,
			String lessonCode, String classNo) {
		String sql = "select lessonDate , status from lessoncheckinfo where id= ? and lessonCode=? and classNo=?";
		JSONArray array = new JSONArray();
		List<Map<String,Object>> list = template.queryForList(sql, new Object[]{id,lessonCode,classNo});
		
		JSONObject json = null;
		for (int i = 0; i < list.size(); i++) {
			json = new JSONObject();
			json.put("date", (String)list.get(i).get("lessonDate"));
			json.put("status", (Integer)list.get(i).get("status"));
			array.add(json);
		}
		return array;
	}

}
