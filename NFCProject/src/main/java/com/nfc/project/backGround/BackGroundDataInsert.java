package com.nfc.project.backGround;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;

public class BackGroundDataInsert {

	void execute(){
		try {
			Connection con = null;

			con = DriverManager.getConnection("jdbc:mysql://localhost/nfcproject",
					"root", "apmsetup");


			System.out.println("DB Connect..");
			String jsonInfo = "";
			BufferedReader reader;
			int result;
			int arraySize =0  ;
			JSONArray array;
			PreparedStatement preparedStmt;
			String query,LessonCode,ClassNo,Teacher,LessonName;

			for(int f = 1 ; f <= 2 ; f++){
				
				FileInputStream fis = new FileInputStream(new File("Lesson"+f+".txt")); 

				InputStreamReader isr = new InputStreamReader(fis,"euc-kr"); 


				reader = new BufferedReader(isr);

				jsonInfo = reader.readLine();

				System.out.println("FILE "+f+" READ..");
					
				array = new JSONArray(jsonInfo);

				

				System.out.println("insert start...");
				System.out.println(array.getJSONObject(0).toString());
				System.out.println(array.getJSONObject(0).get("교과목코드"));
				for(int i = 0 ; i < array.length() ; i++){
					//LessonCode
					LessonCode = (String)array.getJSONObject(i).get("교과목코드");
					
					
					//ClassNo
					ClassNo = (String)array.getJSONObject(i).get("분반번호");

					//Teacher
					Teacher = (String)array.getJSONObject(i).get("담당강사명");

					//LessonName
					LessonName = (String)array.getJSONObject(i).get("교과목명");




					query = "insert into lesson(LessonCode,ClassNo,Teacher,LessonName) values(?,?,?,?)";
					preparedStmt = con.prepareStatement(query);

					preparedStmt.setString (1, LessonCode );
					preparedStmt.setString (2, ClassNo );
					preparedStmt.setString (3, Teacher );
					preparedStmt.setString (4, LessonName);

					result = preparedStmt.executeUpdate();
					if(result == 0 ){ System.out.println("error"); return;}



					//���ͳ� ����
					if(array.getJSONObject(i).get("강의시간강의실").toString().equals("")){
						continue;
					}

					//LessonPlaceTime
					String data[] = array.getJSONObject(i).get("강의시간강의실").toString().split(",");

					for(int j = 0 ; j <data.length;j++){


						query = "select count(*) from place where placeNo=?";
						preparedStmt = con.prepareStatement(query);

						preparedStmt.setString (1, data[j].split("/")[1] );

						ResultSet rs = preparedStmt.executeQuery();
						rs.next();
						String rsResult= rs.getString("count(*)");

						if(rsResult.equals("0")){
							query = "insert into place (PlaceNO,sizeX,sizeY) values(?,?,?)";
							preparedStmt = con.prepareStatement(query);
							preparedStmt.setString (1, data[j].split("/")[1] );
							preparedStmt.setInt(2, 8);
							preparedStmt.setInt(3, 8);

							result = preparedStmt.executeUpdate();
							if(result == 0 ){ System.out.println("error"); return;}
							//	else{System.out.println("Place insert..."); }
						}


						query = "insert into lessonplacetime(lessonCode,classNo,PlaceNo,LessonTime) values(?,?,?,?)";
						preparedStmt = con.prepareStatement(query);
						preparedStmt.setString(1, LessonCode);
						preparedStmt.setString(2, ClassNo);
						preparedStmt.setString(3, data[j].split("/")[1]);
						preparedStmt.setString(4, data[j].split("/")[0]);

						result = preparedStmt.executeUpdate();
						if(result == 0 ){ System.out.println("error"); return;}
						//else{System.out.println("LessonPlaceTime insert...");}

					}
				}

				System.out.println("insert all  ok");
				arraySize = array.length()+1;
			}





		} catch (SQLException sqex) {
			System.out.println("SQLException: " + sqex.getMessage());
			System.out.println("SQLState: " + sqex.getSQLState());
		}catch(IOException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		BackGroundDataInsert obj = new BackGroundDataInsert();
		obj.execute();

	}
}
