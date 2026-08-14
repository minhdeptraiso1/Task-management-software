package com.project.taskmanagement.ai.meeting.dto;
import jakarta.validation.constraints.*;
public class MeetingMinutesRequest {
 private MeetingType meetingType; private String meetingTitle,meetingTime,participants,rawNotes;
 @NotNull(message="Loại meeting không được để trống") public MeetingType getMeetingType(){return meetingType;} public void setMeetingType(MeetingType v){meetingType=v;}
 @Size(max=255,message="Tiêu đề cuộc họp không được vượt quá 255 ký tự") public String getMeetingTitle(){return meetingTitle;} public void setMeetingTitle(String v){meetingTitle=v;}
 @Size(max=100,message="Thời gian họp không được vượt quá 100 ký tự") public String getMeetingTime(){return meetingTime;} public void setMeetingTime(String v){meetingTime=v;}
 @Size(max=500,message="Danh sách người tham gia không được vượt quá 500 ký tự") public String getParticipants(){return participants;} public void setParticipants(String v){participants=v;}
 @NotBlank(message="Ghi chú cuộc họp không được để trống") @Size(min=20,max=10000,message="Ghi chú cuộc họp phải từ 20 đến 10000 ký tự") public String getRawNotes(){return rawNotes;} public void setRawNotes(String v){rawNotes=v;}
}
