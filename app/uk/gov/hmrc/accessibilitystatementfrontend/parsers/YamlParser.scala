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

package uk.gov.hmrc.accessibilitystatementfrontend.parsers

import io.circe.{yaml => circeYaml, _}
import play.api.Logging
import uk.gov.hmrc.accessibilitystatementfrontend.config.StatementSource
import scala.util.Try

class YamlParser[T: Decoder] extends Logging {
  def parse(yaml: String): Either[Error, T] =
    circeYaml.parser
      .parse(yaml)
      .flatMap(_.as[T])

  def parseFromSource(statementSource: StatementSource): Either[Throwable, T] =
    try {
      logger.info(s"Parsing YAML source file for source: ${statementSource.filename}")

      val parsedYaml: Either[Throwable, T] =
        Try(statementSource.source.mkString)
          .toEither
          .flatMap(parse)

      parsedYaml match {
        case Right(parsed) => Right(parsed)
        case Left(error) =>
          logger.error(s"Parsing error for source ${statementSource.filename}, error is: $error")
          Left(error)
      }
    }
    finally {
      Try(statementSource.source.close())
    }
}
