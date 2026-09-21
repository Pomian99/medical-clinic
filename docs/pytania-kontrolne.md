
1. Wymień trzy problemy `main`-a, który ręcznie tworzy wszystkie obiekty aplikacji.

- tworzenie wszystkich potrzebnych klas ręcznie
- zachowanie kolejności w tworzeniu klas
- przy dużych aplikacjach rozrost maina i brak czytelności

2. Po czym poznajesz klasę modelową, a po czym funkcjonalną? Które z nich oddajemy
   Springowi i dlaczego tylko te?

klasy modelowe - przechowujądane dotyczące pojedynczych jednostek np. użytkownik, produkt.

klasy funkcjonalne - przechowują, zarządzają bądź operują na wielu danych. Zajmują się obsługą danych, baz danych oraz funkcjonalności, które użytkownik może wywołać.

Springowi oddaje się klasy funkcjonalne, gdyż zazwyczaj potrzebujemy tylko jednej z nich, która jest wykorzystywana w całym programie.

3. Co to jest bean, a co kontener? Jedno zdanie na każde pojęcie.

kontener - obiekt w springu przechowujący oraz zarządzający utworzonymi przez siebie klasami

bean - obiekty, które są tworzone za pomocą springa

4. Czym różni się wstrzykiwanie przez konstruktor od wstrzykiwania przez pole? Podaj dwie
   przewagi konstruktora.

Przy wstrzykiwaniu przez konstruktor wysztkie podane zależności klasy są ustawiane przy jej tworzeniu. W przypadku wstrzykiwania przez pole są one ustawiane po utworzeniu obiektu.

Obiekty przy wstrzykiwaniu przez konstruktor są odporne na problemy związane z brakiem potrzebnej zależności oraz są łatwiejsze do testowania bez springa, gdyż łatwiej utworzyć obiekt z klasą potrzebną do testu.

5. Czym różni się `new` od konstruktora?

konstruktor jest metodą tworzącą klasę i określającą zasady jest tworzenia. `new` jest operatorem, który wywołuje utworzenie nowego obiektu przy użyciu konstruktora

6. Opisz, jak mini-kontener z zajęć buduje kontroler: co dzieje się krok po kroku i w jakiej
   kolejności powstają obiekty.

Najpierw kontener sprawdza pliki w poszukiwaniu klas zakwalifikowanych do tworzenia. Po napotkaniu takich klas zaczyna tworzyć obiekty. Sprawdza kontruktor i jeśli wymaga innych klas, to sprawdza czy w swoim słowniku posiada klasę możliwą do zaaplikowania. W razie potrzeby tworzy potrzebne klasy powtarzając proces. Po prawidłowym utworzeniu klasy dodaje do słownika w postaci klasa:obiekt i przechodzi dalej.

7. Czym różni się odwrócenie sterowania (IoC) od wstrzykiwania zależności (DI)?

IoC jest zasadą stosowaną w programowaniu. DI jest konkretnym sposobem implementacji IoC. IoC określa co chcemy osiągnąć a DI jak to osiągamy.
