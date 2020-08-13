/*
 * Copyright 2020 HM Revenue & Customs
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

package testUtils

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext

trait TestSupport extends UnitSpec with GuiceOneAppPerSuite with MaterializerSupport {

  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  lazy val cc: ControllerComponents = stubControllerComponents()

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")
}
