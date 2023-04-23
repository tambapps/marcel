package com.tambapps.marcel.android.marshell

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tambapps.marcel.android.marshell.databinding.ActivityFilePickerBinding
import com.tambapps.marcel.android.marshell.service.PermissionHandler
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class FilePickerActivity : AppCompatActivity() {

  companion object {
    val SCRIPT_FILE_EXTENSIONS = arrayOf(".mcl", ".txt", ".marcel")
    const val PICKED_FILE_PATH_KEY = "pfpk"
    const val ALLOWED_FILE_EXTENSIONSKEY = "afek"
    const val DIRECTORY_ONLY_KEY = "pick_directoryk"
    const val START_DIRECTORY_KEY = "start_directory"
  }

  class Contract: ActivityResultContract<Intent, File?>() {
    override fun createIntent(context: Context, input: Intent) = input

    override fun parseResult(resultCode: Int, intent: Intent?)
        = if (resultCode == Activity.RESULT_OK && intent != null) intent.getSerializableExtra(FilePickerActivity.PICKED_FILE_PATH_KEY) as File
    else null
  }

  @Inject
  lateinit var sharedPreferences: SharedPreferences
  @Inject
  lateinit var permissionHandler: PermissionHandler
  private lateinit var fragment: FilePickerFragment
  private lateinit var currentDir: File
  private lateinit var internalStorageRoot: File
  private lateinit var pathAdapter: PathRecyclerViewAdapter
  var dirOnly = false
  private var fileNamePattern: Pattern? = null
  private lateinit var binding: ActivityFilePickerBinding
  private lateinit var pickDirectoryButton: TextView
  private lateinit var directoryText: TextView
  private lateinit var pathRecyclerView: RecyclerView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityFilePickerBinding.inflate(layoutInflater)
    setContentView(binding.root)
    internalStorageRoot = getDeviceRootDirectory()
    pickDirectoryButton = binding.pickDirectoryButton
    directoryText = binding.directoryText
    pathRecyclerView = binding.pathRecyclerView

    val fileExtensions = intent.getStringArrayExtra(ALLOWED_FILE_EXTENSIONSKEY)
    dirOnly = intent.hasExtra(DIRECTORY_ONLY_KEY)
    pickDirectoryButton.visibility = if (dirOnly) View.VISIBLE else View.GONE
    if (fileExtensions != null) {
      fileNamePattern = Pattern.compile(
          // pattern for file without extension (actually filename without a dot in it
          "^((?!\\.).)*\$|" +
          fileExtensions.asSequence().map { "." + Pattern.quote(it) }.joinToString("|"))
    }

    if (!permissionHandler.hasFilesPermission(this)) {
      Toast.makeText(applicationContext, "Please grant files permissions from settings screen", Toast.LENGTH_SHORT).show()
      finish()
      return
    }

    onBackPressedDispatcher.addCallback(this) {
      if (currentDir.parentFile == null || currentDir == getDeviceRootDirectory()) {
        finish()
      } else {
        fragment.onBackPressed()
      }
    }
    if (savedInstanceState == null) {
      currentDir = File(sharedPreferences.getString(START_DIRECTORY_KEY, getDeviceRootDirectory().path)!!)
      directoryText.text = getDirectoryName(currentDir)
      val rootPath = currentDir.absolutePath
      fragment = FilePickerFragment.newInstance(rootPath)
      supportFragmentManager.beginTransaction()
          .replace(R.id.container, fragment, rootPath)
          .commitNow()
    }
    pathAdapter = PathRecyclerViewAdapter(currentDir)
    pathRecyclerView.apply {
      layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
      adapter = pathAdapter
    }
  }

  private fun initFragment() {
    currentDir = internalStorageRoot
    directoryText.text = getString(R.string.internal_storage)
    val rootPath = currentDir.absolutePath
    fragment = FilePickerFragment.newInstance(rootPath)
    supportFragmentManager.beginTransaction()
        .replace(R.id.container, fragment, rootPath)
        .commitNow()
    pathAdapter = PathRecyclerViewAdapter(currentDir)
    pathRecyclerView.apply {
      layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true)
      adapter = pathAdapter
    }
  }

  fun pickDirectory(v: View) {
    proposeFile(currentDir)
  }

  fun proposeFile(file: File) {

    val title = getString(if (file.isDirectory) R.string.pick_this_directory_q else R.string.pick_this_file_q)
    val fileName = if (file == internalStorageRoot) getString(R.string.internal_storage_root) else file.name
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(getString(R.string.do_you_want_to_pick_file, fileName))
        .setNeutralButton(android.R.string.no, null)
        .setPositiveButton("yes") { dialog, which ->
          setResult(Activity.RESULT_OK, Intent(intent).apply {
            putExtra(PICKED_FILE_PATH_KEY, file)
          })
          if (file.parentFile != null) {
            sharedPreferences.edit(true) {
              putString(START_DIRECTORY_KEY, file.parentFile!!.absolutePath)
            }

          }
          finish()
        }
        .show()
  }

  fun replaceFragment(file: File) {
    if (file == currentDir) return
    fragment = FilePickerFragment.newInstance(file.path)
    val enterAnimation: Int
    val exitAnimation : Int
    if (File(currentDir, file.name).exists()) {
      enterAnimation = R.anim.slide_in_left
      exitAnimation = R.anim.slide_out_right
    } else {
      enterAnimation = R.anim.slide_in_right
      exitAnimation = R.anim.slide_out_left
    }
    currentDir = file
    pathAdapter.update(currentDir)
    pathAdapter.notifyDataSetChanged()
    pathRecyclerView.scrollToPosition(pathAdapter.itemCount - 1)
    directoryText.text = getDirectoryName(currentDir)

    supportFragmentManager.beginTransaction()
        .setCustomAnimations(enterAnimation, exitAnimation)
        .replace(R.id.container, fragment, file.path)
        .commitNow()
  }

  private fun getDirectoryName(currentDir: File): String {
    return if (currentDir == internalStorageRoot) getString(R.string.internal_storage)
    else currentDir.name
  }

  fun fileFilter(file: File): Boolean {
    return file.isDirectory || !dirOnly && fileNamePattern?.matcher(file.name)?.find() ?: true
  }

  class PathViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView = itemView.findViewById<TextView>(R.id.name)
  }

  inner class PathRecyclerViewAdapter(dir: File): RecyclerView.Adapter<PathViewHolder>() {

    private val directories = mutableListOf<File>()

    init {
      update(dir)
    }

    fun update(dir: File?) {
      directories.clear()
      if (dir != null) {
        directories.add(dir)
      }
      var file: File? = dir
      while (file != null && file != internalStorageRoot) {
        file = file.parentFile
        directories.add(0, file)
      }
    }

    override fun onBindViewHolder(holder: PathViewHolder, position: Int) {
      val file = directories[position]
      val name = if (file == internalStorageRoot) getString(R.string.internal_storage) else file.name
      holder.textView.text = name
      holder.textView.setOnClickListener {
        replaceFragment(file)
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PathViewHolder {
      return PathViewHolder(LayoutInflater.from(parent.context)
          .inflate(R.layout.directory_name_layout, parent, false))
    }


    override fun getItemCount(): Int {
      return directories.size
    }
  }

  fun getDeviceRootDirectory(): File {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
      return Environment.getExternalStorageDirectory()!!
    } else {
      // inspired from Environment.getLegacyExternalStorageDirectory()
      return File(System.getenv("EXTERNAL_STORAGE")!!)
    }
  }
}

