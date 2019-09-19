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

  def getAwsCommands(file: File, requestedServices: IndexedSeq[SupportedService] = SupportedService.values): List[Command] =
    commandsFromJson(cfToJson(file), requestedServices)

  private def commandsFromJson(json: Json, requestedServices: IndexedSeq[SupportedService]): List[Command] =
    for {
      resourcesJson <- json.hcursor.downField("Resources").focus.toList
      resourcesJsonObject <- resourcesJson.asObject.toList
      resourceJson <- filterRequestedServices(resourcesJsonObject.values.toList, requestedServices)
      resourceType <- resourceJson.hcursor.downField("Type").as[String].toList
      service <- SupportedService.withAwsName(resourceType).toList
      transformedResourceJson = Dictionary.replace(resourceJson, Dictionary(json))
      command <- List(service.createCommand(transformedResourceJson))
    } yield command

  private def filterRequestedServices(json: List[Json], requestedServices: IndexedSeq[SupportedService]): List[Json] = json.filter { j =>
      j.hcursor.downField("Type").as[String].toOption.exists(requestedServices.map(_.awsName).contains)
    }
}