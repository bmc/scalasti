package org.clapper.scalasti

import org.scalatest.{FlatSpec, Matchers}

import java.io._

import grizzled.file.util.{joinPath, withTemporaryDirectory, dirname}
import grizzled.util.withResource
import grizzled.util.CanReleaseResource.Implicits.CanReleaseCloseable

case class TemplateData(path: String, templateString: String)
case class TemplateGroupFileData(path:        String,
                                 groupString: String,
                                 templates:   Seq[TemplateData])
case class TemplateGroupDirData(path: String, templates: Seq[TemplateData])

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

  /** Create a directory and ensure it actually exists.
    *
    * @param dir the directory
    */
  protected def mkdir(dir: String): Unit = {
    val fDir = new File(dir)
    if (! fDir.exists) {
      fDir.mkdirs()
      if (! fDir.exists)
        throw new RuntimeException(s"Can't create $dir")
    }
  }

  /** Create a parent directory for a path.
    *
    * @param path  the path to the file
    */
  protected def makeParentDir(path: File): Unit = {
    val parent = dirname(path.getPath)
    if ((parent != ".") && (parent != ".."))
      mkdir(parent)
  }


  /** Sets up a temporary directory for a template group (an `STGroupDir`),
    * then creates the template group file and any related template files. In
    * all cases, the filenames can be paths, though they must not be absolute.
    *
    * @param group     the template group data to be created
    * @param encoding  the encoding to use to read and write the files
    * @param code      the block of code to run after everything is created.
    *                  It will receive the `STGroup` object.
    */
  protected def withTemplateGroupFile(group: TemplateGroupFileData,
                                      encoding: String = "ASCII")
                                     (code: STGroupFile => Unit): Unit = {
    withTemporaryDirectory("scalasti") { dir =>

      require(! (group.path startsWith File.separator))
      val groupPath = new File(joinPath(dir.getPath, group.path))

      withResource(openTextFileForWrite(groupPath, encoding)) { out =>
        out.println(group.groupString)
      }

      createTemplates(dir, group.templates, encoding)
      code(STGroupFile(groupPath.toURI.toURL))
    }
  }

  /** Sets up a temporary directory for a template group directory (an
    * `STGroupDir`), then creates templates within the directory. In all cases,
    * the filenames can be paths, though they must not be absolute.
    *
    * @param group     the template group directory data to be created
    * @param code      the block of code to run after everything is created.
    *                  It will receive the `STGroup` object.
    */
  protected def withTemplateGroupDir(group: TemplateGroupDirData,
                                     encoding: String = "ASCII")
                                    (code: STGroupDir => Unit): Unit = {
    withTemporaryDirectory("scalasti") { dir =>
      require(! (group.path startsWith File.separator))
      val groupPath = new File(joinPath(dir.getPath, group.path))
      mkdir(groupPath.getPath)
      createTemplates(groupPath, group.templates, encoding)
      code(STGroupDir(groupPath.getPath))
    }
  }

  // --------------------------------------------------------------------------
  // Private methods
  // --------------------------------------------------------------------------

  private def createTemplates(parentDir: File,
                              templates: Seq[TemplateData],
                              encoding: String): Unit = {
    for (template <- templates) {
      require(! (template.path startsWith File.separator))
      val templatePath = new File(joinPath(parentDir.getPath, template.path))
      withResource(openTextFileForWrite(templatePath, encoding)) { out =>
        out.println(template.templateString)
      }
    }
  }
}
