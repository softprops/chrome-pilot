package chrome

object Exiting {
  def ok[T](f: => T): Int = {
    f; 0
  }
  def err[T](f: => T): Int = {
    f; 1
  }

  def ok(msg: String): Int = ok {
    println(msg)
  }

  def err(msg: String): Int = err { 
    System.err.println(msg)
  }

  def err(t: Throwable): Int =
    err("error making request: %s" format t.getMessage)
}
