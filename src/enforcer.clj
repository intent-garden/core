(ns garden.enforcer
  (:require [cheshire.core :as json])) ; Библиотека для JSON

(defn check-free-node [node]
  (let [children (:inner node)]
    (loop [[current next & rest] children]
      (when current
        ;; Если нашли вызов функции "free"
        (if (and (= (:kind current) "CallExpr")
                 (clojure.string/includes? (get-in current [:inner 0 :name]) "free"))
          ;; Проверяем, что следующий узел - это присваивание NULL
          (if (and next 
                   (= (:kind next) "BinaryOperator")
                   (= (:opcode next) "=")
                   (clojure.string/includes? (json/generate-string next) "NULL"))
            (println "CHECK PASSED: free() followed by NULL assignment.")
            (throw (Exception. "INTENT VIOLATION: free() without NULL assignment!"))))
        (recur (cons next rest))))))

;; Запуск: bb -m garden.enforcer clang_ast.json
