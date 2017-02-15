package org.clapper.scalasti

import org.scalatest._
import matchers._

import scala.util.control.NonFatal

/** Various custom ScalaTest matchers
  */
trait CustomMatchers {
  class SuccessfulRenderMatcher(expectedValue: String) extends Matcher[ST] {
    def apply(template: ST): MatchResult = {
      val ok = template
        .render()
        .map { _ => true }
        .recover {
          case NonFatal(e) => false
        }
        .get

      MatchResult(
        ok,
        "Template did not render properly",
        "Template rendered properly"
      )
    }
  }

  def renderSuccessfullyAs(expected: String): Matcher[ST] = {
    new SuccessfulRenderMatcher(expected)
  }
}
