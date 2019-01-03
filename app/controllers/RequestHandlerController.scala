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

package controllers

import javax.inject.Inject

import models.DataModel
import models.HttpMethod._
import play.api.mvc.{Action, AnyContent, Result}
import repositories.DataRepository
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global

class RequestHandlerController @Inject()(dataRepository: DataRepository) extends BaseController {

  def getRequestHandler(url: String): Action[AnyContent] = Action.async { implicit request =>

    val dataNotUsingQueryStringParameters =
      dataRepository.find("_id" -> s"""${request.uri.takeWhile(_ != '?')}""", "method" -> GET)
    val dataUsingQueryStringParameters =
      dataRepository.find("_id" -> request.uri, "method" -> GET)

    def getResult(data: List[DataModel]): Result = data match {
      case head :: _ if head.response.nonEmpty => Status(head.status)(head.response.get) //return status and body
      case head :: _ => Status(head.status) //Only return status, no body.
      case _ => NotFound(errorResponseBody(request.uri))
    }

    for {
      dataBasedOnUrlPath <- dataNotUsingQueryStringParameters
      dataBasedOnCompleteUri <- dataUsingQueryStringParameters
    } yield {
      if (dataBasedOnCompleteUri.nonEmpty) getResult(dataBasedOnCompleteUri) else getResult(dataBasedOnUrlPath)
    }
  }

  def postRequestHandler(url: String): Action[AnyContent] = Action.async { implicit request =>
    dataRepository.find("_id" -> s"""${request.uri.takeWhile(_ != '?')}""", "method" -> POST).map {
      case head :: _ if head.response.nonEmpty => Status(head.status)(head.response.get) //return status and body
      case head :: _ => Status(head.status) //Only return status, no body.
      case _ => NotFound(errorResponseBody(request.uri))
    }
  }

  def errorResponseBody(path: String): JsValue = {
    Json.obj(
      "status" -> "404",
      "message" -> s"Could not find endpoint in Dynamic Stub matching the URI: $path",
      "path" -> path
    )
  }
}
