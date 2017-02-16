package org.clapper.scalasti

import java.util.{ArrayList => JArrayList, HashMap => JHashMap, List => JList, Map => JMap}

import org.clapper.classutil.{ClassUtil, ScalaObjectToBean}


/** Functions for mapping Scala to Java objects, isolated here for safety
  * and API clarity.
  */
private[scalasti] object ScalaToJava {

  /** Converts a value to a Java object.
    *
    * @param v  the Scala value
    *
    * @return a Java object
    */
  def anyToJava(v: Any, raw: Boolean = false): AnyRef = {
    import ClassUtil.isPrimitive
    v match {
      case Some(x)             => anyToJava(x, raw)
      case None                => null
      case seq: Seq[_]         => seqToJava(seq, raw)
      case it:  Iterator[_]    => iterToJava(it, raw)
      case map: Map[_, _]      => mapToJava(
                                    map.asInstanceOf[Map[String, AnyRef]]
                                  )
      case _: String           => v.asInstanceOf[AnyRef]
      case _: Number           => v.asInstanceOf[AnyRef]
      case o if isPrimitive(o) => v.asInstanceOf[AnyRef]
      case _: Any              => if (raw) v.asInstanceOf[AnyRef]
                                  else ScalaObjectToBean(v)
    }
  }

  /** Maps a Scala map of attributes into a Java map of attributes. The
    * Scala map is converted to a `java.util.HashMap`. The keys are
    * assumed to be strings. The values are mapped as follows:
    *
    * - A Scala `Seq` (which includes lists and array buffers) is mapped to
    *   a `java.util.List`, so it's treated as a multivalued attribute by the
    *   underlying ST library.
    * - A Scala iterator is also mapped to a `java.util.List`.
    * - Anything else is treated as a single-valued object.
    *
    * @param map  The Scala map to convert.
    *
    * @return the Java map
    */
  private def mapToJava(map: Map[String, AnyRef]): JMap[String, AnyRef] = {
    val result = new JHashMap[String, AnyRef]

    for { (name, value) <- map }
      result.put(name, anyToJava(value))

    result
  }

  /** Convert a Scala sequence to a Java list.
    *
    * @param seq  the sequence
    *
    * @return the list
    */
  private def seqToJava(seq: Seq[Any], raw: Boolean): JList[AnyRef] = {
    val list = new JArrayList[AnyRef]
    for (a <- seq)
      if (raw)
        list.add(a.toString)
      else
        list.add(anyToJava(a))

    list
  }

  /** Convert a Scala iterator to a Java list.
    *
    * @param it  the iterator
    *
    * @return the Java equivalent
    */
  private def iterToJava(it: Iterator[Any], raw: Boolean): JList[AnyRef] = {
    seqToJava(it.toSeq, raw)
  }
}

