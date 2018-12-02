package allstars.com.mediaviewer.ui.main

import allstars.com.mediaviewer.R
import allstars.com.mediaviewer.databinding.MainFragmentBinding
import allstars.com.mediaviewer.model.ANIMATIONS_DURATION
import allstars.com.mediaviewer.model.EXTRA_COUNTER
import allstars.com.mediaviewer.model.MENU_SETTINGS_ID
import allstars.com.mediaviewer.model.VIEW_TIME_SEC
import allstars.com.mediaviewer.model.dto.Content
import allstars.com.mediaviewer.model.dto.ContentType
import allstars.com.mediaviewer.ui.settings.SettingDialog
import allstars.com.mediaviewer.util.AnimatorListenerAdapter
import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.view.animation.*
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.main_fragment.view.*


class MainFragment : Fragment() {

    private var ready: Boolean = false
    private var isPlayingVideo: Boolean = false
    private var isLoopRunning: Boolean = false
    private var shouldStop: Boolean = false
    private var counter = 0
    private var viewTime = VIEW_TIME_SEC
    private lateinit var currentContentType: ContentType

    var contentList: MutableList<Content> = ArrayList()

    var handler = Handler()

    lateinit var binding: MainFragmentBinding

    companion object {
        const val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.main_fragment, container, false)
        registerForContextMenu(binding.root)
        binding.main.setOnClickListener { activity?.openContextMenu(binding.root) }
        setUpViewSwitcher()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        observeViewModel()
        requestPermission()
    }

    private fun observeViewModel() {
        viewModel.viewTime.observe(this, Observer {
            it?.let {
                viewTime = it
            }
        })
    }

    private fun requestPermission() {
        activity?.let {
            if (ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(it, getString(R.string.give_permission_text), Toast.LENGTH_LONG).show()
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        it,
                        Manifest.permission.READ_CONTACTS
                    )
                ) {
                    Toast.makeText(it, getString(R.string.give_permission_text), Toast.LENGTH_LONG).show()
                    requestPermission()
                } else {
                    ActivityCompat.requestPermissions(
                        it,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_READ_CONTACTS
                    )
                }
            } else {
                startShow()
            }
        }
    }

    private fun setUpViewSwitcher() {
        val fadeInSet = AnimationSet(true)
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = DecelerateInterpolator()
        fadeIn.duration = ANIMATIONS_DURATION
        val zoomIn = ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        zoomIn.fillAfter = true
        zoomIn.duration = ANIMATIONS_DURATION

        val fadeOutSet = AnimationSet(true)

        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.interpolator = AccelerateInterpolator()
        fadeOut.duration = ANIMATIONS_DURATION

        val zoomOut = ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        zoomOut.fillAfter = true
        zoomOut.duration = ANIMATIONS_DURATION

        fadeInSet.addAnimation(fadeIn)
        fadeInSet.addAnimation(zoomIn)
        fadeOutSet.addAnimation(fadeOut)
        fadeOutSet.addAnimation(zoomOut)
        fadeInSet.startOffset = ANIMATIONS_DURATION

        binding.viewSwitcher.inAnimation = fadeInSet
        binding.viewSwitcher.outAnimation = fadeOutSet
    }

    private fun playContent(content: Content) {
        setMode(ContentType.PHOTO)
        activity?.let {
            Glide.with(it)
                .load(content.path)
                .apply(RequestOptions.centerCropTransform())
                .into(binding.viewSwitcher.nextView as ImageView)
        }
        binding.viewSwitcher.showNext()
        binding.viewSwitcher.inAnimation.setAnimationListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(p0: Animation?) {
                if (content.type == ContentType.VIDEO) {
                    setMode(content.type)
                    binding.videoView.setDataSource(content.path)
                    binding.videoView.prepareAsync { mediaPlayer ->
                        mediaPlayer.start()
                        ready = true
                        isPlayingVideo = true
                    }
                }
            }
        })
    }

    override fun onStop() {
        if (ready) {
            binding.videoView.stop()
            isPlayingVideo = false
        }
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        if (ready) {
            binding.videoView.start()
            isPlayingVideo = true
        }
    }

    private fun startShow() {
        viewModel.contentList.observe(this, Observer {
            contentList.addAll(it as MutableList<Content>)
            runLoop()
        })
    }

    private fun runLoop() {
        handler.post(object : Runnable {
            override fun run() {
                activity?.let {
                    if (counter < contentList.size - 1 && !shouldStop) {
                        isLoopRunning = true
                        playContent(contentList[counter])
                        counter++
                        handler.postDelayed(this, viewTime * 1000L)
                    } else {
                        isLoopRunning = false
                    }
                }
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_CONTACTS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startShow()
                } else {
                    Toast.makeText(context, getString(R.string.give_permission_text), Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    private fun setMode(type: ContentType) {
        currentContentType = type
        if (type == ContentType.PHOTO) {
            binding.root.viewSwitcher.visibility = View.VISIBLE
            binding.root.videoView.visibility = View.GONE
            if (isPlayingVideo && ready) {
                binding.videoView.stop()
                isPlayingVideo = false
            }
        } else {
            binding.root.viewSwitcher.visibility = View.GONE
            binding.root.videoView.visibility = View.VISIBLE
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.add(0, MENU_SETTINGS_ID, 0, getString(R.string.settings))
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            MENU_SETTINGS_ID -> invokeSettings()
        }
        return super.onContextItemSelected(item)
    }

    private fun invokeSettings() {
        val dialog = SettingDialog.newInstance(
            text = "",
            hint = getString(R.string.set_time_hint),
            isMultiline = false
        )
        dialog.onOk = {
            val text = dialog.editText.text.toString()
            viewModel.saveSettingsToShPref(Integer.parseInt(text))
        }
        dialog.show(fragmentManager, "TAG")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(EXTRA_COUNTER, counter)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            counter = it.getInt(EXTRA_COUNTER, 0)
            if (counter > 0)
                counter--
        }
    }
}
