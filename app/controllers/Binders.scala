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

import models.{ReturnsQueryParams, OptEither}
import play.api.mvc.{PathBindable, QueryStringBindable}
import uk.gov.hmrc.domain.Vrn

object Binders {

  implicit def vrnBinder(implicit stringBinder: PathBindable[String]): PathBindable[Vrn] =
    new PathBindable[Vrn] {
      val vrnRegex = """^\d{9}$"""

      def unbind(key: String, vrn: Vrn): String = stringBinder.unbind(key, vrn.value)

      def bind(key: String, value: String): Either[String, Vrn] = {
        if (value.matches(vrnRegex)) {
          Right(Vrn(value))
        } else {
          Left("ERROR_VRN_INVALID")
        }
      }
    }

  implicit def returnsQueryParamsBinder(implicit stringBinder: QueryStringBindable[String])
  : QueryStringBindable[ReturnsQueryParams] =
    new QueryStringBindable[ReturnsQueryParams] {
      override def bind(key: String, params: Map[String, Seq[String]]): OptEither[ReturnsQueryParams] = {
        val from = stringBinder.bind("from", params)
        val to = stringBinder.bind("to", params)
        val status = stringBinder.bind("status", params)

        val query = ReturnsQueryParams.from(from, to, status)
        if (query.isRight) {
          Some(Right(query.right.get))
        }
        else {
          Some(Left(query.left.get))
        }
      }

      override def unbind(key: String, value: ReturnsQueryParams): String = {
        stringBinder.unbind(key, value.map(key).toString)
      }
    }

}

