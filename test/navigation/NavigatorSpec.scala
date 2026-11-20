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

package navigation

import base.SpecBase
import controllers.routes
import pages._
import models._

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from ExcuseEndDateKnownPage to ExcuseEndDateController" in {
        navigator.nextPage(ExcuseEndDateKnownPage, NormalMode, UserAnswers("id")) mustBe routes.ExcuseEndDateController.onPageLoad(NormalMode)
      }

      "must go from ExcuseEndDatePage to AdditionalDetailsController" in {
        navigator.nextPage(ExcuseEndDatePage, NormalMode, UserAnswers("id")) mustBe routes.AdditionalDetailsController.onPageLoad(NormalMode)
      }

      "must go from CisContractorEndDatePage to AdditionalDetailsController" in {
        navigator.nextPage(CisContractorEndDatePage, NormalMode, UserAnswers("id")) mustBe routes.AdditionalDetailsController.onPageLoad(NormalMode)
      }

      "must go from FileReturnDatePage to AdditionalDetailsController" in {
        navigator.nextPage(FileReturnDatePage, NormalMode, UserAnswers("id")) mustBe routes.AdditionalDetailsController.onPageLoad(NormalMode)
      }

      "must go from AdditionalDetailsPage to SendYourAppealController" in {
        navigator.nextPage(AdditionalDetailsPage, NormalMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad() // TODO: DTR-9786 - update once CIS-8 Send your appeal is implemented
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from ExcuseEndDatePage to SendYourAppealController" in {
        navigator.nextPage(ExcuseEndDatePage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad() // TODO: DTR-9786 - update once CIS-8 Send your appeal is implemented
      }

      "must go from CisContractorEndDatePage to SendYourAppealController" in {
        navigator.nextPage(CisContractorEndDatePage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad() // TODO: DTR-9786 - update once CIS-8 Send your appeal is implemented
      }

      "must go from FileReturnDatePage to SendYourAppealController" in {
        navigator.nextPage(FileReturnDatePage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad() // TODO: DTR-9786 - update once CIS-8 Send your appeal is implemented
      }

      "must go from AdditionalDetailsPage to SendYourAppealController" in {
        navigator.nextPage(AdditionalDetailsPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad() // TODO: DTR-9786 - update once CIS-8 Send your appeal is implemented
      }
    }
  }
}
