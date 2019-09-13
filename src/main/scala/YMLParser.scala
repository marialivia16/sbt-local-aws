import java.io.{File, FileInputStream, InputStreamReader}

import _root_.io.circe._
import _root_.io.circe.yaml.parser
import cats.data.NonEmptyList
import cats.syntax.either._
import models.{Dictionary, PluginError, SupportedService}

object YMLParser {

  type Command = Either[NonEmptyList[PluginError], String]

  def cfToJson(file: File): Json = parser.parse(new InputStreamReader(new FileInputStream(file))).fold (
    pf => {
      println(s"[ERROR] Parsing failure for file ${file.getPath}: ${pf.message}")
      Json.Null
    }, identity
  )

  def getSupportedServices(file: File): List[Command] = {
    val json = cfToJson(file)
    commandsFromJson(json)
  }

  private def commandsFromJson(json: Json): List[Command] =
    for {
      resourcesJson <- json.hcursor.downField("Resources").focus.toList
      resourcesJsonObject <- resourcesJson.asObject.toList
      resourceJson <- resourcesJsonObject.values
      resourceType <- resourceJson.hcursor.downField("Type").as[String].toList
      service <- SupportedService.withAwsName(resourceType).toList
      transformedResourceJson = Dictionary.replace(resourceJson, Dictionary(json))
      _ = println(s"Found a resource of type: ${service.awsName}")
      command <- List(service.createCommand(transformedResourceJson))
    } yield command
}