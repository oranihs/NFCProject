package com.nfc.project.dao;

import java.util.ArrayList;

import org.springframework.jdbc.core.JdbcTemplate;

import com.nfc.project.vo.LessonVO;

public class LessonDAOImpl {
	
	
	private JdbcTemplate template;

	public void setTemplate(JdbcTemplate template) {
		this.template = template;
	}

	
	public int insertUserLessonList(String id, ArrayList<LessonVO> lessonList){
		int insertCount =0;
			for(int i = 0 ; i < lessonList.size() ; i++){
				String sql = "insert into lessonUser(id,classNo,LessonCode) values(?,?,?)";
				int result = template.update(sql,id,lessonList.get(i).getClassNo(),lessonList.get(i).getLessonCode());
				
				if(result  == 0){
					System.out.println("INSERT USER FAIL");
				}else if(result == 1){
					System.out.println("INSERT USER OK");
				}
				insertCount += result;
			}
			
		System.out.println("LIST SIZE "+ ": "+insertCount+" INSERT OK "+lessonList.size() );
		return insertCount;
	}
}
