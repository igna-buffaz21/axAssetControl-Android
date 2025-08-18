package aumax.estandar.axappestandar.utils.dialogs

import android.app.Activity
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

class ModalAsignarTagRFIDDialog(
    context: Context,
    private val activo: Activo,
    private val onTagAssigned: (activo: Activo, tagCodes: List<String>) -> Unit // callback cuando se confirme
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
        // Configurar información del activo
        binding.tvActivoNombre.text = activo.name
        binding.tvActivoCodigo.text = "Código: ${activo.tagRfid}"

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
            if (tagList.isNotEmpty()) {
                onTagAssigned(activo, tagList.toList())
                dismiss()
            }
        }

        updateTagCodesUI()
    } ///muestra info del activo, botones(cerrar, confirmar)

    private fun addTagCode(tagCode: String) {
        if (!tagList.contains(tagCode)) {
            tagList.add(tagCode)
            updateTagCodesUI()
            (context as? Activity)?.runOnUiThread {
                Toast.makeText(context, "Tag leído: $tagCode", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTagCodesUI() {
        binding.apply {
            if (tagList.isEmpty()) { //ningun tag
                layoutTagCodes.visibility = View.GONE
                tvNoCodes.visibility = View.VISIBLE
                btnConfirmar.isEnabled = false
            } else { //hay tags
                layoutTagCodes.visibility = View.VISIBLE
                tvNoCodes.visibility = View.GONE
                btnConfirmar.isEnabled = true

                // Limpiar contenedor y agregar códigos
                layoutTagCodes.removeAllViews()

                tagList.forEachIndexed { index, code ->
                    val codeView = createTagCodeView(code, index)
                    layoutTagCodes.addView(codeView)
                }
            }
        }
    }

    private fun createTagCodeView(code: String, index: Int): View { //crea la vista de cada tag
        val codeView = LayoutInflater.from(context)
            .inflate(R.layout.item_tag_code, binding.layoutTagCodes, false)

        val tvCode = codeView.findViewById<TextView>(R.id.tv_tag_code)
        val btnRemove = codeView.findViewById<ImageButton>(R.id.btn_remove_code)

        tvCode.text = code
        btnRemove.setOnClickListener {
            removeTagCode(index)
        }

        return codeView
    }

    private fun removeTagCode(index: Int) { //elimina un tag de la lista
        if (index in tagList.indices) {
            tagList.removeAt(index)
            updateTagCodesUI()
        }
    }

    fun addExternalTagCode(tagCode: String) { //agrega tags desde la activity
        addTagCode(tagCode)
    }

    companion object {
        fun show(
            context: Context,
            activo: Activo,
            onTagAssigned: (activo: Activo, tagCodes: List<String>) -> Unit
        ) : ModalAsignarTagRFIDDialog {
            val dialog = ModalAsignarTagRFIDDialog(context, activo, onTagAssigned)
            dialog.show()
            return dialog
        }
    }
}