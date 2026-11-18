package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class WhyYouAreAppealingSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "WhyYouAreAppealing" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(WhyYouAreAppealing.values.toSeq)

      forAll(gen) {
        whyYouAreAppealing =>

          JsString(whyYouAreAppealing.toString).validate[WhyYouAreAppealing].asOpt.value mustEqual whyYouAreAppealing
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!WhyYouAreAppealing.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[WhyYouAreAppealing] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(WhyYouAreAppealing.values.toSeq)

      forAll(gen) {
        whyYouAreAppealing =>

          Json.toJson(whyYouAreAppealing) mustEqual JsString(whyYouAreAppealing.toString)
      }
    }
  }
}
