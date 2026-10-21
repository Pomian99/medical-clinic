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

# D4

### I
Z perspektywy Springa `@Component` oraz `@Service` mają takie samo działanie. Oznaczają klasy, które powinny być utworzone jako Beany zarządzane przez Springa.

Dla czytającego są one oznaczeniem z funkcji, jaką spełnia dana klasa. Dodanie adnotacji `@Service` sugeruje, że klasa implementuje logikę biznesową aplikacji.

### II
Jest to klasa zewnętrzna i nie mamy kontroli nad jej implementacją. Any wprowadzić ją do kontenera należy utworzyć klasę konfiguracji wykorzystując adnotację `@Configuration`. Wewnątrz tej klasy tworzymy metodę zwracającą obiekt, który chcemy dodać do kontenera z adnotacją `@Bean` ponad nią.

### III
Sprawdzam, czy inne beany powstają poprawnie. Przeglądam czy ścieżka danej klasy nie wychodzi poza zakres widoczności Springa.
