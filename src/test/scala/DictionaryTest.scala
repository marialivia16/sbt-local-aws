import helpers.TestHelpers._
import models.Dictionary
import org.scalatest.{FlatSpec, Matchers}

class DictionaryTest extends FlatSpec with Matchers {
  "Dictionary" should "extract parameters from cf" in {
    Dictionary.loadFromJson(cfJson) shouldBe Dictionary(Map("AppName" -> "Example", "Environment" -> "CODE"))
  }
}
