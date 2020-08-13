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

package controllers

import javax.inject.Inject
import models.HttpMethod._
import models.{DataModel, ErrorResponse}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import repositories.DataRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.SchemaValidation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RequestHandlerController @Inject()(dataRepository: DataRepository,
                                         schemaValidation: SchemaValidation,
                                         cc: ControllerComponents) extends BackendController(cc) {

  def getRequestHandler(url: String): Action[AnyContent] = Action.async { implicit request =>

    val dataNotUsingQueryStringParameters =
      dataRepository.find("_id" -> s"""${request.uri.takeWhile(_ != '?')}""", "method" -> GET)
    val dataUsingQueryStringParameters =
      dataRepository.find("_id" -> request.uri, "method" -> GET)

    def getResult(data: Option[DataModel]): Result = data match {
      case Some(result) if result.response.nonEmpty => Status(result.status)(result.response.get)
      case Some(result) => Status(result.status)
      case _ => NotFound(
        Json.toJson(ErrorResponse(
          NOT_FOUND.toString,
          s"Could not find endpoint in Dynamic Stub matching the URI: ${request.uri}"
        ))
      )
    }

    for {
      dataBasedOnUrlPath <- dataNotUsingQueryStringParameters
      dataBasedOnCompleteUri <- dataUsingQueryStringParameters
    } yield {
      if (dataBasedOnCompleteUri.nonEmpty) getResult(dataBasedOnCompleteUri) else getResult(dataBasedOnUrlPath)
    }
  }

  def postRequestHandler(url: String): Action[AnyContent] = Action.async { implicit request =>
    dataRepository.find("_id" -> request.uri, "method" -> POST) flatMap {
      case Some(stubData) =>
        stubData.schemaId match {
          case Some(schemaId) =>
            schemaValidation.validateRequestJson(schemaId, request.body.asJson) map {
              case true => Status(stubData.status)(stubData.response.getOrElse(JsObject(Seq.empty)))
              case false => BadRequest(
                Json.toJson(ErrorResponse(
                  BAD_REQUEST.toString,
                  s"Request did not validate against schema: $schemaId. Request: ${request.body.asJson}"
                ))
              )
            }
          case None => Future.successful(BadRequest(
            Json.toJson(ErrorResponse(
              BAD_REQUEST.toString,
              s"No schemaId found in data for URI: ${request.uri}. This is required for POST requests."
            ))
          ))
        }
      case None => Future.successful(BadRequest(
        Json.toJson(ErrorResponse(
          NOT_FOUND.toString,
          s"Could not find endpoint in Dynamic Stub matching the URI: ${request.uri}"
        ))
      ))
    }
  }
}
