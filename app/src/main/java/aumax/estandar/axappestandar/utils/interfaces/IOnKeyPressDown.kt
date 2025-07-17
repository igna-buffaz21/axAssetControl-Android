package aumax.estandar.axappestandar.utils.interfaces

import android.view.KeyEvent

interface IOnKeyPressDown {
    fun keyPress(keyCode: Int,event: KeyEvent?)
}