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
package com.kevin.wheelview.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kevin.wheelview.WheelView
import com.kevin.wheelview.sample.province.ProvinceSelectorDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val list = mutableListOf(1, 212, 33, 4231435, 53, 6431, 37)
        wheel_view.setDataItems(list)
        wheel_view.setCyclic(true)
        wheel_view.setTextAlign(WheelView.TEXT_ALIGN_LEFT)
        wheel_view.setCurvedRefractRatio(1.0f)
        wheel_view.setSoundEffect(true)

        ProvinceSelectorDialog.getInstance().show(this)




    }
}
