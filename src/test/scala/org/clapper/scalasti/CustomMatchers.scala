package org.clapper.scalasti

import org.scalatest._
import matchers._

import scala.util.control.NonFatal

/** Various custom ScalaTest matchers
  */
trait CustomMatchers {
  class SuccessfulRenderMatcher(expected: String) extends Matcher[ST] {
    private val OkMessage = "Template rendered superbly."

    def apply(template: ST): MatchResult = {
      template
        .render()
        .map { s =>
          MatchResult(
            s == expected,
            s"""Template rendered to "$s". Expected "$expected".""",
            OkMessage
          )
        }
        .recover {
          case NonFatal(e) =>
            MatchResult(
              false,
              s"""render() failed: ${e.getMessage}""",
              OkMessage
            )
        }
        .get
    }
  }

  def renderSuccessfullyAs(expected: String): Matcher[ST] = {
    new SuccessfulRenderMatcher(expected)
  }
}
