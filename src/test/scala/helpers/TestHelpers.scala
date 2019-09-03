package helpers

import java.io.{File, FileInputStream, InputStreamReader}

import io.circe.Json
import io.circe.yaml.parser

object TestHelpers {
  val cfFile: File = new File(getClass.getResource("/cf.yml").getPath)
  val cfJson: Json = parser.parse(new InputStreamReader(new FileInputStream(cfFile))).getOrElse(Json.Null)
}
