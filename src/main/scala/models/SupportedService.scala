package models

import cats.Semigroupal
import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.instances.either._
import cats.instances.list._
import cats.instances.option._
import cats.syntax.bifunctor._
import cats.syntax.either._
import cats.syntax.option._
import cats.syntax.traverse._
import enumeratum._
import io.circe.{Error, Json, ParsingFailure}

sealed abstract class SupportedService(val awsName: String) extends EnumEntry {
  def createCommand(json: Json, dictionary: Dictionary = Dictionary(Map.empty)): Either[NonEmptyList[Error], String]
}

object SupportedService extends Enum[SupportedService] with CirceEnum[SupportedService] {

  def withAwsName(awsName: String): Option[SupportedService] = values find (_.awsName == awsName)

  case object DynamoDB extends SupportedService(awsName = "AWS::DynamoDB::Table") {

    override def createCommand(json: Json, dictionary: Dictionary): Either[NonEmptyList[Error], String] = {
      val propertiesJson = json.hcursor.downField("Properties").focus.get
      Semigroupal.map5[ValidatedNel[Error, ?], String, String, String, Option[String], String, String](
        extractTableName(propertiesJson, dictionary),
        extractAttributeDefinitions(propertiesJson),
        extractKeySchema(propertiesJson),
        extractSecondaryIndexes(propertiesJson).sequence[ValidatedNel[Error, ?], String],
        extractProvisionedThroughput(propertiesJson)
      ) {
        case (tableName, attributeDefinitions, keySchema, gsi, provisionedThroughput) =>
          val g: String = gsi.fold[String]("")(g => s"""--global-secondary-indexes $g \\""")

          s"""
             |awslocal dynamodb create-table \\
             |--table-name $tableName \\
             |--attribute-definitions $attributeDefinitions \\
             |--key-schema $keySchema \\
             |$g
             |--provisioned-throughput $provisionedThroughput \\
             |--region eu-west-1
            """.stripMargin
      }.toEither
    }

    val RegExPattern = "\\$\\{([a-zA-Z]+)}".r("name")

    private def extractFieldName(json: Json, dictionary: Dictionary): Validated[NonEmptyList[Error], String] = {
      json.as[String].toValidatedNel.findValid {
        json.hcursor.downField("Sub").as[String].map(
          RegExPattern.replaceAllIn(_, m => dictionary.entries(m.group("name")))
        ).toValidatedNel
      }
    }

    private def extractTableName(json: Json, dictionary: Dictionary): ValidatedNel[Error, String] =
      json.hcursor.downField("TableName")
        .focus.toValidNel(ParsingFailure(s"Missing field TableName on $json", new IllegalStateException))
        .andThen(extractFieldName(_, dictionary))

    private def extractAttributeDefinitions(json: Json): ValidatedNel[Error, String] =
      json.hcursor.downField("AttributeDefinitions").as[List[Json]].toValidatedNel.andThen { jsonList =>
        jsonList.traverse[ValidatedNel[Error, ?], String] { attributeDefinitionJson =>
          Semigroupal.map2[ValidatedNel[Error, ?], String, String, String](
            attributeDefinitionJson.hcursor.downField("AttributeName").as[String].toValidatedNel,
            attributeDefinitionJson.hcursor.downField("AttributeType").as[String].toValidatedNel) {
            case (attributeName, keyType) => s"AttributeName=$attributeName,AttributeType=$keyType"
          }
        }.map(_.mkString(" "))
      }

    private def extractKeySchema(json: Json): ValidatedNel[Error, String] =
      json.hcursor.downField("KeySchema").as[List[Json]].toValidatedNel.andThen { jsonList =>
        jsonList.traverse[ValidatedNel[Error, ?], String] { keySchemaJson =>
          Semigroupal.map2[ValidatedNel[Error, ?], String, String, String](
            keySchemaJson.hcursor.downField("AttributeName").as[String].toValidatedNel,
            keySchemaJson.hcursor.downField("KeyType").as[String].toValidatedNel) {
            case (attributeName, keyType) => s"AttributeName=$attributeName,KeyType=$keyType"
          }
        }.map(_.mkString(" "))
      }

    private def extractProvisionedThroughput(json: Json): ValidatedNel[Error, String] =
      Semigroupal.map2[ValidatedNel[Error, ?], Int, Int, String](
        json.hcursor.downField("ProvisionedThroughput").downField("ReadCapacityUnits").as[Int].leftWiden[Error].toValidatedNel,
        json.hcursor.downField("ProvisionedThroughput").downField("WriteCapacityUnits").as[Int].leftWiden[Error].toValidatedNel) {
        case (readCapacityUnits, writeCapacityUnits) => s"ReadCapacityUnits=$readCapacityUnits,WriteCapacityUnits=$writeCapacityUnits"
      }

    private def extractProjectionType(json: Json): ValidatedNel[Error, String] =
      json.hcursor.downField("Projection").downField("ProjectionType").as[String].toValidatedNel.map { projectionType =>
        s"Projection={ProjectionType=$projectionType"
      }

    private def extractSecondaryIndexes(json: Json): Option[ValidatedNel[Error, String]] =
      json.hcursor.downField("GlobalSecondaryIndexes").as[List[Json]].toOption.map { jsonList =>
        jsonList.traverse[ValidatedNel[Error, ?], String] { gsiJson =>
          Semigroupal.map4[ValidatedNel[Error, ?], String, String, String, String, String](
            gsiJson.hcursor.downField("IndexName").as[String].toValidatedNel,
            extractKeySchema(gsiJson),
            extractProjectionType(gsiJson),
            extractProvisionedThroughput(gsiJson)
          ) {
            case (indexName, keySchema, projectionType, provisionedThroughput) =>
              val formattedKeySchema = keySchema.split(" ").map(k => s"{$k}").mkString(",")
              s"'IndexName=$indexName,KeySchema=[$formattedKeySchema],$projectionType,ProvisionedThroughput={$provisionedThroughput}'"
          }
        }.map(_.mkString(" "))
      }
  }

  case object S3 extends SupportedService(awsName = "AWS::S3::Bucket") {
    override def createCommand(json: Json, dictionary: Dictionary): Either[NonEmptyList[Error], String] = ???
  }

  val values = findValues

}