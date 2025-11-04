호출 예시
package com.runningcity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.runningcity.model.Data
import com.runningcity.network.RetrofitClient
import com.runningcity.network.RetrofitInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val api = RetrofitClient.instance.create(RetrofitInterface::class.java)
        val call = api.getData(name = "Oscar", age = "9살")

        call.enqueue(object : Callback<Data> {
            override fun onResponse(call: Call<Data>, response: Response<Data>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("Retrofit", "성공: ${body?.name} 는 ${body?.age} 이에요!")
                } else {
                    Log.d("Retrofit", "응답 에러 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Data>, t: Throwable) {
                Log.e("Retrofit", "요청 실패: ${t.message}", t)
            }
        })
    }
}
주의: BASE_URL은 꼭 슬래시(/)로 끝나야함 @GET("API_Test.php")처럼 상대 경로만