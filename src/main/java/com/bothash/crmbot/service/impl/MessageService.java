package com.bothash.crmbot.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.bothash.crmbot.entity.Message;
import com.bothash.crmbot.repository.MessageRepository;

@Service
public class MessageService {
	@Autowired
    private MessageRepository messageRepository;

    public Page<Message> getMessages(int page, int size) {
        return messageRepository.findAll(PageRequest.of(page - 1, size));
    }

    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }

    public Message updateMessage(Long id, String messageName, String text) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        message.setMessageName(messageName);
        message.setText(text);

        return messageRepository.save(message);
    }

    
    public Message findByName(String name) {
    	return this.messageRepository.findByMessageName(name);
    }

	public List<Message> findAll() {
		return this.messageRepository.findAll();
	}
}
