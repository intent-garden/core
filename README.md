# Intent-Garden (S-OAK) Core Protocol

## Цель: Создание детерминированного слоя верификации для C-кода, генерируемого ИИ или человеком.

## 1. Структура Репозитория (intent-garden/core)

 - `/specs` — Описание DSL (EDN/Clojure).
 - `/src` — Clojure-движок (Парсинг JSON AST -> Валидация).
 - `/lib` — Набор базовых Интентов (Memory, Threading, Logic).

## 2. Жизненный цикл "Семантического Якоря"

Цикл исключает галлюцинации за счет разделения труда:

1. *Intent Definition (intent.edn):*
  Формальное описание правил на Clojure DSL.
  Пример: `{:entity :buffer :type "char*" :lifecycle :managed}`.

2. *Semantic Echo:*
  Скрипт на Clojure (чистая функция) превращает `intent.edn` в текстовый отчет.
  Проверка: Человек читает текст и подтверждает, что логика верна.

3. *AI Injection:*

`intent.edn` подается в промпт ИИ-кодеру.
Требование: ИИ обязан расставить теги `// [[garden:target(ID)]]` вокруг реализации.

4. *AST Enforcement:*
`clang -Xclang -ast-dump=json` выдает дерево C-кода. Clojure-скрипт заходит в узлы, помеченные тегами, и проверяет инварианты (наличие `free`, отсутствие `void*` кастов и т.д.).
