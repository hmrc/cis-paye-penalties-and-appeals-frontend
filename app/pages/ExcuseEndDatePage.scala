package pages

import pages.QuestionPage
import play.api.libs.json.JsPath

import java.time.LocalDate

case object ExcuseEndDatePage extends QuestionPage[LocalDate] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "excuseEndDate"
}
