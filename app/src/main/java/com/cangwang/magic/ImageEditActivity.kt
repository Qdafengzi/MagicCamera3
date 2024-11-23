//package com.cangwang.magic
//
//import android.animation.Animator
//import android.animation.AnimatorListenerAdapter
//import android.animation.ObjectAnimator
//import android.annotation.SuppressLint
//import android.content.pm.ActivityInfo
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.Window
//import android.view.WindowManager
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.cangwang.magic.adapter.FilterAdapter
//import com.cangwang.magic.databinding.ActivityAlbumBinding
//import com.cangwang.magic.util.OpenGLJniLib
//import com.cangwang.magic.view.ImageFilterSurfaceCallback
//import com.werb.pickphotoview.model.SelectModel
//import com.werb.pickphotoview.util.PickConfig
//
///**
// * 图片编辑
// */
//class ImageEditActivity: AppCompatActivity(){
//
//    private var mAdapter: FilterAdapter? = null
//    private var mSurfaceCallback: ImageFilterSurfaceCallback?=null
//    private var beautyLevel:Int = 0
//    private val types = OpenGLJniLib.getFilterTypes()
//
//    lateinit var binding :ActivityAlbumBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
//        requestWindowFeature(Window.FEATURE_NO_TITLE)
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//        binding = ActivityAlbumBinding.inflate(LayoutInflater.from(this))
//        setContentView(binding.root)
//        initView()
//    }
//
//    override fun onResume() {
//        super.onResume()
//    }
//
//    private fun initView(){
//
//        binding.layoutFilter.filterListView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
//        mAdapter = FilterAdapter(this, types)
//        mAdapter?.filterListener= object: FilterAdapter.onFilterChangeListener{
//            override fun onFilterChanged(type: Int) {
//                mSurfaceCallback?.setFilterType(type)
//            }
//        }
//        binding.layoutFilter.filterListView.adapter= mAdapter
//        initPreview()
//        binding.btnAlbumFilter.setOnClickListener {
//            showFilters()
//        }
//        binding.layoutFilter.btnCameraClosefilter.setOnClickListener {
//            hideFilters()
//        }
//
//        binding.btnAlbumSave.setOnClickListener {
//            mSurfaceCallback?.saveImage()
//        }
//
//        binding.btnAlbumBeauty.setOnClickListener {
//            AlertDialog.Builder(this)
//                    .setSingleChoiceItems(arrayOf("关闭", "1", "2", "3", "4", "5"), beautyLevel) {
//                        dialog, which ->
//                        beautyLevel = which
//                        OpenGLJniLib.setImageBeautyLevel(which)
//                        dialog.dismiss()
//                    }
//                    .setNegativeButton("取消", null)
//                    .show()
//        }
//
//    }
//
//    private fun initPreview(){
//        val selectPaths = intent.getSerializableExtra(PickConfig.INTENT_IMG_LIST_SELECT) as SelectModel
//        mSurfaceCallback = ImageFilterSurfaceCallback(selectPaths.path)
//
//        binding.albumSurfaceview.holder.addCallback(mSurfaceCallback)
//    }
//
//    @SuppressLint("ObjectAnimatorBinding")
//    private fun showFilters() {
//        val animator = ObjectAnimator.ofInt(binding.layoutFilter.root, "translationY", binding.layoutFilter.root.height, 0)
//        animator.duration = 200
//        animator.addListener(object :AnimatorListenerAdapter(){
//            override fun onAnimationStart(animation: Animator) {
//
//                binding.btnAlbumSave.isClickable = false
//                binding.layoutFilter.root.visibility = View.VISIBLE
//            }
//        })
//
//        animator.start()
//    }
//
//    @SuppressLint("ObjectAnimatorBinding")
//    private fun hideFilters() {
//
//        val animator = ObjectAnimator.ofInt(binding.layoutFilter.root, "translationY", 0, binding.layoutFilter.root.height)
//        animator.duration = 200
//        animator.addListener(object : AnimatorListenerAdapter(){
//            override fun onAnimationEnd(animation: Animator) {
//                // TODO Auto-generated method stub
//                binding.layoutFilter.root.visibility = View.INVISIBLE
//                binding.btnAlbumSave.isClickable = true
//            }
//
//            override fun onAnimationCancel(animation: Animator) {
//                // TODO Auto-generated method stub
//                binding.layoutFilter.root.visibility = View.INVISIBLE
//                binding.btnAlbumSave.isClickable = true
//            }
//        })
//        animator.start()
//    }
//}