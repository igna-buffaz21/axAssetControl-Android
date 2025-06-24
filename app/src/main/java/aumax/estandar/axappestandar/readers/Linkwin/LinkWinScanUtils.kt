package aumax.estandar.axappestandar.readers.Linkwin

import android.content.Context
import android.content.Intent

class LinkWinScanUtils(context: Context) {
    /**
     * Open scan service
     */
    private val ACTION_SCAN_INIT = "com.rfid.SCAN_INIT"

    /**
     * Scanning
     */
    private val ACTION_SCAN = "com.rfid.SCAN_CMD"

    /**
     * Stop Scanning
     */
    private val ACTION_STOP_SCAN = "com.rfid.STOP_SCAN"

    /**
     * Close scan service
     */
    private val ACTION_CLOSE_SCAN = "com.rfid.CLOSE_SCAN"

    /**
     * Scan result output mode, 0 -- BroadcastReceiver mode; 1 -- Focus input mode (default)
     */
    private val ACTION_SET_SCAN_MODE = "com.rfid.SET_SCAN_MODE"

    /**
     * Scan timeout (Value:1000,2000,3000,4000,5000,6000,7000,8000,9000,10000)
     */
    private val ACTION_SCAN_TIME = "com.rfid.SCAN_TIME"

    private var context: Context? = null

    init {
        this.context = context
        val intent = Intent()
        intent.action = ACTION_SCAN_INIT
        context.sendBroadcast(intent)
    }
    /**
     * Initialize ScanUtil and open scan service
     * @param context Context

    fun LinkWinScanUtils(context: Context) {
    this.context = context
    val intent = Intent()
    intent.action = ACTION_SCAN_INIT
    context.sendBroadcast(intent)
    }
     */
    /**
     * Start Scanning
     */
    fun scan() {
        val intent = Intent()
        intent.action = ACTION_SCAN
        context!!.sendBroadcast(intent)
    }

    /**
     * Stop Scanning
     */
    fun stopScan() {
        val intent = Intent()
        intent.action = ACTION_STOP_SCAN
        context!!.sendBroadcast(intent)
    }

    /**
     * Set the scan result output mode
     * @param mode 0 -- BroadcastReceiver mode; 1 -- Focus input mode (default)
     */
    fun setScanMode(mode: Int) {
        val intent = Intent()
        intent.action = ACTION_SET_SCAN_MODE
        intent.putExtra("mode", mode)
        context!!.sendBroadcast(intent)
    }

    /**
     * Close scan service
     */
    fun close() {
        val toKillService = Intent()
        //        toKillService.putExtra("iscamera", true);
        toKillService.action = ACTION_CLOSE_SCAN
        context!!.sendBroadcast(toKillService)
    }

    /**
     * Set scan timeout
     * @param timeout Value:1000,2000,3000,4000,5000(default),6000,7000,8000,9000,10000
     */
    fun setTimeout(timeout: String?) {
        val intent = Intent()
        intent.action = ACTION_SCAN_TIME
        intent.putExtra("time", timeout)
        context!!.sendBroadcast(intent)
    }
}