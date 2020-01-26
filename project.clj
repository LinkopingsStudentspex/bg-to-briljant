(defproject bg-to-briljant "1.0.0"
  :description "A software for translating from the Excel-files produced by Bankgiro to the CSV format Briljant imports."
  :url "https://github.com/LinkopingsStudentspex/bg-to-briljant"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure   "1.9.0"]
                 [dk.ative/docjure      "1.12.0"]
                 [org.clojure/tools.cli "0.4.1"]]
  :main ^:skip-aot bg-to-briljant.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
