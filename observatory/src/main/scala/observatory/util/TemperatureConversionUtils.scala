package observatory.util

object TemperatureConversionUtils {

  def fahrenheitToCelsius(f: Double): Double = (f - 32.0) * (5.0/9.0)
}
