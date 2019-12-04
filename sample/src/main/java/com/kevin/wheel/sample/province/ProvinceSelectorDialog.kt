/*
 * Copyright (c) 2019 Kevin zhou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kevin.wheel.sample.province

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.Gson
import com.kevin.dialog.BaseDialog
import com.kevin.wheel.sample.util.LocalFileUtils
import com.google.gson.reflect.TypeToken
import com.kevin.wheel.WheelView
import com.kevin.wheel.sample.R

/**
 * ProvinceSelectorDialog
 *
 * @author zhouwenkai@baidu.com, Created on 2019-11-15 17:20:29
 *         Major Function：<b>省市区域选择Dialog</b>
 *         <p/>
 *         Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
class ProvinceSelectorDialog : BaseDialog() {

    private lateinit var binding: ProvinceSelectorBinding
    private lateinit var provinceList: MutableList<Province>

    override fun createView(
        context: Context?,
        inflater: LayoutInflater,
        container: ViewGroup?
    ): View {
        binding = ProvinceSelectorBinding.inflate(layoutInflater, container, false)
        binding.view = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 解析本地省市区域数据
        val provinceStr = LocalFileUtils.getStringFormAsset(context, "province.json")
        provinceList =
            Gson().fromJson(provinceStr, object : TypeToken<MutableList<Province>>() {}.type)
        binding.provinceWheelView.setDataItems(provinceList.map { it.name }.toMutableList())

        // 设置音效
        binding.provinceWheelView.setSoundEffectResource(R.raw.button_choose)
        binding.provinceWheelView.setSoundEffect(true)
        binding.cityWheelView.setSoundEffectResource(R.raw.button_choose)
        binding.cityWheelView.setSoundEffect(true)
        binding.areaWheelView.setSoundEffectResource(R.raw.button_choose)
        binding.areaWheelView.setSoundEffect(true)

        // 省份选择变化监听
        binding.provinceWheelView.setOnItemSelectedListener(object :
            WheelView.OnItemSelectedListener {
            override fun onItemSelected(wheelView: WheelView, data: Any, position: Int) {
                // 切换城市
                binding.cityWheelView.setDataItems(provinceList[position].city.map { it.name }.toMutableList())
                // 切换区域
                val cityPosition = binding.cityWheelView.getSelectedItemPosition()
                binding.areaWheelView.setDataItems(provinceList[position].city[cityPosition].area)
            }
        })

        // 城市选择变化监听
        binding.cityWheelView.setOnItemSelectedListener(object : WheelView.OnItemSelectedListener {
            override fun onItemSelected(wheelView: WheelView, data: Any, position: Int) {
                // 切换区域
                val provincePosition = binding.provinceWheelView.getSelectedItemPosition()
                binding.areaWheelView.setDataItems(provinceList[provincePosition].city[position].area)
            }
        })
    }

    /**
     * 显示弹窗
     *
     * @param activity
     * @return
     */
    fun show(
        activity: FragmentActivity
    ): ProvinceSelectorDialog {
        super.show(activity.supportFragmentManager,
            TAG
        )
        return this
    }

    fun onCancelClick(view: View) {

    }

    fun onConfirmClick(view: View) {
        val province = binding.provinceWheelView.getSelectedItemData()
        val city = binding.cityWheelView.getSelectedItemData()
        val area = binding.areaWheelView.getSelectedItemData()

        Toast.makeText(context, "$province-$city-$area", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "ProvinceSelectorDialog"

        fun getInstance(): ProvinceSelectorDialog {
            val dialog = ProvinceSelectorDialog()
            dialog.setGravity(Gravity.BOTTOM)
                // 设置宽度为屏幕宽度
                .setWidth(1f)
                // 设置黑色透明背景
                .setDimEnabled(true)
                .setBackgroundColor(Color.WHITE)
                // 设置动画
                .setAnimations(android.R.style.Animation_InputMethod)
            return dialog
        }
    }
}