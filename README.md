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


# Användarguide

Det här programmet tar en eller flera bankgirorapport(er) från
Swedbank som argument och spottar ur sig CSV-filer för
bokföringsprogrammet att läsa in.

Programmet kräver att Java finns installerat på datorn och körs
vanligen via en terminal. Till exempel kan man köra programmet likt:

```
java -jar <sökvag till bg-to-briljant-x.x.x-standalone.jar> <sökväg till bankgirorapport typ in/Bg5606-2060_Insättningsuppgifter_20191018.xlsx>
```

Då kommer programmet att spotta ur sig en CSV-fil kallad
`2019-10-18.csv`.

Programmet kommer som standard använda en fil som heter `settings.edn`
för att avgöra hur den ska klassa alla transaktioner. Skulle du vilja
ange en annan inställningsfil kan du göra det med flaggan
`--settings, t.ex:

```
java -jar <sökväg till bg-to-briljant-1.0.0-standalone.jar> --settings /en/annan/inställningsfil.edn in/Bg5606-2060_Insättningsuppgifter_20191018.xlsx
```
