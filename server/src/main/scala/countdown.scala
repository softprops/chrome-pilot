package chrome

class Countdown[T](from: Int)(f: => T) {
  import java.util.concurrent.atomic.AtomicInteger 
  private val ai  = new AtomicInteger(from)
  def tick = if (ai.decrementAndGet == 0) f else println("%d tabs still open " format ai.get)
  def reset(to: Int) = ai.getAndSet(to)
}
