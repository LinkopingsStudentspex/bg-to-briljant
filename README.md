# bg-to-briljant

Ett kanske fungerande program för att ta bankgiroutdrag i excelformat
(xlsx), sortera in betalningarna i olika kategorier baserat på
meddelande, och spotta ur sig CSV-filer som Briljant sedan kan äta
upp. Automatiserar en liten del i ekonomichefens arbete.


## Användning

Programmet tar som argument sökvägen till de filer du vill konvertera.

    $ java -jar bg-to-briljant-0.1.0-standalone.jar [filer]

Eller om du har källkoden framför dig:

    $ lein run [filer]


## Utveckling

Om du aldrig använt Clojure förut rekommenderar jag antingen IDE:n
Cursive eller Emacs-läget CIDER.

### Produktionsbyggen

För att skapa ett produktionsbygge, en release, så kör:

    $ lein uberjar

Detta kommer skapa ett fristående javaprogram i `target/uberjar` som
går att köra enligt instruktionerna i [##Användning].
