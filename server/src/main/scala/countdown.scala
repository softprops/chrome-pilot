package chrome

class Countdown[T](from: Int)(f: => T) {
  import java.util.concurrent.atomic.AtomicInteger 
  private val ai  = new AtomicInteger(from)
  def tick = if (ai.decrementAndGet == 0) f
  def reset(to: Int) = ai.getAndSet(to)
}
