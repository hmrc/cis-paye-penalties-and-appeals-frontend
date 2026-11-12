package viewmodels.checkAnswers.summary

import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

sealed trait SummaryRowResult
object SummaryRowResult {
  case class Row(row: SummaryListRow) extends SummaryRowResult
  case class Missing(call: Call) extends SummaryRowResult
}
