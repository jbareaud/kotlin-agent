# Kotlin-Agent

A powerful, async-based coding agent built in **Kotlin** using **LangChain4j** and **Mordant** for a beautiful Terminal User Interface (TUI). This project demonstrates a modern, coroutine-driven architecture for building intelligent CLI agents without heavyweight frameworks.

---

## 🚀 Features

- **Async/Coroutine Architecture**: Non-blocking I/O using Kotlin Coroutines for smooth CLI experience
- **Intelligent Tool System**: Automatically discover and execute tools via `@Toolable` annotation
  - `read`: Read file contents
  - `bash`: Execute shell commands (ls, grep, find, etc.)
  - `edit`: Make surgical edits to files
  - `write`: Create or overwrite files
  - `ls`: List directory contents
- **Streaming Responses**: Real-time token streaming from LLM with Mordant's live display
- **Mordant TUI**: Beautiful terminal UI with animations, Markdown rendering, and styling
- **Human-in-the-Loop**: Manual tool authorization before execution
- **Dynamic Model Switching**: Change LLM providers at runtime (Ollama, OpenAI, etc.)
- **Standalone Execution**: Build a fat JAR using ShadowJar with all dependencies
- **Structured Concurrency**: Proper lifecycle management with `CoroutineScope`

---

## 🛠️ Technology Stack

| Component | Version | Description |
|-----------|---------|-------------|
| Kotlin | 2.2.20 | Language with coroutines |
| Kotlinx Coroutines | 1.10.2 | Structured concurrency |
| LangChain4j | 1.13.0 | LLM orchestration |
| Mordant | 3.0.2 | Terminal UI framework |
| Gradle ShadowJar | 9.4.1 | Fat JAR builder |
| ClassGraph | 4.8.184 | Tool discovery at runtime |
| SLF4J Simple | 2.0.17 | Logging |

### Dependencies

```toml
[versions]
kotlin = "2.2.20"
kotlinxCoroutines = "1.10.2"
langchain4j = "1.13.0"
langchain4jbeta = "1.13.0-beta23"
mordant = "3.0.2"
classgraph = "4.8.184"
slf4j-simple = "2.0.17"
shadow = "9.4.1"
```

---

## 📁 Project Structure

```
kotlin-agent/
├── agent-core/          # Main application (orchestration)
│   └── src/main/kotlin/
│       ├── App.kt              # Main entrypoint
│       ├── commands/           # Command registry (/exit, etc.)
│       ├── chat/               # Chat message handling
│       └── orchestration/      # AgentOrchestrator
├── agent-tools/         # Tool implementations
│   └── src/main/kotlin/tools/
│       ├── specs/              # Tool definitions (bash, read, edit, write, ls)
│       └── ToolRegistry.kt     # Tool discovery and execution
└── agent-tui/           # Terminal UI wrapper (Mordant)
    └── src/main/kotlin/tui/
        ├── BasicMordantWrapper.kt   # Animated TUI
        └── SimpleConsoleWrapper.kt  # Console fallback
```

---

## 🎯 Getting Started

### Prerequisites

- JDK 17 or higher
- Gradle 8.7+
- Optional: Ollama/Llama3 or OpenAI API key for LLM

### Build

```bash
# Build the standalone fat JAR
./gradlew shadowJar

# The executable will be at: build/libs/kotlin-agent-all.jar
```

### Run

```bash
# Run the standalone application
java -jar build/libs/kotlin-agent-all.jar

# Or run via Gradle
./gradlew run
```

The agent will then start its interactive TUI session.

---

## 📜 Usage

### Interactive Command

1. **Start the agent** - Enter any question or task

2. **Commands**
   - `/exit` - Exit the session
   - Any text - Process as natural language

3. **Example interaction**

   ```
   User: How do I read a file?
   
   Agent: I can read files for you! 
       → I read src/main/kotlin/App.kt and found it's 12 lines long.
   ```

---

## 🔧 Available Tools

Each tool is automatically discovered via the `@Toolable` annotation and LangChain4j's tool specification mechanism.

| Tool | Description |
|------|-------------|
| `read` | Read file contents from disk |
| `write` | Create or overwrite files |
| `edit` | Make surgical edits (requires exact text match) |
| `bash` | Execute shell commands |
| `ls` | List directory contents with path utilities |

### Tool Execution Flow

1. LLM decides to call a tool during chat
2. Tool request displayed in TUI ("🛠️ AI wants to call: read(file.txt)")
3. Tool executes via reflection with `Dispatcher.IO`
4. Result added to chat history
5. LLM resumes with new context

---

## 🏗️ Architecture

### Async Orchestration Loop

```
User Input → CoroutineScope → StreamingChatModelFlow → EventCollector → TUI Render
           ↓
        Tool Interception
           ↓
   Manual Authorization (Human-in-the-loop)
           ↓
       Execute Tool
           ↓
  Add to Chat Memory → LLM Resume
```

### Key Design Decisions

1. **No Spring Boot** - Lightweight, <10MB binary
2. **Manual Lifecycle** - You control when LLM initializes
3. **Flow-based Architecture** - Kotlin flows bridge Java callbacks
4. **Mordant Live Display** - Prevents UI blocking during streaming

See [`doc/PLAN.md`](doc/PLAN.md) for detailed architectural documentation.

---

## 📤 Output

The executable is a single fat JAR with all dependencies bundled:

```bash
java -jar build/libs/kotlin-agent-all.jar
# or
java -jar build/libs/kotlin-agent-all.jar --help
```

---

## 📄 License

This project is open source. 

---

*Built with ❤️ using Kotlin Coroutines and LangChain4j*
