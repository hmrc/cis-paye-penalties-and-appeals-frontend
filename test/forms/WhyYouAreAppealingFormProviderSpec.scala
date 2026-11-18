package forms

import forms.behaviours.OptionFieldBehaviours
import models.WhyYouAreAppealing
import play.api.data.FormError

class WhyYouAreAppealingFormProviderSpec extends OptionFieldBehaviours {

  val form = new WhyYouAreAppealingFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "whyYouAreAppealing.error.required"

    behave like optionsField[WhyYouAreAppealing](
      form,
      fieldName,
      validValues  = WhyYouAreAppealing.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
