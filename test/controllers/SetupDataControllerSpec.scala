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

import mocks.{MockDataRepository, MockSchemaValidation}
import models.DataModel
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.mvc.Http.Status
import testUtils.TestSupport

class SetupDataControllerSpec extends TestSupport with MockDataRepository with MockSchemaValidation {

  object TestSetupDataController extends SetupDataController(mockSchemaValidation, mockDataRepository)

  "SetupDataController .addData" when {

    "request body is valid" when {

      "method is GET or POST" when {

        "request has a schema ID" when {

          val model: DataModel = DataModel(
            _id = "1234",
            schemaId = Some("TestSchema"),
            method = "GET",
            response = Some(Json.parse("{}")),
            status = Status.OK
          )

          "response json is valid" when {

            "adding data to DB is successful" should {

              lazy val request = FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
              lazy val result = TestSetupDataController.addData(request)

              "return 200" in {
                mockValidateResponseJson("TestSchema")(response = true)
                mockAddEntry(model)(successWriteResult)

                status(result) shouldBe Status.OK
              }
            }

            "adding data to DB is unsuccessful" should {

              lazy val request = FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
              lazy val result = TestSetupDataController.addData(request)

              "return 500" in {
                mockValidateResponseJson("TestSchema")(response = true)
                mockAddEntry(model)(errorWriteResult)

                status(result) shouldBe Status.INTERNAL_SERVER_ERROR
              }
            }
          }

          "response json is not valid" should {

            lazy val request = FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
            lazy val result = TestSetupDataController.addData(request)

            "return 400" in {
              mockValidateResponseJson("TestSchema")(response = false)

              status(result) shouldBe Status.BAD_REQUEST
            }
          }
        }

        "request has no schema ID" should {

          val model: DataModel = DataModel(
            _id = "1234",
            schemaId = None,
            method = "GET",
            response = Some(Json.parse("{}")),
            status = Status.OK
          )

          "adding data to DB is successful" should {

            lazy val request = FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
            lazy val result = TestSetupDataController.addData(request)

            "return 200" in {
              mockAddEntry(model)(successWriteResult)

              status(result) shouldBe Status.OK
            }
          }

          "adding data to DB is unsuccessful" should {

            lazy val request = FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
            lazy val result = TestSetupDataController.addData(request)

            "return 500" in {
              mockAddEntry(model)(errorWriteResult)

              status(result) shouldBe Status.INTERNAL_SERVER_ERROR
            }
          }
        }
      }

      "method is PUT" should {

        val model: DataModel = DataModel(
          _id = "1234",
          method = "PUT",
          response = Some(Json.parse("{}")),
          status = Status.OK
        )

        lazy val request = FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
        lazy val result = TestSetupDataController.addData(request)

        "return 405" in {
          status(result) shouldBe Status.METHOD_NOT_ALLOWED
        }
      }
    }

    "request body is not valid" should {

      lazy val request = FakeRequest().withBody(Json.obj("key" -> "value")).withHeaders(("Content-Type", "application/json"))
      lazy val result = TestSetupDataController.addData(request)

      "return 400" in {
        status(result) shouldBe Status.BAD_REQUEST
      }
    }
  }

  "SetupDataController .removeDataBySchemaId" when {

    "removal is successful" should {

      lazy val request = FakeRequest()
      lazy val result = TestSetupDataController.removeDataBySchemaId("schemaId")(request)

      "return 200" in {
        mockRemoveBySchemaId("schemaId")(successWriteResult)

        status(result) shouldBe Status.OK
      }
    }

    "removal is unsuccessful" should {

      lazy val request = FakeRequest()
      lazy val result = TestSetupDataController.removeDataBySchemaId("schemaId")(request)

      "return 500" in {
        mockRemoveBySchemaId("schemaId")(errorWriteResult)

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }
  }

  "SetupDataController.removeAllData" when {

    "removal is successful" should {

      lazy val request = FakeRequest()
      lazy val result = TestSetupDataController.removeAll(request)

      "return 200" in {
        mockRemoveAll()(successWriteResult)

        status(result) shouldBe Status.OK
      }
    }

    "removal is unsuccessful" should {

      lazy val request = FakeRequest()
      lazy val result = TestSetupDataController.removeAll(request)

      "return 500" in {
        mockRemoveAll()(errorWriteResult)

        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }
  }
}