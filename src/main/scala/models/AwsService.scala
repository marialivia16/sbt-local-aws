package models

import cats.data.NonEmptyList
import enumeratum._
import io.circe.Error

sealed abstract class AwsService(val awsName: String) extends EnumEntry {
  def createJsonCommand(): Either[NonEmptyList[Error], String]
}

object AwsService extends Enum[AwsService] with CirceEnum[AwsService] {

  def withAwsName(awsName: String): Option[AwsService] = values find (_.awsName == awsName)

  case object DynamoDB extends AwsService(awsName = "AWS::DynamoDB::Table") {
    override def createJsonCommand(): Either[NonEmptyList[Error], String] = ???
  }

  case object S3 extends AwsService(awsName = "AWS::S3::Bucket") {
    override def createJsonCommand(): Either[NonEmptyList[Error], String] = ???
  }

  val values = findValues

}