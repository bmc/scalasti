package org.clapper.scalasti

import org.stringtemplate.v4.{AttributeRenderer => _STAttrRenderer}

/** Miscellaneous type aliases and related definitions, used primarily to
  * simplify type signatures in function and method definitions.
  */
object TypeAliases {

  /** An attribute to be mapped when applied to an underlying template.
    *
    * @param value  the attribute value
    * @param raw    whether it's a raw value to be applied as is, or a value
    *               to be mapped to a JavaBean
    */
  case class Attribute(value: Any, raw: Boolean)

  /** An attribute renderer, as stored internally within StringTemplate.
    */
  type AttrRenderers = Map[Class[_], _STAttrRenderer]

  /** An attribute map, used within a template.
    */
  type AttrMap = Map[String, Attribute]

  /** An empty attribute map.
    */
  val EmptyAttrMap = Map.empty[String, Attribute]
}
