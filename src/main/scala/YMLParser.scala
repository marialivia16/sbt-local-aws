import java.io.{File, FileInputStream, InputStreamReader}

import _root_.io.circe._
import _root_.io.circe.yaml.parser
import cats.data.NonEmptyList
//import cats.instances.either._
import cats.syntax.either._
import models.{Dictionary, SupportedService}

object YMLParser {

  def cfToJson(file: File): Either[ParsingFailure, Json] = parser.parse(new InputStreamReader(new FileInputStream(file)))

  def getAWSServices(file: File): Either[ParsingFailure, List[Either[NonEmptyList[Error], String]]] = {
    cfToJson(file).map { json =>

      val parameters: Dictionary = Dictionary.loadFromJson(json)

      val commands: List[Either[NonEmptyList[Error], String]] =
        for {
          resourcesJson <- json.hcursor.downField("Resources").focus.toList
          resourcesJsonObject <- resourcesJson.asObject.toList
          resourceJson <- resourcesJsonObject.values
          resourceType <- resourceJson.hcursor.downField("Type").as[String].toList
          service <- SupportedService.withAwsName(resourceType).toList
          command <- List(service.createCommand(resourceJson, parameters))
        } yield command

      commands
    }
  }
}