package org.clapper.scalasti

import org.scalatest.{FlatSpec, Matchers}

import java.io._

import grizzled.file.util.{joinPath, withTemporaryDirectory, dirname}
import grizzled.util.withResource
import grizzled.util.CanReleaseResource.Implicits.CanReleaseCloseable

/** Base class for testers
  */
class BaseSpec extends FlatSpec with Matchers {

  /** Sets up a temporary directory for a template group. In all cases, the
    * filenames can be paths, though they must not be absolute.
    *
    * @param group     the (filename, groupString) for the template group
    * @param templates a sequence of (filename, templateString) pairs for the
    *                  corresponding templates.
    * @param encoding  the encoding to use to read and write the files
    * @param code      the block of code to run after everything is created.
    *                  It will receive the `STGroup` object.
    */
  protected def withTemplateGroup(group: (String, String),
                                  templates: Seq[(String, String)],
                                  encoding: String = "ASCII")
                                 (code: STGroup => Unit): Unit = {
    withTemporaryDirectory("scalasti") { dir =>

      def makeParentDir(path: File) = {
        val dir = dirname(path.getPath)
        if ((dir != ".") && (dir != "..")) {
          val fDir = new File(dir)
          fDir.mkdirs()
          if (! fDir.exists)
            throw new RuntimeException(s"Can't create $dir")
        }
      }

      def openWithEncoding(path: File, encoding: String) = {
        makeParentDir(path)
        new PrintWriter(
          new OutputStreamWriter(new FileOutputStream(path), encoding)
        )
      }

      val (pathString, groupString) = group
      require(! (pathString startsWith File.separator))
      val groupPath = new File(joinPath(dir.getPath, pathString))

      withResource(openWithEncoding(groupPath, encoding)) { out =>
        out.println(groupString)
      }

      for ((path, templateString) <- templates) {
        val templatePath = new File(joinPath(dir.getPath, path))
        require(! (path startsWith File.separator))
        withResource(openWithEncoding(templatePath, encoding)) { out =>
          out.println(templateString)
        }
      }

      code(STGroupFile(groupPath.toURI.toURL))
    }
  }
}

