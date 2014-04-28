package org.clapper.scalasti

import org.stringtemplate.v4.{STGroupDir => _STGroupDir}
import java.io.File
import java.net.URL
import org.antlr.runtime.Token

/** `STGroupDir` wraps the StringTemplate API's `STGroupDir` class. An
  * `STGroupDir` specifies a directory or directory tree full of templates
  * and/or group files, with the files loaded on demand. As a subclass of
  * `STGroup`, an `STGroupDir` contains all the methods of the parent class.
  * This class cannot be instantiated directly; use the `apply()` methods on
  * the companion object.
  */
class STGroupDir private[scalasti](native: _STGroupDir)
  extends STGroup(native)

/** Companion object for `STGroupDir`. This object provides `apply()` methods
  * for instantiating `STGroupDir` objects.
  */
object STGroupDir {

  /** Create an `STGroupDir` that reads its templates and groups from the
    * directory rooted at the specified URL. Note that this constructor
    * throws no errors. Instead, the errors are caught when templates are
    * later instantiated via `instanceOf()`.
    *
    * @param root                the root URL
    * @param encoding            the encoding
    * @param delimiterStartChar  the starting delimiter character
    * @param delimiterStopChar   the ending delimiter character
    *
    * @return the group
    */
  def apply(root:               URL,
            encoding:           String = Constants.DefaultEncoding,
            delimiterStartChar: Char   = Constants.DefaultStartChar,
            delimiterStopChar:  Char   = Constants.DefaultStopChar):
    STGroupDir = {

    val native = new _STGroupDir(root,
                                 encoding,
                                 delimiterStartChar,
                                 delimiterStopChar)
    return new STGroupDir(native)
  }

/** Create an `STGroupDir` that reads its templates and groups from the
  * the specified directory rooted at the specified URL. Note that this
  * constructor throws no errors. Instead, the errors are caught when templates
  * are later instantiated via `instanceOf()`.
  *
  * @param path the path to the root directory
  *
  * @return the group
  */
  def apply(path: String): STGroupDir = {
    apply(path,
          Constants.DefaultEncoding,
          Constants.DefaultStartChar,
          Constants.DefaultStopChar)
  }

  /** Create an `STGroupDir` that reads its templates and groups from the
    * the specified directory rooted at the specified URL. Note that this
    * constructor throws no errors. Instead, the errors are caught when templates
    * are later instantiated via `instanceOf()`.
    *
    * @param path               the path to the root directory
    * @param encoding           the encoding to assume
    * @param delimiterStartChar the starting delimiter character
    * @param delimiterStopChar  the ending delimiter character
    *
    * @return the group
    */
  def apply(path:               String,
            encoding:           String,
            delimiterStartChar: Char,
            delimiterStopChar:  Char): STGroupDir = {

    apply(new File(path).toURI.toURL,
          encoding,
          delimiterStartChar,
          delimiterStopChar)
  }
}
