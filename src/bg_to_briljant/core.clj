(ns bg-to-briljant.core
  (:require [dk.ative.docjure.spreadsheet :as dc]
            [bg-to-briljant.arguments     :refer [validate-args]]
            [bg-to-briljant.utilities     :as util])
  (:gen-class))

(def debetkonto 1933)
(def headers    "PREL\n1;.U\n")
(def projekt    2019)

(defn dokument->datum
  [dokument]
  (->> dokument
       (dc/select-sheet "Insättningsuppgift via internet")
       (dc/select-cell "A5")
       dc/read-cell))

(defn dokument->total
  [dokument]
  (->> dokument
       (dc/select-sheet "Insättningsuppgift via internet")
       (dc/select-cell "B15")
       dc/read-cell))

(defn total-csv-rad
  [datum totalbelopp]
  (str ";" datum ";" debetkonto ";;;;;;" totalbelopp ";BG-inbet. " datum ";;"))

(def kategori->kreditkonto
  {:sittningspaket  3168
   :spexpay         3152
   :faktura         1510
   :sittning-extern 3169
   :okategoriserat  0}) ; Vi sätter noll här för det bör ersättas. Kan vara fel val.

(defn associera-kreditkonto
  [transaktion]
  (assoc transaktion :kreditkonto (kategori->kreditkonto (:kategori transaktion))))

(defn associera-kreditunderkonto
  [transaktion]
  (case (:kategori transaktion)
    :faktura (assoc transaktion :kreditunderkonto (re-find #"\d\d\d\d\d" (:betalningsreferens transaktion)))
    transaktion))

(defn kategorisera
  "Kategorisera transaktionen"
  [transaktion]
  (assoc transaktion :kategori
         (condp re-find (:betalningsreferens transaktion)
           #"(?i)sittningspaket"  :sittningspaket
           #"\d\d\d\d\d\d\d\d"    :spexpay
           #"80\d\d\d"            :faktura
           #"(?i)(sits|sittning)" :sittning-extern
           #""                    :okategoriserat)))
(def kategori->underprojekt
  {:sittningspaket  201
   :spexpay         501
   :faktura         ""   ; Fakturor har inget självklart underprojekt.
   :sittning-extern 201
   :okategoriserat  0})

(defn associera-underprojekt
  [transaktion]
  (assoc transaktion :underprojekt (kategori->underprojekt (:kategori transaktion))))

(defn dokument->transaktioner
  "Extraherar alla transaktioner ur ett excelark från
  bankgirocentralen. Härleder även en mängd information för var
  transaktion baserat på transaktionens meddelande."
  [dokument]
  (->> dokument
     (dc/select-sheet "Insättningsuppgift via internet")
     (dc/select-columns {:A :avsändare
                         :C :betalningsreferens
                         :D :avinummer
                         :E :belopp})
     (drop 19) ; Det är efter rad 19 som transaktionerna börjar.
     (map kategorisera)
     (map associera-kreditkonto)
     (map associera-kreditunderkonto)
     (map associera-underprojekt)))

(defn transaktion->csv-string
  "Tar ett datum och en transaktion och returnerar en bit text i
  CSV-format som representerar transaktionen i Briljant-format."
  [datum {:keys [kreditkonto kreditunderkonto underprojekt belopp betalningsreferens avsändare]}]
  (str ";" datum ";" kreditkonto ";" kreditunderkonto ";;;" projekt","underprojekt  ";;"
       (- belopp) ";" betalningsreferens " " (util/capitalize-words avsändare) ";;"))


(defn -main
  [& args]
  (let [{:keys [arguments options exit-message ok?]} (validate-args args)]
    (if exit-message
      (do (println exit-message)
          (System/exit (if ok? 0 1)))
      (for [dokument (map dc/load-workbook arguments)]
        (let [datum (dokument->datum dokument)]
          (spit (str "out/" datum ".csv")
                (str headers
                     (total-csv-rad datum (dokument->total dokument))
                     "\n"
                     (->> dokument
                          dokument->transaktioner
                          (map (partial transaktion->csv-string datum))
                          (clojure.string/join "\n"))
                     "\n")))))))
