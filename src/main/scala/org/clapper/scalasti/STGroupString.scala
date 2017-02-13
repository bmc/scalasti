package org.clapper.scalasti

import org.stringtemplate.v4.{STGroupString => _STGroupString}

/** `STGroupString` wraps the StringTemplate API's `STGroupString` class.
  * An `STGroupString` object reads a template group from a string. See the
  * StringTemplate API documentation for details. Since `STGroupString` a
  * subclass of `STGroup`, all the methods on the parent class are available
  * on this one. This class cannot be instantiated directly; use the `apply()`
  * methods on the companion object.
  */
class STGroupString private[scalasti](native: _STGroupString)
  extends STGroup(nativeOpt = Some(native))

/** Companion object for `STGroupString`. This object provides `apply()`
  * methods for instantiating `STGroupString` objects.
  */
object STGroupString {

  /** Create an `STGroupString`.
    *
    * @param text                the text of the template group
    * @param sourceName          the name to assign the group, if any
    * @param delimiterStartChar  the starting delimiter character
    * @param delimiterStopChar   the ending delimiter character
    *
    * @return the group
    */
  def apply(text:               String,
            sourceName:         String = "<string>",
            delimiterStartChar: Char = Constants.DefaultStartChar,
            delimiterStopChar:  Char = Constants.DefaultStopChar):
    STGroupString = {

    val native = new _STGroupString(sourceName,
                                    text,
                                    delimiterStartChar,
                                    delimiterStopChar)
    new STGroupString(native)
  }
}
