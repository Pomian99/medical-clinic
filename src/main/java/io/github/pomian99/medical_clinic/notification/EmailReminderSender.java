package io.github.pomian99.medical_clinic.notification;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class EmailReminderSender implements ReminderSender{

    @Override
    public void send(String to, String message) {
        System.out.printf("e-mail do %s: %s%n", to, message);
    }
}
