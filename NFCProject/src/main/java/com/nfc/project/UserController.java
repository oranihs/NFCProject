package com.nfc.project;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nfc.project.dao.LessonDAOImpl;
import com.nfc.project.dao.UserDAOImpl;
import com.nfc.project.vo.LessonVO;
import com.nfc.project.vo.UserVO;

@Controller
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	UserDAOImpl userImpl ;		
	
	@Autowired
	LessonDAOImpl lessonImpl ;		
	
	// 해당 아이디 있는지 확인
	@RequestMapping(value = "/haveUser", method = RequestMethod.GET,  produces="application/json")
	public @ResponseBody JSONObject home(@RequestParam("id") String id) {
		 // Test
		JSONObject json = new JSONObject();
			json.put("result", String.valueOf(userImpl.haveUser(id)));
			System.out.println(json.get("result").toString());
		return json;
	}
	
	//로그인 
	@RequestMapping(value = "/Login", method = RequestMethod.POST,  produces="application/json")
	public @ResponseBody JSONObject Login(@RequestParam("id") String id , @RequestParam("pw") String pw){
		
		JSONObject json = new JSONObject();
			boolean result = userImpl.haveUser(id);
			
			if(result){
				
				result = userImpl.pwCheck(id, pw);
				
				if(result){
					System.out.println("true");
					json.put("result", "true");
				}else{
					json.put("result", "false");
				}
			}else{
				json.put("result", "needSignUp");
			}
		return json;
	}
	
	// 나의 수업 리스트 출력
	@RequestMapping(value = "/getLessonList", method = RequestMethod.GET,  produces="application/json;charset=UTF-8")
	public @ResponseBody org.json.JSONArray getLessonList(@RequestParam("id") String id ){
		
			ArrayList<LessonVO> resultList =  userImpl.selectUserLessonList(id);
			
			org.json.JSONArray resultArray = new org.json.JSONArray(resultList);
			
			
		return resultArray;
	}
	
	
	//  해당 과목 출석 리스트 출력
	//  결석 0 ,출석 1  , 지각 2 
	//  결과데이터 date 날짜 , status 출석 상태  
	@RequestMapping(value = "/getCheckList", method = RequestMethod.GET,  produces="application/json;charset=UTF-8")
	public @ResponseBody JSONArray getCheckList(@RequestParam("id") String id,@RequestParam("lessonCode") String lessonCode, @RequestParam("classNo") String classNo){
		return userImpl.selectUserCheckList(id, lessonCode, classNo);
	}
	
	// 회원가입
	@RequestMapping(value = "/Signup", method = RequestMethod.POST,  produces="application/json")
	public @ResponseBody JSONObject Sinup(@RequestParam("id") String id , @RequestParam("pw") String pw){
		
		JSONObject json = new JSONObject();
			boolean result = userImpl.haveUser(id);
			
			if(result){
				json.put("result", "false");
				json.put("error", "already user exist");
				return json;
			}
			
			
			// Komoh Login;
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext httpContext = new BasicHttpContext();
	        HttpResponse httpResponse;
	        HttpPost httpPost = new HttpPost("http://m.kumoh.ac.kr/login?currentUri="); 
	        httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
			httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
			
			List<NameValuePair> parameters = new ArrayList<NameValuePair>(); 
			parameters.add(new BasicNameValuePair("j_username", id)); 
			parameters.add(new BasicNameValuePair("j_password", pw));
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
				httpResponse = httpClient.execute(httpPost, httpContext);
				
//				System.out.println(httpResponse.getStatusLine().toString());
				
				if(httpResponse.getStatusLine().toString().equals("HTTP/1.1 302 Found")){
					
					System.out.println("Kumoh login ok");
					System.out.println(EntityUtils.toString(httpResponse.getEntity()));
					
					HttpGet get = new HttpGet("http://m.kumoh.ac.kr/ugradService/cgeRegEnq/basicInfo/getBasicInfo.json");
					get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
					get.addHeader("Content-Type", "application/x-www-form-urlencoded");
					httpResponse = httpClient.execute(get, httpContext)	;
					String userData = EntityUtils.toString(httpResponse.getEntity());
					org.json.JSONObject userObject = new org.json.JSONObject(userData);
					
					System.out.println(userData);
					
					get = new HttpGet("http://m.kumoh.ac.kr/ugradService/lectReqEnq/lectReqEnqList.json?viewPage=1&listCount=15");
					get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");
					get.addHeader("Content-Type", "application/x-www-form-urlencoded");
					httpResponse = httpClient.execute(get, httpContext); 
		
					String userLesson = EntityUtils.toString(httpResponse.getEntity());
					System.out.println(userLesson);
					
					
					UserVO user = new UserVO();
					user.setId(id);
					user.setName(userObject.getString("name"));
					user.setType(userObject.getString("cge_reg"));
					user.setPw(pw);
					
					boolean insertResult = userImpl.insertUser(user);
					if(!insertResult){
						json.put("result", "false");
						json.put("error", "userInsert fail");
						return json;
					}
					
					LessonVO lessonVO ;
					
					org.json.JSONArray lessonList = new org.json.JSONArray(userLesson);
					ArrayList<LessonVO> insertLessonList = new ArrayList<LessonVO>();
					for(int i = 0 ; i < lessonList.length() ; i++){
						String lesson = lessonList.getJSONObject(i).getString("개설교과목코드");
						String lessonData[] = lesson.split("-");
						lessonVO = new LessonVO();
						lessonVO.setLessonCode(lessonData[0]);
						lessonVO.setClassNo(lessonData[1]);
						insertLessonList.add(lessonVO);
					}
					
					int lessonInsertResult = lessonImpl.insertUserLessonList(id, insertLessonList); 
					
					
					if(lessonInsertResult != lessonList.length()){
						json.put("result", "false");
						json.put("error", "lessonInsert fail");
						return json;
					}
					
				}else {
					System.out.println("Kumoh login false");
					
				}
				
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		
		json.put("result", "true");
		return json;
	}

	
	
	
	
	
}
