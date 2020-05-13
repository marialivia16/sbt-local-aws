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
package models

import io.circe.{Json, JsonObject}

final case class Dictionary(entries: Map[String, String])

object Dictionary {

  def apply(json: Json): Dictionary = {
    val parameters: List[(String, Json)] =
      for {
        parametersJson <- json.hcursor.downField("Parameters").focus.toList
        parametersObject <- parametersJson.asObject.toList
        parameterField <- parametersObject.toList
      } yield parameterField

    Dictionary(parameters.flatMap {
      case (key, value) =>
        value.hcursor.downField("Default").as[String].toOption.map(key -> _) //TODO Maybe not string
    }.toMap
    )
  }

  def replace(json: Json, dictionary: Dictionary): Json = replace(json, stringReplace(dictionary))

  def stringReplace(dictionary: Dictionary): String => String = (jsonStr: String) =>
    dictionary.entries.foldLeft(jsonStr) { case (acc, (key, value)) =>
      val toReplace = s"""$${$key}"""
      acc.replace(toReplace, value)
    }

  private def replace(js: Json, f: String => String): Json = js
    .mapString(f)
    .mapArray(_.map(replace(_, f)))
    .mapObject(obj => {
      val updatedObj = obj.toMap.map {
        case (k, v) => f(k) -> replace(v, f)
      }
      JsonObject.apply(updatedObj.toSeq: _*)
    })
    .withObject { o =>
      o.toList match {
        case List(("Sub", str)) => str
        case fields             => Json.obj(fields :_*)
      }
    }
}
