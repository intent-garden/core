(ns garden.enforcer
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.walk :refer [prewalk]]))

;; --- The Registry: Loading your Lisp-Intents from .edn files ---
(defn load-specs []
  (-> "specs/c_safety.edn" slurp edn/read-string))

(defmulti verify-intent 
  "The Multimethod Dispatch: Choosing the rule based on Intent ID"
  (fn [intent-id node] (keyword intent-id)))

;; --- RULE 1: The 'safe-free' Law ---
(defmethod verify-intent :safe-free [_ node]
  (let [body (:inner node) ; For a FunctionDecl, :inner is the CompoundStmt
        ;; Flatten the tree slightly to look at top-level statements in the block
        statements (get-in node [:inner 0 :inner])] 
    (loop [[curr next & rest] statements]
      (if (and curr (str/includes? (json/generate-string curr) "free"))
        ;; We found 'free'. Now, is the NEXT statement an assignment to 0/NULL?
        (if (and next 
                 (= (:kind next) "BinaryOperator")
                 (= (:opcode next) "=")
                 (re-find #"0|NULL|nullptr" (json/generate-string next)))
          (do (println "   ✅ [VERIFIED]: safe-free invariant satisfied.") true)
          (throw (Exception. "   🚨 [VIOLATION]: free() called but pointer NOT nulled!")))
        (if rest (recur (cons next rest)) false)))))

;; --- RULE 2: Placeholder for your next "Campus" hack ---
(defmethod verify-intent :default [intent-id _]
  (println "   ⚠️  Warning: No verifier found for intent:" intent-id))

;; --- The main Orchestrator ---
(defn enforce-file [source-path ast-path]
  (let [tags (scan-source-for-tags source-path) ; From previous step
        ast (json/parse-string (slurp ast-path) true)]
    (doseq [{:keys [intent-id line-number]} tags]
      (println (format "🔍 Checking Intent '%s' near line %d..." intent-id line-number))
      (let [anchor-node (find-node-after-line ast line-number)]
        (verify-intent intent-id anchor-node)))))

;; Функция поиска тегов в текстовом файле
(defn scan-source-for-tags [file-path]
  (with-open [reader (io/reader file-path)]
    (doall
     (keep-indexed
      (fn [idx line]
        ;; Ищем паттерн [[garden:intent(something)]]
        (when-let [match (re-find #"\[\[garden:intent\((.*?)\)\]\]" line)]
          {:intent-id (second match)
           :line-number (inc idx)})) ; +1 потому что в Clang строки с 1
      (line-seq reader)))))

;; Функция поиска узла в AST, который идет СРАЗУ ПОСЛЕ тега
(defn find-node-after-line [ast target-line]
  (let [found (atom nil)]
    (prewalk (fn [node]
               (let [node-line (get-in node [:range :begin :line])]
                 ;; Ищем первый узел, строка которого >= строке тега
                 (when (and (map? node) 
                            node-line
                            (>= node-line target-line))
                   (if (or (not @found) (< node-line (get-in @found [:range :begin :line])))
                     (reset! found node))))
               node)
             ast)
    @found))

;; Функция поиска узла по номеру строки
(defn find-node-at-line [ast target-line]
  (let [found (atom nil)]
    (prewalk (fn [node]
               (when (and (map? node)
                          (= (get-in node [:range :begin :line]) target-line))
                 (reset! found node))
               node)
             ast)
    @found))

;; Реальная проверка логики Safe-Free
(defn verify-safe-free [node]
  (let [children (:inner node)] ; В C++ CompoundStmt содержит список команд
    (loop [[curr next & rest] children]
      (if (and curr 
               (= (:kind curr) "CallExpr")
               (clojure.string/includes? (json/generate-string curr) "free"))
        ;; Мы нашли free, теперь ЖЕСТКО смотрим на следующий узел
        (if (and next 
                 (= (:kind next) "BinaryOperator")
                 (= (:opcode next) "=")
                 (or (clojure.string/includes? (json/generate-string next) "NULL")
                     (clojure.string/includes? (json/generate-string next) "0")))
          true  ;; ПРОВЕРКА ПРОЙДЕНА
          false ;; ИИ НАВРАЛ
          )
        (if rest (recur (cons next rest)) false)))))

(defn enforce-all [source-file ast-file]
  (let [tags (scan-source-for-tags source-file)
        ast (json/parse-string (slurp ast-file) true)]
    (println (format "--- Garden-Enforcer: Scanning %s ---" source-file))
    (doseq [{:keys [intent-id line-number]} tags]
      (println (format "📍 Found Intent: '%s' at line %d" intent-id line-number))
      (let [target-node (find-node-after-line ast line-number)]
        (if target-node
          ;; Здесь мы вызываем нашу логику верификации (как в прошлом шаге)
          (verify-intent intent-id target-node) 
          (println "   ⚠️  Could not find any AST node after this tag."))))))

(defn -main [& args]
  (let [src "test.c"
        ast "ast.json"]
    (enforce-all src ast)))
