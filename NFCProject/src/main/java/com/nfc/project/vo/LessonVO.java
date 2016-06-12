package com.nfc.project.vo;

import java.util.List;

public class LessonVO {
	private String classNo;
	private String lessonCode;
	private String teacher;
	private String lessonName;
	private List<String> lessonTime;
	private List<String> placeNo;
	public String getClassNo() {
		return classNo;
	}
	public void setClassNo(String classNo) {
		this.classNo = classNo;
	}
	public String getLessonCode() {
		return lessonCode;
	}
	public void setLessonCode(String lessonCode) {
		this.lessonCode = lessonCode;
	}
	public String getTeacher() {
		return teacher;
	}
	public void setTeacher(String teacher) {
		this.teacher = teacher;
	}
	public String getLessonName() {
		return lessonName;
	}
	public void setLessonName(String lessonName) {
		this.lessonName = lessonName;
	}
	
	
	
	public List<String> getLessonTime() {
		return lessonTime;
	}
	public List<String> getPlaceNo() {
		return placeNo;
	}
	public void setLessonTime(List<String> lessonTime) {
		this.lessonTime = lessonTime;
	}
	public void setPlaceNo(List<String> placeNo) {
		this.placeNo = placeNo;
	}
	
	

	public LessonVO(String classNo, String lessonCode, String teacher,
			String lessonName, List<String> lessonTime, List<String> placeNo) {
		super();
		this.classNo = classNo;
		this.lessonCode = lessonCode;
		this.teacher = teacher;
		this.lessonName = lessonName;
		this.lessonTime = lessonTime;
		this.placeNo = placeNo;
	}
	public LessonVO() {
		super();
	}
	
	
	public LessonVO(String classNo, String lessonCode) {
		super();
		this.classNo = classNo;
		this.lessonCode = lessonCode;
	}
	@Override
	public String toString() {
		return "LessonVO [classNo=" + classNo + ", lessonCode=" + lessonCode + ", teacher=" + teacher + ", lessonName="
				+ lessonName + ", lessonTime=" + lessonTime + ", placeNo=" + placeNo + "]";
	}
	
}
