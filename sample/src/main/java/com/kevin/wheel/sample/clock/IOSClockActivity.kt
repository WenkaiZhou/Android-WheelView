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
package com.kevin.wheel.sample.clock

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.kevin.wheel.WheelView
import com.kevin.wheel.sample.R
import kotlinx.android.synthetic.main.activity_ios_clock.*
import java.util.*

/**
 * IOSClockActivity
 *
 * @author zhouwenkai@baidu.com, Created on 2019-11-16 13:12:29
 *         Major Function：<b>IOS系统闹钟</b>
 *         <p/>
 *         Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
class IOSClockActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ios_clock)


        val hour = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
        val minute = Calendar.getInstance()[Calendar.MINUTE]

        // 是否上午
        var forenoon = hour / 12 == 0

        noon_wheel_view.setDataItems(mutableListOf("上午", "下午"))
        noon_wheel_view.setSelectedItemPosition(if (forenoon) 0 else 1)
        hour_wheel_view.setDataItems((1..12).toMutableList())
        hour_wheel_view.setSelectedItemPosition((hour % 12) - 1)
        minute_wheel_view.setDataItems((1 until 60).toMutableList())
        minute_wheel_view.setSelectedItemPosition(minute - 1)


        var lastSelectedHour = hour

        // 上下午滚轮选择监听
        noon_wheel_view.setOnItemSelectedListener(object : WheelView.OnItemSelectedListener {
            override fun onItemSelected(wheelView: WheelView, data: Any, position: Int) {
                forenoon = !forenoon
            }
        })

        hour_wheel_view.setOnItemSelectedListener(object : WheelView.OnItemSelectedListener {
            override fun onItemSelected(wheelView: WheelView, data: Any, position: Int) {
                val selectedHour = data as Int
                if (lastSelectedHour == 12 && selectedHour == 1) {
                    forenoon = !forenoon
                    noon_wheel_view.setSelectedItemPosition(if (forenoon) 0 else 1, true)
                } else if (lastSelectedHour == 1 && selectedHour == 12) {
                    forenoon = !forenoon
                    noon_wheel_view.setSelectedItemPosition(if (forenoon) 0 else 1, true)
                }
                lastSelectedHour = selectedHour
            }
        })
    }
}