package com.softoolshop.adminpanel.dto;

import java.time.LocalDateTime;

@lombok.Data
public class ContactFormDTO {

	private Long enqId;
	private String name;
    private String email;
    private String subject;
    private String message;
    private LocalDateTime crtDate;
    private short replyFlg; 
    private String replyMessage;
}
