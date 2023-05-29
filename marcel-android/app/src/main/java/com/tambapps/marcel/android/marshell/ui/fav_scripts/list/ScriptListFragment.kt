package com.tambapps.marcel.android.marshell.ui.fav_scripts.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tambapps.marcel.android.marshell.R
import com.tambapps.marcel.android.marshell.databinding.FragmentScriptListBinding
import com.tambapps.marcel.android.marshell.room.entity.CacheableScript
import com.tambapps.marcel.android.marshell.service.CacheableScriptService
import com.tambapps.marcel.android.marshell.ui.ResourceParentFragment
import com.tambapps.marcel.android.marshell.ui.fav_scripts.form.ScriptFormFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ScriptListFragment: ResourceParentFragment.ChildFragment() {

  companion object {
    fun newInstance() = ScriptListFragment()
  }

  @Inject
  lateinit var scriptService: CacheableScriptService
  private var _binding: FragmentScriptListBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentScriptListBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    CoroutineScope(Dispatchers.IO).launch {
      val scripts = scriptService.list()
      withContext(Dispatchers.Main) {
        binding.recyclerView.apply {
          layoutManager = LinearLayoutManager(requireContext())
          adapter = MyAdapter(scripts, this@ScriptListFragment::onScriptClick, this@ScriptListFragment::onScriptLongClick)
        }
      }
    }
  }

  private fun onScriptClick(script: CacheableScript) {}
  private fun onScriptLongClick(script: CacheableScript) {}
  override fun onFabClick() {
    navigateTo(ScriptFormFragment.newInstance(), R.drawable.save)
  }

  private class MyViewHolder(root: View): RecyclerView.ViewHolder(root) {
    val textView: TextView = root.findViewById(R.id.name)
    val dotsButton: ImageView = root.findViewById(R.id.dots)
  }

  private class MyAdapter(private val scripts: List<CacheableScript>, private val onScriptClick: (CacheableScript) -> Unit,
                          private val onScriptLongClick: (CacheableScript) -> Unit): RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
      val layout =  LayoutInflater.from(parent.context)
        .inflate(R.layout.script_item_layout, parent, false)
      return MyViewHolder(layout)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
      val env = scripts[position]
      holder.textView.text = env.name
      holder.itemView.setOnClickListener {
        onScriptClick.invoke(env)
      }
      holder.itemView.setOnLongClickListener {
        onScriptLongClick.invoke(env)
        return@setOnLongClickListener true
      }
      holder.dotsButton.setOnClickListener {
        onScriptLongClick.invoke(env)
      }
    }

    override fun getItemCount(): Int {
      return scripts.size
    }
  }

}