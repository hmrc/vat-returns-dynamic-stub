/*
 * Copyright 2019 HM Revenue & Customs
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

package config

import javax.inject.Inject

import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Result}
import play.api.mvc.Results._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.http.JsonErrorHandler

import scala.concurrent.{ExecutionContext, Future}

class ApiErrorHandler @Inject()(configuration: Configuration, auditConnector: AuditConnector)(implicit ec: ExecutionContext)
  extends JsonErrorHandler(configuration, auditConnector) {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    super.onClientError(request, statusCode, message).map { result =>
      message match {
        case "ERROR_VRN_INVALID" => invalidVrnResponse
        case "INVALID_DATE_FROM" => invalidDateFromResponse
        case "INVALID_DATE_TO" => invalidDateToResponse
        case "INVALID_DATE_RANGE" => invalidDateRangeResponse
        case "INVALID_STATUS" => invalidStatusResponse
        case _ => result
      }
    }
  }

  private val invalidVrnResponse = BadRequest(Json.obj(
    "code" -> "VRN_INVALID",
    "message" -> "The supplied VRN is invalid."
  ))

  private val invalidDateFromResponse = BadRequest(Json.obj(
    "code" -> "INVALID_DATE_FROM",
    "message" -> "The supplied date from is invalid."
  ))

  private val invalidDateToResponse = BadRequest(Json.obj(
    "code" -> "INVALID_DATE_TO",
    "message" -> "The supplied date to is invalid."
  ))

  private val invalidDateRangeResponse = BadRequest(Json.obj(
    "code" -> "INVALID_DATE_RANGE",
    "message" -> "The supplied date range is invalid."
  ))

  private val invalidStatusResponse = BadRequest(Json.obj(
    "code" -> "INVALID_STATUS",
    "message" -> "The supplied status is invalid."
  ))

}
