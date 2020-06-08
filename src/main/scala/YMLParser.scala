/*
 * Copyright 2019+ sbt-aws-local contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.{File, FileInputStream, InputStreamReader}

import _root_.io.circe._
import _root_.io.circe.yaml.parser
import cats.data.NonEmptyList
import cats.syntax.either._
import models.{PluginError, SupportedService}

object YMLParser {

  type Command = Either[NonEmptyList[PluginError], String]

  def cfToJson(file: File): Json = parser.parse(new InputStreamReader(new FileInputStream(file))).fold (
    pf => {
      println(s"[ERROR] Parsing failure for file ${file.getPath}: ${pf.message}")
      Json.Null
    }, identity
  )

  def getAwsResourcesNames(file: File): List[String] = {
    val json = cfToJson(file)
    (for {
      resourcesJson <- json.hcursor.downField("Resources").focus.toList
      resourcesJsonObject <- resourcesJson.asObject.toList
      resourceJson <- resourcesJsonObject.values
      resourceType <- resourceJson.hcursor.downField("Type").as[String].toList
    } yield resourceType).flatMap(SupportedService.withAwsType).map(_.name).distinct
  }
}