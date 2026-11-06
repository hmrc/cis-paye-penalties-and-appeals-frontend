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

package utils

import base.SpecBase
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.Pagination

class PaginationHelperSpec extends AnyFreeSpec with Matchers with SpecBase {

  "getPaginationWithInfoText" - {

    "return empty state when there are 0 rows" in new Fixture {
      val result: Option[(List[Int], Option[Pagination], Option[String])] =
        getPaginationWithInfoText(
          rows = List.empty,
          totalRowCount = 0,
          paginationIndex = Some(1),
          urlSelector
        )(messages)

      val (_, paginationComponent, infoText) = result.get

      result mustBe defined

      paginationComponent mustBe None
      infoText mustBe None
    }


    "return items without pagination components if one page exists" in new Fixture {
      val result: Option[(List[Int], Option[Pagination], Option[String])] =
        getPaginationWithInfoText(
          rows = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
          totalRowCount = 10,
          paginationIndex = Some(1),
          urlSelector
        )(messages)

      val (rows, paginationComponent, infoText) = result.get

      result mustBe defined

      rows mustBe List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      paginationComponent mustBe None
      infoText mustBe None
    }

    "return items with correct pagination components on first page when multiples pages exist" in new Fixture {
      val result: Option[(List[Int], Option[Pagination], Option[String])] =
        getPaginationWithInfoText(
          rows = List.fill(10)(1),
          totalRowCount = 11,
          paginationIndex = Some(1),
          urlSelector
        )(messages)

      val (_, paginationComponent, infoText) = result.get
      val pagination: Pagination = paginationComponent.get

      result mustBe defined

      paginationComponent mustBe defined
      pagination.previous mustBe None
      pagination.next mustBe defined
      pagination.items mustBe defined
      infoText mustBe defined
    }

    "return items with correct pagination components on middle page" in new Fixture {
      val result: Option[(List[Int], Option[Pagination], Option[String])] =
        getPaginationWithInfoText(
          rows = List.fill(10)(1),
          totalRowCount = 25,
          paginationIndex = Some(2),
          urlSelector
        )(messages)

      val (_, paginationComponent, infoText) = result.get
      val pagination: Pagination = paginationComponent.get

      result mustBe defined

      paginationComponent mustBe defined
      pagination.previous mustBe defined
      pagination.next mustBe defined
      pagination.items mustBe defined
      infoText mustBe defined
    }

    "return items with correct pagination components on last page" in new Fixture {
      val result: Option[(List[Int], Option[Pagination], Option[String])] =
        getPaginationWithInfoText(
          rows = List.fill(10)(1),
          totalRowCount = 25,
          paginationIndex = Some(3),
          urlSelector
        )(messages)

      val (_, paginationComponent, infoText) = result.get
      val pagination: Pagination = paginationComponent.get

      result mustBe defined

      paginationComponent mustBe defined
      pagination.previous mustBe defined
      pagination.next mustBe None
      pagination.items mustBe defined
      infoText mustBe defined
    }

    "return None when page index is out of scope" in new Fixture {
      val result: Option[(List[Int], Option[Pagination], Option[String])] =
        getPaginationWithInfoText(
          rows = List(1, 2, 3),
          totalRowCount = 3,
          paginationIndex = Some(999),
          urlSelector
        )(messages)

      result mustBe None
    }

    "return None when page index is 0" in new Fixture {
      val result: Option[(List[Int], Option[Pagination], Option[String])] =
        getPaginationWithInfoText(
          rows = List(1, 2, 3),
          totalRowCount = 3,
          paginationIndex = Some(0),
          urlSelector
        )(messages)

      result mustBe None
    }
  }

  trait Fixture extends PaginationHelper {
    val urlSelector: Int => String = (pageIndex: Int) => controllers.routes.AppealablePenaltiesController.onPageLoad(Some(pageIndex)).url
    val application: Application = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = stubMessages()
  }
}
