package org.clapper.scalasti

import org.stringtemplate.v4.misc.STMessage

/** An error listener that throws any errors it gets as exceptions.
  */
class ThrowExceptionErrorListener extends STErrorListener {

  def compileTimeError(msg: STMessage): Unit = {
    throw new Exception(msg.toString)
  }

  def runTimeError(msg: STMessage): Unit = {
    throw new Exception(msg.toString)
  }

  def IOError(msg: STMessage): Unit = {
    throw new Exception(msg.toString)
  }

  def internalError(msg: STMessage): Unit = {
    throw new Exception(msg.toString)
  }
}
