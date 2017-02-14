package org.clapper.scalasti

import org.stringtemplate.v4.{AttributeRenderer => _STAttrRenderer, STGroup => _STGroup}

import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe.runtimeMirror
import scala.util.Try
import scala.collection.JavaConverters._
import java.net.URL

import org.stringtemplate.v4.misc.ErrorManager

import scala.util.control.NonFatal

/** A Scala wrapper for the String Template library's `STGroup` class. This
  * class provides access to most of the methods on the underlying class,
  * with Scala semantics, where appropriate. This class cannot be instantiated
  * directly; use the `apply()` methods on the companion object, or create one
  * of the subclasses.
  *
  * This class does not expose all of the underlying functions of the actual
  * StringTemplate API. If you need access to the full Java StringTemplate API,
  * you can retrieve the underlying `STGroup` by calling the `nativeGroup`
  * method.
  *
  * '''WARNING''': This API presents an ''immutable'' view of the
  * StringTemplate API. Calling update functions (like [[load]](),
  * [[unload]](), [[registerRenderer]](), etc.) copy both the Scalasti
  * object ''and'' the underlying StringTemplate object it wraps. Dropping down
  * to * the native StringTemplate, while supported, bypasses all immutability
  * protections. It also means you're now interacting with the StringTemplate
  * library, which expects objects with Java semantics, not Scala semantics.
  */
