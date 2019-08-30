import java.io.{FileInputStream, InputStreamReader}

import _root_.io.circe._
import _root_.io.circe.yaml.parser
import cats.data.NonEmptyList
import cats.instances.either._
import cats.syntax.either._
import sbt._

import scala.collection.immutable.Seq


object LocalAWS extends AutoPlugin {
  object autoImport {
    lazy val createLocalAWS = TaskKey[Unit]("createLocalAWS", "Creates a local AWS stack from provided cloudformation.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    createLocalAWS := {
      YMLParser.getAWSServices(new File("cf.yml"))
    }
  )
}

final case class Dictionary(entries: Map[String, String])
object Dictionary {
  def loadFromJson(json: Json): Dictionary = {
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
}

object YMLParser {

  def getAWSServices(file: File): Either[ParsingFailure, Unit] = {
    val fileInputStream = new FileInputStream(file)
    parser.parse(new InputStreamReader(fileInputStream)).map { json =>

      val parameters: Dictionary = Dictionary.loadFromJson(json)

      println(parameters)

        val commands: List[Either[NonEmptyList[Error], String]] =
          for {
            resourcesJson <- json.hcursor.downField("Resources").focus.toList
            resourcesJsonObject <- resourcesJson.asObject.toList
            resourceJson <- resourcesJsonObject.values
            resourceType <- resourceJson.hcursor.downField("Type").as[String].toList
            service <- SupportedService.withAwsName(resourceType).toList
            command <- List(service.createCommand(resourceJson, parameters))
          } yield command

        println(commands)
    }
  }
}

object PluginApp extends App {
  YMLParser.getAWSServices(new File("cf.yml"))
}
