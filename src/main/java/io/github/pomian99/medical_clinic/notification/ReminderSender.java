package io.github.pomian99.medical_clinic.notification;

public interface ReminderSender {
    void send(String to, String message);
}
