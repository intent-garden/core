(ns garden.audit  ;; <--- Открываем здесь
  (:require [cheshire.core :as json]
            [clojure.string :as str]))

;; Функция-ищейка: ищет конкретные паттерны в AST
(defn audit-node-raii [node]
  (let [kind (:kind node)
        name (:name node)]
    ;; Находим все объявления функций
    (when (= kind "FunctionDecl")
      (println (format "🔍 [АНАЛИЗ] Найдена функция: %s" name))
      ;; Если функция связана с выделением памяти (например, в libwui)
      (when (str/includes? (str/lower-case (or name "")) "create")
        (println "   ⚠️  ВНИМАНИЕ: Требуется проверка RAII-контракта!")))))

(defn audit-node-raw [node]
  (let [kind (:kind node)
        name (:name node)
        type (get-in node [:type :qualType])] ; Clang stores types here
    
    (when (= kind "VarDecl") ; If it's a variable declaration
      (when (and type (or (clojure.string/includes? type "char *")
                          (clojure.string/includes? type "void *")))
        (println (format "🚨 [DANGER] Found unmanaged pointer: '%s' of type '%s'" name type))
        (println "   -> Recommendation: Wrap in [[garden:intent]] or use libwui smart-types.")))

    (when (= kind "FunctionDecl")
      (println (format "🔍 [FOUND] Function: %s" name)))))

(defn -main []
  (let [ast-file "ast.json"]
    (if (.exists (java.io.File. ast-file))
      (let [raw (slurp ast-file)
            ;; Если первый символ - BOM (65279), отрезаем его
            clean (if (= (int (first raw)) 65279) (subs raw 1) raw)
            ast (json/parse-string clean true)]
        (println "--- Анализ начат ---")
        (clojure.walk/prewalk (fn [n] (audit-node-raii n) n) ast)
        (clojure.walk/prewalk (fn [n] (audit-node-raw n) n) ast)
      )
      (println "Файл не найден"))))

(-main)
