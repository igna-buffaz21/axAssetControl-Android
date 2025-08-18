package aumax.estandar.axappestandar.utils.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import aumax.estandar.axappestandar.R
import aumax.estandar.axappestandar.data.models.Activos.Activo
import aumax.estandar.axappestandar.databinding.LayoutModalTagRfidBinding

class TagRfidSSDialog(
    context: Context,
    private val onTagAssigned: (tagCodes: List<String>) -> Unit // callback cuando se confirme
) : Dialog(context) {

    private lateinit var binding: LayoutModalTagRfidBinding //
    private var tagList: MutableList<String> = mutableListOf() //lista donde estan los tags

    override fun dismiss() {
        super.dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = LayoutModalTagRfidBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindow()
        setupViews()
    }

    private fun setupWindow() {
        window?.apply {
            // Hacer que ocupe toda la pantalla con fondo transparente
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Agregar animaciones suaves
            setWindowAnimations(android.R.style.Animation_Dialog)
        }
    } ///pantalla completa, animaciones, fondo

    private fun setupViews() {
        binding.tvModalTitle.text = "Leer Tag RFID"

        binding.layoutActivoInfo.visibility = View.GONE

        binding.btnCancelar.visibility = View.GONE

        binding.btnConfirmar.visibility = View.GONE

        binding.codigoDelTag.visibility = View.GONE

        binding.tvNoCodes.text = "Buscando Tag..."

        binding.btnCloseModal.setOnClickListener {
            dismiss()
        }

        binding.modalBackground.setOnClickListener {
            dismiss()
        }

        binding.modalCard.setOnClickListener {
            // No hacer nada para evitar que se propague el click
        }

        binding.btnCancelar.setOnClickListener {
            dismiss()
        }

        binding.btnConfirmar.setOnClickListener {

        }

    } ///muestra info del activo, botones(cerrar, confirmar)

    private fun addTagCode(tagCode: String) {
        if (!tagList.contains(tagCode)) {
            tagList.add(tagCode)

            Toast.makeText(context, "Tag leído: $tagCode", Toast.LENGTH_SHORT).show()

            onTagAssigned(listOf(tagCode)) //ejecuta la funcion para buscar activos

            dismiss() //sale del dialog
        }
    } ///agrega tag a la lista si no esta repetido


    fun addExternalTagCode(tagCode: String) { //agrega tags desde la activity
        addTagCode(tagCode)
    }

    companion object {
        fun show(
            context: Context,
            onTagAssigned: (tagCodes: List<String>) -> Unit
        ) : TagRfidSSDialog {
            val dialog = TagRfidSSDialog(context, onTagAssigned)
            dialog.show()
            return dialog
        }
    }
}