package io.github.pomian99.medical_clinic.notification;

import org.springframework.stereotype.Component;

@Component
public class SmsReminderSender implements ReminderSender{

    @Override
    public void send(String to, String message) {
        System.out.printf("SMS do %s: %s%n", to, message);
    }
}
