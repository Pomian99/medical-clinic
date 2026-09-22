# D1
```
***************************
APPLICATION FAILED TO START
***************************

Description:

Parameter 0 of constructor in io.github.pomian99.medical_clinic.notification.VisitReminderService required a single bean, but 2 were found:
        - emailReminderSender: defined in file [C:\Users\bpomi\IdeaProjects\medical-clinic\target\classes\io\github\pomian99\medical_clinic\notification\EmailReminderSender.class]
        - smsReminderSender: defined in file [C:\Users\bpomi\IdeaProjects\medical-clinic\target\classes\io\github\pomian99\medical_clinic\notification\SmsReminderSender.class]

This may be due to missing parameter name information

Action:

Consider marking one of the beans as @Primary, updating the consumer to accept multiple beans, or using @Qualifier to identify the bean that should be consumed
```

`VisitReminderService` potrzebował beana implementującego `ReminderService`. Do wyboru był dostępne dwie implementacje i nie wiadomo, którą kontener powinien wybrać. Komunikat sugeruje użycie `@Primary` lub `@Qualifier`

# D2

`@Primary` ustawia konkretną klasę jako główną implementację do wyboru, gdy istnieje kilka klas implementujących ten sam interfejs.

# D3

Po dodaniu `@Qualifier` nad polem zamiast tworzenia konstruktora ręcznie, aplikacja uruchomiła się i zbudowała się poprawnie, lecz wykorzystała `EmailReminderSender` jako implementację `ReminderSender`.
