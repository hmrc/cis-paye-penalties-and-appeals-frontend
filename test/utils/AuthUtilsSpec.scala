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
import models.EmployerReference
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}

class AuthUtilsSpec extends SpecBase {

  private def cisOrg(number: String = "754", reference: String = "EZ00100", state: String = "Activated") =
    Enrolment("HMRC-CIS-ORG", Seq(EnrolmentIdentifier("TaxOfficeNumber", number), EnrolmentIdentifier("TaxOfficeReference", reference)), state)

  "contractorReference" - {

    "must return the tax office number and reference from an activated HMRC-CIS-ORG enrolment" in {
      AuthUtils.contractorReference(Set(cisOrg())) mustBe Some(EmployerReference("754", "EZ00100"))
    }

    "must return None when there is no HMRC-CIS-ORG enrolment" in {
      AuthUtils.contractorReference(Set(Enrolment("IR-PAYE-AGENT", Seq(EnrolmentIdentifier("IRAgentReference", "ABC123")), "Activated"))) mustBe None
    }

    "must return None when the enrolment is not activated" in {
      AuthUtils.contractorReference(Set(cisOrg(state = "Inactive"))) mustBe None
    }

    "must return None when the tax office number is not 3 digits" in {
      AuthUtils.contractorReference(Set(cisOrg(number = "75"))) mustBe None
      AuthUtils.contractorReference(Set(cisOrg(number = "75A"))) mustBe None
    }

    "must return None when an identifier is missing" in {
      val noReference = Enrolment("HMRC-CIS-ORG", Seq(EnrolmentIdentifier("TaxOfficeNumber", "754")), "Activated")
      AuthUtils.contractorReference(Set(noReference)) mustBe None
    }
  }

  private def irPaye(number: String = "754", reference: String = "EZ00100", state: String = "Activated") =
    Enrolment("IR-PAYE", Seq(EnrolmentIdentifier("TaxOfficeNumber", number), EnrolmentIdentifier("TaxOfficeReference", reference)), state)

  "payeEmployerReference" - {

    "must return the tax office number and reference from an activated IR-PAYE enrolment" in {
      AuthUtils.payeEmployerReference(Set(irPaye())) mustBe Some(EmployerReference("754", "EZ00100"))
    }

    "must return None when there is no IR-PAYE enrolment" in {
      AuthUtils.payeEmployerReference(Set(cisOrg())) mustBe None
      AuthUtils.payeEmployerReference(Set.empty) mustBe None
    }

    "must return None when the enrolment is not activated" in {
      AuthUtils.payeEmployerReference(Set(irPaye(state = "Inactive"))) mustBe None
    }

    "must return None when the tax office number is not 3 digits" in {
      AuthUtils.payeEmployerReference(Set(irPaye(number = "75"))) mustBe None
      AuthUtils.payeEmployerReference(Set(irPaye(number = "7544"))) mustBe None
      AuthUtils.payeEmployerReference(Set(irPaye(number = "75A"))) mustBe None
    }

    "must return None when the reference is blank" in {
      AuthUtils.payeEmployerReference(Set(irPaye(reference = "  "))) mustBe None
    }

    "must return None when an identifier is missing" in {
      val noReference = Enrolment("IR-PAYE", Seq(EnrolmentIdentifier("TaxOfficeNumber", "754")), "Activated")
      AuthUtils.payeEmployerReference(Set(noReference)) mustBe None
    }

    "must not accept an HMRC-CIS-ORG enrolment as a PAYE employer" in {
      AuthUtils.payeEmployerReference(Set(cisOrg())) mustBe None
    }
  }
}
