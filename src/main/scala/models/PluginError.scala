package models

import io.circe.Error

trait PluginError

case class CirceError(err: Error) extends PluginError
case class NotImplemented(msg: String) extends PluginError