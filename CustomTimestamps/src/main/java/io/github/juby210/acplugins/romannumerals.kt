import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.PinePatchFn
import com.aliucord.utils.ReflectUtils.getMethod
import com.aliucord.utils.ReflectUtils.invokeMethod
import com.discord.models.message.Message
import com.discord.stores.StoreStream
import com.discord.utilities.message.MessageUtils
import com.discord.utilities.time.ClockFactory
import java.text.SimpleDateFormat
import java.util.*

@AliucordPlugin
class RomanTimestamps : Plugin() {

    override fun start(context: Context) {
        patcher.patch(
            getMethod(MessageUtils::class.java, "getDisplayTimestampFromMessage"),
            PinePatchFn { callFrame ->
                val originalResult = callFrame.result as String
                val message = callFrame.args[0] as Message
                val clock = ClockFactory.get()
                val now = clock.instant().toEpochMilli()
                val isToday = SimpleDateFormat("dd").format(now) == SimpleDateFormat("dd").format(message.timestamp)
                val dateFormat = if (isToday) "h:mm a" else "MMM dd"
                val timeString = SimpleDateFormat(dateFormat).format(message.timestamp)
                val romanNumeral = toRomanNumerals(message.timestamp.toString().takeLast(5).toInt())

                // Concatenate the timestamp and the roman numeral
                callFrame.result = timeString + " " + romanNumeral
            }
        )
    }

    override fun stop(context: Context) = patcher.unpatchAll()

    private fun toRomanNumerals(number: Int): String {
        if (number == 0) return "N"
        if (number > 4999) throw Exception("Number too large, max is 4999")
        val romanNumerals = mapOf(
            1000 to "M", 900 to "CM", 500 to "D", 400 to "CD",
            100 to "C", 90 to "XC", 50 to "L", 40 to "XL",
            10 to "X", 9 to "IX", 5 to "V", 4 to "IV",
            1 to "I"
        )
        var remaining = number
        var roman = ""
        for ((value, numeral) in romanNumerals) {
            while (remaining >= value) {
                remaining -= value
                roman += numeral
            }
        }
        return roman
    }
}

