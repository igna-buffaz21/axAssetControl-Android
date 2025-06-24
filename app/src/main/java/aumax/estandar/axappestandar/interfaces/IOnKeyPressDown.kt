package aumax.estandar.axappestandar.interfaces

import android.view.KeyEvent

interface IOnKeyPressDown {
    fun keyPress(keyCode: Int,event: KeyEvent?)
}