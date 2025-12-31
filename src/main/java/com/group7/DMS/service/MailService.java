package com.group7.DMS.service;

public interface MailService {
    void send(String to, String subject, String text);
}