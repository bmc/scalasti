package org.clapper.scalasti

import org.scalatest.{FlatSpec, Matchers}

import java.io._

import grizzled.file.util.{joinPath, withTemporaryDirectory, dirname}
import grizzled.util.withResource
import grizzled.util.CanReleaseResource.Implicits.CanReleaseCloseable

case class TemplateData(path: String, templateString: String)
case class TemplateGroupData(path:        String,
                             groupString: String,
                             templates:   Seq[TemplateData])

/** Base class for testers
  */
abstract class BaseSpec extends FlatSpec with Matchers {

  /** Open a text file for writing, with the specified encoding.
    *
    * @param path      path to the file
    * @param encoding  the encoding to use
    *
    * @return a `PrintWriter` that will write to the file
    */
  protected def openTextFileForWrite(path:     File,
                                     encoding: String): PrintWriter = {
    makeParentDir(path)
    new PrintWriter(
      new OutputStreamWriter(new FileOutputStream(path), encoding)
    )
  }

  /** Create a parent directory for a path.
    *
    * @param path  the path to the file
    */
  protected def makeParentDir(path: File): Unit = {
    val dir = dirname(path.getPath)
    if ((dir != ".") && (dir != "..")) {
      val fDir = new File(dir)
      fDir.mkdirs()
      if (! fDir.exists)
        throw new RuntimeException(s"Can't create $dir")
    }
  }

  /** Sets up a temporary directory for a template group. In all cases, the
    * filenames can be paths, though they must not be absolute.
    *
    * @param group     the template group data to be created
    * @param encoding  the encoding to use to read and write the files
    * @param code      the block of code to run after everything is created.
    *                  It will receive the `STGroup` object.
    */
  protected def withTemplateGroup(group: TemplateGroupData,
                                  encoding: String = "ASCII")
                                 (code: STGroup => Unit): Unit = {
    withTemporaryDirectory("scalasti") { dir =>

      require(! (group.path startsWith File.separator))
      val groupPath = new File(joinPath(dir.getPath, group.path))

      withResource(openTextFileForWrite(groupPath, encoding)) { out =>
        out.println(group.groupString)
      }

      for (template <- group.templates) {
        require(! (template.path startsWith File.separator))
        val templatePath = new File(joinPath(dir.getPath, template.path))
        withResource(openTextFileForWrite(templatePath, encoding)) { out =>
          out.println(template.templateString)
        }
      }

      code(STGroupFile(groupPath.toURI.toURL))
    }
  }
}

