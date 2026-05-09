```kotlin

interface Effect

enum class DB: Effect {
  Read, Write
}

enum class Network: Effect {
  Read, Write
}


```
