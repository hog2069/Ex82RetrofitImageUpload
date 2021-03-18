package com.mrhi2020.ex82retrofitimageupload;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RetrofitService {

    //MultipartBody.Part file 한개를 전달 [MultipartBody.Part : 식별자, 파일명, 실제파일데이터를 가진 RequestBody 객체를 가진 객체
    @Multipart
    @POST("/Retrofit/fileUpload.php")
    Call<String> uploadImage(@Part MultipartBody.Part file);

}
