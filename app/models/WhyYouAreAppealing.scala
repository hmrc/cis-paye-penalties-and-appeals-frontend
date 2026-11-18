/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Hint, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait WhyYouAreAppealing

object WhyYouAreAppealing extends Enumerable.Implicits {

  case object Unavailable extends WithName("21") with WhyYouAreAppealing
  case object Contractor extends WithName("18") with WhyYouAreAppealing
  case object NoLongerCis extends WithName("20") with WhyYouAreAppealing
  case object FiledReturnOnTime extends WithName("5") with WhyYouAreAppealing
  case object SeriousIllness extends WithName("7") with WhyYouAreAppealing
  case object IssuesWithComputer extends WithName("8") with WhyYouAreAppealing
  case object Victim extends WithName("13") with WhyYouAreAppealing
  case object SomeoneDied extends WithName("3") with WhyYouAreAppealing
  case object FireFlood extends WithName("6") with WhyYouAreAppealing
  case object Divider extends WithName("divider") with WhyYouAreAppealing
  case object NoneOfThese extends WithName("17") with WhyYouAreAppealing

  val values: Seq[WhyYouAreAppealing] = Seq(
    Unavailable, Contractor, NoLongerCis, FiledReturnOnTime, SeriousIllness, IssuesWithComputer,
    Victim, SomeoneDied, FireFlood, Divider, NoneOfThese
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (Divider, index) =>
      RadioItem(
        content = Text(messages("site.or")),
        id      = Some(s"value_$index"),
        divider = Some(messages("site.or"))
      )
    case (NoneOfThese, index) =>
      RadioItem(
        content = Text(messages(s"whyYouAreAppealing.${NoneOfThese.toString}")),
        value   = Some(NoneOfThese.toString),
        id      = Some(s"value_$index"),
        hint    = Some(Hint(content = Text(messages(s"whyYouAreAppealing.17.hint"))))
      )
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"whyYouAreAppealing.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[WhyYouAreAppealing] =
    Enumerable(values.map(v => v.toString -> v): _*)
}