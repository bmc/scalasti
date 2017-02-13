package org.clapper.scalasti

import org.stringtemplate.v4.{STGroup => _STGroup, STGroupFile => _STGroupFile}
import java.net.URL

import org.stringtemplate.v4.misc.ErrorManager

/** `STGroupFile` wraps the StringTemplate API's `STGroupFile` class.
  * An `STGroupFile` object reads a template group from a file. See the
  * StringTemplate API documentation for details. Since `STGroupFile` a
  * subclass of `STGroup`, all the methods on the parent class are available
  * on this one. This class cannot be instantiated directly; use the `apply()`
  * methods on the companion object.
  */
class STGroupFile private[scalasti](native: _STGroupFile)
  extends STGroup(nativeOpt = Some(native)) {

  /** Create a new underlying StringTemplate object, applying whatever
    * constructor parameters were used with the current object. Does not
    * apply the renderers.
    *
    * Subclasses should override this method.
    *
    * @return the new underlying object
    */
  override protected[this] def newUnderlying: _STGroup = {
    new _STGroupFile(native.url,
                     native.encoding,
                     native.delimiterStartChar,
                     native.delimiterStopChar)
  }
}

/** Companion object for `STGroupFile`. This object provides `apply()`
  * methods for instantiating `STGroupFile` objects.
  */
object STGroupFile {

  /** Create an `STGroupFile` that reads a template group from a URL, assuming
    * the default encoding, using the default start and stop characters. If
    * the URL doesn't exist, this method doesn't throw an error; instead, the
    * error shows up when you later call `instanceOf()` on the resulting group.
    * (That behavior is an artifact of the underlying StringTemplate API.)
    *
    * @param url the URL from which to read
    *
    * @return the group
    */
  def apply(url: URL): STGroupFile = {
    val native = new _STGroupFile(url,
                                  Constants.DefaultEncoding,
                                  Constants.DefaultStartChar,
                                  Constants.DefaultStopChar)
    new STGroupFile(native)
  }

  /** Create an `STGroupFile` that reads a template group from a URL, with a
    * specified encoding, using the default start and stop characters. If the
    * URL doesn't exist, this method doesn't throw an error; instead, the error
    * shows up when you later call `instanceOf()` on the resulting group. (That
    * behavior is an artifact of the underlying StringTemplate API.)
    *
    * @param url      the URL from which to read
    * @param encoding the encoding
    *
    * @return the group
    */
  def apply(url: URL, encoding: String): STGroupFile = {
    val native = new _STGroupFile(url,
                                  encoding,
                                  Constants.DefaultStartChar,
                                  Constants.DefaultStopChar)
    new STGroupFile(native)
  }

  /** Create an `STGroupFile` that reads a template group from a file, assuming
    * the default encoding, using the default start and stop characters. If
    * the file doesn't exist, this method doesn't throw an error; instead, the
    * error shows up when you later call `instanceOf()` on the resulting group.
    * (That behavior is an artifact of the underlying StringTemplate API.)
    *
    * The specified path can be absolute or relative. If it's relative, and
    * StringTemplate cannot find the file, it will look for the file as a
    * resource within the CLASSPATH.
    *
    * @param path the path of the file
    *
    * @return the group
    */
  def apply(path: String): STGroupFile = {
    val native = new _STGroupFile(path,
                                  Constants.DefaultEncoding,
                                  Constants.DefaultStartChar,
                                  Constants.DefaultStopChar)
    new STGroupFile(native)
  }

  /** Create an `STGroupFile` that reads a template group from a file, with a
    * specific encoding, using the default start and stop characters. If the
    * file doesn't exist, this method doesn't throw an error; instead, the
    * error shows up when you later call `instanceOf()` on the resulting group.
    * (That behavior is an artifact of the underlying StringTemplate API.)
    *
    * @param path     the path of the file
    * @param encoding the encoding
    *
    * @return the group
    */
  def apply(path: String, encoding: String): STGroupFile = {
    val native = new _STGroupFile(path,
                                  encoding,
                                  Constants.DefaultStartChar,
                                  Constants.DefaultStopChar)
    new STGroupFile(native)
  }

  /** Create an `STGroupFile` that reads a template group from a file, with a
    * specific encoding, start character, and stop character. If the file
    * doesn't exist, this method doesn't throw an error; instead, the error
    * shows up when you later call `instanceOf()` on the resulting group. (That
    * behavior is an artifact of the underlying StringTemplate API.)
    *
    * @param path                the path of the file
    * @param encoding            the encoding
    * @param delimiterStartChar  the starting delimiter character
    * @param delimiterStopChar   the ending delimiter character
    *
    * @return the group
    */
  def apply(path:               String,
            encoding:           String,
            delimiterStartChar: Char,
            delimiterStopChar:  Char): STGroupFile = {
    val native = new _STGroupFile(path,
                                  encoding,
                                  delimiterStartChar,
                                  delimiterStopChar)
    new STGroupFile(native)
  }
}
