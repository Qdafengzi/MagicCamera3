package com.cangwang.magic

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cangwang.magic.adapter.FilterAdapter
import com.cangwang.magic.camera.CameraCompat
import com.cangwang.magic.databinding.ActivityCameraBinding
import com.cangwang.magic.util.OpenGLJniLib
import com.cangwang.magic.view.CameraFilterSurfaceCallbackV2

/**
 * Created by cangwang on 2018/9/12.
 */
class CameraFilterV2Activity: AppCompatActivity(){

    private var isRecording = false
    private val MODE_PIC = 1
    private val MODE_VIDEO = 2
    private var mode = MODE_PIC

    private var mAdapter: FilterAdapter? = null
    private var mSurfaceCallback:CameraFilterSurfaceCallbackV2?=null
    private var beautyLevel:Int = 0

    var mCamera: CameraCompat?=null

    lateinit var  binding: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding =  ActivityCameraBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        initView()
    }

    private val types = OpenGLJniLib.getFilterTypes()

    fun initView(){

        binding.layoutFilter.filterListView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        mAdapter = FilterAdapter(this, types)
        mAdapter?.filterListener= object:FilterAdapter.onFilterChangeListener{
            override fun onFilterChanged(type: Int) {
                mSurfaceCallback?.setFilterType(type)
            }
        }
        binding.layoutFilter.filterListView.adapter= mAdapter
        binding.btnCameraFilter.setOnClickListener {
            showFilters()
        }

        binding.layoutFilter.btnCameraClosefilter.setOnClickListener {
            hideFilters()
        }

        binding.btnCameraShutter.setOnClickListener {
            takePhoto()
        }

        binding.btnCameraSwitch.setOnClickListener {
            mCamera?.switchCamera()
        }

        binding.btnCameraMode.setOnClickListener {

        }

        binding.btnCameraBeauty.setOnClickListener {
            AlertDialog.Builder(this)
                    .setSingleChoiceItems(arrayOf("关闭", "1", "2", "3", "4", "5"), beautyLevel) {
                        dialog, which ->
                        beautyLevel = which
                        OpenGLJniLib.setBeautyLevel(which)
                        dialog.dismiss()
                    }
                    .setNegativeButton("取消", null)
                    .show()
        }
//        val screenSize =Point()
//        windowManager.defaultDisplay.getSize(screenSize)
//        val params = glsurfaceview_camera.layoutParams as RelativeLayout.LayoutParams
//        params.width= screenSize.x
//        params.height = screenSize.x* 16/9
//        glsurfaceview_camera.layoutParams = params

    }

    override fun onResume() {
        super.onResume()
        initCamera()
    }

    private fun initCamera(){
        mCamera = CameraCompat.newInstance(this)
        mSurfaceCallback = CameraFilterSurfaceCallbackV2(mCamera)
        binding.glsurfaceviewCamera.holder.addCallback(mSurfaceCallback)
        mCamera?.startPreview()
    }

    override fun onPause() {
        mCamera?.stopPreview(false)
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun takePhoto(){
        mSurfaceCallback?.takePhoto()
    }

    fun takeVideo(){

    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun showFilters() {
        val animator = ObjectAnimator.ofInt( binding.layoutFilter.root, "translationY",  binding.layoutFilter.root.height, 0)
        animator.duration = 200
        animator.addListener(object :AnimatorListenerAdapter(){
            override fun onAnimationStart(animation: Animator) {
                binding.btnCameraShutter.isClickable = false
                binding.layoutFilter.root.visibility = View.VISIBLE
            }
        })

        animator.start()
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun hideFilters() {

        val animator = ObjectAnimator.ofInt(binding.layoutFilter.root, "translationY", 0, binding.layoutFilter.root.height)
        animator.duration = 200
        animator.addListener(object :AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator) {
                // TODO Auto-generated method stub

                binding.layoutFilter.root.visibility = View.INVISIBLE
                binding.btnCameraShutter.isClickable = true
            }

            override fun onAnimationCancel(animation: Animator) {
                // TODO Auto-generated method stub
                binding.layoutFilter.root.visibility = View.INVISIBLE
                binding.btnCameraShutter.isClickable = true
            }
        })
        animator.start()
    }

    override fun onBackPressed() {
        if(binding.layoutFilter.root.visibility ==View.VISIBLE){
            hideFilters()
        }else {
            super.onBackPressed()
        }
    }
}