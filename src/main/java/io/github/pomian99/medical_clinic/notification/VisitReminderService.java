package io.github.pomian99.medical_clinic.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VisitReminderService {
    @Qualifier("smsReminderSender")
    private final ReminderSender reminderSender;

    public void remind(String to) {
        reminderSender.send(to, "przypomnienie o wizycie jutro o 10:00");
    }
}
