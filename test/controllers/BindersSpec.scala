/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers

import java.time.LocalDate

import models.ReturnsQueryParams
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.play.test.UnitSpec

class BindersSpec extends UnitSpec with GuiceOneAppPerSuite {

  "VRN path binder" when {

    "it receives valid VRN" should {

      "bind the value to a `Vrn` instance" in {
        import play.api.mvc.PathBindable.bindableString

        val result = Binders.vrnBinder.bind("vrn", "123456789")

        result shouldBe Right(Vrn("123456789"))
      }

      "unbind the Vrn to a string" in {
        import play.api.mvc.PathBindable.bindableString

        val result = Binders.vrnBinder.unbind("vrn", Vrn("123456789"))

        result shouldBe "123456789"
      }

    }

    "it receives an invalid VRN" should {

      "return an error code" in {
        import play.api.mvc.PathBindable.bindableString

        val result = Binders.vrnBinder.bind("vrn", "abc")

        result shouldBe Left("ERROR_VRN_INVALID")
      }

    }

  }

  "Returns query binder" when {

    "it receives a valid returns query" should {

      "bind to an `ReturnsQueryParams` instance" in {
        import play.api.mvc.QueryStringBindable.bindableString

        val result = Binders.returnsQueryParamsBinder.bind("", Map(
          "from" -> Seq("2017-01-01"),
          "to" -> Seq("2017-03-31"),
          "status" -> Seq("O")
        ))

        result shouldBe Some(Right(ReturnsQueryParams(
          LocalDate.parse("2017-01-01"),
          LocalDate.parse("2017-03-31"),
          "O"
        )))
      }

      "unbind the ReturnsQueryParams to a string for its keys" in {
        import play.api.mvc.QueryStringBindable.bindableString

        val result = Binders.returnsQueryParamsBinder.unbind("from", ReturnsQueryParams(
          LocalDate.parse("2017-01-01"),
          LocalDate.parse("2017-03-31"),
          "O"
        ))

        result shouldBe "from=2017-01-01"
      }

    }

    "it receives an invalid returns query" should {

      "return an invalid date from" in {
        import play.api.mvc.QueryStringBindable.bindableString

        val result = Binders.returnsQueryParamsBinder.bind("", Map(
          "from" -> Seq("abc"),
          "to" -> Seq("2017-03-31"),
          "status" -> Seq("O")
        ))

        result shouldBe Some(Left("INVALID_DATE_FROM"))
      }

      "return an invalid date to" in {
        import play.api.mvc.QueryStringBindable.bindableString

        val result = Binders.returnsQueryParamsBinder.bind("", Map(
          "from" -> Seq("2017-01-01"),
          "to" -> Seq("abc"),
          "status" -> Seq("O")
        ))

        result shouldBe Some(Left("INVALID_DATE_TO"))
      }

      "return an invalid status" in {
        import play.api.mvc.QueryStringBindable.bindableString

        val result = Binders.returnsQueryParamsBinder.bind("", Map(
          "from" -> Seq("2017-01-01"),
          "to" -> Seq("2017-03-31"),
          "status" -> Seq("1")
        ))

        result shouldBe Some(Left("INVALID_STATUS"))
      }

    }

  }

}