class FilePickerFragment: Fragment() {

  companion object {
    const val PATH_KEY = "pk"
    fun newInstance(path: String): FilePickerFragment {
      val args = Bundle()
      args.putString(PATH_KEY, path)

      return FilePickerFragment().apply {
        arguments = args
      }
    }

    fun newLauncher(caller: ActivityResultCaller) {

    }
  }
  private lateinit var recyclerView: RecyclerView
  private var adapter: FileChildrenRecyclerAdapter? = null
  private lateinit var activity: FilePickerActivity

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    recyclerView = RecyclerView(inflater.context).apply {
      layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
      layoutManager = LinearLayoutManager(inflater.context)
      setHasFixedSize(true)
    }
    activity = requireActivity() as FilePickerActivity
    val path = requireArguments().getString(PATH_KEY)
    var file =  if (path == null || !File(path).exists()) (
        requireActivity() as FilePickerActivity
        ).getDeviceRootDirectory() else File(path)
    val files = file.listFiles()
    if (files == null || files.none(activity::fileFilter)) {
      return TextView(activity).apply {
        text = when {
          files == null -> "The app couldn't read files"
          activity.dirOnly -> context.getString(R.string.no_child_dirs)
          else -> context.getString(R.string.empty_dir)
        }
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 23f)
        gravity = Gravity.CENTER
      }
    }
    return recyclerView
  }

  override fun onStart() {
    super.onStart()
    activity = requireActivity() as FilePickerActivity
    val path = requireArguments().getString(PATH_KEY)
    recyclerView.adapter = FileChildrenRecyclerAdapter(File(path))
    adapter = recyclerView.adapter as FileChildrenRecyclerAdapter
  }

  private fun onFileClick(file: File) {
    val activity = requireActivity() as FilePickerActivity
    if (file.isDirectory) {
      activity.replaceFragment(file)
    } else {
      activity.proposeFile(file)
    }
  }

  // return true if event was consumed
  fun onBackPressed() {
    val parentFile = adapter?.directory?.parentFile ?: return
    activity.replaceFragment(parentFile)
  }

  class FileChildrenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView = itemView.findViewById<ImageView>(R.id.imageview)
    val textView = itemView.findViewById<TextView>(R.id.name)
  }

  inner class FileChildrenRecyclerAdapter(val directory: File): RecyclerView.Adapter<FileChildrenViewHolder>() {

    private val children = (directory.listFiles() ?: emptyArray()).filter(activity::fileFilter).sortedWith(compareBy(File::isFile, File::getName))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileChildrenViewHolder {
      return FileChildrenViewHolder(LayoutInflater.from(parent.context)
        .inflate(R.layout.file_layout, parent, false))
    }

    override fun onBindViewHolder(holder: FileChildrenViewHolder, position: Int) {
      val file = children[position]
      holder.imageView.setImageResource(if (file.isDirectory) R.drawable.folder else R.drawable.file)
      holder.textView.text = file.name
      holder.itemView.setOnClickListener {
        onFileClick(file)
      }
    }

    override fun getItemCount(): Int {
      return children.size
    }
  }
}