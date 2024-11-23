package com.cangwang.magic

import android.Manifest
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
import com.cangwang.magic.databinding.ActivityCameraBinding
import com.cangwang.magic.util.CameraHelper
import com.cangwang.magic.view.CameraSurfaceCallback

/**
 * Created by cangwang on 2018/9/12.
 */
class CameraActivity:AppCompatActivity(){
    private val MODE_PIC = 1
    private var CAMERA_PERMISSION_REQ = 1

    var mCamera: Camera?=null
    private val ASPECT_RATIO_ARRAY = floatArrayOf(9.0f / 16, 3.0f / 4)
    var mAspectRatio = ASPECT_RATIO_ARRAY[0]

    var mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK

    lateinit var  binding:ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    }


    fun initView(){

        binding.btnCameraFilter.visibility = View.GONE
        binding.btnCameraShutter.visibility = View.GONE

        binding.btnCameraSwitch.setOnClickListener {

        }

        binding.btnCameraMode.visibility = View.GONE
        binding.btnCameraBeauty.visibility = View.GONE

        val screenSize =Point()
        windowManager.defaultDisplay.getSize(screenSize)
        val params =binding.glsurfaceviewCamera.layoutParams as RelativeLayout.LayoutParams
        params.width= screenSize.x;
        params.height = screenSize.x* 16/9
        binding.glsurfaceviewCamera.layoutParams = params
    }

    override fun onResume() {
        super.onResume()
        mCamera = openCamera(binding.glsurfaceviewCamera.holder)
        binding.glsurfaceviewCamera.holder.addCallback(CameraSurfaceCallback(mCamera))
    }

    override fun onPause() {
        super.onPause()
        mCamera?.stopPreview()
        mCamera?.release()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == CAMERA_PERMISSION_REQ &&(grantResults.size != 1 || grantResults[0] == PermissionChecker.PERMISSION_GRANTED)) {
            initView()
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
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

}