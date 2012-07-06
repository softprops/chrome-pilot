package chrome

object Term {
  def red(f: => Any) = Console.RED + f + Console.RESET
  def green(f: => Any) = Console.GREEN + f + Console.RESET
  def magenta(f: => Any) = Console.MAGENTA + f + Console.RESET
  def cyan(f: => Any) = Console.CYAN + f + Console.RESET
  def bold(f: => Any) = Console.BOLD + f + Console.RESET
}
