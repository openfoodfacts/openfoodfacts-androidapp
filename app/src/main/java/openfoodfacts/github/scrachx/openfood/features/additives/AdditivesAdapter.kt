package openfoodfacts.github.scrachx.openfood.features.additives

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.databinding.AdditivesItemBinding
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.utils.AutoUpdatableAdapter

/**
 * Created by prajwalm on 16/04/18.
 */
class AdditivesAdapter(
    initialValue: List<AdditiveName> = emptyList(),
    private val clickListener: ((String) -> Unit)?,
) : RecyclerView.Adapter<AdditivesAdapter.ViewHolder>(), AutoUpdatableAdapter {

    var additives: List<AdditiveName> by autoNotifying(
        initialValue = initialValue,
        comparator = compareBy { it.id }
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AdditivesItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name = additives[position].name

        holder.additiveName.text = name
        holder.itemView.setOnClickListener {
            clickListener?.invoke(name)
        }
    }

    override fun getItemCount() = additives.count()

    class ViewHolder(binding: AdditivesItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val additiveName = binding.additiveName
    }
}

