package tradly.social.data.model

import kotlinx.coroutines.*

object CoroutinesManager
{

    fun <T: Any> io(work: suspend (() -> T?)): Job =
        CoroutineScope(Dispatchers.IO).launch {
            work()
        }

    fun <T: Any> ioThenMain(work: suspend (() -> T), callback: ((T) -> Unit)? = null): Job =
        CoroutineScope(Dispatchers.Main).launch {
            val data = CoroutineScope(Dispatchers.IO).async { return@async work() }.await()
            callback?.let { it(data) }
        }

    fun <T: Any> ioThenMain(job: Job,work: suspend (() -> T), callback: ((T) -> Unit)? = null): Job =
        CoroutineScope(Dispatchers.Main+job).launch {
            val data = CoroutineScope(Dispatchers.IO).async { return@async work() }.await()
            callback?.let { it(data) }
        }

    fun <T: Any> ioThenMain(scope:CoroutineScope,work: suspend (() -> T), callback: ((T) -> Unit)? = null): Job =
       scope.launch {
            val data = async(Dispatchers.IO) { return@async work() }.await()
            callback?.let { it(data) }
        }
}