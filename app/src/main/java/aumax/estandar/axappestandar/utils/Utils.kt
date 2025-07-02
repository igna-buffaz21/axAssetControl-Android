package aumax.estandar.axappestandar.utils

import android.content.Context
import android.text.Html
import android.widget.Toast

object Utils {

    class ToastUtils {
        companion object {
            fun toastError(context: Context, errorMessage: String): Toast {
                return Toast.makeText(
                    context,
                    Html.fromHtml("<h3><font color='#ff0000'><b>$errorMessage</b></font></h3>"),
                    Toast.LENGTH_SHORT
                )
            }

            fun toastSuccess(context: Context, message: String): Toast {
                return Toast.makeText(
                    context,
                    Html.fromHtml("<h3><font color='#28A745'><b>$message</b></font></h3>"),
                    Toast.LENGTH_SHORT
                )
            }
        }
    }
}