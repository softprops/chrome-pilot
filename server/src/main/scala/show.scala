package chrome

trait Show[T] {
  def show(t: T): String
}

object Show {
  implicit object ShowTab extends Show[Tab] {
    def show(t: Tab): String = t.info.toString
  }
  def apply[T: Show](t: T) = implicitly[Show[T]].show(t)
  //def apply[T](t: T)(implicit s: Show[T]) = s.show(t)
}
