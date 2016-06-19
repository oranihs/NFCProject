package com.nfc.project.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;

import com.nfc.project.GetCurrentDate;
import com.nfc.project.vo.LessonVO;
import com.nfc.project.vo.UserVO;

public class LessonDAOImpl {
	
	
	private JdbcTemplate template;

	public void setTemplate(JdbcTemplate template) {
		this.template = template;
	}

	public  int classOnOff(String id,String  placeNo ){
		String sql = "select count(*) from user where user.id=? and user.type='교수'";
		
		int result = template.queryForInt(sql,new Object[]{id});
		
		if(result == 0){
			return -1;
		}else{
			sql = "update place set able =  if( able = 0 , 1, 0)  where  placeNo=?";
			template.update(sql,new Object[]{placeNo});
			sql = "select able from place where placeNo =?";
			result = template.queryForInt(sql,new Object[]{placeNo});
			
			if(result ==0){
				sql  = "delete from lessoningUser where placeNo=?";
				template.update(sql,new Object[]{placeNo});
				
			}
		}
		
		return result;
	}
	
	public LessonVO selectOneLesson(String lessonCode , String classNo){
		
		ArrayList<LessonVO> lessonList =null;
		
		String sql = " select  l.lessonCode , l.ClassNo , l.lessonName, l.Teacher, t.lessonTime , p.PlaceNo"
					+" from lesson as l  natural join lessonplacetime as t natural join place p"
					+" where l.lessonCode=? and l.classNo=?";
		List<Map<String, Object>> resultList =  template.queryForList(sql, new Object[]{lessonCode,classNo});
		
		LessonVO vo =null;
		String before ="";
		for(int i = 0 ; i < resultList.size() ; i++){
			if(i == 0){
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
				vo.getLessonTime().add((String) resultList.get(i).get("lessonTime"));
				vo.getPlaceNo().add((String) resultList.get(i).get("placeNo"));
			}
		}
		
		return vo;
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
	
	
	
	
	
	public JSONObject checkLessoningUser(String id,LessonVO vo, int seatX,int seatY,String placeNo){
		String sql  = "select count(*) from LessoningUser where id=? and placeNo = ? and seatx = ? and seaty = ?";
		int result = template.queryForInt(sql, new Object[]{id,placeNo,seatX,seatY});
	
		JSONObject json = new JSONObject();
		//  자리  체크 
		if(result == 0){
			sql = "delete from lessoningUser where id=?";
			template.update(sql,new Object[]{id});
			
			sql = "insert into LessoningUser(id,placeNo,seatx,seaty)"
					+" select ?,placeNo , ? , ? from place "+
					"where placeNo = ? and able = 1";
			result =	template.update(sql,new Object[]{id,seatX,seatY,placeNo});
			if(result == 1){
				json.put("placeResult", "true");
				result =  checkLessonInfo(id, vo,-1);
				if(result == 1){
					json.put("checkResult", "true");
				}else if(result == -1){
					json.put("checkResult", "false");
					json.put("error", "자리 입력 성공 /출석 체크  실패 , 아직 출석 체크 시간 아님 ");
				}else{
					json.put("checkResult", "false");
					json.put("error", "자리 입력 성공 /출석 체크  실패 , 이미 해당수업 출석체크 함<자리만 이동 >");
				}
			}else{
				json.put("placeResult", "false");
				json.put("checkResult", "false");
				json.put("error", "자리 입력 실패 , 교수님이 출석 체크 가능 설정 안함");
			}
		}
		return json;
	}
	
	
	
	
	
	// 사용 불가능 false , 
	public boolean usingPlace(String placeNo , int seatX, int seatY){
		
		String sql = "select count(*) from lessoningUser where placeNo =? and seatX = ? and seatY = ?";
		int result = template.queryForInt(sql,new Object[]{placeNo,seatX,seatY});
		if(result == 0){ return true;}
		
		return false;
	}
	
	// 강의실 출석 가능 여부 상태
	public JSONObject lessonState(String placeNo ){
		
		String sql = "select able from place where placeNo=?";
		List<Map<String,Object>> result =  template.queryForList(sql,new Object[]{placeNo});

		JSONObject json = null;
		json = new JSONObject();
		json.put("result", (Integer)result.get(0).get("able"));
		return json;
	}
	
	
	
	
	
	//return 0 출석 체크 실패   , 1 출석 체크 성공 , -1 출석 체크 시간 아님
	//status 0 결석, 1 출석 , 2 지각 
	public int checkLessonInfo(String id,LessonVO vo , int status){
		String dayOfWeek[] = {"" , "일","월","화","수","목","금","토"};
		String classtime[] = {"1","2","3","4","5","6","7","8","9","A","B","C","D"};
		int result = -1;
		String sql = "select count(*) from lessonCheckInfo where id=? and lessonDate = ? and lessonCode = ? and classNo=?" ;
		String timeStamp = GetCurrentDate.getDate();
		int insertStatus =0;
		
		
		int day = GetCurrentDate.getDayOfWeek();

		
		
		if(template.queryForInt(sql, new Object[]{id,timeStamp.split(",")[0],vo.getLessonCode(),vo.getClassNo()}) != 0){
			return 0;
		}
		
		int hour = Integer.parseInt(timeStamp.split(",")[1].split(":")[0]);
		int minutes = Integer.parseInt(timeStamp.split(",")[1].split(":")[1]);
		
		ArrayList<String> lessonTimeList = (ArrayList<String>)vo.getLessonTime();
		int index =-1;
		for (int i = 0; i < lessonTimeList.size(); i++) {
			if(lessonTimeList.get(i).contains(dayOfWeek[day])){
				index = i;
			}
		}
		
		sql = "insert into lessonCheckInfo(id,lessonDate,Status,lessonCode,classNo) values(?,?,?,?,?)";
		String str =  lessonTimeList.get(index);
		System.out.println(str);
		System.out.println(classtime[hour - 9].charAt(0));
		System.out.println(str.charAt(1));
		if(classtime[hour - 9].charAt(0)==  str.charAt(1)){
			if(minutes  < 10){
				insertStatus = 1;
			}else if(minutes <20){
				insertStatus = 2;
			}else{
				insertStatus = 0;
			}
		}else if(classtime[hour - 8].charAt(0)==  str.charAt(1)){
			if(minutes >= 55){
				insertStatus = 1;
			}else{
				return -1;
			}
		}
		if(status != -1){
			insertStatus = status;
		}
		
		result = template.update(sql,new Object[]{id,timeStamp.split(",")[0],insertStatus,vo.getLessonCode(),vo.getClassNo()});
		return result;
	}


	
	
	// 해당 과목 출석체크 하지 않은 사람 출력
	public JSONArray getNotCheckList(LessonVO vo ){
		JSONArray array  = new JSONArray();
		String timeStamp = GetCurrentDate.getDate();
		
		
		String sql = "select u.id,u.name from lessonUser as lu natural join user u "
				+ "where u.type='재학생' and lu.lessonCode=? and lu.classNo=? and lu.id not in"
				+ " (select id from lessonCheckInfo where lessonCode = ? and classNo=? and lessonDate=?)";
		
		List<Map<String, Object>> resultList = template.queryForList(sql,new Object[]{vo.getLessonCode(),vo.getClassNo(),
				vo.getLessonCode(),vo.getClassNo(),timeStamp.split(",")[0]});
		
		for (int i = 0; i < resultList.size(); i++) {
			array.add(new UserVO((String)resultList.get(i).get("id"),(String)resultList.get(i).get("name")));
		}
		
		
		return array;
	}
	
	// 해당 과목 출석중인  사람 출력
		public JSONArray getCheckList(String placeNo){
			JSONArray array  = new JSONArray();
			String currentDate = GetCurrentDate.getDate().split(",")[0];
			String sql = "select u.name,l.seatX, l.seatY , li.status from lessoningUser as l natural join user  as u "
					+ "natural join lessonCheckInfo as li where placeNo = ? and li.lessonDate = ?";
			
			List<Map<String, Object>> resultList = template.queryForList(sql,new Object[]{placeNo,currentDate});
			JSONObject json;
			for (int i = 0; i < resultList.size(); i++) {
				 json = new JSONObject();
				json.put("name", (String)resultList.get(i).get("name"));
				json.put("seatX", (Integer)resultList.get(i).get("seatX"));
				json.put("seatY", (Integer)resultList.get(i).get("seatY"));
				json.put("status", (Integer)resultList.get(i).get("status"));
				array.add(json);
			}
			return array;
		}
}
