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

package controllers

import mocks.{MockDataRepository, MockSchemaValidation}
import models.DataModel
import play.api.test.Helpers.call
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.mvc.Http.Status
import testUtils.TestSupport

class RequestHandlerControllerSpec extends TestSupport with MockDataRepository with MockSchemaValidation {

  object TestRequestHandlerController extends RequestHandlerController(mockDataRepository, mockSchemaValidation, cc)

  lazy val successModel = DataModel(
    _id = "test",
    method = "GET",
    status = Status.OK,
    response = Some(Json.parse("""{"something" : "hello"}"""))
  )

  "The getRequestHandler method" when {

    "the record is found in the database" should {

      lazy val result = {
        mockFind(Some(successModel))
        TestRequestHandlerController.getRequestHandler("/test")(FakeRequest())
      }

      "return the status code specified in the model" in {
        status(result) shouldBe Status.OK
      }

      "return the body specified in the model" in {
        await(bodyOf(result)) shouldBe s"${successModel.response.get}"
      }
    }

    "the record cannot be found in the database" should {

      lazy val result = {
        mockFind(None)
        TestRequestHandlerController.getRequestHandler("/test")(FakeRequest())
      }

      "return a 404 status" in {
        status(result) shouldBe Status.NOT_FOUND
      }

      "return the expected body when the endpoint cannot be found" in {
        await(bodyOf(result)) shouldBe s"${TestRequestHandlerController.errorResponseBody}"
      }

    }



    "return a 404 status when the endpoint cannot be found" in {
      lazy val result = TestRequestHandlerController.getRequestHandler("/test")(FakeRequest())

      mockFind(None)
      status(result) shouldBe Status.NOT_FOUND
    }
  }

  "The postRequestHandler method" when {

    "matching data is found" when {

      "the data has a schemaId" when {

        "request JSON validates against the schema" when {

          "the data has a response body" should {

            val model = DataModel(
              _id = "test",
              schemaId = Some("schemaId"),
              method = "POST",
              status = Status.OK,
              response = Some(Json.obj("hello" -> "world"))
            )

            lazy val request = FakeRequest("POST", "/").withBody(Json.obj("" -> ""))
            lazy val result = call(TestRequestHandlerController.postRequestHandler("url"), request)

            "return the status" in {
              mockFind(Some(model))
              mockValidateRequestJson("schemaId")(response = true)

              await(status(result)) shouldBe Status.OK
            }

            "return the body" in {
              await(jsonBodyOf(result)) shouldBe Json.toJson(model.response)
            }
          }

          "the data has no response body" should {

            val model = DataModel(
              _id = "test",
              schemaId = Some("schemaId"),
              method = "POST",
              status = Status.OK,
              response = None
            )

            lazy val request = FakeRequest("POST", "/").withBody(Json.toJson(model))
            lazy val result = call(TestRequestHandlerController.postRequestHandler("url"), request)

            "return the status" in {
              mockFind(Some(model))
              mockValidateRequestJson("schemaId")(response = true)

              await(status(result)) shouldBe Status.OK
            }
          }
        }

        "request JSON does not validate against schema" should {

          val model = DataModel(
            _id = "test",
            schemaId = Some("schemaId"),
            method = "POST",
            status = Status.OK,
            response = None
          )

          lazy val request = FakeRequest("POST", "/").withBody(Json.obj("" -> ""))
          lazy val result = call(TestRequestHandlerController.postRequestHandler("url"), request)

          "return 400" in {
            mockFind(Some(model))
            mockValidateRequestJson("schemaId")(response = false)

            await(status(result)) shouldBe Status.BAD_REQUEST
          }
        }
      }

      "the matching data has no schemaId" when {

        val model = DataModel(
          _id = "test",
          schemaId = None,
          method = "POST",
          status = Status.OK,
          response = None
        )

        lazy val request = FakeRequest("POST", "/").withBody(Json.obj("" -> ""))
        lazy val result = call(TestRequestHandlerController.postRequestHandler("url"), request)

        "return 400" in {
          mockFind(Some(model))

          await(status(result)) shouldBe Status.BAD_REQUEST
        }
      }
    }

    "no matching data is found" should {

      lazy val request = FakeRequest("POST", "/").withBody(Json.obj("" -> ""))
      lazy val result = call(TestRequestHandlerController.postRequestHandler("url"), request)

      "return 404" in {
        mockFind(None)

        await(status(result)) shouldBe Status.BAD_REQUEST
      }
    }
  }
}