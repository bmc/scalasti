package org.clapper.scalasti

import org.stringtemplate.v4.{STGroupDir => _STGroupDir}
import java.io.File
import java.net.URL

import scala.util.{Failure, Success, Try}

/** `STGroupDir` wraps the StringTemplate API's `STGroupDir` class. An
  * `STGroupDir` specifies a directory or directory tree full of templates
  * and/or group files, with the files loaded on demand. As a subclass of
  * `STGroup`, an `STGroupDir` contains all the methods of the parent class.
  * This class cannot be instantiated directly; use the `apply()` methods on
  * the companion object.
  */
class STGroupDir private[scalasti](native: _STGroupDir)
  extends STGroup(native = native) {

  /** Create a new underlying StringTemplate object, applying whatever
    * constructor parameters were used with the current object. Does not
    * apply the attrRenderers.
    *
    * Subclasses should override this method.
    *
    * @return the new underlying object
    */
  override protected[this] def newUnderlying: _STGroupDir = {
    new _STGroupDir(native.root,
                    native.encoding,
                    native.delimiterStartChar,
                    native.delimiterStopChar)
  }

  /** Force a load. Templates are normally loaded on demand; this method
    * attempts to load them up front. '''Note''': Even though this method
    * attempts to detect failure, the underlying StringTemplate method seems
    * to trap load errors and display them on standard output, ''without''
    * passing them up the stack. Testing for errors might not return what
    * you expect.
    *
    * @return `Success(newSTGroup)` on success. `Failure(exception)` on load
    *         failure.
    */
  override def load(): Try[STGroup] = {
    // Note that the underlying class doesn't seem to support load() at the
    // moment. So, at the very least, we can check to see whether the
    // root exists.
    def checkRoot: Try[File] = {
      val rootURL = native.root
      val scheme = rootURL.getProtocol
      val file = new File(rootURL.getFile)

      if (scheme != "file")
        Failure(new Exception(s"""STGroupDir scheme is "$scheme", not "file""""))
      else if (! file.exists)
        Failure(new Exception(s""""$file" does not exist."""))
      else if (! file.isDirectory)
        Failure(new Exception(s""""$file" is not a directory."""))
      else
        Success(file)
    }

    for { _   <- checkRoot
          grp <- super.load() }
    yield grp
  }
}

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
    * @param startDelimiter      the starting delimiter character
    * @param endDelimiter        the ending delimiter character
    *
    * @return the group
    */
  def apply(root:               URL,
            encoding:           String = Constants.DefaultEncoding,
            startDelimiter:     Char   = Constants.DefaultStartChar,
            endDelimiter:       Char   = Constants.DefaultStopChar):
    STGroupDir = {

    val native = new _STGroupDir(root,
                                 encoding,
                                 startDelimiter,
                                 endDelimiter)
    new STGroupDir(native)
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
    * @param startDelimiter the starting delimiter character
    * @param endDelimiter  the ending delimiter character
    *
    * @return the group
    */
  def apply(path:               String,
            encoding:           String,
            startDelimiter: Char,
            endDelimiter:  Char): STGroupDir = {

    apply(new File(path).toURI.toURL,
          encoding,
          startDelimiter,
          endDelimiter)
  }
}
