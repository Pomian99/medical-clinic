package io.github.pomian99.medical_clinic.notification;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class VisitReminderService {
    private final ReminderSender reminderSender;

    public VisitReminderService(@Qualifier("smsReminderSender") ReminderSender reminderSender) {
        this.reminderSender = reminderSender;
    }

    public void remind(String to) {
        reminderSender.send(to, "przypomnienie o wizycie jutro o 10:00");
    }
}