case class STGroup(
  delimiterStartChar: Char = Constants.DefaultStartChar,
  delimiterStopChar:  Char = Constants.DefaultStopChar,
  private[scalasti] val native: _STGroup,
  private[scalasti] val attrRenderers: Map[Class[_], _STAttrRenderer] =
    Map.empty[Class[_], _STAttrRenderer],
  private[scalasti] val loaded: Boolean = false
) {

  applyRenderers(native)
  makeErrorsExceptions(native)

  import TypeAliases._

  /** Get the underlying Java StringTemplate `STGroup` object.
    *
    * @return the underlying `STGroup`
    */
  def nativeGroup: _STGroup = native

  /** Get the template names defined by the group. This function calls
    * `[[load]]()`, if it hasn't already been called. Thus, calling this
    * function on an unloaded group causes a temporary, loaded group to be
    * created, then thrown away.
    *
    * NOTE: Currently, because of the way StringTemplate is implemented,
    * this method only returns templates defined in the group itself.
    * Templates that are loaded via "import" will not be returned here.
    * That's a StringTemplate limitation, not a Scalasti limitation.
    *
    * @return a set of the template names supplied by this group
    */
  def templateNames: Set[String] = {
    val underlying: _STGroup = if (loaded) {
      native
    }
    else {
      load().map(_.native)
            .recover { case NonFatal(_) => this.native }
            .get
    }

    underlying.getTemplateNames.asScala.toSet
  }

  /** Determine whether a named template is defined in this group. The names
    * must be fully-qualified template paths (e.g., "/g1/name").
    *
    * @param name  the template name
    *
    * @return `true` if defined, `false` if not
    */
  def isDefined(name: String): Boolean = native.isDefined(name)

  /** Get the root directory, if this is the group directory, or the group
    * file, if this is a group file.
    *
    * @return the root
    */
  def rootDirURL: URL = native.getRootDirURL

  /** Get the group's name.
    *
    * @return the group name
    */
  def name: String = native.getName

  /** Get the group file name.
    *
    * @return the file name
    */
  def fileName: String = native.getFileName

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
  def load(): Try[STGroup] = Try {
    val underlying = cloneUnderlying()
    underlying.load()
    this.copy(native = underlying, loaded = true)
  }

  /** Force an unload. Returns a new `STGroup` with an unloaded native
    * StringTemplate `STGroup`.
    *
    * @return the new object
    */
  def unload(): STGroup = {
    val underlying = cloneUnderlying()
    underlying.unload()
    this.copy(native = underlying, loaded = false)
  }

  /** Get an instance of a template defined in the group.
    *
    * @param templateName  the name of the template
    *
    * @return `Success(ST)` if the template was found and loaded.
    *         `Failure(exception)` if the template could not be demand-loaded.
    */
  def instanceOf(templateName: String): Try[ST] = {

    def retrieveTemplate(): Try[ST] = Try {
      val opt = for { cst      <- Option(native.lookupTemplate(templateName))
                      nativeST <- Option(native.getInstanceOf(templateName)) }
        yield new ST(native       = nativeST,
                     attributeMap = EmptyAttrMap,
                     template     = cst.template)

      opt.getOrElse {
        throw new Exception(s"Unable to get an instance of $templateName")
      }
    }

    for { _  <- load()
          st <- retrieveTemplate() }
      yield st
  }

  /** Register a renderer for a particular type. When the underlying
    * StringTemplate API attempts to render a template, it'll use this
    * renderer when it encounters values of this type.
    *
    * '''NOTE''': By default, Scalasti automatically wraps Scala objects
    * in dynamically generated Java Beans when they're added to templates,
    * because the StringTemplate API uses Java Bean semantics to access
    * object fields. For this reason, you cannot use an `AttributeRenderer`
    * unless you add the values it is to render as raw objects. See the
    * `ST.add()` method for details.
    *
    * @param r   the renderer
    * @tparam T  the type
    *
    * @return a new `STGroup` object with the new renderer
    *
    * @see [[ST.add]]
    */
  def registerRenderer[T: ru.TypeTag](r: AttributeRenderer[T]): STGroup = {
    val tpe = ru.typeTag[T].tpe
    val cls = runtimeMirror(r.getClass.getClassLoader).runtimeClass(tpe)
    val newRenderers = this.attrRenderers + (cls -> r.stRenderer)
    val newUnderlying = cloneUnderlying(newRenderers)
    this.copy(native        = newUnderlying,
              attrRenderers = newRenderers,
              loaded        = this.loaded)
  }

  /** Get the attribute renders. Note that the attribute attrRenderers returned
    * are the underlying StringTemplate attribute attrRenderers, not the
    * Scalasti attrRenderers. This method is intended primarily for debugging use.
    *
    * @return A map of the attribute renders, which might be empty
    */
  def renderers: Map[Class[_], _STAttrRenderer] = attrRenderers

  // --------------------------------------------------------------------------
  // Protected methods
  // --------------------------------------------------------------------------

  /** Create a new underlying StringTemplate object, applying whatever
    * constructor parameters were used with the current object. Does not
    * apply the attrRenderers.
    *
    * Subclasses should override this method.
    *
    * @return the new underlying object
    */
  protected[this] def newUnderlying: _STGroup = {
    new _STGroup(delimiterStartChar, delimiterStopChar)
  }

  // --------------------------------------------------------------------------
  // Private methods
  // --------------------------------------------------------------------------

  /** Create a new clone of the underlying StringTemplate `STGroupFile` object,
    * applying all attrRenderers.
    *
    * @param renderers  the renderers to apply. Defaults to this object's.
    *
    * @return the clone
    */
  private def cloneUnderlying(renderers: AttrRenderers = this.attrRenderers):
    _STGroup = {

    val underlying = newUnderlying
    makeErrorsExceptions(underlying)
    applyRenderers(underlying, renderers)
    underlying
  }

  /** Ensure that any errors that occur during template processing result
    * in a thrown exception that can be captured in a `Try`.
    *
    * @param underlying the native `STGroup` object whose error handling is to
    *                   be changed
    */
  private def makeErrorsExceptions(underlying: _STGroup): Unit = {
    underlying.errMgr = new ErrorManager(new ThrowExceptionErrorListener)
  }

  /** Apply all attrRenderers to an underlying StringTemplate `STGroup` (or
    * derived class).
    *
    * @param underlying the underlying `STGroup`
    * @param renderers  the renderers to apply. Defaults to this object's.
    */
  private def applyRenderers(
    underlying: _STGroup,
    renderers:  Map[Class[_], _STAttrRenderer] = this.attrRenderers): Unit = {

    for ( (cls, renderer) <- renderers ) {
      underlying.registerRenderer(cls, renderer)
    }
  }
}

