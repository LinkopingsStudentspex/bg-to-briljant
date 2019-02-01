(ns bg-to-briljant.core
  (:require [dk.ative.docjure.spreadsheet :as dc]
            [bg-to-briljant.arguments     :refer [validate-args]])
  (:gen-class))

(defn load-files
  [files]
  (for [file files]
    (dc/load-workbook file)))

(defn get-date
  [document]
  (->> (dc/select-sheet "Insättningsuppgift via internet" document)
       (dc/select-cell "A5" )
       dc/read-cell))

(def kategori->kreditkonto
  {:sittningspaket  3168
   :spexpay         3152
   :faktura         1510
   :sittning-extern 3169
   :okategoriserat  0})

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

(->> document
     (dc/select-sheet "Insättningsuppgift via internet")
     (dc/select-columns {:A :avsändare
                         :C :betalningsreferens
                         :D :avinummer
                         :E :belopp})
     (drop 19) ; Det är efter rad 19 som transaktionerna börjar.
     (map kategorisera)
     (map associera-kreditkonto)
     (map associera-kreditunderkonto)
     )


(defn -main
  [& args]
  (let [{:keys [arguments options exit-message ok?]} (validate-args args)]
    (if exit-message
      (do (println exit-message)
          (System/exit (if ok? 0 1)))
      (for [document (load-files arguments)]
        nil))))
