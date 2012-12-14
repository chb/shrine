package net.shrine.utilities

/**
 * @author clint
 * @date Dec 7, 2012
 */
trait Command extends (() => Unit) {
  def perform() = apply()
}

object Command {
  /*def apply(f: () => Any): Command = new Command {
    override def apply(): Unit = f
  }*/
  
  def apply(f: => Any): Command = new Command {
    override def apply(): Unit = f
  }
}