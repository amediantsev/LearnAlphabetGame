package app.learnalphabet.ui

/** Loads each key once and remembers the result, including "not found". Thread-safe. */
class MemoCache<K, V : Any>(private val load: (K) -> V?) {
    private val values = HashMap<K, V>()
    private val missing = HashSet<K>()

    @Synchronized
    fun peek(key: K): V? = values[key]

    @Synchronized
    fun isKnownMissing(key: K): Boolean = key in missing

    fun get(key: K): V? {
        synchronized(this) {
            values[key]?.let { return it }
            if (key in missing) return null
        }
        val loaded = load(key)
        synchronized(this) {
            if (loaded != null) values[key] = loaded else missing += key
        }
        return loaded
    }
}
