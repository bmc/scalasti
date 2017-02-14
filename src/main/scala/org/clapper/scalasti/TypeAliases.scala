package org.clapper.scalasti

import org.stringtemplate.v4.{AttributeRenderer => _STAttrRenderer}

/** Miscellaneous type aliases and related definitions, used primarily to
  * simplify type signatures in function and method definitions.
  */
object TypeAliases {

  /** An attribute renderer, as stored internally within StringTemplate.
    */
  type AttrRenderers = Map[Class[_], _STAttrRenderer]

  /** An attribute map, used within a template.
    */
  type AttrMap = Map[String, Any]

  /** An empty attribute map.
    */
  val EmptyAttrMap = Map.empty[String, Any]
}
