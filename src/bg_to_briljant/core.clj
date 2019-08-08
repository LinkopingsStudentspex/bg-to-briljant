(ns bg-to-briljant.core
  (:require [dk.ative.docjure.spreadsheet :as dc]
            [bg-to-briljant.arguments     :refer [validate-args]]
            [bg-to-briljant.utilities     :as util])
  (:gen-class))

(def settings (read-string (slurp "settings.edn")))
(def headers    "PREL\n1;.U\n")

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
  (str ";" datum ";" (:debetkonto settings) ";;;;;;" totalbelopp ";BG-inbet. " datum ";;"))

(defn associera-kreditkonto
  [transaktion]
  (assoc transaktion :kreditkonto ((:kategori->kreditkonto settings) (:kategori transaktion))))

(defn associera-kreditunderkonto
  [transaktion]
  (case (:kategori transaktion)
    :faktura (assoc transaktion :kreditunderkonto (re-find #"\d\d\d\d\d" (:betalningsreferens transaktion)))
    transaktion))

(defn kategorisera
  "Kategorisera en transaktion att vara i någon av de kotegorier som
  anges i inställningarna för programmet."
  [transaktion]
  (assoc transaktion :kategori
         (or (util/condp-fn util/re-find-safe (:betalningsreferens transaktion) (:meddelande-regex->kategori settings))
             :okategoriserat)))

(defn associera-underprojekt
  [transaktion]
  (assoc transaktion :underprojekt ((:kategori->underprojekt settings) (:kategori transaktion))))

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
  (str ";" datum
       ";" kreditkonto
       ";" kreditunderkonto
       ";;;" (if underprojekt (str (:projekt settings)","underprojekt) "")
       ";;" (- belopp)
       ";" betalningsreferens " " (util/capitalize-words avsändare)
       ";;"))


(defn -main
  [& args]
  (let [{:keys [arguments options exit-message ok?]} (validate-args args)]
    (if exit-message
      (do (println exit-message)
          (System/exit (if ok? 0 1)))
      (for [dokument (map dc/load-workbook arguments)]
        (let [datum   (dokument->datum dokument)
              outpath (str "out/" datum ".csv")]
          (println "Writing to " outpath)
          (spit outpath
                (str headers
                     (total-csv-rad datum (dokument->total dokument))
                     "\n"
                     (->> dokument
                          dokument->transaktioner
                          (map (partial transaktion->csv-string datum))
                          (clojure.string/join "\n"))
                     "\n")))))))
