package com.softoolshop.adminpanel.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.softoolshop.adminpanel.dto.ContactFormDTO;
import com.softoolshop.adminpanel.service.ContactFormService;

@RestController
@RequestMapping("/api/contact")
public class ContactController {
	
	@Autowired
	private ContactFormService cntctFormService;
	
	@PostMapping
    public ResponseEntity<ContactFormDTO> receiveMessage(@RequestBody ContactFormDTO contactForm) {
        
		ContactFormDTO response = cntctFormService.saveMessage(contactForm);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
	
	@GetMapping
	public ResponseEntity<List<ContactFormDTO>> getAllMessages() {
		List<ContactFormDTO> response = cntctFormService.getAllMessages();
	    return ResponseEntity.ok(response);
	}
	
	@PutMapping
    public ResponseEntity<?> replyToMessage(@RequestBody ContactFormDTO replyRequest) {
		Long enqId = replyRequest.getEnqId();
		Map<String, Object> response = cntctFormService.replyToMessage(enqId, replyRequest);
		return ResponseEntity.ok(response);
	}
	

}
