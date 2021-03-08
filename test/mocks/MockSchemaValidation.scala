/*
 * Copyright 2021 HM Revenue & Customs
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

package mocks

import com.github.fge.jsonschema.main.JsonSchema
import org.scalamock.handlers.{CallHandler2, CallHandler3}
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.test.UnitSpec
import utils.SchemaValidation
import scala.concurrent.{ExecutionContext, Future}

trait MockSchemaValidation extends UnitSpec with MockFactory {

  val mockSchemaValidation: SchemaValidation = mock[SchemaValidation]

  def mockLoadResponseSchema(schemaId: String)
                            (response: JsonSchema): CallHandler2[String, ExecutionContext, Future[JsonSchema]] = {
    (mockSchemaValidation.loadResponseSchema(_: String)(_: ExecutionContext))
      .expects(schemaId, *)
      .returning(response)
  }

  def mockValidateResponseJson(schemaId: String)
                              (response: Boolean): CallHandler3[String, Option[JsValue], ExecutionContext, Future[Boolean]] = {
    (mockSchemaValidation.validateResponseJson(_: String, _: Option[JsValue])(_: ExecutionContext))
      .expects(schemaId, *, *)
      .returning(response)
  }

  def mockLoadUrlRegex(schemaId: String)
                      (response: String): CallHandler2[String, ExecutionContext, Future[String]] = {
    (mockSchemaValidation.loadUrlRegex(_: String)(_: ExecutionContext))
      .expects(schemaId, *)
      .returning(response)
  }

  def mockValidateUrlMatch(schemaId: String)
                          (response: Boolean): CallHandler3[String, String, ExecutionContext, Future[Boolean]] = {
    (mockSchemaValidation.validateUrlMatch(_: String, _: String)(_: ExecutionContext))
      .expects(schemaId, *, *)
      .returning(response)
  }

  def mockValidateRequestJson(schemaId: String)
                             (response: Boolean): CallHandler3[String, Option[JsValue], ExecutionContext, Future[Boolean]] = {
    (mockSchemaValidation.validateRequestJson(_: String, _: Option[JsValue])(_: ExecutionContext))
      .expects(schemaId, *, *)
      .returning(response)
  }
}
