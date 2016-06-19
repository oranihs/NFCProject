package com.nfc.project;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nfc.project.dao.LessonDAOImpl;
import com.nfc.project.dao.UserDAOImpl;
import com.nfc.project.vo.LessonVO;


@Controller
public class LessonController {
	private static final Logger logger = LoggerFactory.getLogger(LessonController.class);

	@Autowired
	UserDAOImpl userImpl ;		
	
	@Autowired
	LessonDAOImpl lessonImpl ;	
	
	
	@RequestMapping(value = "/checkLesson", method = RequestMethod.GET,  produces="application/json;charset=UTF-8")
	public @ResponseBody JSONObject checkLesson(@RequestParam("id") String id,@RequestParam("classNo") String classNo,
									@RequestParam("lessonCode") String lessonCode ,@RequestParam("placeNo") String placeNo ,
									@RequestParam("seatX")	int seatX , @RequestParam("seatY") int  seatY ) {
		
		LessonVO vo = lessonImpl.selectOneLesson(lessonCode, classNo);
		JSONObject json =null;
		
		boolean result = lessonImpl.usingPlace(placeNo, seatX, seatY);
		if(result){
			json = lessonImpl.checkLessoningUser(id, vo, seatX, seatY,placeNo);
		}else{
			json = new JSONObject();
			json.put("placeResult", "false");
			json.put("checkResult", "false");
			json.put("error", "이미 해당자리 사용하는사람 있음");
		}
		return json;
	}
	
	// 해당 수업 출석체크 안한 사람 출력
	@RequestMapping(value = "/getNotCheckList", method = RequestMethod.GET,  produces="application/json;charset=UTF-8")
	public @ResponseBody JSONArray getNotCheckList(@RequestParam("classNo") String classNo,@RequestParam("lessonCode") String lessonCode ) {
		
		LessonVO vo = lessonImpl.selectOneLesson(lessonCode, classNo);
		return lessonImpl.getNotCheckList(vo);
	}
	
	// 강의실 사람들 출력 AJAX
	@RequestMapping(value = "/getCheckListAJAX", method = RequestMethod.GET,  produces="application/json")
	public @ResponseBody JSONArray getCheckList(@RequestBody String data  ) {
		String placeNo = null ;
		System.out.println("data :"+data);
		try{
		org.json.JSONObject parse = new org.json.JSONObject(data);
		placeNo = parse.getString("placeNo");
		}catch(Exception e){}
		return lessonImpl.getCheckList(placeNo);
	}
	
	
	
	@RequestMapping(value = "/lessonOnOff", method = RequestMethod.GET,  produces="application/json;charset=UTF-8")
	public @ResponseBody JSONObject lessonOnOff(@RequestParam("id") String id,@RequestParam("placeNo") String placeNo,
			@RequestParam("lessonCode") String lessonCode,@RequestParam("classNo") String classNo) {
		
		JSONObject json = new JSONObject();
		int result = lessonImpl.classOnOff(id, placeNo);
		// 수업 종료 출석 체크 안한 사람들 결석 처리
		if(result == 0){
			LessonVO vo = lessonImpl.selectOneLesson(lessonCode, classNo);
			JSONArray array =  lessonImpl.getNotCheckList(vo);
			for (int i = 0; i < array.size(); i++) {
				lessonImpl.checkLessonInfo(((String)((JSONObject)array.get(i)).get("id")), vo, 0);
			}
			
		}
			json.put("result",result);
		return json;
	}
	
}
