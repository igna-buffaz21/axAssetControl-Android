package aumax.estandar.axappestandar.data.local

import android.content.Context
import androidx.room.Room

object DataBaseProvider {
    @Volatile
    private var INSTANCE: AppDataBase? = null

    fun getDataBase(context: Context): AppDataBase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDataBase::class.java,
                "axAssetControl"
            )
                .fallbackToDestructiveMigration()
                .build()
            INSTANCE = instance
            instance
        }
    }
}