# Intent-Garden (S-OAK) Core

> **Deterministic verification engine for AI-generated C/C++ code.**
>
> We don't believe in "safe languages". We believe in safe intentions.

## 📖 About

**Garden-Core** is a Clojure engine that audits AI-generated C/C++ code through Clang AST and EDN contracts. It serves as the "last mile" between stochastic AI output and predictable code execution.

### How It Works

> **We don't fix AI mistakes. We create an environment where invalid code physically cannot pass the build stage.**

The engine eliminates hallucinations through strict division of labor:

1. **Intent Injection** — AI agent receives specification from `rulerom/registry`
2. **Garden-Tagging** — AI writes C code and marks it with `[[garden:intent(...)]]` tags
3. **AST Enforcement** — Engine parses Clang JSON AST and validates against contracts
4. **Semantic Echo** — Generates Markdown reports for human verification

## 📂 Repository Structure

```
core/
├── deps.edn              # Babashka/Clojure configuration
├── src/
│   ├── enforcer.clj      # Engine: AST parsing + validation
│   └── echo.clj          # Markdown report generator (Mirror)
├── specs/                # Local intent prototypes (EDN)
│   ├── c_core.edn
│   ├── memory-safety.edn
│   └── resource_guard.edn
├── scripts/              # Build utilities
├── MANIFEST.md           # Semantic Anchor Manifest
└── CONTRACT.md           # AI Agent Contract
```

### Related Repositories

| Repository | Purpose |
|------------|---------|
| [`intent-garden/core`](.) | Clojure validation engine |
| [`rulerom/registry`](https://github.com/rule-rom/registry) | Global Intent contract library (Git Submodule) |

## 🛠️ Technology Stack

| Component | Purpose |
|-----------|---------|
| **Clojure / Babashka** | Fast data processing logic without JVM overhead |
| **Clang LibTooling** | Parses `-ast-dump=json` for honest code structure |
| **EDN** | Contract format understandable by humans and machines |

## 🔄 Semantic Anchor Lifecycle

```mermaid
graph TD
    A["<b>1. Human Definition</b><br/>Architect Input / Task Description"] --> B{AI Scan}
    B -->|Search| C["<b>Rule-Rom Library</b>"]
    B -->|Generate| D["<b>Draft Intent</b>"]

    C & D --> E["<b>2. Intent Formalization</b><br/>Lisp/EDN Contract"]
    E --> F["<i>Semantic Echo</i><br/>Auto-generated Markdown Audit"]
    F -->|Verification| G(("Human Approval"))
    G -->|Fix| E

    G -->|Approved| H["<b>3. Prompt Injection</b><br/>Contract + Requirements"]
    H --> I["<b>AI Coding & Tagging</b><br/>C/C++ Code + garden-tags"]

    I --> J["<b>4. AST Enforcement</b><br/>Clang JSON AST + Babashka"]
    J --> K{Verification}
    K -->|Violation| L["<b>Build Failed</b><br/>Retry Loop"]
    L --> H

    K -->|Success| M["<b>5. Certification</b><br/>Deterministic Safety Proof"]
    M --> N["Production Ready Code"]

    style A fill:#e1f5fe,stroke:#01579b
    style E fill:#f3e5f5,stroke:#4a148c
    style I fill:#fff3e0,stroke:#e65100
    style J fill:#e8f5e9,stroke:#1b5e20
    style M fill:#dcedc8,stroke:#33691e
    style L fill:#ffebee,stroke:#b71c1c
```

## 🚀 Quick Start

### 1. Environment Setup (Windows / Scoop)

```powershell
scoop install babashka llvm
```

### 2. Running Local Audit

Assume you have a `test.c` file with tags:

```c
// [[garden:intent(safe-free)]]
free(p);
p = NULL;
// [[/garden:intent]]
```

**Step 1: Generate AST**

```powershell
clang -Xclang -ast-dump=json -fsyntax-only test.c > ast.json
```

**Step 2: Run Enforcer**

```powershell
bb -m garden.enforcer ast.json
```

### 3. Contract Example (EDN)

```edn
{:intent :safe-free
 :entities [:ptr]
 :must-set-null true
 :description "After free(), pointer must be set to NULL"}
```

## 📋 Tagging Protocol (Garden-Tagging)

Every code block related to an Intent **MUST** be tagged:

```c
// [[garden:intent(INTENT_ID)]]
void implementation_starts_here() {
    // Your logic
}
// [[/garden:intent]]
```

### Tagging Rules

| Rule | Description |
|------|-------------|
| **No Orphans** | Never place a tag without implementation |
| **Exact ID** | `INTENT_ID` must match the key in `.edn` file |
| **Scope** | Tags wrap the smallest logical unit |
| **Vacuum Rule** | Code outside `[[garden:intent]]` is considered dead and discarded |

## 📜 Manifest (Summary)

1. **Code is Cheap, Meaning is Expensive** — value lies in intentions, not instructions
2. **AI is Gas, Formal Logic is Brakes** — deterministic oversight over probabilistic execution
3. **Lisp is the Ideal Contract Language** — homoiconicity for the "Constitution of the System"
4. **Semantic Cage** — AI generates inside a DSL cage of safety rules
5. **Validation Instead of Hope-and-Pray** — check at generation time, not after
6. **Zero-Cost Security** — contract operates at metaprogramming stage
7. **Death of the "Black Box"** — AI as black box implementation in a white box of intentions

## 📄 Documents

| Document | Description |
|----------|-------------|
| [`CONTRACT.md`](CONTRACT.md) | AI Agent Contract (tagging rules) |
| [`specs/`](specs/) | EDN contract examples |
| [`rulerom/registry`](https://github.com/rule-rom/registry) | Global Intent Contract Library (Git Submodule) |

## 🔗 Related Projects

- **RuleROM** — Intent contract management system
- **rulerom/registry** — Global mirror of standards and intents

---

[🌿 Garden-Core](https://intent-garden.org) | [📖 Rule-Rom](https://rulerom.com) | [🐉 Decima8](https://decima8.org) | [🖼️ LibWUI](https://libwui.org)
