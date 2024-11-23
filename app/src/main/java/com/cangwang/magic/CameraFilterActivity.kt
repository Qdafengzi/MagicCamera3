package com.cangwang.magic

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.graphics.Point
import android.hardware.Camera
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.recyclerview.widget.LinearLayoutManager
import com.cangwang.magic.adapter.FilterAdapter
import com.cangwang.magic.databinding.ActivityCameraBinding
import com.cangwang.magic.util.CameraHelper
import com.cangwang.magic.util.OpenGLJniLib
import com.cangwang.magic.view.CameraFilterSurfaceCallback

/**
 * Created by cangwang on 2018/9/12.
 */
class CameraFilterActivity: AppCompatActivity(){

    private var isRecording = false
    private val MODE_PIC = 1
    private val MODE_VIDEO = 2
    private var mode = MODE_PIC
    private var CAMERA_PERMISSION_REQ = 1
    private var STORGE_PERMISSION_REQ = 2
    private var mAdapter: FilterAdapter? = null
    private var mSurfaceCallback:CameraFilterSurfaceCallback?=null
    private var beautyLevel:Int = 0

    var mCamera: Camera?=null
    private val ASPECT_RATIO_ARRAY = floatArrayOf(9.0f / 16, 3.0f / 4)
    var mAspectRatio = ASPECT_RATIO_ARRAY[0]

    var mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK
    lateinit var  binding: ActivityCameraBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding =  ActivityCameraBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        
        
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_DENIED) run {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),CAMERA_PERMISSION_REQ)
        }else {
            initView()
        }
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_DENIED) run {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),STORGE_PERMISSION_REQ)
        }
    }

//    private val types = arrayOf(MagicFilterType.NONE, MagicFilterType.FAIRYTALE, MagicFilterType.SUNRISE,
//            MagicFilterType.SUNSET, MagicFilterType.WHITECAT, MagicFilterType.BLACKCAT, MagicFilterType.SKINWHITEN, MagicFilterType.HEALTHY,
//            MagicFilterType.SWEETS, MagicFilterType.ROMANCE, MagicFilterType.SAKURA, MagicFilterType.WARM, MagicFilterType.ANTIQUE,
//            MagicFilterType.NOSTALGIA, MagicFilterType.CALM, MagicFilterType.LATTE, MagicFilterType.TENDER, MagicFilterType.COOL,
//            MagicFilterType.EMERALD, MagicFilterType.EVERGREEN, MagicFilterType.CRAYON, MagicFilterType.SKETCH, MagicFilterType.AMARO,
//            MagicFilterType.BRANNAN, MagicFilterType.BROOKLYN, MagicFilterType.EARLYBIRD, MagicFilterType.FREUD, MagicFilterType.HEFE, MagicFilterType.HUDSON,
//            MagicFilterType.INKWELL, MagicFilterType.KEVIN, MagicFilterType.LOMO, MagicFilterType.N1977, MagicFilterType.NASHVILLE,
//            MagicFilterType.PIXAR, MagicFilterType.RISE, MagicFilterType.SIERRA, MagicFilterType.SUTRO, MagicFilterType.TOASTER2,
//            MagicFilterType.VALENCIA, MagicFilterType.WALDEN, MagicFilterType.XPROII)
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
            switchCamera()
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
        val screenSize =Point()
        windowManager.defaultDisplay.getSize(screenSize)
        
        val params = binding.glsurfaceviewCamera.layoutParams as RelativeLayout.LayoutParams
        params.width= screenSize.x
        params.height = screenSize.x* 16/9
        binding.glsurfaceviewCamera.layoutParams = params

    }

    override fun onResume() {
        super.onResume()
        initCamera()
    }

    private fun initCamera(){
        releaseCamera()
        mCamera = openCamera(binding.glsurfaceviewCamera.holder)
        mSurfaceCallback = CameraFilterSurfaceCallback(mCamera)
        binding.glsurfaceviewCamera.holder.addCallback(mSurfaceCallback)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        mSurfaceCallback?.releaseOpenGL()
        releaseCamera()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        if (grantResults.size !=1 || grantResults[0] ==PackageManager.PERMISSION_GRANTED){
//            if (mode == MODE_PIC){
//                takePhoto()
//            }else{
//                takeVideo()
//            }
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQ &&(grantResults.size != 1 || grantResults[0] == PermissionChecker.PERMISSION_GRANTED)) {
            initView()
            initCamera()
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    fun takePhoto(){
        mSurfaceCallback?.takePhoto()
    }

    fun takeVideo(){

    }

    fun switchCamera(){
        mCameraId = if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK ) Camera.CameraInfo.CAMERA_FACING_FRONT else Camera.CameraInfo.CAMERA_FACING_BACK
        releaseCamera()
        mCamera = openCamera(binding.glsurfaceviewCamera.holder)
        mSurfaceCallback?.changeCamera(mCamera)
    }

    fun releaseCamera(){
        mCamera?.setPreviewCallback(null)
        mCamera?.stopPreview()
        mCamera?.release()
        mCamera = null
    }


    fun openCamera(holder: SurfaceHolder?):Camera?{
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PermissionChecker.PERMISSION_GRANTED) {
            return null
        }

        if (mCamera!=null){
            return mCamera
        }
        mCamera = CameraHelper.openCamera(mCameraId)

        mCamera?.let {
            //这里android 相机长和宽默认偏移90度，所以传入要对调
            CameraHelper.setOptimalSize(it,mAspectRatio,CameraHelper.getScreenHeight(),CameraHelper.getScreenWidth())
            CameraHelper.setDisplayOritation(this,it,mCameraId)
        }
        return mCamera
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun showFilters() {

        val animator = ObjectAnimator.ofInt(binding.layoutFilter.root, "translationY", binding.layoutFilter.root.height, 0)
        animator.duration = 200
        animator.addListener(object :AnimatorListenerAdapter(){
            override fun onAnimationStart(animation: Animator) {
                findViewById<View>(R.id.btn_camera_shutter).isClickable = false
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
                findViewById<View>(R.id.btn_camera_shutter).isClickable = true
            }

            override fun onAnimationCancel(animation: Animator) {
                // TODO Auto-generated method stub
                binding.layoutFilter.root.visibility = View.INVISIBLE
                findViewById<View>(R.id.btn_camera_shutter).isClickable = true
            }
        })
        animator.start()
    }
}