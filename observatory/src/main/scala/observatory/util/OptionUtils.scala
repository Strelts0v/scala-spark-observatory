package observatory.util

import scala.util.{Failure, Success, Try}

object OptionUtils {

  def optInt(a: String) = Try(a.toInt) match {
    case Success(v) => Some(v)
    case Failure(_) => None
  }

  def optDouble(a: String) = Try(a.toDouble) match {
    case Success(v) => Some(v)
    case Failure(_) => None
  }

}
